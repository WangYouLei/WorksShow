<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getUserInfo,
  updateProfile,
  changePassword,
  logout,
  sendForgotPasswordCode,
  resetPassword,
} from '@/api/auth'
import type { UserInfo, UpdateProfileData, ChangePasswordData, ResetPasswordData } from '@/api/auth'

const router = useRouter()
const activeTab = ref<'profile' | 'password' | 'portfolio' | 'career'>('profile')
const userInfo = ref<UserInfo | null>(null)
const loading = ref(false)
const error = ref('')
const success = ref('')

// 资料编辑表单
const profileForm = reactive<UpdateProfileData>({
  nickname: '',
  gender: 0,
})

// 密码修改表单
const passwordForm = reactive<ChangePasswordData & { confirm: string }>({
  oldPassword: '',
  newPassword: '',
  confirm: '',
})

// 忘记密码表单(通过邮箱验证码重置)
const showForgotPassword = ref(false)
const forgotForm = reactive<ResetPasswordData & { confirm: string }>({
  email: '',
  code: '',
  newPassword: '',
  confirm: '',
})
const codeCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const loadUserInfo = async () => {
  try {
    userInfo.value = await getUserInfo()
    profileForm.nickname = userInfo.value.nickname
    profileForm.gender = userInfo.value.gender
  } catch (e) {
    error.value = (e as Error).message
  }
}

