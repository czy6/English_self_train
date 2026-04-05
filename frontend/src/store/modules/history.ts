import { getHistoryListAPI } from '@/api/history'
import type { HistoryType } from '@/types/history'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useHistoryStore = defineStore('history', () => {
  // 1.分页相关
  const page = ref(1)
  const pageSize = ref(20)
  const total = ref(0)
  const loading = ref(false)
  const hasMore = ref(true)

  // 1.1 统计数据（来自后端）
  const stats = ref({
    totalPractice: 0,
    totalLearningHours: 0,
    averageAccuracy: 0,
  })

  // 2.历史记录
  const historyList = ref<HistoryType[]>([])

  // 2.1 重置状态
  const resetState = () => {
    page.value = 1
    historyList.value = []
    hasMore.value = true
  }

  // 2.2 获取历史记录列表
  const getHistoryList = async () => {
    if (loading.value || !hasMore.value) return

    loading.value = true
    try {
      const res = await getHistoryListAPI({
        page: page.value,
        pageSize: pageSize.value,
      })

      // 后端返回结构：{ success: true, data: { total, list, stats } }
      const responseData = res.data || res

      // 更新总数
      total.value = responseData.total || 0

      // 更新统计数据（只在第一页时更新）
      if (page.value === 1 && responseData.stats) {
        stats.value = {
          totalPractice: responseData.stats.totalPractice || 0,
          totalLearningHours: responseData.stats.totalLearningHours || 0,
          averageAccuracy: responseData.stats.averageAccuracy || 0,
        }
      }

      // 确保 list 是数组
      const newData = Array.isArray(responseData.list) ? responseData.list : []

      if (newData.length === 0) {
        hasMore.value = false
      } else {
        // 先递增页码
        const nextPage = page.value + 1
        page.value = nextPage

        // 追加数据
        historyList.value = [...historyList.value, ...newData]

        // 判断是否还有更多数据
        if (total.value > 0 && historyList.value.length >= total.value) {
          hasMore.value = false
        }
      }
    } catch (error) {
      console.error('获取历史记录失败:', error)
      hasMore.value = false
    } finally {
      loading.value = false
    }
  }

  // 2.3 重置并重新加载
  const reload = () => {
    page.value = 1
    historyList.value = []
    hasMore.value = true
    getHistoryList()
  }

  // 1.1 总练习数（优先使用后端统计数据）
  const totalCount = computed(() => stats.value.totalPractice || historyList.value.length || 0)

  // 1.2 总学习小时（优先使用后端统计数据）
  const totalHours = computed(() => {
    if (stats.value.totalLearningHours) {
      return stats.value.totalLearningHours.toFixed(1)
    }
    const seconds = historyList.value.reduce((acc, item) => (acc += item.timeSpent), 0)
    return (seconds / 3600).toFixed(1) || '0.0'
  })

  // 1.3 平均正确率（优先使用后端统计数据）
  const avgAccuracy = computed(() => {
    if (stats.value.averageAccuracy) {
      return stats.value.averageAccuracy.toFixed(1)
    }
    if (historyList.value.length === 0) return '0.0'
    const userScoreSum = historyList.value.reduce((acc, item) => acc + item.userScore, 0)
    const questionScoreSum = historyList.value.reduce((acc, item) => acc + item.score, 0)
    return Math.round((userScoreSum / questionScoreSum) * 1000) / 10 || '0.0'
  })

  // 2.处理历史记录列表
  // 2.1 从 ISO 字符串提取 yyyy-MM-dd 与 HH:mm
  function splitDateTime(iso: string) {
    const [date, fullTime] = iso.split('T')
    const time = fullTime.slice(0, 5) // "20:47"
    return { date, time }
  }

  // 2.2 分组 + 排序
  function groupByDay(list: HistoryType[]) {
    const map = new Map<string, Array<Omit<HistoryType, 'createdTime'> & { time: string }>>()

    list.forEach((item) => {
      // 1. 拆出日期和时间
      const { date, time } = splitDateTime(item.createdTime)
      // 2. 计算正确率
      item.accuracy = Math.round((item.userScore / item.score) * 1000) / 10
      // 3. 构造当天元素
      const { createdTime, ...rest } = item
      // 4. 归集
      if (!map.has(date)) map.set(date, [])
      map.get(date)!.push({ ...rest, time })
    })

    // 日期升序；同一天内按 HH:mm 升序
    return Array.from(map.entries())
      .sort(([a], [b]) => new Date(b).getTime() - new Date(a).getTime())
      .map(([date, arr]) => ({
        date,
        list: arr.sort((x, y) => x.time.localeCompare(y.time)),
      }))
  }
  const groupedHistory = computed(() => groupByDay(historyList.value) || [])

  // 3. 处理跳转
  enum QuestionType {
    '听力' = 'listening',
    '阅读' = 'reading',
    '翻译' = 'translation',
    '写作' = 'writing',
  }
  const handleJump = (questionType: string, questionId: number, recordId: number) => {
    uni.navigateTo({
      url:
        `/pages/${QuestionType[questionType as keyof typeof QuestionType]}/${
          QuestionType[questionType as keyof typeof QuestionType]
        }` + `?questionId=${questionId}&recordId=${recordId}&isHistoryJump=true`,
    })
  }

  // 4.全局处理
  // 4.1 图标分类配置
  const categories = [
    { name: '听力', icon: 'headphones', color: '#3a7bd5' },
    { name: '阅读', icon: 'map', color: '#00b42a' },
    { name: '写作', icon: 'compose', color: '#ff7d00' },
    { name: '翻译', icon: 'chatboxes', color: '#f53f3f' },
  ]
  // 4.2 获取分类图标
  const getCategoryIcon = (category: string) => {
    return categories.find((c) => c.name === category)?.icon || 'help'
  }
  // 4.3 获取分类颜色
  const getCategoryColor = (category: string) => {
    return categories.find((c) => c.name === category)?.color || '#999'
  }

  return {
    /** 历史记录列表 */
    historyList,
    /** 获取历史记录列表 */
    getHistoryList,
    /** 重置并重新加载 */
    reload,
    /** 重置状态 */
    resetState,
    /** 总练习数 */
    totalCount,
    /** 总学习小时 */
    totalHours,
    /** 平均正确率 */
    avgAccuracy,
    /** 按日期分组的历史记录 */
    groupedHistory,
    /** 处理跳转 */
    handleJump,
    /** 获取分类图标 */
    getCategoryIcon,
    /** 获取分类颜色 */
    getCategoryColor,
    /** 加载状态 */
    loading,
    /** 是否有更多数据 */
    hasMore,
    /** 统计数据 */
    stats,
    /** 当前页码 */
    page,
  }
})
