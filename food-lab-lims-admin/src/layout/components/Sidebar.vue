<template>
  <div class="sidebar-container">
    <div class="logo">
      <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='45' fill='%23409eff'/%3E%3Ctext x='50' y='60' text-anchor='middle' fill='white' font-size='30' font-weight='bold'%3EL%3C/text%3E%3C/svg%3E" alt="logo" />
      <span v-if="!sidebarCollapsed" class="title">食品检测 LIMS</span>
    </div>
    <el-scrollbar class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :unique-opened="true"
        router
        background-color="transparent"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-sub-menu v-if="route.children && route.children.length > 0" :index="resolvePath(route.path)">
            <template #title>
              <el-icon v-if="route.meta && route.meta.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <span>{{ route.meta.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="resolvePath(route.path + '/' + child.path)"
            >
              <el-icon v-if="child.meta && child.meta.icon">
                <component :is="child.meta.icon" />
              </el-icon>
              <template #title>{{ child.meta.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="resolvePath(route.path)">
            <el-icon v-if="route.meta && route.meta.icon">
              <component :is="route.meta.icon" />
            </el-icon>
            <template #title>{{ route.meta.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)

const activeMenu = computed(() => {
  return route.path
})

const menuRoutes = computed(() => {
  return router.options.routes.filter(
    r => r.path !== '/login' && r.path !== '/404' && r.path !== '/:pathMatch(.*)*' && r.children
  ).map(r => ({
    ...r,
    path: r.path === '/' ? '/dashboard' : r.path
  }))
})

const resolvePath = (path) => {
  if (path.startsWith('http')) return path
  if (path.startsWith('/')) return path
  return '/' + path
}
</script>

<style lang="scss" scoped>
.sidebar-container {
  width: $sidebar-width;
  height: 100%;
  background-color: #304156;
  transition: width $transition-duration;
  overflow: hidden;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

.logo {
  height: $header-height;
  display: flex;
  align-items: center;
  padding: 0 16px;
  background-color: #2b2f3a;
  overflow: hidden;

  img {
    width: 32px;
    height: 32px;
  }

  .title {
    margin-left: 12px;
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    white-space: nowrap;
  }
}

.scrollbar-wrapper {
  height: calc(100% - #{$header-height});
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #263445 !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #409eff !important;
  color: #fff !important;
}
</style>
