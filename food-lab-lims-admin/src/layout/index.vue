<template>
  <div class="layout-container">
    <div class="sidebar">
      <Sidebar />
    </div>
    <div class="main-container" :class="{ collapsed: collapsed }">
      <div class="header">
        <Navbar />
      </div>
      <div class="app-main">
        <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'

const appStore = useAppStore()
const collapsed = computed(() => appStore.sidebarCollapsed)
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100%;
  height: 100%;
  display: flex;
}

.sidebar {
  flex-shrink: 0;
  transition: width $transition-duration;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--el-bg-color-page, #f0f2f5);
  transition: margin-left $transition-duration;
}

.header {
  flex-shrink: 0;
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