const onUpdateProfile = async () => {
  if (!profileForm.nickname || profileForm.nickname.trim().length < 2) {
    error.value = '昵称至少 2 个字符'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const updated = await updateProfile(profileForm)
    userInfo.value = updated
    // 同步更新 localStorage
    localStorage.setItem('works-show:user', JSON.stringify(updated))
    success.value = '资料更新成功'
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

const onChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    error.value = '请填写完整'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirm) {
    error.value = '两次输入的新密码不一致'
    return
  }
  if (passwordForm.newPassword.length < 6) {
    error.value = '新密码至少 6 个字符'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    success.value = '密码修改成功'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirm = ''
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

// 发送重置密码验证码(60秒倒计时)
const onSendForgotCode = async () => {
  if (codeCountdown.value > 0) return
  if (!forgotForm.email) {
    error.value = '请先填写邮箱'
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

// 通过邮箱验证码重置密码
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
    success.value = '密码重置成功'
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
  // 已登录场景:默认填入当前用户邮箱
  forgotForm.email = userInfo.value?.email ?? ''
  showForgotPassword.value = true
  error.value = ''
  success.value = ''
}

const backToChangePassword = () => {
  showForgotPassword.value = false
  error.value = ''
  success.value = ''
}

const onLogout = () => {
  localStorage.removeItem('works-show:token')
  localStorage.removeItem('works-show:user')
  router.push('/login')
}

const switchTab = (tab: typeof activeTab.value) => {
  activeTab.value = tab
  error.value = ''
  success.value = ''
}

onMounted(loadUserInfo)

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<template>
  <div class="account-page">
    <div class="account-card">
      <!-- 顶部栏 -->
      <div class="account-header">
        <button class="back-btn" @click="router.back()">← 返回</button>
        <h1 class="account-title">账号管理</h1>
        <button class="logout-btn" @click="onLogout">退出登录</button>
      </div>

      <!-- Tab 导航 -->
      <div class="tab-bar">
        <button
          :class="['tab-btn', { active: activeTab === 'profile' }]"
          @click="switchTab('profile')"
        >
          账号信息
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'password' }]"
          @click="switchTab('password')"
        >
          修改密码
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'portfolio' }]"
          @click="switchTab('portfolio')"
        >
          简历管理
        </button>
        <button
          :class="['tab-btn', { active: activeTab === 'career' }]"
          @click="switchTab('career')"
        >
          求职意向
        </button>
      </div>

      <!-- 账号信息 -->
      <div v-if="activeTab === 'profile'" class="tab-content">
        <form class="form" @submit.prevent="onUpdateProfile">
          <label class="field">
            <span class="field-label">昵称</span>
            <input v-model="profileForm.nickname" type="text" class="field-input" placeholder="2-20 字符" />
          </label>
          <label class="field">
            <span class="field-label">性别</span>
            <select v-model.number="profileForm.gender" class="field-input">
              <option :value="0">未知</option>
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </label>
          <!-- 只读信息 -->
          <div class="field">
            <span class="field-label">邮箱(登录账号,不可修改)</span>
            <input :value="userInfo?.email ?? ''" type="text" class="field-input readonly" disabled />
          </div>
          <div class="field">
            <span class="field-label">手机号(登录账号,不可修改)</span>
            <input :value="userInfo?.phone ?? ''" type="text" class="field-input readonly" disabled />
          </div>

          <p v-if="error" class="msg-error">{{ error }}</p>
          <p v-if="success" class="msg-success">{{ success }}</p>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '保存中…' : '保存' }}
          </button>
        </form>
      </div>

      <!-- 修改密码 -->
      <div v-if="activeTab === 'password'" class="tab-content">
        <!-- 原密码修改 -->
        <form v-if="!showForgotPassword" class="form" @submit.prevent="onChangePassword">
          <label class="field">
            <span class="field-label">原密码</span>
            <input v-model="passwordForm.oldPassword" type="password" class="field-input" placeholder="请输入原密码" />
          </label>
          <label class="field">
            <span class="field-label">新密码(6-30 字符)</span>
            <input v-model="passwordForm.newPassword" type="password" class="field-input" placeholder="请输入新密码" />
          </label>
          <label class="field">
            <span class="field-label">确认新密码</span>
            <input v-model="passwordForm.confirm" type="password" class="field-input" placeholder="再次输入新密码" />
          </label>

          <p v-if="error" class="msg-error">{{ error }}</p>
          <p v-if="success" class="msg-success">{{ success }}</p>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '修改中…' : '修改密码' }}
          </button>
          <button type="button" class="link-btn" @click="switchToForgotPassword">忘记密码?通过邮箱验证码重置</button>
        </form>

        <!-- 忘记密码(邮箱验证码重置) -->
        <form v-else class="form" @submit.prevent="onResetPassword">
          <label class="field">
            <span class="field-label">邮箱(验证码将发送至此邮箱)</span>
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

          <p v-if="error" class="msg-error">{{ error }}</p>
          <p v-if="success" class="msg-success">{{ success }}</p>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '重置中…' : '重置密码' }}
          </button>
          <button type="button" class="link-btn" @click="backToChangePassword">← 返回原密码修改</button>
        </form>
      </div>

      <!-- 简历管理 -->
      <div v-if="activeTab === 'portfolio'" class="tab-content">
        <div class="redirect-card">
          <p class="redirect-desc">在编辑器中管理你的简历信息（个人信息、作品、技能、经历）。</p>
          <button class="redirect-btn" @click="router.push('/')">前往模板选择 →</button>
        </div>
      </div>

      <!-- 求职意向 -->
      <div v-if="activeTab === 'career'" class="tab-content">
        <div class="redirect-card">
          <p class="redirect-desc">在编辑器中管理你的求职意向（期望职位、薪资、城市等）。</p>
          <button class="redirect-btn" @click="router.push('/')">前往模板选择 →</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.account-page {
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 16px;
  background: #0a0a0f;
}
.account-card {
  width: 100%;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(212, 165, 116, 0.15);
  border-radius: 12px;
  overflow: hidden;
}
.account-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(245, 241, 232, 0.08);
}
.back-btn {
  font-size: 13px;
  color: rgba(245, 241, 232, 0.6);
  background: none;
  border: none;
  cursor: pointer;
}
.back-btn:hover {
  color: #d4a574;
}
.account-title {
  margin: 0;
  font-size: 16px;
  color: #f5f1e8;
}
.logout-btn {
  font-size: 13px;
  color: #c8553d;
  background: none;
  border: none;
  cursor: pointer;
}
.logout-btn:hover {
  text-decoration: underline;
}
.tab-bar {
  display: flex;
  border-bottom: 1px solid rgba(245, 241, 232, 0.08);
}
.tab-btn {
  flex: 1;
  padding: 12px 8px;
  font-size: 13px;
  color: rgba(245, 241, 232, 0.5);
  background: none;
  border: none;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
}
.tab-btn.active {
  color: #d4a574;
  border-bottom-color: #d4a574;
}
.tab-content {
  padding: 24px;
}
.form {
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
  transition: border-color 0.2s;
}
.field-input:focus {
  border-color: #d4a574;
}
.field-input.readonly {
  opacity: 0.5;
  cursor: not-allowed;
}
select.field-input {
  cursor: pointer;
}
select.field-input option {
  background: #1a1a2e;
  color: #f5f1e8;
}
.msg-error {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #c8553d;
  background: rgba(200, 85, 61, 0.1);
  border-radius: 4px;
}
.msg-success {
  margin: 0;
  padding: 8px 12px;
  font-size: 13px;
  color: #6abf69;
  background: rgba(106, 191, 105, 0.1);
  border-radius: 4px;
}
.submit-btn {
  margin-top: 6px;
  padding: 11px;
  font-size: 14px;
  color: #0a0a0f;
  background: #d4a574;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.submit-btn:not(:disabled):hover {
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
.redirect-card {
  text-align: center;
  padding: 32px 16px;
}
.redirect-desc {
  margin: 0 0 20px;
  font-size: 14px;
  color: rgba(245, 241, 232, 0.6);
  line-height: 1.6;
}
.redirect-btn {
  padding: 10px 24px;
  font-size: 14px;
  color: #d4a574;
  background: rgba(212, 165, 116, 0.1);
  border: 1px solid rgba(212, 165, 116, 0.3);
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}
.redirect-btn:hover {
  background: rgba(212, 165, 116, 0.2);
}
</style>
