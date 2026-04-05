import {
  getFavoriteListAPI,
  getFavoriteQuestionTypeAPI,
  getQuestionContentByIdAPI,
} from '@/api/question'
import type { QuestionContentType, QuestionType } from '@/types/question'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useFavoriteStore = defineStore('favorite', () => {
  // 1.获取收藏列表
  const favoritesList = ref<QuestionContentType[]>([])
  const questionTypeMap = ref<Map<number, QuestionType>>(new Map())

  const getFavoriteList = async () => {
    try {
      // 第一步：获取收藏题号列表
      const { success, data } = await getFavoriteListAPI()

      if (!success || !data) {
        favoritesList.value = []
        return
      }

      // 解析题号列表（可能是字符串或数组）
      let questionIds: number[] = []
      if (typeof data === 'string') {
        try {
          questionIds = JSON.parse(data)
        } catch (error) {
          console.error('解析收藏列表失败:', error)
          favoritesList.value = []
          return
        }
      } else if (Array.isArray(data)) {
        questionIds = data
      }

      if (questionIds.length === 0) {
        favoritesList.value = []
        return
      }

      // 第二步：获取题型分类（包含 mainType）
      const { success: typeSuccess, data: typeData } = await getFavoriteQuestionTypeAPI()

      if (typeSuccess && typeData && Array.isArray(typeData)) {
        // 构建题号到题型信息的映射
        const typeMap = new Map<number, QuestionType>()
        typeData.forEach((item: QuestionType) => {
          typeMap.set(item.questionId, item)
        })
        questionTypeMap.value = typeMap
      }

      // 第三步：批量获取题目详情
      const questionDetails = await Promise.all(
        questionIds.map(async (questionId: number) => {
          try {
            const res = await getQuestionContentByIdAPI(questionId)
            return res.data
          } catch (error) {
            console.error(`获取题目 ${questionId} 失败:`, error)
            return null
          }
        }),
      )

      // 过滤掉失败的请求
      favoritesList.value = questionDetails.filter((item) => item !== null)
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      favoritesList.value = []
    }
  }

  // 2.分类数据
  const categories = [
    { name: '听力', icon: 'headphones', color: '#3a7bd5' },
    { name: '阅读', icon: 'map', color: '#00b42a' },
    { name: '翻译', icon: 'chatboxes', color: '#f53f3f' },
    { name: '写作', icon: 'compose', color: '#ff7d00' },
  ]

  // 3.当前选中分类
  const currentMainType = ref(0)

  // 4.切换分类
  const switchCategory = (index: number) => {
    currentMainType.value = index
  }

  // 5.过滤当前分类的收藏
  const filteredFavorites = computed(() => {
    const currentCategoryName = categories[currentMainType.value].name

    // 如果收藏列表为空，直接返回
    if (favoritesList.value.length === 0) {
      return []
    }

    // 根据 questionTypeMap 中的 mainType 过滤
    return favoritesList.value.filter((item) => {
      const questionType = questionTypeMap.value.get(item.questionId)
      if (questionType) {
        return questionType.mainType === currentCategoryName
      }
      return false
    })
  })

  // 6.跳转
  enum FavoriteQuestionType {
    '听力' = 'listening',
    '阅读' = 'reading',
    '翻译' = 'translation',
    '写作' = 'writing',
  }

  const handleJump = (questionId: number) => {
    // 从 questionTypeMap 中获取 mainType
    const questionType = questionTypeMap.value.get(questionId)
    if (!questionType) {
      uni.showToast({
        title: '题目信息不存在',
        icon: 'none',
      })
      return
    }

    const mainType = questionType.mainType
    const pagePath = FavoriteQuestionType[mainType as keyof typeof FavoriteQuestionType]

    if (!pagePath) {
      uni.showToast({
        title: '未知题目类型',
        icon: 'none',
      })
      return
    }

    uni.navigateTo({
      url: `/pages/${pagePath}/${pagePath}?questionId=${questionId}&isAIJump=true`,
    })
  }

  return {
    /** 收藏列表 */
    favoritesList,
    /** 获取收藏列表 */
    getFavoriteList,
    /** 分类数据 */
    categories,
    /** 当前选中分类 */
    currentMainType,
    /** 切换分类 */
    switchCategory,
    /** 过滤当前分类的收藏 */
    filteredFavorites,
    /** 处理跳转 */
    handleJump,
  }
})
