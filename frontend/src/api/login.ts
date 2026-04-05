import type { CodeResult, LoginForm, TokenResult } from '@/types/login'
import { request } from '@/utils/request'

/**
 * 获取验证码
 */
export const getCodeAPI = (params: { [key: string]: string }) => {
  return request<CodeResult>({
    method: 'GET',
    url: '/user/code',
    data: params,
  })
}

/**
 * 登录请求
 */
export const postLoginAPI = (data: LoginForm) => {
  return request<TokenResult>({
    method: 'POST',
    url: '/user/login',
    data,
  })
}
