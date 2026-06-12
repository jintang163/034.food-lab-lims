import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const theme = ref('light')

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    document.documentElement.setAttribute('data-theme', newTheme)
  }

  return {
    sidebarCollapsed,
    theme,
    toggleSidebar,
    toggleTheme,
    setTheme
  }
}, {
  persist: {
    key: 'app-store',
    paths: ['sidebarCollapsed', 'theme']
  }
})
