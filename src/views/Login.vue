<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { login, getUserInfo, sendForgotPasswordCode, resetPassword } from '@/api/auth'
import type { LoginData, ResetPasswordData } from '@/api/auth'

const router = useRouter()
const form = reactive<LoginData>({ account: '', password: '' })
const loading = ref(false)
const error = ref('')
const success = ref('')

// 忘记密码表单
const showForgotPassword = ref(false)
const forgotForm = reactive<ResetPasswordData & { confirm: string }>({
  email: '',
  code: '',
  newPassword: '',
  confirm: '',
})
const codeCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const onSubmit = async () => {
  if (!form.account || !form.password) {
    error.value = '请输入账号和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const result = await login(form)
    localStorage.setItem('works-show:token', result.token)
    const user = await getUserInfo()
    localStorage.setItem('works-show:user', JSON.stringify(user))
    router.push('/')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

const onSendForgotCode = async () => {
  if (codeCountdown.value > 0) return
  if (!forgotForm.email) {
    error.value = '请输入邮箱'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await sendForgotPasswordCode(forgotForm.email)
    success.value = '验证码已发送至邮箱,请查收'
    codeCountdown.value = 60
    countdownTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

const onResetPassword = async () => {
  if (!forgotForm.email || !forgotForm.code || !forgotForm.newPassword) {
    error.value = '请填写完整'
    return
  }
  if (forgotForm.newPassword !== forgotForm.confirm) {
    error.value = '两次输入的新密码不一致'
    return
  }
  if (forgotForm.newPassword.length < 6) {
    error.value = '新密码至少 6 个字符'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      newPassword: forgotForm.newPassword,
    })
    success.value = '密码重置成功,请使用新密码登录'
    forgotForm.code = ''
    forgotForm.newPassword = ''
    forgotForm.confirm = ''
    showForgotPassword.value = false
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

const switchToForgotPassword = () => {
  showForgotPassword.value = true
  error.value = ''
  success.value = ''
}

const backToLogin = () => {
  showForgotPassword.value = false
  error.value = ''
  success.value = ''
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 登录 -->
      <template v-if="!showForgotPassword">
        <h1 class="auth-title">登录</h1>
        <p class="auth-subtitle">登录后即可管理你的简历与求职意向</p>

        <form class="auth-form" @submit.prevent="onSubmit">
          <label class="field">
            <span class="field-label">手机号或邮箱</span>
            <input
              v-model="form.account"
              type="text"
              class="field-input"
              placeholder="请输入手机号或邮箱"
              autocomplete="username"
            />
          </label>
          <label class="field">
            <span class="field-label">密码</span>
            <input
              v-model="form.password"
              type="password"
              class="field-input"
              placeholder="请输入密码"
              autocomplete="current-password"
            />
          </label>

          <p v-if="error" class="auth-error">{{ error }}</p>

          <button type="submit" class="auth-btn" :disabled="loading">
            {{ loading ? '登录中…' : '登录' }}
          </button>
          <button type="button" class="link-btn" @click="switchToForgotPassword">忘记密码?去修改</button>
        </form>

        <p class="auth-footer">
          还没有账号？
          <router-link to="/register" class="auth-link">去注册</router-link>
        </p>
      </template>

      <!-- 忘记密码 -->
      <template v-else>
        <h1 class="auth-title">重置密码</h1>
        <p class="auth-subtitle">通过邮箱验证码重置你的密码</p>

        <form class="auth-form" @submit.prevent="onResetPassword">
          <label class="field">
            <span class="field-label">邮箱</span>
            <input v-model="forgotForm.email" type="email" class="field-input" placeholder="请输入注册邮箱" />
          </label>
          <label class="field">
            <span class="field-label">验证码</span>
            <div class="code-row">
              <input v-model="forgotForm.code" type="text" class="field-input" placeholder="6 位验证码" />
              <button type="button" class="code-btn" :disabled="codeCountdown > 0 || loading" @click="onSendForgotCode">
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
              </button>
            </div>
          </label>
          <label class="field">
            <span class="field-label">新密码(6-30 字符)</span>
            <input v-model="forgotForm.newPassword" type="password" class="field-input" placeholder="请输入新密码" />
          </label>
          <label class="field">
            <span class="field-label">确认新密码</span>
            <input v-model="forgotForm.confirm" type="password" class="field-input" placeholder="再次输入新密码" />
          </label>

          <p v-if="error" class="auth-error">{{ error }}</p>
          <p v-if="success" class="auth-success">{{ success }}</p>

          <button type="submit" class="auth-btn" :disabled="loading">
            {{ loading ? '重置中…' : '重置密码' }}
          </button>
          <button type="button" class="link-btn" @click="backToLogin">← 返回登录</button>
        </form>
      </template>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0a0a0f;
}
.auth-card {
  width: 380px;
  padding: 40px 32px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(212, 165, 116, 0.15);
  border-radius: 12px;
}
.auth-title {
  margin: 0 0 8px;
  font-size: 24px;
  color: #f5f1e8;
}
.auth-subtitle {
  margin: 0 0 28px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.5);
}
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field-label {
  font-size: 13px;
  color: rgba(245, 241, 232, 0.7);
}
.field-input {
  padding: 10px 12px;
  font-size: 14px;
  color: #f5f1e8;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(245, 241, 232, 0.15);
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s ease;
}
.field-input:focus {
  border-color: #d4a574;
}
.auth-error {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border-radius: 4px;
}
.auth-success {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #4ade80;
  background: rgba(74, 222, 128, 0.1);
  border-radius: 4px;
}
.auth-btn {
  margin-top: 6px;
  padding: 11px;
  font-size: 14px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;
}
.auth-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.auth-btn:not(:disabled):hover {
  background: #e3b885;
}
.link-btn {
  margin-top: 4px;
  padding: 4px;
  font-size: 13px;
  color: rgba(212, 165, 116, 0.8);
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}
.link-btn:hover {
  color: #d4a574;
}
.code-row {
  display: flex;
  gap: 8px;
}
.code-row .field-input {
  flex: 1;
}
.code-btn {
  padding: 10px 14px;
  font-size: 13px;
  color: #d4a574;
  background: rgba(212, 165, 116, 0.1);
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s;
}
.code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.code-btn:not(:disabled):hover {
  background: rgba(212, 165, 116, 0.2);
}
.auth-footer {
  margin: 24px 0 0;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.5);
  text-align: center;
}
.auth-link {
  color: #d4a574;
  text-decoration: none;
}
.auth-link:hover {
  text-decoration: underline;
}
</style>
