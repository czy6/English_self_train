/**
 * 历史记录
 */
export type HistoryType = {
  /** 试题id */
  questionId: number
  /** 试题主类型 */
  mainType: string
  /** 试题子类型 */
  subType: string
  /** 试题难度 */
  difficulty: string
  /** 试题要求作答时间 */
  requiredTime: number
  /** 用户实际用时 */
  timeSpent: number
  /** 试题分数 */
  score: number
  /** 记录id */
  recordId: number
  /** 用户实际得分 */
  userScore: number
  /** 记录时间 */
  createdTime: string
  /** 正确率 */
  accuracy?: number
}

/**
 * 按日期分割的历史记录列表
 */
export type GroupedHistoryType = {
  /** 日期 */
  date: string
  /** 历史记录列表 */
  list: HistoryType[]
}
