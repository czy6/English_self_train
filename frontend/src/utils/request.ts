import { useAiStore, useUserStore } from '@/store'
import storage from '@/utils/storage.ts'

const baseURL = 'http://127.0.0.1:8080'

// 是否正在刷新 Token 的标志
let isRefreshing = false
// 重试队列
let refreshSubscribers: ((newToken: string) => void)[] = []

/**
 * 添加到重试队列
 */
function subscribeTokenRefresh(cb: (newToken: string) => void) {
  refreshSubscribers.push(cb)
}

/**
 * 执行重试队列
 */
function onRefreshed(newToken: string) {
  console.log('执行重试队列，队列长度:', refreshSubscribers.length)
  refreshSubscribers.forEach((cb) => cb(newToken))
  refreshSubscribers = []
  // 注意：不在这里重置 isRefreshing，让刷新逻辑自己控制
}

// 添加拦截器
const httpInterceptor = {
  // 拦截前触发
  invoke(options: UniApp.RequestOptions) {
    // 1.非 https 开头的请求，拼接 baseURL
    if (!options.url.startsWith('http')) {
      options.url = baseURL + options.url
    }
    // 2.请求超时默认 60s，修改 3x60s(ai 回复可能有点久)
    options.timeout = 180000
    // 3. 添加小程序端请求头标识
    options.header = {
      ...options.header,
      'source-client': 'miniapp',
    }
    // 4.添加 token 请求头标识
    const token = storage.getSync('token')
    if (token) {
      options.header.Authorization = token
    }
  },
}

uni.addInterceptor('request', httpInterceptor)
uni.addInterceptor('uploadFile', httpInterceptor)

interface Data<T> {
  success: boolean
  errorMsg: string | null
  data: T
  total: number
}

// 刷新 Token 的函数
const refreshAuthToken = async (): Promise<string> => {
  const refreshToken = storage.getSync('refreshToken')

  if (!refreshToken) {
    throw new Error('无 Refresh Token，请重新登录')
  }

  console.log('Access Token 已过期，使用 Refresh Token 刷新...')

  try {
    const res = await uni.request({
      url: '/user/refresh',
      method: 'POST',
      header: {
        Authorization: refreshToken,
      },
    })

    if (res.statusCode === 200 && (res.data as any).success) {
      const { accessToken, accessTokenTTL } = (res.data as any).data

      // 保存新 Token
      storage.setSync('token', accessToken)
      console.log('Token 刷新成功')

      return accessToken
    } else {
      throw new Error((res.data as any).errorMsg || 'Token 刷新失败')
    }
  } catch (error) {
    console.error('Token 刷新失败:', error)
    throw error
  }
}

