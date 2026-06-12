<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-message">抱歉，您访问的页面不存在</div>
      <el-button type="primary" @click="goHome">
        <el-icon><House /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page, #f0f2f5);
}

.error-content {
  text-align: center;

  .error-code {
    font-size: 120px;
    font-weight: bold;
    color: var(--el-color-primary, #409eff);
    line-height: 1;
    margin-bottom: 20px;
  }

  .error-message {
    font-size: 20px;
    color: var(--el-text-color-regular, #606266);
    margin-bottom: 30px;
  }
}
</style>
