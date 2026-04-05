import type {
  QuestionContentType,
  AnswerRecordType,
  SubmitAnswerType,
  QuestionType,
} from '@/types/question'
import type { AbilityResponse } from '@/types/ability'
import { request } from '@/utils/request'

/**
 * 获取试题类型
 */
export const getSubTypeListAPI = (mainType: string) => {
  return request<string[]>({
    method: 'GET',
    url: `/question/type?mainType=${mainType}`,
  })
}

/**
 * 获取试题列表
 */
export const getQuestionContentListAPI = (currentType: string, currentDifficulty: string) => {
  return request<QuestionContentType[]>({
    method: 'GET',
    url: `/question/content?subType=${currentType}&difficulty=${currentDifficulty}`,
  })
}

/**
 * 根据id获取试题
 */
export const getQuestionContentByIdAPI = (questionId: number) => {
  return request<QuestionContentType>({
    method: 'GET',
    url: `/question/content/id?questionId=${questionId}`,
  })
}

/**
 * 提交作答记录
 */
export const submitAnswerRecordAPI = (data: SubmitAnswerType) => {
  return request<AnswerRecordType>({
    method: 'POST',
    url: '/question/submit/answer',
    data,
  })
}

/**
 * 根据记录id获取作答结果以及解析
 */
export const getRecordByIdAPI = (recordId: number) => {
  return request<AnswerRecordType>({
    method: 'GET',
    url: `/question/train/record/${recordId}`,
  })
}

/**
 * 根据 questionId 集合获取对应分类
 */
export const getFavoriteQuestionTypeAPI = () => {
  return request<QuestionType[]>({
    method: 'GET',
    url: `/question/type/favorite`,
  })
}

/**
 * 收藏
 */
export const favoriteAPI = (questionId: number) => {
  return request({
    method: 'POST',
    url: `/question/favorite?questionId=${questionId}`,
  })
}

/**
 * 获取收藏列表（题号 ID 列表）
 */
export const getFavoriteListAPI = () => {
  return request<number[]>({
    method: 'GET',
    url: '/question/favoriteList',
  })
}

/**
 * 批量获取收藏题目详情
 */
export const getBatchQuestionDetailAPI = () => {
  return request<QuestionContentType[]>({
    method: 'GET',
    url: '/question/batch/detail',
  })
}

/**
 * 获取熟练度列表
 */
export const getProficientListAPI = (mainType: string, difficulty: string) => {
  return request<AbilityResponse>({
    method: 'GET',
    url: '/question/ability',
    data: {
      mainType,
      difficulty,
    },
  })
}

/**
 * AI 智能对话 - 通用接口
 * @param type 业务类型：1-错题深度解析，2-个性化出题，3-图文辅助理解，4-学习规划，5-写作批改，6-通用问答
 */
export const chatWithAIAPI = (type: 1 | 2 | 3 | 4 | 5 | 6) => {
  return request({
    method: 'GET',
    url: `/question/agent/chat?type=${type}`,
  })
}

/**
 * AI 智能对话 - 错题深度解析
 */
export const chatErrorAnalysisAPI = () => {
  return chatWithAIAPI(1)
}

/**
 * AI 智能对话 - 个性化出题
 */
export const chatCustomPracticeAPI = () => {
  return chatWithAIAPI(2)
}

/**
 * AI 智能对话 - 图文辅助理解
 */
export const chatImageUnderstandingAPI = () => {
  return chatWithAIAPI(3)
}

/**
 * AI 智能对话 - 学习规划
 */
export const chatStudyPlanAPI = () => {
  return chatWithAIAPI(4)
}

/**
 * AI 智能对话 - 写作/翻译批改
 */
export const chatWritingCorrectionAPI = () => {
  return chatWithAIAPI(5)
}

/**
 * AI 智能对话 - 通用英语问答
 */
export const chatGeneralQAAPI = () => {
  return chatWithAIAPI(6)
}

/**
 * AI 智能对话 - 查询结果（轮询接口）
 * @param chatId 聊天 ID
 * @param conversationId 会话 ID
 */
export const getChatResultAPI = (chatId: string, conversationId: string) => {
  return request({
    method: 'GET',
    url: `/question/agent/result?chatId=${chatId}&conversationId=${conversationId}`,
  })
}
