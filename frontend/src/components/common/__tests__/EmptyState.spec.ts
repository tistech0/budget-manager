import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EmptyState from '../EmptyState.vue'

describe('EmptyState', () => {
  const defaultProps = {
    icon: '🎯',
    title: 'No data found',
    description: 'There is currently no data to display'
  }

  it('renders empty state container', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    const container = wrapper.find('.empty-state')
    expect(container.exists()).toBe(true)
  })

  it('applies glass-card styling', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    const container = wrapper.find('.empty-state')
    expect(container.classes()).toContain('glass-card')
  })

  it('displays icon', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    const icon = wrapper.find('.empty-icon')
    expect(icon.exists()).toBe(true)
    expect(icon.text()).toBe('🎯')
  })

  it('displays title', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    const title = wrapper.find('.empty-title')
    expect(title.exists()).toBe(true)
    expect(title.text()).toBe('No data found')
  })

  it('displays description', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    const description = wrapper.find('.empty-description')
    expect(description.exists()).toBe(true)
    expect(description.text()).toBe('There is currently no data to display')
  })

  it('displays different icons', () => {
    const icons = ['📊', '💼', '🎉', '⚠️']

    icons.forEach(iconValue => {
      const wrapper = mount(EmptyState, {
        props: {
          ...defaultProps,
          icon: iconValue
        }
      })

      const icon = wrapper.find('.empty-icon')
      expect(icon.text()).toBe(iconValue)
    })
  })

  it('displays custom title', () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        title: 'Custom Title Here'
      }
    })

    const title = wrapper.find('.empty-title')
    expect(title.text()).toBe('Custom Title Here')
  })

  it('displays custom description', () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        description: 'This is a custom description with more details about the empty state.'
      }
    })

    const description = wrapper.find('.empty-description')
    expect(description.text()).toBe('This is a custom description with more details about the empty state.')
  })

  it('does not render action button when actionLabel is not provided', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    const button = wrapper.find('.btn')
    expect(button.exists()).toBe(false)
  })

  it('renders action button when actionLabel is provided', () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: 'Add New Item'
      }
    })

    const button = wrapper.find('.btn')
    expect(button.exists()).toBe(true)
    expect(button.text()).toBe('Add New Item')
  })

  it('applies primary and large button classes to action button', () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: 'Click Me'
      }
    })

    const button = wrapper.find('.btn')
    expect(button.classes()).toContain('btn-primary')
    expect(button.classes()).toContain('btn-lg')
  })

  it('emits action event when button is clicked', async () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: 'Take Action'
      }
    })

    const button = wrapper.find('.btn')
    await button.trigger('click')

    expect(wrapper.emitted('action')).toBeTruthy()
    expect(wrapper.emitted('action')).toHaveLength(1)
  })

  it('emits action event multiple times on multiple clicks', async () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: 'Take Action'
      }
    })

    const button = wrapper.find('.btn')
    await button.trigger('click')
    await button.trigger('click')
    await button.trigger('click')

    expect(wrapper.emitted('action')).toHaveLength(3)
  })

  it('supports custom actions slot', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps,
      slots: {
        actions: '<button class="custom-action">Custom Action</button>'
      }
    })

    const customButton = wrapper.find('.custom-action')
    expect(customButton.exists()).toBe(true)
    expect(customButton.text()).toBe('Custom Action')

    // Default button should not exist when slot is provided
    const defaultButton = wrapper.find('.btn-primary')
    expect(defaultButton.exists()).toBe(false)
  })

  it('supports multiple buttons in actions slot', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps,
      slots: {
        actions: `
          <button class="action-1">Action 1</button>
          <button class="action-2">Action 2</button>
        `
      }
    })

    expect(wrapper.find('.action-1').exists()).toBe(true)
    expect(wrapper.find('.action-2').exists()).toBe(true)
  })

  it('renders elements in correct order', () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: 'Action'
      }
    })

    const container = wrapper.find('.empty-state')
    const children = Array.from(container.element.children)

    expect(children[0].classList.contains('empty-icon')).toBe(true)
    expect(children[1].classList.contains('empty-title')).toBe(true)
    expect(children[2].classList.contains('empty-description')).toBe(true)
    expect(children[3].classList.contains('btn')).toBe(true)
  })

  it('updates icon reactively', async () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    expect(wrapper.find('.empty-icon').text()).toBe('🎯')

    await wrapper.setProps({ icon: '📊' })

    expect(wrapper.find('.empty-icon').text()).toBe('📊')
  })

  it('updates title reactively', async () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    expect(wrapper.find('.empty-title').text()).toBe('No data found')

    await wrapper.setProps({ title: 'New Title' })

    expect(wrapper.find('.empty-title').text()).toBe('New Title')
  })

  it('updates description reactively', async () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    expect(wrapper.find('.empty-description').text()).toBe('There is currently no data to display')

    await wrapper.setProps({ description: 'New description' })

    expect(wrapper.find('.empty-description').text()).toBe('New description')
  })

  it('shows action button when actionLabel is added', async () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    expect(wrapper.find('.btn').exists()).toBe(false)

    await wrapper.setProps({ actionLabel: 'New Action' })

    expect(wrapper.find('.btn').exists()).toBe(true)
    expect(wrapper.find('.btn').text()).toBe('New Action')
  })

  it('hides action button when actionLabel is removed', async () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: 'Initial Action'
      }
    })

    expect(wrapper.find('.btn').exists()).toBe(true)

    await wrapper.setProps({ actionLabel: undefined })

    expect(wrapper.find('.btn').exists()).toBe(false)
  })

  it('handles long description text', () => {
    const longDescription = 'This is a very long description that contains multiple sentences to test how the component handles larger amounts of text. It should wrap properly and remain readable even with extensive content.'

    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        description: longDescription
      }
    })

    const description = wrapper.find('.empty-description')
    expect(description.text()).toBe(longDescription)
  })

  it('handles multi-line description', () => {
    const multilineDescription = 'Line 1\nLine 2\nLine 3'

    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        description: multilineDescription
      }
    })

    const description = wrapper.find('.empty-description')
    expect(description.text()).toBe(multilineDescription)
  })

  it('handles special characters in text', () => {
    const wrapper = mount(EmptyState, {
      props: {
        icon: '🎉',
        title: 'Special chars: <>&"\'',
        description: 'Description with special chars: <>&"\''
      }
    })

    const title = wrapper.find('.empty-title')
    const description = wrapper.find('.empty-description')

    expect(title.text()).toBe('Special chars: <>&"\'')
    expect(description.text()).toBe('Description with special chars: <>&"\'')
  })

  it('handles empty string actionLabel', () => {
    const wrapper = mount(EmptyState, {
      props: {
        ...defaultProps,
        actionLabel: ''
      }
    })

    // Empty string is falsy, so button should not be displayed
    expect(wrapper.find('.btn').exists()).toBe(false)
  })

  it('renders with all props provided', () => {
    const wrapper = mount(EmptyState, {
      props: {
        icon: '💼',
        title: 'Complete Title',
        description: 'Complete Description',
        actionLabel: 'Complete Action'
      }
    })

    expect(wrapper.find('.empty-icon').text()).toBe('💼')
    expect(wrapper.find('.empty-title').text()).toBe('Complete Title')
    expect(wrapper.find('.empty-description').text()).toBe('Complete Description')
    expect(wrapper.find('.btn').text()).toBe('Complete Action')
  })

  it('has proper semantic HTML structure', () => {
    const wrapper = mount(EmptyState, {
      props: defaultProps
    })

    // Icon should be a span
    expect(wrapper.find('.empty-icon').element.tagName).toBe('SPAN')

    // Title should be an h3
    expect(wrapper.find('.empty-title').element.tagName).toBe('H3')

    // Description should be a p
    expect(wrapper.find('.empty-description').element.tagName).toBe('P')
  })
})
