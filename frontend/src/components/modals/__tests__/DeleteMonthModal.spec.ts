import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DeleteMonthModal from '../DeleteMonthModal.vue'

describe('DeleteMonthModal', () => {
  beforeEach(() => {
    const app = document.createElement('div')
    app.id = 'app'
    document.body.appendChild(app)
  })

  afterEach(() => {
    document.body.innerHTML = ''
  })

  const defaultProps = {
    modelValue: true,
    monthLabel: 'janvier 2025',
    monthValue: '2025-01'
  }

  it('does not render when modelValue is false', () => {
    mount(DeleteMonthModal, {
      props: {
        ...defaultProps,
        modelValue: false
      }
    })

    expect(document.querySelector('.modal-overlay')).toBeNull()
  })

  it('renders when modelValue is true', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    expect(document.querySelector('.modal-overlay')).toBeTruthy()
  })

  it('displays month label in title', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const title = document.querySelector('.modal-title')
    expect(title?.textContent).toContain('janvier 2025')
  })

  it('displays month label in warning text', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const warning = document.querySelector('.warning-text')
    expect(warning?.textContent).toContain('janvier 2025')
  })

  it('displays confirmation input', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    expect(input).toBeTruthy()
    expect(input.placeholder).toBe('Tapez SUPPRIMER pour confirmer')
  })

  it('delete button is disabled by default', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    expect(deleteBtn.disabled).toBe(true)
  })

  it('delete button is enabled when SUPPRIMER is typed', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    input.value = 'SUPPRIMER'
    input.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    expect(deleteBtn.disabled).toBe(false)
  })

  it('delete button remains disabled for incorrect text', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    input.value = 'supprimer'
    input.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    expect(deleteBtn.disabled).toBe(true)
  })

  it('does not emit confirm when clicked without correct text', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    deleteBtn.click()
    await wrapper.vm.$nextTick()

    // Should not emit confirm without correct text
    expect(wrapper.emitted('confirm')).toBeFalsy()
  })

  it('emits confirm event with month value when confirmed correctly', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    input.value = 'SUPPRIMER'
    input.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    deleteBtn.click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('confirm')).toBeTruthy()
    expect(wrapper.emitted('confirm')?.[0]).toEqual(['2025-01'])
  })

  it('emits cancel event when cancel button clicked', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const cancelBtn = document.querySelector('.btn-secondary') as HTMLButtonElement
    cancelBtn.click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('emits update:modelValue false when cancel button clicked', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const cancelBtn = document.querySelector('.btn-secondary') as HTMLButtonElement
    cancelBtn.click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })

  it('emits cancel when close button (X) clicked', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const closeBtn = document.querySelector('.btn-close') as HTMLButtonElement
    closeBtn.click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('cancel')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })

  it('emits cancel when overlay clicked', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const overlay = document.querySelector('.modal-overlay') as HTMLElement
    overlay.click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('does not emit cancel when modal content clicked', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const content = document.querySelector('.modal-content') as HTMLElement
    content.click()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('cancel')).toBeFalsy()
  })

  it('resets confirmation text when modal closes', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    input.value = 'SUPPRIMER'
    input.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()

    await wrapper.setProps({ modelValue: false })
    await wrapper.vm.$nextTick()

    await wrapper.setProps({ modelValue: true })
    await wrapper.vm.$nextTick()

    const newInput = document.querySelector('.confirmation-input') as HTMLInputElement
    expect(newInput.value).toBe('')
  })

  it('resets state when modal closes and reopens', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    // Enter text
    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    input.value = 'WRONG'
    input.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()

    // Close modal
    await wrapper.setProps({ modelValue: false })
    await wrapper.vm.$nextTick()

    // Reopen
    await wrapper.setProps({ modelValue: true })
    await wrapper.vm.$nextTick()

    // Input should be reset
    const newInput = document.querySelector('.confirmation-input') as HTMLInputElement
    expect(newInput.value).toBe('')
  })

  it('disables buttons when isDeleting is true', () => {
    mount(DeleteMonthModal, {
      props: {
        ...defaultProps,
        isDeleting: true
      }
    })

    const cancelBtn = document.querySelector('.btn-secondary') as HTMLButtonElement
    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement

    expect(cancelBtn.disabled).toBe(true)
    expect(deleteBtn.disabled).toBe(true)
  })

  it('shows deleting text when isDeleting is true', () => {
    mount(DeleteMonthModal, {
      props: {
        ...defaultProps,
        isDeleting: true
      }
    })

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    expect(deleteBtn.textContent).toContain('Suppression...')
  })

  it('shows default delete text when isDeleting is false', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const deleteBtn = document.querySelector('.btn-danger') as HTMLButtonElement
    expect(deleteBtn.textContent).toContain('Supprimer le mois')
  })

  it('displays deletion list items', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const listItems = document.querySelectorAll('.deletion-list li')
    expect(listItems.length).toBeGreaterThan(0)
    expect(listItems[0].textContent).toContain('salaire validé')
  })

  it('displays info box about non-affected items', () => {
    mount(DeleteMonthModal, {
      props: defaultProps
    })

    const infoText = document.querySelector('.info-text')
    expect(infoText).toBeTruthy()
    expect(infoText?.textContent).toContain('comptes et objectifs')
  })

  it('supports Enter key to confirm when text is correct', async () => {
    const wrapper = mount(DeleteMonthModal, {
      props: defaultProps
    })

    const input = document.querySelector('.confirmation-input') as HTMLInputElement
    input.value = 'SUPPRIMER'
    input.dispatchEvent(new Event('input'))
    await wrapper.vm.$nextTick()

    const enterEvent = new KeyboardEvent('keyup', { key: 'Enter' })
    input.dispatchEvent(enterEvent)
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('confirm')).toBeTruthy()
  })
})
