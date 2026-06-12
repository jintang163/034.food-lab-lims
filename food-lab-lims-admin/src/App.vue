<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

onMounted(() => {
  if (appStore.theme) {
    document.documentElement.setAttribute('data-theme', appStore.theme)
  }
})
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100%;
}

[data-theme='dark'] {
  --el-bg-color: #1a1a1a;
  --el-bg-color-page: #141414;
  --el-text-color-primary: #e5eaf3;
  --el-text-color-regular: #cfd3dc;
  --el-text-color-secondary: #a3a6ad;
  --el-border-color: #4c4d4f;
  --el-fill-color: #2b2b2c;
  --el-fill-color-light: #262727;
  --el-fill-color-lighter: #1f2020;
}
</style>
