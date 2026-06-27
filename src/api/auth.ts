import request from './request'

/** 登录参数(手机号或邮箱 + 密码) */
export interface LoginData {
  account: string
  password: string
}

/** 发送验证码参数 */
export interface SendCodeData {
  email: string
}

/** 注册参数(昵称 + 密码 + 邮箱 + 手机号 + 验证码) */
export interface RegisterData {
  nickname: string
  password: string
  email: string
  phone: string
  code: string
}

/** 登录响应 */
export interface LoginResult {
  token: string
  userId: number
  nickname: string
}

/** 用户信息 */
export interface UserInfo {
  id: number
  email: string
  phone: string
  nickname: string
  gender: number
  status: number
  createTime: string
  updateTime: string
}

/** 更新用户资料参数 */
export interface UpdateProfileData {
  nickname: string
  gender?: number
}

/** 修改密码参数 */
export interface ChangePasswordData {
  oldPassword: string
  newPassword: string
}

/** 发送邮箱验证码 */
export function sendCode(data: SendCodeData) {
  return request.post<void>('/user/sendCode', data) as unknown as Promise<void>
}

/** 用户登录 */
export function login(data: LoginData) {
  return request.post<LoginResult>('/user/login', data) as unknown as Promise<LoginResult>
}

/** 用户注册 */
export function register(data: RegisterData) {
  return request.post<void>('/user/register', data) as unknown as Promise<void>
}

/** 获取当前登录用户信息 */
export function getUserInfo() {
  return request.get<UserInfo>('/user/info') as unknown as Promise<UserInfo>
}

/** 更新当前用户资料(昵称、性别) */
export function updateProfile(data: UpdateProfileData) {
  return request.put<UserInfo>('/user/info', data) as unknown as Promise<UserInfo>
}

/** 修改密码 */
export function changePassword(data: ChangePasswordData) {
  return request.put<void>('/user/password', data) as unknown as Promise<void>
}

/** 忘记密码:重置参数 */
export interface ResetPasswordData {
  email: string
  code: string
  newPassword: string
}

/** 发送重置密码验证码到指定邮箱(公开接口) */
export function sendForgotPasswordCode(email: string) {
  return request.post<void>('/user/forgot-password/send-code', { email }) as unknown as Promise<void>
}

/** 通过邮箱验证码重置密码(公开接口) */
export function resetPassword(data: ResetPasswordData) {
  return request.post<void>('/user/forgot-password/reset', data) as unknown as Promise<void>
}

/** 退出登录(服务端无状态,仅清除本地 token) */
export function logout() {
  return request.post<void>('/user/logout') as unknown as Promise<void>
}
