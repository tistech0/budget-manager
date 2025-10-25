import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import BaseModal from '../BaseModal.vue'

describe('BaseModal', () => {
  beforeEach(() => {
    // Create a div with id="app" for Teleport target
    const app = document.createElement('div')
    app.id = 'app'
    document.body.appendChild(app)
  })

  afterEach(() => {
    // Clean up
    document.body.innerHTML = ''
    document.body.style.overflow = ''
  })

  it('does not render when modelValue is false', () => {
    mount(BaseModal, {
      props: {
        modelValue: false,
        title: 'Test Modal'
      }
    })

    expect(document.querySelector('.modal-overlay')).toBeNull()
  })

  it('renders when modelValue is true', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    expect(document.querySelector('.modal-overlay')).toBeTruthy()
  })

  it('displays modal title', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'My Custom Title'
      }
    })

    const title = document.querySelector('.modal-title')
    expect(title?.textContent).toBe('My Custom Title')
  })

  it('renders default slot content', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      },
      slots: {
        default: '<p class="test-content">Modal Body Content</p>'
      }
    })

    const content = document.querySelector('.test-content')
    expect(content?.textContent).toBe('Modal Body Content')
  })

  it('renders footer slot when provided', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      },
      slots: {
        footer: '<button class="test-footer-btn">Save</button>'
      }
    })

    const footer = document.querySelector('.modal-footer')
    expect(footer).toBeTruthy()

    const button = document.querySelector('.test-footer-btn')
    expect(button?.textContent).toBe('Save')
  })

  it('does not render footer when slot not provided', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const footer = document.querySelector('.modal-footer')
    expect(footer).toBeNull()
  })

  it('applies medium size class by default', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const container = document.querySelector('.modal-container')
    expect(container?.classList.contains('max-w-2xl')).toBe(true)
  })

  it('applies small size class when specified', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        size: 'small'
      }
    })

    const container = document.querySelector('.modal-container')
    expect(container?.classList.contains('max-w-md')).toBe(true)
  })

  it('applies large size class when specified', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        size: 'large'
      }
    })

    const container = document.querySelector('.modal-container')
    expect(container?.classList.contains('max-w-4xl')).toBe(true)
  })

  it('emits update:modelValue when close button is clicked', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const closeBtn = document.querySelector('.modal-close-btn') as HTMLButtonElement
    closeBtn?.click()

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })

  it('emits close event when close button is clicked', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const closeBtn = document.querySelector('.modal-close-btn') as HTMLButtonElement
    closeBtn?.click()

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('closes on overlay click when closeOnClickOutside is true', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        closeOnClickOutside: true
      }
    })

    const overlay = document.querySelector('.modal-overlay') as HTMLElement
    overlay?.click()

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })

  it('does not close on overlay click when closeOnClickOutside is false', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        closeOnClickOutside: false
      }
    })

    const overlay = document.querySelector('.modal-overlay') as HTMLElement
    overlay?.click()

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeFalsy()
  })

  it('does not close when clicking inside modal container', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const container = document.querySelector('.modal-container') as HTMLElement
    container?.click()

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeFalsy()
  })

  it('closes on Escape key when closeOnEsc is true', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        closeOnEsc: true
      }
    })

    const event = new KeyboardEvent('keydown', { key: 'Escape' })
    document.dispatchEvent(event)

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual([false])
  })

  it('does not close on Escape key when closeOnEsc is false', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        closeOnEsc: false
      }
    })

    const event = new KeyboardEvent('keydown', { key: 'Escape' })
    document.dispatchEvent(event)

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeFalsy()
  })

  it('does not close on other keys', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const event = new KeyboardEvent('keydown', { key: 'Enter' })
    document.dispatchEvent(event)

    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('update:modelValue')).toBeFalsy()
  })

  it('prevents body scroll when modal is open', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: false,
        title: 'Test Modal'
      }
    })

    expect(document.body.style.overflow).toBe('')

    await wrapper.setProps({ modelValue: true })

    expect(document.body.style.overflow).toBe('hidden')
  })

  it('prevents and restores body scroll when modal opens and closes', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: false,
        title: 'Test Modal'
      }
    })

    // Open modal
    await wrapper.setProps({ modelValue: true })
    await wrapper.vm.$nextTick()
    expect(document.body.style.overflow).toBe('hidden')

    // Close modal
    await wrapper.setProps({ modelValue: false })
    await wrapper.vm.$nextTick()
    expect(document.body.style.overflow).toBe('')
  })

  it('restores body scroll on unmount', () => {
    document.body.style.overflow = 'hidden'

    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    wrapper.unmount()

    expect(document.body.style.overflow).toBe('')
  })

  it('has close button with aria-label', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const closeBtn = document.querySelector('.modal-close-btn')
    expect(closeBtn?.getAttribute('aria-label')).toBe('Fermer')
  })

  it('renders with teleport to body', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    await wrapper.vm.$nextTick()

    // Modal should be teleported to body
    const modalOverlay = document.querySelector('.modal-overlay')
    expect(modalOverlay).toBeTruthy()
  })

  it('includes close icon SVG in close button', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const svg = document.querySelector('.modal-close-btn svg')
    expect(svg).toBeTruthy()
    expect(svg?.getAttribute('viewBox')).toBe('0 0 24 24')
  })

  it('renders header with title and close button', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      }
    })

    const header = document.querySelector('.modal-header')
    expect(header).toBeTruthy()

    const title = header?.querySelector('.modal-title')
    expect(title).toBeTruthy()

    const closeBtn = header?.querySelector('.modal-close-btn')
    expect(closeBtn).toBeTruthy()
  })

  it('renders body with slot content', () => {
    mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal'
      },
      slots: {
        default: '<div class="custom-content">Content</div>'
      }
    })

    const body = document.querySelector('.modal-body')
    expect(body).toBeTruthy()

    const content = body?.querySelector('.custom-content')
    expect(content?.textContent).toBe('Content')
  })

  it('updates when modelValue changes', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: false,
        title: 'Test Modal'
      }
    })

    expect(document.querySelector('.modal-overlay')).toBeNull()

    await wrapper.setProps({ modelValue: true })

    expect(document.querySelector('.modal-overlay')).toBeTruthy()

    await wrapper.setProps({ modelValue: false })

    expect(document.querySelector('.modal-overlay')).toBeNull()
  })

  it('updates title reactively', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Original Title'
      }
    })

    expect(document.querySelector('.modal-title')?.textContent).toBe('Original Title')

    await wrapper.setProps({ title: 'Updated Title' })

    expect(document.querySelector('.modal-title')?.textContent).toBe('Updated Title')
  })

  it('updates size reactively', async () => {
    const wrapper = mount(BaseModal, {
      props: {
        modelValue: true,
        title: 'Test Modal',
        size: 'small'
      }
    })

    let container = document.querySelector('.modal-container')
    expect(container?.classList.contains('max-w-md')).toBe(true)

    await wrapper.setProps({ size: 'large' })

    container = document.querySelector('.modal-container')
    expect(container?.classList.contains('max-w-4xl')).toBe(true)
    expect(container?.classList.contains('max-w-md')).toBe(false)
  })
})
