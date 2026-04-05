import { request } from '@/utils/request'
import type { UserDetail } from '@/types/user'

/**
 * 获取用户信息
 */
export const getUserInfoAPI = () => {
  return request<UserDetail>({
    method: 'GET',
    url: '/user/info',
  })
}

/**
 * 修改用户信息
 */
export const updateUserInfoAPI = (data: UserDetail) => {
  return request<UserDetail>({
    method: 'PUT',
    url: '/user/info',
    data,
  })
}
