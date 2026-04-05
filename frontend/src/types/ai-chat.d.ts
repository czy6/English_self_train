/**
 * AI 聊天请求类型
 */
export type ChatRequestType = {
  /** 用户 ID */
  userId: number
  /** 聊天类型：1-错题深度解析，2-个性化出题，4-学习规划 */
  type: 1 | 2 | 4
  /** 用户输入 */
  input: string
}

/**
 * AI 聊天响应状态
 */
export type ChatStatus = 'completed' | 'processing' | 'failed'

/**
 * AI 聊天响应数据类型
 */
export type ChatResponseDataType = {
  /** 聊天 ID */
  chatId: string
  /** 会话 ID */
  conversationId: string
  /** 响应状态 */
  status: ChatStatus
  /** 响应文本（Markdown 格式） */
  text: string
}

/**
 * AI 聊天响应类型
 */
export type ChatResponseType = {
  success: boolean
  errorMsg: string | null
  data: ChatResponseDataType
}
