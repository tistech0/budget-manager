import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MonthSelector from '../MonthSelector.vue'

describe('MonthSelector', () => {
  it('renders month label correctly', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        canGoToPrevious: true,
        canGoToNext: true
      }
    })

    expect(wrapper.find('.current-month').text()).toBe('janvier 2025')
  })

  it('renders period info when provided', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        periodInfo: 'Cycle du 1/01 au 31/01'
      }
    })

    expect(wrapper.find('.period-info').text()).toBe('Cycle du 1/01 au 31/01')
  })

  it('does not render period info when not provided', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025'
      }
    })

    expect(wrapper.find('.period-info').exists()).toBe(false)
  })

  it('emits change-month event with prev when previous button is clicked', async () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        canGoToPrevious: true
      }
    })

    const prevButton = wrapper.findAll('.month-btn')[0]
    await prevButton.trigger('click')

    expect(wrapper.emitted('change-month')).toBeTruthy()
    expect(wrapper.emitted('change-month')![0]).toEqual(['prev'])
  })

  it('emits change-month event with next when next button is clicked', async () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        canGoToNext: true
      }
    })

    const nextButton = wrapper.findAll('.month-btn')[1]
    await nextButton.trigger('click')

    expect(wrapper.emitted('change-month')).toBeTruthy()
    expect(wrapper.emitted('change-month')![0]).toEqual(['next'])
  })

  it('disables previous button when canGoToPrevious is false', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        canGoToPrevious: false
      }
    })

    const prevButton = wrapper.findAll('.month-btn')[0]
    expect(prevButton.attributes('disabled')).toBeDefined()
  })

  it('disables next button when canGoToNext is false', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        canGoToNext: false
      }
    })

    const nextButton = wrapper.findAll('.month-btn')[1]
    expect(nextButton.attributes('disabled')).toBeDefined()
  })

  it('disables all buttons when disabled prop is true', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        disabled: true
      }
    })

    const buttons = wrapper.findAll('.month-btn')
    buttons.forEach(button => {
      expect(button.attributes('disabled')).toBeDefined()
    })
  })

  it('shows delete button when showDelete is true', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        showDelete: true
      }
    })

    expect(wrapper.find('.month-delete-btn').exists()).toBe(true)
  })

  it('hides delete button when showDelete is false', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        showDelete: false
      }
    })

    expect(wrapper.find('.month-delete-btn').exists()).toBe(false)
  })

  it('emits delete-month event when delete button is clicked', async () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        showDelete: true
      }
    })

    const deleteButton = wrapper.find('.month-delete-btn')
    await deleteButton.trigger('click')

    expect(wrapper.emitted('delete-month')).toBeTruthy()
  })

  it('has proper ARIA labels for accessibility', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025'
      }
    })

    const nav = wrapper.find('nav')
    expect(nav.attributes('aria-label')).toBe('Navigation mensuelle')

    const prevButton = wrapper.findAll('.month-btn')[0]
    expect(prevButton.attributes('aria-label')).toBe('Aller au mois précédent')

    const nextButton = wrapper.findAll('.month-btn')[1]
    expect(nextButton.attributes('aria-label')).toBe('Aller au mois suivant')
  })

  it('supports keyboard navigation with arrow keys', async () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025',
        canGoToPrevious: true,
        canGoToNext: true
      }
    })

    const prevButton = wrapper.findAll('.month-btn')[0]
    await prevButton.trigger('keydown.left')
    expect(wrapper.emitted('change-month')).toBeTruthy()
    expect(wrapper.emitted('change-month')![0]).toEqual(['prev'])

    const nextButton = wrapper.findAll('.month-btn')[1]
    await nextButton.trigger('keydown.right')
    expect(wrapper.emitted('change-month')).toBeTruthy()
    expect(wrapper.emitted('change-month')![1]).toEqual(['next'])
  })

  it('has aria-live region for screen reader announcements', () => {
    const wrapper = mount(MonthSelector, {
      props: {
        monthLabel: 'janvier 2025'
      }
    })

    const monthInfo = wrapper.find('.month-info')
    expect(monthInfo.attributes('role')).toBe('status')
    expect(monthInfo.attributes('aria-live')).toBe('polite')
  })
})
