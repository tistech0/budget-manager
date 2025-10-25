import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  formatCurrency,
  formatPercentage,
  formatDate,
  formatDateTime,
  formatMonth,
  truncateText,
  generateRandomColor,
  isValidEmail,
  isValidAmount,
  slugify,
  daysDifference,
  formatNumber,
  generateTempId
} from '../formatters'

describe('formatters', () => {
  describe('formatCurrency', () => {
    it('should format positive numbers correctly', () => {
      const result = formatCurrency(1234.56)
      expect(result).toContain('1')
      expect(result).toContain('234,56')
      expect(result).toContain('€')
    })

    it('should format zero correctly', () => {
      expect(formatCurrency(0)).toContain('0,00')
      expect(formatCurrency(0)).toContain('€')
    })

    it('should format negative numbers correctly', () => {
      const result = formatCurrency(-500.75)
      expect(result).toContain('-')
      expect(result).toContain('500,75')
      expect(result).toContain('€')
    })

    it('should format large numbers with proper thousand separators', () => {
      const result = formatCurrency(1000000)
      expect(result).toContain('1')
      expect(result).toContain('000')
      expect(result).toContain('€')
    })

    it('should handle null values', () => {
      expect(formatCurrency(null)).toContain('0,00')
      expect(formatCurrency(null)).toContain('€')
    })

    it('should handle undefined values', () => {
      expect(formatCurrency(undefined)).toContain('0,00')
      expect(formatCurrency(undefined)).toContain('€')
    })

    it('should always show 2 decimal places', () => {
      expect(formatCurrency(100)).toContain('100,00')
      expect(formatCurrency(99.9)).toContain('99,90')
    })
  })

  describe('formatPercentage', () => {
    it('should format percentage correctly', () => {
      const result = formatPercentage(50)
      expect(result).toContain('50')
      expect(result).toContain('%')
    })

    it('should format decimal percentages', () => {
      const result = formatPercentage(33.33)
      expect(result).toContain('33,3')
      expect(result).toContain('%')
    })

    it('should format zero correctly', () => {
      const result = formatPercentage(0)
      expect(result).toContain('0')
      expect(result).toContain('%')
    })

    it('should format 100% correctly', () => {
      const result = formatPercentage(100)
      expect(result).toContain('100')
      expect(result).toContain('%')
    })

    it('should handle null values', () => {
      expect(formatPercentage(null)).toBe('0%')
    })

    it('should handle undefined values', () => {
      expect(formatPercentage(undefined)).toBe('0%')
    })

    it('should round to max 1 decimal place', () => {
      const result = formatPercentage(33.3333)
      expect(result).toContain('33,3')
      expect(result).toContain('%')
    })

    it('should handle values over 100%', () => {
      const result = formatPercentage(150)
      expect(result).toContain('150')
      expect(result).toContain('%')
    })
  })

  describe('formatDate', () => {
    it('should format Date object correctly', () => {
      const date = new Date('2024-01-15T10:30:00')
      expect(formatDate(date)).toMatch(/15\/01\/2024/)
    })

    it('should format date string correctly', () => {
      expect(formatDate('2024-12-25')).toMatch(/25\/12\/2024/)
    })

    it('should handle ISO date strings', () => {
      expect(formatDate('2024-06-30T12:00:00.000Z')).toMatch(/30\/06\/2024/)
    })
  })

  describe('formatDateTime', () => {
    it('should format Date object with time', () => {
      const date = new Date('2024-01-15T14:30:00')
      const result = formatDateTime(date)
      expect(result).toMatch(/15\/01\/2024/)
      expect(result).toMatch(/14:30/)
    })

    it('should format date string with time', () => {
      const result = formatDateTime('2024-12-25T23:45:00')
      expect(result).toMatch(/25\/12\/2024/)
      expect(result).toMatch(/23:45/)
    })
  })

  describe('formatMonth', () => {
    it('should format month string correctly', () => {
      expect(formatMonth('2024-01')).toMatch(/janvier 2024/)
    })

    it('should format December correctly', () => {
      expect(formatMonth('2024-12')).toMatch(/décembre 2024/)
    })

    it('should handle different years', () => {
      expect(formatMonth('2023-06')).toMatch(/juin 2023/)
    })
  })

  describe('truncateText', () => {
    it('should not truncate text shorter than maxLength', () => {
      expect(truncateText('Hello', 10)).toBe('Hello')
    })

    it('should not truncate text equal to maxLength', () => {
      expect(truncateText('Hello', 5)).toBe('Hello')
    })

    it('should truncate text longer than maxLength', () => {
      expect(truncateText('Hello World!', 8)).toBe('Hello...')
    })

    it('should handle empty strings', () => {
      expect(truncateText('', 10)).toBe('')
    })

    it('should include ellipsis in the maxLength count', () => {
      const result = truncateText('1234567890', 8)
      expect(result.length).toBe(8)
      expect(result).toBe('12345...')
    })
  })

  describe('generateRandomColor', () => {
    const validColors = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
      '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'
    ]

    it('should return a valid color from the palette', () => {
      const color = generateRandomColor()
      expect(validColors).toContain(color)
    })

    it('should return a hex color format', () => {
      const color = generateRandomColor()
      expect(color).toMatch(/^#[0-9A-F]{6}$/i)
    })

    it('should potentially return different colors on multiple calls', () => {
      const colors = new Set()
      for (let i = 0; i < 50; i++) {
        colors.add(generateRandomColor())
      }
      // With 10 colors and 50 calls, we should get multiple different colors
      expect(colors.size).toBeGreaterThan(1)
    })
  })

  describe('isValidEmail', () => {
    it('should validate correct email addresses', () => {
      expect(isValidEmail('user@example.com')).toBe(true)
      expect(isValidEmail('test.user@domain.co.uk')).toBe(true)
      expect(isValidEmail('name+tag@example.com')).toBe(true)
    })

    it('should reject invalid email addresses', () => {
      expect(isValidEmail('invalid')).toBe(false)
      expect(isValidEmail('@example.com')).toBe(false)
      expect(isValidEmail('user@')).toBe(false)
      expect(isValidEmail('user @example.com')).toBe(false)
      expect(isValidEmail('')).toBe(false)
    })

    it('should reject emails without @ symbol', () => {
      expect(isValidEmail('userexample.com')).toBe(false)
    })

    it('should reject emails without domain', () => {
      expect(isValidEmail('user@')).toBe(false)
    })

    it('should reject emails with spaces', () => {
      expect(isValidEmail('user @example.com')).toBe(false)
      expect(isValidEmail('user@example .com')).toBe(false)
    })
  })

  describe('isValidAmount', () => {
    it('should validate positive numbers', () => {
      expect(isValidAmount(100)).toBe(true)
      expect(isValidAmount(0.01)).toBe(true)
      expect(isValidAmount(1000000)).toBe(true)
    })

    it('should validate zero', () => {
      expect(isValidAmount(0)).toBe(true)
    })

    it('should reject negative numbers', () => {
      expect(isValidAmount(-1)).toBe(false)
      expect(isValidAmount(-0.01)).toBe(false)
    })

    it('should reject NaN', () => {
      expect(isValidAmount(NaN)).toBe(false)
    })

    it('should reject non-numbers', () => {
      expect(isValidAmount('100' as any)).toBe(false)
      expect(isValidAmount(null as any)).toBe(false)
      expect(isValidAmount(undefined as any)).toBe(false)
    })
  })

  describe('slugify', () => {
    it('should convert text to lowercase', () => {
      expect(slugify('HELLO WORLD')).toBe('hello-world')
    })

    it('should replace spaces with hyphens', () => {
      expect(slugify('hello world')).toBe('hello-world')
    })

    it('should remove accents', () => {
      expect(slugify('Café français')).toBe('cafe-francais')
      expect(slugify('été')).toBe('ete')
    })

    it('should remove special characters', () => {
      expect(slugify('hello@world!')).toBe('helloworld')
      expect(slugify('test#123$')).toBe('test123')
    })

    it('should handle multiple spaces', () => {
      expect(slugify('hello   world')).toBe('hello-world')
    })

    it('should handle leading/trailing spaces', () => {
      const result = slugify('  hello world  ')
      // Should remove leading/trailing hyphens after trim, but implementation may vary
      expect(result).toContain('hello-world')
    })

    it('should handle multiple hyphens', () => {
      expect(slugify('hello---world')).toBe('hello-world')
    })

    it('should handle empty strings', () => {
      expect(slugify('')).toBe('')
    })

    it('should handle complex French text', () => {
      expect(slugify('Hébergement été 2024')).toBe('hebergement-ete-2024')
    })
  })

  describe('daysDifference', () => {
    it('should calculate difference between two dates', () => {
      const date1 = new Date('2024-01-01')
      const date2 = new Date('2024-01-10')
      expect(daysDifference(date1, date2)).toBe(9)
    })

    it('should return absolute difference regardless of order', () => {
      const date1 = new Date('2024-01-10')
      const date2 = new Date('2024-01-01')
      expect(daysDifference(date1, date2)).toBe(9)
    })

    it('should return 0 for same date', () => {
      const date = new Date('2024-01-01')
      expect(daysDifference(date, date)).toBe(0)
    })

    it('should handle dates one day apart', () => {
      const date1 = new Date('2024-01-01')
      const date2 = new Date('2024-01-02')
      expect(daysDifference(date1, date2)).toBe(1)
    })

    it('should handle dates across months', () => {
      const date1 = new Date('2024-01-31')
      const date2 = new Date('2024-02-01')
      expect(daysDifference(date1, date2)).toBe(1)
    })

    it('should handle dates across years', () => {
      const date1 = new Date('2023-12-31')
      const date2 = new Date('2024-01-01')
      expect(daysDifference(date1, date2)).toBe(1)
    })
  })

  describe('formatNumber', () => {
    it('should format numbers with thousand separators', () => {
      const result = formatNumber(1000)
      expect(result).toContain('1')
      expect(result).toContain('000')
    })

    it('should format large numbers correctly', () => {
      const result = formatNumber(1000000)
      expect(result).toContain('1')
      expect(result).toContain('000')
    })

    it('should format small numbers without separators', () => {
      expect(formatNumber(100)).toBe('100')
    })

    it('should format zero correctly', () => {
      expect(formatNumber(0)).toBe('0')
    })

    it('should format negative numbers', () => {
      const result = formatNumber(-1000)
      expect(result).toContain('-')
      expect(result).toContain('1')
      expect(result).toContain('000')
    })

    it('should format decimal numbers', () => {
      const result = formatNumber(1234.56)
      expect(result).toContain('1')
      expect(result).toContain('234')
      expect(result).toContain(',56')
    })
  })

  describe('generateTempId', () => {
    it('should generate an ID starting with temp_', () => {
      const id = generateTempId()
      expect(id).toMatch(/^temp_/)
    })

    it('should generate unique IDs', () => {
      const id1 = generateTempId()
      const id2 = generateTempId()
      expect(id1).not.toBe(id2)
    })

    it('should generate IDs with timestamp component', () => {
      const id = generateTempId()
      const parts = id.split('_')
      expect(parts.length).toBe(3)
      expect(parts[0]).toBe('temp')
      expect(Number(parts[1])).toBeGreaterThan(0)
    })

    it('should generate IDs with random component', () => {
      const id = generateTempId()
      const parts = id.split('_')
      expect(parts[2]).toBeTruthy()
      expect(parts[2].length).toBeGreaterThan(0)
    })

    it('should generate multiple unique IDs', () => {
      const ids = new Set()
      for (let i = 0; i < 10; i++) {
        ids.add(generateTempId())
      }
      expect(ids.size).toBe(10)
    })
  })
})
