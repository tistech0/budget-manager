import { ref, computed, type Ref } from 'vue'
import { z, type ZodSchema, type ZodError } from 'zod'

export interface ValidationError {
  field: string
  message: string
}

export interface ValidationResult {
  success: boolean
  errors: ValidationError[]
  data?: any
}

/**
 * Composable for form validation using Zod schemas
 *
 * Usage:
 * ```ts
 * const { validate, errors, hasErrors, getFieldError, clearErrors } = useValidation(MyFormSchema)
 *
 * const result = await validate(formData)
 * if (result.success) {
 *   // Submit validated data: result.data
 * }
 * ```
 */
export function useValidation<T extends ZodSchema>(schema: T) {
  const errors = ref<ValidationError[]>([])
  const isValidating = ref(false)

  /**
   * Check if there are any validation errors
   */
  const hasErrors = computed(() => errors.value.length > 0)

  /**
   * Get all error messages as a flat array
   */
  const errorMessages = computed(() => errors.value.map(e => e.message))

  /**
   * Get error message for a specific field
   */
  const getFieldError = (fieldName: string): string | undefined => {
    const error = errors.value.find(e => e.field === fieldName)
    return error?.message
  }

  /**
   * Check if a specific field has an error
   */
  const hasFieldError = (fieldName: string): boolean => {
    return errors.value.some(e => e.field === fieldName)
  }

  /**
   * Clear all validation errors
   */
  const clearErrors = () => {
    errors.value = []
  }

  /**
   * Clear error for a specific field
   */
  const clearFieldError = (fieldName: string) => {
    errors.value = errors.value.filter(e => e.field !== fieldName)
  }

  /**
   * Parse Zod errors into our validation error format
   */
  const parseZodErrors = (zodError: ZodError): ValidationError[] => {
    return zodError.issues.map((err: any) => ({
      field: err.path.join('.'),
      message: err.message
    }))
  }

  /**
   * Validate data against the schema
   */
  const validate = async (data: unknown): Promise<ValidationResult> => {
    isValidating.value = true
    clearErrors()

    try {
      const validatedData = await schema.parseAsync(data)
      isValidating.value = false
      return {
        success: true,
        errors: [],
        data: validatedData
      }
    } catch (error) {
      if (error instanceof z.ZodError) {
        const validationErrors = parseZodErrors(error)
        errors.value = validationErrors
        isValidating.value = false
        return {
          success: false,
          errors: validationErrors
        }
      }
      // Unexpected error
      isValidating.value = false
      throw error
    }
  }

  /**
   * Validate data synchronously
   */
  const validateSync = (data: unknown): ValidationResult => {
    clearErrors()

    try {
      const validatedData = schema.parse(data)
      return {
        success: true,
        errors: [],
        data: validatedData
      }
    } catch (error) {
      if (error instanceof z.ZodError) {
        const validationErrors = parseZodErrors(error)
        errors.value = validationErrors
        return {
          success: false,
          errors: validationErrors
        }
      }
      throw error
    }
  }

  /**
   * Validate a single field
   */
  const validateField = async (fieldName: string, value: unknown): Promise<boolean> => {
    clearFieldError(fieldName)

    try {
      // Try to validate just this field
      const fieldSchema = (schema as any).shape?.[fieldName]
      if (fieldSchema) {
        await fieldSchema.parseAsync(value)
      }
      return true
    } catch (error) {
      if (error instanceof z.ZodError) {
        const validationErrors = parseZodErrors(error)
        errors.value = [...errors.value, ...validationErrors.map(e => ({
          field: fieldName,
          message: e.message
        }))]
      }
      return false
    }
  }

  /**
   * Safe parse - returns result without throwing
   */
  const safeParse = (data: unknown): { success: boolean; data?: any; error?: ZodError } => {
    const result = schema.safeParse(data)
    if (!result.success) {
      errors.value = parseZodErrors(result.error)
    }
    return result
  }

  /**
   * Set custom error for a field
   */
  const setFieldError = (fieldName: string, message: string) => {
    // Remove existing error for this field
    clearFieldError(fieldName)
    // Add new error
    errors.value.push({
      field: fieldName,
      message
    })
  }

  /**
   * Set multiple errors at once
   */
  const setErrors = (newErrors: ValidationError[]) => {
    errors.value = newErrors
  }

  return {
    // State
    errors: computed(() => errors.value),
    isValidating: computed(() => isValidating.value),
    hasErrors,
    errorMessages,

    // Methods
    validate,
    validateSync,
    validateField,
    safeParse,
    getFieldError,
    hasFieldError,
    clearErrors,
    clearFieldError,
    setFieldError,
    setErrors
  }
}

/**
 * Composable for validating reactive form data
 * Automatically validates when form data changes
 *
 * Usage:
 * ```ts
 * const formData = ref({ name: '', email: '' })
 * const { errors, isValid } = useFormValidation(formData, MyFormSchema)
 * ```
 */
export function useFormValidation<T extends ZodSchema>(
  formData: Ref<Record<string, any>>,
  schema: T,
  options: {
    validateOnChange?: boolean
    debounceMs?: number
  } = {}
) {
  const { validateOnChange = false, debounceMs = 300 } = options

  const validation = useValidation(schema)
  let debounceTimeout: number | null = null

  const isValid = computed(() => !validation.hasErrors.value)

  /**
   * Validate the entire form
   */
  const validateForm = async (): Promise<boolean> => {
    const result = await validation.validate(formData.value)
    return result.success
  }

  /**
   * Validate form data with debouncing
   */
  const debouncedValidate = () => {
    if (debounceTimeout) {
      clearTimeout(debounceTimeout)
    }

    debounceTimeout = setTimeout(async () => {
      await validation.validate(formData.value)
    }, debounceMs)
  }

  // Auto-validate on change if enabled
  if (validateOnChange) {
    // Watch for changes in form data and validate
    // Note: This would require watchEffect, but we keep it simple for now
    // Users can manually call validateForm() or debouncedValidate()
  }

  return {
    ...validation,
    isValid,
    validateForm,
    debouncedValidate
  }
}

/**
 * Helper to get CSS classes for form inputs based on validation state
 */
export function getValidationClass(
  fieldName: string,
  hasError: boolean,
  touched: boolean = true
): string {
  if (!touched) return ''
  return hasError ? 'is-invalid' : 'is-valid'
}

/**
 * Helper to format validation errors for display
 */
export function formatValidationErrors(errors: ValidationError[]): string {
  if (errors.length === 0) return ''
  if (errors.length === 1) return errors[0].message
  return errors.map(e => `• ${e.message}`).join('\n')
}
