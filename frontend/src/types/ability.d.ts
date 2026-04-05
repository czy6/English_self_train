// 能力分析相关类型定义

// 主类型枚举
export enum MainType {
  LISTENING = 'listening', // 听力
  READING = 'reading', // 阅读
  TRANSLATION = 'translation', // 翻译
  WRITING = 'writing' // 写作
}

// 听力子类型枚举
export enum ListeningSubType {
  ACADEMIC_LECTURE = 'academic_lecture', // 学术讲座
  SHORT_CONVERSATION = 'short_conversation', // 短对话
  LONG_CONVERSATION = 'long_conversation', // 长对话
  NEWS = 'news' // 新闻
}

// 阅读子类型枚举
export enum ReadingSubType {
  EXPOSITORY = 'expository', // 说明文
  ARGUMENTATIVE = 'argumentative', // 议论文
  NARRATIVE = 'narrative', // 记叙文
  PRACTICAL = 'practical', // 应用文
  TECHNICAL = 'technical', // 科技文
  LITERARY = 'literary' // 文学文本
}

// 翻译子类型枚举
export enum TranslationSubType {
  CHINESE_TO_ENGLISH_SENTENCE = 'chinese_to_english_sentence', // 汉译英短句
  CHINESE_TO_ENGLISH_PARAGRAPH = 'chinese_to_english_paragraph', // 汉译英段落
  ENGLISH_TO_CHINESE_SENTENCE = 'english_to_chinese_sentence', // 英译汉短句
  ENGLISH_TO_CHINESE_PARAGRAPH = 'english_to_chinese_paragraph', // 英译汉段落
  POETRY = 'poetry' // 诗歌翻译
}

// 写作子类型枚举
export enum WritingSubType {
  THANK_YOU_LETTER = 'thank_you_letter', // 感谢信
  COMPLAINT_LETTER = 'complaint_letter', // 投诉信
  ARGUMENTATIVE_ESSAY = 'argumentative_essay', // 议论文
  CHART_ESSAY = 'chart_essay', // 图表作文
  STORY_CONTINUATION = 'story_continuation', // 故事续写
  OPINION_ESSAY = 'opinion_essay' // 观点类作文
}

// 难度级别 (1-9级)
export type DifficultyLevel = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9

// 能力数据接口
export interface AbilityData {
  mainType: MainType
  subType: string
  difficulty: DifficultyLevel
  questionIds: Set<number>
  userQuestionIds: Set<number>
  noUserQuestionIds: Set<number>
  proficient: number // 熟练度 (0.0-1.0)
}

// 雷达图数据接口
export interface RadarChartData {
  mainType: MainType
  difficulty: DifficultyLevel
  dimensions: string[] // 子类型名称
  scores: number[] // 熟练度分数 (0-100)
  proficient: number // 平均熟练度
}

// 后端API响应接口
export interface AbilityResponse {
  code: number
  message: string
  data: AbilityData[]
}

// 本地存储的难度配置
export interface DifficultyConfig {
  listening: DifficultyLevel
  reading: DifficultyLevel
  translation: DifficultyLevel
  writing: DifficultyLevel
}

// 主类型配置接口
export interface MainTypeConfig {
  type: MainType
  name: string
  subTypes: {
    type: string
    name: string
  }[]
}

// 主类型配置映射
export const MAIN_TYPE_CONFIGS: Record<MainType, MainTypeConfig> = {
  [MainType.LISTENING]: {
    type: MainType.LISTENING,
    name: '听力',
    subTypes: [
      { type: ListeningSubType.ACADEMIC_LECTURE, name: '学术讲座' },
      { type: ListeningSubType.SHORT_CONVERSATION, name: '短对话' },
      { type: ListeningSubType.LONG_CONVERSATION, name: '长对话' },
      { type: ListeningSubType.NEWS, name: '新闻' }
    ]
  },
  [MainType.READING]: {
    type: MainType.READING,
    name: '阅读',
    subTypes: [
      { type: ReadingSubType.EXPOSITORY, name: '说明文' },
      { type: ReadingSubType.ARGUMENTATIVE, name: '议论文' },
      { type: ReadingSubType.NARRATIVE, name: '记叙文' },
      { type: ReadingSubType.PRACTICAL, name: '应用文' },
      { type: ReadingSubType.TECHNICAL, name: '科技文' },
      { type: ReadingSubType.LITERARY, name: '文学文本' }
    ]
  },
  [MainType.TRANSLATION]: {
    type: MainType.TRANSLATION,
    name: '翻译',
    subTypes: [
      { type: TranslationSubType.CHINESE_TO_ENGLISH_SENTENCE, name: '汉译英短句' },
      { type: TranslationSubType.CHINESE_TO_ENGLISH_PARAGRAPH, name: '汉译英段落' },
      { type: TranslationSubType.ENGLISH_TO_CHINESE_SENTENCE, name: '英译汉短句' },
      { type: TranslationSubType.ENGLISH_TO_CHINESE_PARAGRAPH, name: '英译汉段落' },
      { type: TranslationSubType.POETRY, name: '诗歌翻译' }
    ]
  },
  [MainType.WRITING]: {
    type: MainType.WRITING,
    name: '写作',
    subTypes: [
      { type: WritingSubType.THANK_YOU_LETTER, name: '感谢信' },
      { type: WritingSubType.COMPLAINT_LETTER, name: '投诉信' },
      { type: WritingSubType.ARGUMENTATIVE_ESSAY, name: '议论文' },
      { type: WritingSubType.CHART_ESSAY, name: '图表作文' },
      { type: WritingSubType.STORY_CONTINUATION, name: '故事续写' },
      { type: WritingSubType.OPINION_ESSAY, name: '观点类作文' }
    ]
  }
}