// 请求函数
export const request = <T>(options: UniApp.RequestOptions) => {
  return new Promise<any>((resolve, reject) => {
    uni.request({
      ...options,
      // 1.请求成功
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          // <--- 收到后端续签的新 token --->
          const newToken = res.header?.authorization || res.header?.Authorization
          if (newToken) {
            storage.setSync('token', newToken) // 更新本地
          }

          // 检测是否是 SSE 响应
          const contentType = res.header?.['content-type'] || res.header?.['Content-Type'] || ''
          const isSSE =
            contentType.includes('text/event-stream') ||
            (typeof res.data === 'string' && res.data.includes('event:'))

          // 如果是 SSE 响应，直接返回原始数据
          if (isSSE) {
            resolve({
              success: true,
              errorMsg: null,
              data: res.data,
              total: 0,
            } as Data<T>)
            return
          }

          // 兼容两种返回格式
          const responseData = res.data as any

          // 如果后端返回包含 success 字段，使用标准格式
          if (typeof responseData === 'object' && 'success' in responseData) {
            resolve(responseData as Data<T>)
          } else {
            // 如果后端返回是 AI 聊天格式：{ data: {...}, text: "..." }
            // 判断依据：有 data 和 text 字段
            if ('data' in responseData && 'text' in responseData) {
              // 合并 data 和 text 到同一个对象中
              const mergedData = {
                ...responseData.data,
                text: responseData.text,
              }
              resolve({
                success: true,
                errorMsg: null,
                data: mergedData,
                total: 0,
              } as Data<T>)
            } else if ('chatId' in responseData || 'text' in responseData) {
              // 扁平结构（chatId, text 等）
              resolve({
                success: true,
                errorMsg: null,
                data: responseData,
                total: 0,
              } as Data<T>)
            } else {
              // 其他情况，假设 responseData.data 存在
              resolve({
                success: true,
                errorMsg: null,
                data: responseData.data || responseData,
                total: responseData.total || 0,
              } as Data<T>)
            }
          }
        } else if (res.statusCode === 401) {
          // 401 错误处理 - Token 无感刷新
          const originalRequest = options

          // 标记为重试请求，防止死循环
          if ((originalRequest as any)._retry) {
            // 刷新失败或已经重试过，清理用户信息，跳转到登录页
            const userStore = useUserStore()
            userStore.userInfo = userStore.defaultUserInfo
            storage.remove('token')
            storage.remove('refreshToken')

            const pages = getCurrentPages()
            const cur = pages[pages.length - 1] as any
            const redirect =
              `/${cur.route}` +
              (cur.options
                ? '?' +
                  Object.keys(cur.options)
                    .map((k) => `${k}=${cur.options[k]}`)
                    .join('&')
                : '')

            uni.navigateTo({
              url: `/pages/login/login?redirect=${encodeURIComponent(redirect)}`,
            })
            uni.showToast({
              icon: 'none',
              title: '登录已过期，请重新登录',
            })
            reject(res)
            return
          }

          ;(originalRequest as any)._retry = true

          // 如果已经在刷新 Token，加入等待队列
          if (isRefreshing) {
            const retryRequest = new Promise<Data<T>>((resolveRetry) => {
              subscribeTokenRefresh((newToken: string) => {
                options.header = {
                  ...options.header,
                  Authorization: newToken,
                }
                request<T>(options).then(resolveRetry)
              })
            })

            retryRequest.then(resolve).catch(reject)
            return
          }

          // 开始刷新 Token
          isRefreshing = true
          console.log('开始刷新 Token，当前请求:', options.url)

          refreshAuthToken()
            .then((newToken) => {
              console.log('Token 刷新成功，开始执行重试队列')

              // 执行重试队列（其他并发请求）
              onRefreshed(newToken)

              // 重试当前请求
              options.header = {
                ...options.header,
                Authorization: newToken,
              }

              return request<T>(options)
            })
            .then((res) => {
              // 成功后重置刷新标志
              isRefreshing = false
              resolve(res)
            })
            .catch((refreshError) => {
              // 刷新失败，重置刷新标志
              isRefreshing = false
              console.error('Token 刷新失败，清除登录状态:', refreshError)

              // 刷新失败，清除 Token 并跳转登录页
              const userStore = useUserStore()
              userStore.userInfo = userStore.defaultUserInfo
              storage.remove('token')
              storage.remove('refreshToken')

              const pages = getCurrentPages()
              const cur = pages[pages.length - 1] as any
              const redirect =
                `/${cur.route}` +
                (cur.options
                  ? '?' +
                    Object.keys(cur.options)
                      .map((k) => `${k}=${cur.options[k]}`)
                      .join('&')
                  : '')

              uni.navigateTo({
                url: `/pages/login/login?redirect=${encodeURIComponent(redirect)}`,
              })
              uni.showToast({
                icon: 'none',
                title: '登录已过期，请重新登录',
              })

              // 重要：reject 当前请求
              reject(refreshError)
            })
        } else {
          uni.showToast({
            icon: 'none',
            title: (res.data as Data<T>).errorMsg || '请求错误',
          })
          reject(res)
        }
      },
      // 2.请求失败
      fail(err) {
        uni.showToast({
          icon: 'none',
          title: '网络错误，换个网络试试',
        })
        reject(err)
      },
    })
  })
}
