<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-left">
        <div class="logo-box">
          <div class="logo">
            <el-icon :size="48"><DataLine /></el-icon>
          </div>
          <h1>食品检测 LIMS 管理系统</h1>
          <p>Food Laboratory Information Management System</p>
        </div>
      </div>
      <div class="login-right">
        <h2 class="login-title">用户登录</h2>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <el-link type="primary">忘记密码？</el-link>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-tips">
          <p>演示账号：admin / admin123</p>
        </div>
      </div>
    </div>
    <div class="footer">
      <p>© 2024 食品检测 LIMS 管理系统 - 版权所有</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { User, Lock, DataLine } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await userStore.login(loginForm)
    
    ElMessage.success('登录成功')
    
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    if (error !== false) {
      console.error('登录失败:', error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-box {
  width: 900px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  display: flex;
  overflow: hidden;
}

.login-left {
  width: 50%;
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  display: flex;
  align-items: center;
  justify-content: center;

  .logo-box {
    text-align: center;
    color: #fff;

    .logo {
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
    }

    h1 {
      font-size: 28px;
      font-weight: 600;
      margin-bottom: 10px;
    }

    p {
      font-size: 14px;
      opacity: 0.8;
    }
  }
}

.login-right {
  width: 50%;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.login-form {
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-tips {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}

.footer {
  position: absolute;
  bottom: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
}

@media (max-width: 960px) {
  .login-box {
    width: 90%;
    max-width: 400px;
  }

  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}
</style>
