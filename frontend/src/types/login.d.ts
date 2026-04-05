/** 验证码 */
export type CodeResult = {
  /** 验证码 */
  code: string
}

/** 请求表单数据 */
export type LoginForm = {
  /** 手机号 */
  phone: string
  /** 验证码 */
  code: string
}

/** token 凭证 */
export type TokenResult = {
  /** token 凭证 */
  token: string
  /** 刷新 Token（用于无感刷新） */
  refreshToken?: string
  /** Access Token 有效期（秒） */
  accessTokenTTL?: number
}