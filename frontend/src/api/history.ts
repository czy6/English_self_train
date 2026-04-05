import type { HistoryType } from '@/types/history'
import { request } from '@/utils/request'

/**
 * 获取历史记录（分页）
 * @param params.page 页码，从 1 开始
 * @param params.pageSize 每页数量
 */
export const getHistoryListAPI = (params?: { page?: number; pageSize?: number }) => {
  return request<{
    list: HistoryType[]
    total: number
    stats?: {
      totalPractice: number
      totalLearningHours: number
      averageAccuracy: number
    }
  }>({
    method: 'GET',
    url: '/question/train/record',
    data: params || {},
  })
}
