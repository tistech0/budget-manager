import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingState from '../LoadingState.vue'

describe('LoadingState', () => {
  it('renders loading spinner', () => {
    const wrapper = mount(LoadingState)

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.exists()).toBe(true)
  })

  it('renders loading state container', () => {
    const wrapper = mount(LoadingState)

    const container = wrapper.find('.loading-state')
    expect(container.exists()).toBe(true)
  })

  it('displays message when provided', () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: 'Loading your data...'
      }
    })

    const message = wrapper.find('.loading-message')
    expect(message.exists()).toBe(true)
    expect(message.text()).toBe('Loading your data...')
  })

  it('does not display message when not provided', () => {
    const wrapper = mount(LoadingState)

    const message = wrapper.find('.loading-message')
    expect(message.exists()).toBe(false)
  })

  it('displays submessage when provided', () => {
    const wrapper = mount(LoadingState, {
      props: {
        submessage: 'This may take a few moments'
      }
    })

    const submessage = wrapper.find('.loading-submessage')
    expect(submessage.exists()).toBe(true)
    expect(submessage.text()).toBe('This may take a few moments')
  })

  it('does not display submessage when not provided', () => {
    const wrapper = mount(LoadingState)

    const submessage = wrapper.find('.loading-submessage')
    expect(submessage.exists()).toBe(false)
  })

  it('displays both message and submessage when both provided', () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: 'Loading...',
        submessage: 'Please wait'
      }
    })

    const message = wrapper.find('.loading-message')
    const submessage = wrapper.find('.loading-submessage')

    expect(message.exists()).toBe(true)
    expect(message.text()).toBe('Loading...')
    expect(submessage.exists()).toBe(true)
    expect(submessage.text()).toBe('Please wait')
  })

  it('applies default medium size class', () => {
    const wrapper = mount(LoadingState)

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.classes()).toContain('size-medium')
  })

  it('applies small size class when specified', () => {
    const wrapper = mount(LoadingState, {
      props: {
        size: 'small'
      }
    })

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.classes()).toContain('size-small')
  })

  it('applies medium size class when specified', () => {
    const wrapper = mount(LoadingState, {
      props: {
        size: 'medium'
      }
    })

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.classes()).toContain('size-medium')
  })

  it('applies large size class when specified', () => {
    const wrapper = mount(LoadingState, {
      props: {
        size: 'large'
      }
    })

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.classes()).toContain('size-large')
  })

  it('does not apply fullscreen class by default', () => {
    const wrapper = mount(LoadingState)

    const container = wrapper.find('.loading-state')
    expect(container.classes()).not.toContain('fullscreen')
  })

  it('applies fullscreen class when specified', () => {
    const wrapper = mount(LoadingState, {
      props: {
        fullscreen: true
      }
    })

    const container = wrapper.find('.loading-state')
    expect(container.classes()).toContain('fullscreen')
  })

  it('does not apply fullscreen class when explicitly false', () => {
    const wrapper = mount(LoadingState, {
      props: {
        fullscreen: false
      }
    })

    const container = wrapper.find('.loading-state')
    expect(container.classes()).not.toContain('fullscreen')
  })

  it('has loading content wrapper', () => {
    const wrapper = mount(LoadingState)

    const content = wrapper.find('.loading-content')
    expect(content.exists()).toBe(true)
  })

  it('displays spinner, message, and submessage in correct order', () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: 'Loading...',
        submessage: 'Please wait'
      }
    })

    const content = wrapper.find('.loading-content')
    const children = content.element.children

    expect(children[0].classList.contains('loading-spinner')).toBe(true)
    expect(children[1].classList.contains('loading-message')).toBe(true)
    expect(children[2].classList.contains('loading-submessage')).toBe(true)
  })

  it('works with all props combined', () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: 'Loading your dashboard',
        submessage: 'Fetching latest data...',
        size: 'large',
        fullscreen: true
      }
    })

    const container = wrapper.find('.loading-state')
    expect(container.classes()).toContain('fullscreen')

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.classes()).toContain('size-large')

    const message = wrapper.find('.loading-message')
    expect(message.text()).toBe('Loading your dashboard')

    const submessage = wrapper.find('.loading-submessage')
    expect(submessage.text()).toBe('Fetching latest data...')
  })

  it('renders with minimal props', () => {
    const wrapper = mount(LoadingState)

    const container = wrapper.find('.loading-state')
    expect(container.exists()).toBe(true)

    const spinner = wrapper.find('.loading-spinner')
    expect(spinner.exists()).toBe(true)
    expect(spinner.classes()).toContain('size-medium')

    expect(wrapper.find('.loading-message').exists()).toBe(false)
    expect(wrapper.find('.loading-submessage').exists()).toBe(false)
    expect(container.classes()).not.toContain('fullscreen')
  })

  it('updates message reactively', async () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: 'Initial message'
      }
    })

    expect(wrapper.find('.loading-message').text()).toBe('Initial message')

    await wrapper.setProps({ message: 'Updated message' })

    expect(wrapper.find('.loading-message').text()).toBe('Updated message')
  })

  it('updates submessage reactively', async () => {
    const wrapper = mount(LoadingState, {
      props: {
        submessage: 'Initial submessage'
      }
    })

    expect(wrapper.find('.loading-submessage').text()).toBe('Initial submessage')

    await wrapper.setProps({ submessage: 'Updated submessage' })

    expect(wrapper.find('.loading-submessage').text()).toBe('Updated submessage')
  })

  it('updates size reactively', async () => {
    const wrapper = mount(LoadingState, {
      props: {
        size: 'small'
      }
    })

    expect(wrapper.find('.loading-spinner').classes()).toContain('size-small')

    await wrapper.setProps({ size: 'large' })

    expect(wrapper.find('.loading-spinner').classes()).toContain('size-large')
    expect(wrapper.find('.loading-spinner').classes()).not.toContain('size-small')
  })

  it('updates fullscreen reactively', async () => {
    const wrapper = mount(LoadingState, {
      props: {
        fullscreen: false
      }
    })

    expect(wrapper.find('.loading-state').classes()).not.toContain('fullscreen')

    await wrapper.setProps({ fullscreen: true })

    expect(wrapper.find('.loading-state').classes()).toContain('fullscreen')
  })

  it('shows message when added after mount', async () => {
    const wrapper = mount(LoadingState)

    expect(wrapper.find('.loading-message').exists()).toBe(false)

    await wrapper.setProps({ message: 'New message' })

    expect(wrapper.find('.loading-message').exists()).toBe(true)
    expect(wrapper.find('.loading-message').text()).toBe('New message')
  })

  it('hides message when removed after mount', async () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: 'Initial message'
      }
    })

    expect(wrapper.find('.loading-message').exists()).toBe(true)

    await wrapper.setProps({ message: undefined })

    expect(wrapper.find('.loading-message').exists()).toBe(false)
  })

  it('handles empty string message', () => {
    const wrapper = mount(LoadingState, {
      props: {
        message: ''
      }
    })

    // Empty string is falsy, so message should not be displayed
    expect(wrapper.find('.loading-message').exists()).toBe(false)
  })

  it('handles empty string submessage', () => {
    const wrapper = mount(LoadingState, {
      props: {
        submessage: ''
      }
    })

    // Empty string is falsy, so submessage should not be displayed
    expect(wrapper.find('.loading-submessage').exists()).toBe(false)
  })
})
