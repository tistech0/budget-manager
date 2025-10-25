import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BudgetGauges from '../BudgetGauges.vue'
import type { BudgetGauge } from '../BudgetGauges.vue'

describe('BudgetGauges', () => {
  const defaultGauges: BudgetGauge[] = [
    {
      label: 'Compte Courant',
      current: 1500,
      target: 2000,
      color: '#2196F3',
      remaining: 500
    },
    {
      label: 'Charges Fixes',
      current: 800,
      target: 1000,
      color: '#FF9800',
      remaining: 200
    },
    {
      label: 'Dépenses Variables',
      current: 300,
      target: 600,
      color: '#9C27B0',
      remaining: 300
    }
  ]

  it('renders all gauge cards', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: defaultGauges
      }
    })

    const cards = wrapper.findAll('.gauge-card')
    expect(cards).toHaveLength(3)
  })

  it('displays gauge labels correctly', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: defaultGauges
      }
    })

    const labels = wrapper.findAll('.gauge-label')
    expect(labels[0].text()).toBe('Compte Courant')
    expect(labels[1].text()).toBe('Charges Fixes')
    expect(labels[2].text()).toBe('Dépenses Variables')
  })

  it('calculates and displays percentages correctly', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: defaultGauges
      }
    })

    const percentages = wrapper.findAll('.gauge-percent')
    expect(percentages[0].text()).toBe('75%') // 1500 / 2000 = 75%
    expect(percentages[1].text()).toBe('80%') // 800 / 1000 = 80%
    expect(percentages[2].text()).toBe('50%') // 300 / 600 = 50%
  })

  it('rounds percentages to nearest integer', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 333,
            target: 1000,
            color: '#2196F3'
          }
        ]
      }
    })

    const percentage = wrapper.find('.gauge-percent')
    expect(percentage.text()).toBe('33%') // 333 / 1000 = 33.3% rounded to 33%
  })

  it('applies correct status class based on percentage', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Low',
            current: 300,
            target: 1000,
            color: '#2196F3'
          },
          {
            label: 'Medium',
            current: 600,
            target: 1000,
            color: '#FF9800'
          },
          {
            label: 'Good',
            current: 850,
            target: 1000,
            color: '#4CAF50'
          },
          {
            label: 'Over',
            current: 1200,
            target: 1000,
            color: '#F44336'
          }
        ]
      }
    })

    const percentSpans = wrapper.findAll('.gauge-percent')
    expect(percentSpans[0].classes()).toContain('low') // 30% < 50%
    expect(percentSpans[1].classes()).toContain('medium') // 60% between 50-80%
    expect(percentSpans[2].classes()).toContain('good') // 85% between 80-100%
    expect(percentSpans[3].classes()).toContain('over') // 120% >= 100%
  })

  it('displays current and target amounts correctly', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [defaultGauges[0]]
      }
    })

    const current = wrapper.find('.gauge-current')
    const target = wrapper.find('.gauge-target')

    expect(current.text()).toContain('1')
    expect(current.text()).toContain('500')
    expect(target.text()).toContain('2')
    expect(target.text()).toContain('000')
  })

  it('displays remaining amount when provided', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 750,
            target: 1000,
            color: '#2196F3',
            remaining: 250
          }
        ]
      }
    })

    const remaining = wrapper.find('.gauge-remaining')
    expect(remaining.exists()).toBe(true)
    expect(remaining.text()).toContain('Reste')
    expect(remaining.find('.remaining-value').text()).toContain('250')
  })

  it('does not display remaining amount when not provided', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 750,
            target: 1000,
            color: '#2196F3'
          }
        ]
      }
    })

    const remaining = wrapper.find('.gauge-remaining')
    expect(remaining.exists()).toBe(false)
  })

  it('applies negative class to remaining when negative', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 1200,
            target: 1000,
            color: '#F44336',
            remaining: -200
          }
        ]
      }
    })

    const remainingValue = wrapper.find('.remaining-value')
    expect(remainingValue.classes()).toContain('negative')
    expect(remainingValue.text()).toContain('200') // Displays absolute value
  })

  it('sets progress bar width based on percentage', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 750,
            target: 1000,
            color: '#2196F3'
          }
        ]
      }
    })

    const fill = wrapper.find('.gauge-fill')
    const style = fill.attributes('style')
    expect(style).toContain('width: 75%')
  })

  it('caps progress bar width at 100%', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 1500,
            target: 1000,
            color: '#F44336'
          }
        ]
      }
    })

    const fill = wrapper.find('.gauge-fill')
    const style = fill.attributes('style')
    expect(style).toContain('width: 100%')
  })

  it('applies custom color to progress bar', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 500,
            target: 1000,
            color: '#FF5722'
          }
        ]
      }
    })

    const fill = wrapper.find('.gauge-fill')
    const style = fill.attributes('style')
    expect(style).toContain('background: rgb(255, 87, 34)') // #FF5722 in RGB
  })

  it('handles zero target without error', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 0,
            target: 0,
            color: '#2196F3'
          }
        ]
      }
    })

    const percentage = wrapper.find('.gauge-percent')
    expect(percentage.text()).toBe('0%')
  })

  it('has proper ARIA labels for accessibility', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [defaultGauges[0]]
      }
    })

    // Check main region
    const region = wrapper.find('.budget-gauges')
    expect(region.attributes('role')).toBe('region')
    expect(region.attributes('aria-label')).toBe('Jauges budgétaires')

    // Check article structure
    const article = wrapper.find('.gauge-card')
    expect(article.attributes('role')).toBe('article')
    expect(article.attributes('aria-labelledby')).toBe('gauge-label-0')

    // Check label has proper id
    const label = wrapper.find('.gauge-label')
    expect(label.attributes('id')).toBe('gauge-label-0')
  })

  it('has proper ARIA label for percentage', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [defaultGauges[0]]
      }
    })

    const percentSpan = wrapper.find('.gauge-percent')
    expect(percentSpan.attributes('aria-label')).toBe('75 pourcent utilisé')
  })

  it('has progressbar role with proper ARIA attributes', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [defaultGauges[0]]
      }
    })

    const progressbar = wrapper.find('[role="progressbar"]')
    expect(progressbar.exists()).toBe(true)
    expect(progressbar.attributes('aria-valuenow')).toBe('75')
    expect(progressbar.attributes('aria-valuemin')).toBe('0')
    expect(progressbar.attributes('aria-valuemax')).toBe('100')
    expect(progressbar.attributes('aria-label')).toBe('Compte Courant: 75 pourcent')
  })

  it('has proper ARIA labels for amounts', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [defaultGauges[0]]
      }
    })

    const current = wrapper.find('.gauge-current')
    expect(current.attributes('aria-label')).toBe('Montant actuel')

    const target = wrapper.find('.gauge-target')
    expect(target.attributes('aria-label')).toBe('Montant cible')
  })

  it('has proper ARIA label for remaining amount', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 750,
            target: 1000,
            color: '#2196F3',
            remaining: 250
          }
        ]
      }
    })

    const remainingDiv = wrapper.find('.gauge-remaining')
    const ariaLabel = remainingDiv.attributes('aria-label')
    expect(ariaLabel).toContain('Reste à dépenser ou épargner')
    expect(ariaLabel).toContain('250')
  })

  it('applies glass-card styling to all cards', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: defaultGauges
      }
    })

    const cards = wrapper.findAll('.gauge-card')
    cards.forEach(card => {
      expect(card.classes()).toContain('glass-card')
    })
  })

  it('displays separator between current and target', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [defaultGauges[0]]
      }
    })

    const separator = wrapper.find('.gauge-separator')
    expect(separator.exists()).toBe(true)
    expect(separator.text()).toBe('/')
    expect(separator.attributes('aria-hidden')).toBe('true')
  })

  it('handles large numbers correctly', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 123456,
            target: 500000,
            color: '#2196F3',
            remaining: 376544
          }
        ]
      }
    })

    const current = wrapper.find('.gauge-current')
    const currentText = current.text()
    expect(currentText).toContain('123')
    expect(currentText).toContain('456')

    const target = wrapper.find('.gauge-target')
    const targetText = target.text()
    expect(targetText).toContain('500')
    expect(targetText).toContain('000')
  })

  it('renders multiple gauges with unique IDs', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: defaultGauges
      }
    })

    const labels = wrapper.findAll('.gauge-label')
    expect(labels[0].attributes('id')).toBe('gauge-label-0')
    expect(labels[1].attributes('id')).toBe('gauge-label-1')
    expect(labels[2].attributes('id')).toBe('gauge-label-2')

    const articles = wrapper.findAll('.gauge-card')
    expect(articles[0].attributes('aria-labelledby')).toBe('gauge-label-0')
    expect(articles[1].attributes('aria-labelledby')).toBe('gauge-label-1')
    expect(articles[2].attributes('aria-labelledby')).toBe('gauge-label-2')
  })

  it('handles empty gauges array', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: []
      }
    })

    const cards = wrapper.findAll('.gauge-card')
    expect(cards).toHaveLength(0)
  })

  it('displays neutral status class for zero target', () => {
    const wrapper = mount(BudgetGauges, {
      props: {
        gauges: [
          {
            label: 'Test',
            current: 0,
            target: 0,
            color: '#2196F3'
          }
        ]
      }
    })

    const percentSpan = wrapper.find('.gauge-percent')
    expect(percentSpan.classes()).toContain('neutral')
  })
})
