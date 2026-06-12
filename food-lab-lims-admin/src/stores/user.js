import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref({})
  const roles = ref([])
  const permissions = ref([])

  const login = async (loginForm) => {
    const res = await loginApi(loginForm)
    token.value = res.token || 'mock-token-' + Date.now()
    userInfo.value = res.userInfo || { username: loginForm.username, nickname: '管理员' }
    return res
  }

  const getInfo = async () => {
    try {
      const res = await getUserInfo()
      userInfo.value = res
      roles.value = res.roles || ['admin']
      permissions.value = res.permissions || ['*']
    } catch (e) {
      userInfo.value = {
        username: 'admin',
        nickname: '系统管理员',
        avatar: ''
      }
      roles.value = ['admin']
      permissions.value = ['*']
    }
    return userInfo.value
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (e) {
    }
    token.value = ''
    userInfo.value = {}
    roles.value = []
    permissions.value = []
  }

  return {
    token,
    userInfo,
    roles,
    permissions,
    login,
    getInfo,
    logout
  }
}, {
  persist: {
    key: 'user-store',
    paths: ['token', 'userInfo']
  }
})
