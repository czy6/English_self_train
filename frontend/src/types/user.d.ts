// 公共类型
type sameType = {
  /** 头像 */
  avatar: string
  /** 昵称 */
  nickName: string
}

/** 小程序登录 登录用户信息 */
export type LoginResult = sameType & {
  /** 手机号 */
  mobile: string
  /** 登录凭证 */
  token: string
}

/** 用户信息详情 */
export type UserDetail = sameType & {
  /** 等级 */
  level?: number
  /** 手机号 */
  phone?: string
  // 性别
  gender?: string
  // 生日
  birthday?: string
}

