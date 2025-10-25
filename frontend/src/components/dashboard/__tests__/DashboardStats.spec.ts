import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardStats from '../DashboardStats.vue'

describe('DashboardStats', () => {
  const defaultProps = {
    totalAccounts: 5000,
    accountsCount: 3,
    totalGoals: 2000,
    totalGoalsTarget: 4000,
    goalsCount: 2,
    freeMoney: 1500
  }

  it('renders all three stat cards', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const cards = wrapper.findAll('.stat-card')
    expect(cards).toHaveLength(3)
  })

  it('displays accounts total and count correctly', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const accountsCard = wrapper.findAll('.stat-card')[0]
    expect(accountsCard.find('.stat-label').text()).toBe('Comptes')
    expect(accountsCard.find('.stat-value').text()).toContain('5')
    expect(accountsCard.find('.stat-value').text()).toContain('000')
    expect(accountsCard.find('.stat-count').text()).toBe('3 comptes')
  })

  it('handles singular account count correctly', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        accountsCount: 1
      }
    })

    const accountsCard = wrapper.findAll('.stat-card')[0]
    expect(accountsCard.find('.stat-count').text()).toBe('1 compte')
  })

  it('displays goals total, count, and progress correctly', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const goalsCard = wrapper.findAll('.stat-card')[1]
    expect(goalsCard.find('.stat-label').text()).toBe('Objectifs')
    expect(goalsCard.find('.stat-value').text()).toContain('2')
    expect(goalsCard.find('.stat-value').text()).toContain('000')
    expect(goalsCard.find('.stat-count').text()).toBe('2 objectifs')
    expect(goalsCard.find('.stat-progress').text()).toBe('50% atteint')
  })

  it('handles singular goal count correctly', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        goalsCount: 1
      }
    })

    const goalsCard = wrapper.findAll('.stat-card')[1]
    expect(goalsCard.find('.stat-count').text()).toBe('1 objectif')
  })

  it('calculates goal progress percentage correctly', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        totalGoals: 3000,
        totalGoalsTarget: 4000
      }
    })

    const goalsCard = wrapper.findAll('.stat-card')[1]
    expect(goalsCard.find('.stat-progress').text()).toBe('75% atteint')
  })

  it('handles zero goal target without error', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        totalGoals: 0,
        totalGoalsTarget: 0
      }
    })

    const goalsCard = wrapper.findAll('.stat-card')[1]
    expect(goalsCard.find('.stat-progress').text()).toBe('0% atteint')
  })

  it('rounds goal progress to nearest integer', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        totalGoals: 1234,
        totalGoalsTarget: 4567
      }
    })

    const goalsCard = wrapper.findAll('.stat-card')[1]
    // 1234 / 4567 = 27.01... should round to 27%
    expect(goalsCard.find('.stat-progress').text()).toBe('27% atteint')
  })

  it('displays positive free money with correct class and info', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        freeMoney: 1500
      }
    })

    const freeMoneyCard = wrapper.findAll('.stat-card')[2]
    expect(freeMoneyCard.find('.stat-label').text()).toBe('Épargne libre')
    expect(freeMoneyCard.find('.stat-value').text()).toContain('1')
    expect(freeMoneyCard.find('.stat-value').text()).toContain('500')
    expect(freeMoneyCard.find('.stat-value').classes()).toContain('positive')
    expect(freeMoneyCard.find('.stat-info').text()).toBe('Disponible')
  })

  it('displays negative free money with correct class and info', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        freeMoney: -500
      }
    })

    const freeMoneyCard = wrapper.findAll('.stat-card')[2]
    expect(freeMoneyCard.find('.stat-value').classes()).toContain('negative')
    expect(freeMoneyCard.find('.stat-info').text()).toBe('Attention')
  })

  it('displays zero free money with neutral class and info', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        freeMoney: 0
      }
    })

    const freeMoneyCard = wrapper.findAll('.stat-card')[2]
    expect(freeMoneyCard.find('.stat-value').classes()).not.toContain('positive')
    expect(freeMoneyCard.find('.stat-value').classes()).not.toContain('negative')
    expect(freeMoneyCard.find('.stat-info').text()).toBe('Équilibré')
  })

  it('has proper ARIA labels for accessibility', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    // Check main region
    const region = wrapper.find('.dashboard-stats')
    expect(region.attributes('role')).toBe('region')
    expect(region.attributes('aria-label')).toBe('Statistiques financières')

    // Check accounts card
    const accountsCard = wrapper.findAll('.stat-card')[0]
    expect(accountsCard.attributes('role')).toBe('article')
    expect(accountsCard.attributes('aria-labelledby')).toBe('stat-accounts-label')
    expect(accountsCard.find('#stat-accounts-label').exists()).toBe(true)
    expect(accountsCard.find('.stat-value').attributes('aria-label')).toBe('Total des comptes')
    expect(accountsCard.find('.stat-count').attributes('aria-label')).toBe('Nombre de comptes')

    // Check goals card
    const goalsCard = wrapper.findAll('.stat-card')[1]
    expect(goalsCard.attributes('role')).toBe('article')
    expect(goalsCard.attributes('aria-labelledby')).toBe('stat-goals-label')
    expect(goalsCard.find('#stat-goals-label').exists()).toBe(true)
    expect(goalsCard.find('.stat-value').attributes('aria-label')).toBe('Total des objectifs')
    expect(goalsCard.find('.stat-count').attributes('aria-label')).toBe("Nombre d'objectifs")

    // Check free money card
    const freeMoneyCard = wrapper.findAll('.stat-card')[2]
    expect(freeMoneyCard.attributes('role')).toBe('article')
    expect(freeMoneyCard.attributes('aria-labelledby')).toBe('stat-free-label')
    expect(freeMoneyCard.find('#stat-free-label').exists()).toBe(true)
  })

  it('has proper ARIA label for goal progress', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const goalsCard = wrapper.findAll('.stat-card')[1]
    const progressSpan = goalsCard.find('.stat-progress')
    expect(progressSpan.attributes('aria-label')).toBe('50 pourcent de progression atteint')
  })

  it('has proper ARIA label for free money with amount and status', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        ...defaultProps,
        freeMoney: 1500
      }
    })

    const freeMoneyCard = wrapper.findAll('.stat-card')[2]
    const valueDiv = freeMoneyCard.find('.stat-value')
    const ariaLabel = valueDiv.attributes('aria-label')!
    expect(ariaLabel).toContain('Épargne libre')
    expect(ariaLabel).toContain('1')
    expect(ariaLabel).toContain('500')
    expect(ariaLabel).toContain('Disponible')
  })

  it('uses semantic HTML with article tags', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const articles = wrapper.findAll('article')
    expect(articles).toHaveLength(3)

    articles.forEach(article => {
      expect(article.classes()).toContain('stat-card')
      expect(article.attributes('role')).toBe('article')
    })
  })

  it('displays stat icons with aria-hidden', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const icons = wrapper.findAll('.stat-icon')
    expect(icons).toHaveLength(3)

    icons.forEach(icon => {
      expect(icon.attributes('aria-hidden')).toBe('true')
    })

    expect(icons[0].text()).toBe('💳')
    expect(icons[1].text()).toBe('🎯')
    expect(icons[2].text()).toBe('💰')
  })

  it('applies glass-card styling to all cards', () => {
    const wrapper = mount(DashboardStats, {
      props: defaultProps
    })

    const cards = wrapper.findAll('.stat-card')
    cards.forEach(card => {
      expect(card.classes()).toContain('glass-card')
    })
  })

  it('handles large numbers correctly', () => {
    const wrapper = mount(DashboardStats, {
      props: {
        totalAccounts: 1234567,
        accountsCount: 10,
        totalGoals: 987654,
        totalGoalsTarget: 2000000,
        goalsCount: 5,
        freeMoney: 500000
      }
    })

    const accountsCard = wrapper.findAll('.stat-card')[0]
    const accountsText = accountsCard.find('.stat-value').text()
    expect(accountsText).toContain('1')
    expect(accountsText).toContain('234')
    expect(accountsText).toContain('567')

    const goalsCard = wrapper.findAll('.stat-card')[1]
    const goalsText = goalsCard.find('.stat-value').text()
    expect(goalsText).toContain('987')
    expect(goalsText).toContain('654')

    const freeMoneyCard = wrapper.findAll('.stat-card')[2]
    const freeMoneyText = freeMoneyCard.find('.stat-value').text()
    expect(freeMoneyText).toContain('500')
    expect(freeMoneyText).toContain('000')
  })
})
