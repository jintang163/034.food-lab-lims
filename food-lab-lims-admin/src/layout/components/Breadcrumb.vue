<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="index">
        {{ item.meta.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { House } from '@element-plus/icons-vue'

const route = useRoute()

const breadcrumbList = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched
})
</script>

<style lang="scss" scoped>
.breadcrumb-container {
  margin-left: 20px;

  .el-breadcrumb__item {
    display: flex;
    align-items: center;

    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
