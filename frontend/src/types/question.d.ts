/** 问题列表类型 */
type Questions = {
  /** 问题个数 */
  questionTotal: number
  /** 问题列表 */
  questions: Question[]
  /** 最小词量 */
  min_word_count?: number
}

/** 问题类型 */
type Question = {
  /** 问题序号 */
  questionNumber: number
  /** 问题内容 */
  questionContent: string
  /** 问题选项列表 */
  questionOptions: QuestionOption[]
}

/** 问题选项类型 */
type QuestionOption = {
  /** 选项值 */
  value: string
  /** 选项内容 */
  option: string
}

/** 试题类型 */
export type QuestionContentType = {
  /** 试题 id */
  questionId: number
  /** 试题类型 */
  subType: string
  /** 试题难度 */
  difficulty: string
  /** 试题分数 */
  score: number
  /** 试题要求作答时间 */
  requiredTime: number
  /** 试题标题 */
  questionTitle: string
  /** 试题作者 */
  questionAuthor: string
  /** 试题内容 */
  questionText: string
  /** 试题选项列表 */
  options: Questions
  /** 听力音频地址 */
  audioUrl: string
}

/** 试题题型信息类型（用于收藏） */
export type QuestionType = {
  /** 试题 id */
  questionId: number
  /** 试题类型 id */
  questionTypeId: number
  /** 主类型 */
  mainType: string
  /** 子类型 */
  subType: string
}

/** 用户提交表单类型 */
export type SubmitAnswerType = {
  /** 试题主类型 */
  mainType: string

  /** 试题id */
  questionId: number

  /** 用户提交的答案 */
  userAnswer: string
  
  /** 用户实际用时列表 */
  timeSpent: number
}

/** 解析列表总类型 */
type QuestionExplanations = {
  /** 解析列表 */
  explanations: QuestionExplanation[]
  /** 问题总数量（对应题目总数） */
  questionAnswerTotal: number
  /** 技巧 */
  tips: string
}

/** 单个问题的解析类型 */
type QuestionExplanation = {
  /** 问题序号（对应题目序号） */
  questionNumber?: number
  /** 问题解析内容 */
  questionExplanation?: string
}

/** 用户答题记录类型 */
export type AnswerRecordType = {
  /** 试题id */
  questionId: number;
  
  /** 试题分数 */
  score: number;
  
  /** 用户实际得分 */
  userScore: number;
  
  /** 试题要求作答时间 */
  requiredTime: number;
  
  /** 用户实际用时 */
  timeSpent: number;

  /** 翻译作文批改结果分析，听力阅读默认为空 */
  analysis: string
  
  /** 标准答案列表 or 参考范文 */
  answerText: string;
  
  /** 用户提交的答案 */
  userAnswer: string;
  
  /** 听力阅读答案解析 or 写作翻译技巧 */
  explanation: QuestionExplanations;
}

/**
 * 试题分类
 */
export type QuestionType = {
  /** 试题id */
  questionId: number
  /** 类型id */
  questionTypeId: number,
  /** 试题主类型 */
  mainType: string
  /** 试题子类型 */
  subType: string
}
