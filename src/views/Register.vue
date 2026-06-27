<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register, sendCode } from '@/api/auth'
import type { RegisterData } from '@/api/auth'

const router = useRouter()
const form = reactive<RegisterData>({
  nickname: '',
  password: '',
  email: '',
  phone: '',
  code: '',
})
const loading = ref(false)
const error = ref('')

// 验证码发送倒计时
const codeLoading = ref(false)
const countdown = ref(0)

const onSendCode = async () => {
  if (!form.email) {
    error.value = '请先填写邮箱'
    return
  }
  codeLoading.value = true
  error.value = ''
  try {
    await sendCode({ email: form.email })
    // 开始60秒倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    codeLoading.value = false
  }
}

const onSubmit = async () => {
  if (!form.nickname || !form.password || !form.email || !form.phone || !form.code) {
    error.value = '请填写完整信息'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await register(form)
    router.push('/login')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">注册</h1>
      <p class="auth-subtitle">创建账号,开始管理你的作品集</p>

      <form class="auth-form" @submit.prevent="onSubmit">
        <label class="field">
          <span class="field-label">昵称(2-20 字符)</span>
          <input
            v-model="form.nickname"
            type="text"
            class="field-input"
            placeholder="请输入昵称"
            autocomplete="nickname"
          />
        </label>
        <label class="field">
          <span class="field-label">密码(6-30 字符)</span>
          <input
            v-model="form.password"
            type="password"
            class="field-input"
            placeholder="请输入密码"
            autocomplete="new-password"
          />
        </label>
        <label class="field">
          <span class="field-label">手机号</span>
          <input
            v-model="form.phone"
            type="tel"
            class="field-input"
            placeholder="请输入手机号"
            autocomplete="tel"
          />
        </label>
        <label class="field">
          <span class="field-label">邮箱</span>
          <input
            v-model="form.email"
            type="email"
            class="field-input"
            placeholder="请输入邮箱"
            autocomplete="email"
          />
        </label>
        <label class="field">
          <span class="field-label">邮箱验证码</span>
          <div class="code-row">
            <input
              v-model="form.code"
              type="text"
              class="field-input code-input"
              placeholder="请输入验证码"
              autocomplete="one-time-code"
            />
            <button
              type="button"
              class="code-btn"
              :disabled="countdown > 0 || codeLoading"
              @click="onSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </button>
          </div>
        </label>

        <p v-if="error" class="auth-error">{{ error }}</p>

        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '注册中…' : '注册' }}
        </button>
      </form>

      <p class="auth-footer">
        已有账号?
        <router-link to="/login" class="auth-link">去登录</router-link>
      </p>
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
.code-row {
  display: flex;
  gap: 8px;
}
.code-input {
  flex: 1;
}
.code-btn {
  flex-shrink: 0;
  padding: 10px 14px;
  font-size: 13px;
  color: #d4a574;
  background: rgba(212, 165, 116, 0.1);
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.2s ease;
}
.code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.code-btn:not(:disabled):hover {
  background: rgba(212, 165, 116, 0.2);
}
.auth-error {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
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
