<template>
  <div class="navbar">
    <div class="navbar-left">
      <div class="hamburger" @click="toggleSidebar">
        <el-icon :size="20">
          <component :is="sidebarCollapsed ? 'Expand' : 'Fold'" />
        </el-icon>
      </div>
      <Breadcrumb />
    </div>
    <div class="navbar-right">
      <el-tooltip content="主题切换" placement="bottom">
        <el-icon class="icon-item" :size="20" @click="toggleTheme">
          <component :is="theme === 'light' ? 'Moon' : 'Sunny'" />
        </el-icon>
      </el-tooltip>
      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="icon-item" :size="20" @click="toggleFullscreen">
          <FullScreen />
        </el-icon>
      </el-tooltip>
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ userInfo.nickname || userInfo.username }}</span>
          <el-icon><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="setting">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import Breadcrumb from './Breadcrumb.vue'
import { UserFilled, CaretBottom, User, Setting, SwitchButton, FullScreen, Fold, Expand, Moon, Sunny } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const userInfo = computed(() => userStore.userInfo)
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const theme = computed(() => appStore.theme)

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const toggleTheme = () => {
  appStore.toggleTheme()
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      ElMessage.success('退出成功')
      router.push('/login')
    } catch (e) {
    }
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  } else if (command === 'setting') {
    ElMessage.info('系统设置功能开发中')
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: var(--el-bg-color, #fff);
  border-bottom: 1px solid var(--el-border-color-lighter, #e4e7ed);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.navbar-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.hamburger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  cursor: pointer;
  color: var(--el-text-color-regular, #606266);
  
  &:hover {
    color: var(--el-color-primary, #409eff);
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-item {
  cursor: pointer;
  color: var(--el-text-color-regular, #606266);
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background-color: var(--el-fill-color-light, #f5f7fa);
    color: var(--el-color-primary, #409eff);
  }
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: $header-height;

  &:hover {
    background-color: var(--el-fill-color-light, #f5f7fa);
  }

  .username {
    margin: 0 8px;
    font-size: 14px;
    color: var(--el-text-color-regular, #606266);
  }
}
</style>
