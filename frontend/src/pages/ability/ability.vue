<template>
  <view class="ability-page">
    <!-- 顶部标题 -->
    <view class="header">
      <view class="title-section">
        <text class="title">能力分析</text>
        <text class="subtitle">基于 AI 智能分析的个性化学习诊断报告</text>
      </view>
    </view>

    <!-- 四个分类按钮 -->
    <view class="category-buttons">
      <view
        v-for="mainType in mainTypes"
        :key="mainType"
        class="category-btn"
        :class="{ active: currentCategory === mainType }"
        @click="selectCategory(mainType)"
      >
        <text class="category-text">{{ getMainTypeName(mainType) }}</text>
      </view>
    </view>

    <!-- 熟练度展示框（固定高度） -->
    <view class="proficiency-display">
      <view class="display-header">
        <view class="card-title-section">
          <text class="display-title">{{ getMainTypeName(currentCategory) }}</text>
          <text class="display-subtitle">能力分布</text>
        </view>
        <view class="difficulty-selector">
          <picker
            :value="getPickerValue(currentCategory)"
            :range="difficultyOptions"
            @change="(e: any) => onDifficultyChange(currentCategory, e.detail.value)"
          >
            <view class="difficulty-btn">
              <text class="difficulty-text">{{ getDifficultyValue(currentCategory) }}</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 熟练度列表 -->
      <view class="proficiency-list">
        <view
          v-for="(item, index) in getSubTypeProficiency(currentCategory)"
          :key="index"
          class="proficiency-item"
        >
          <view class="proficiency-label">
            <text class="sub-type-name">{{ item.subType }}</text>
            <text class="proficiency-score">{{ (item.proficient * 100).toFixed(0) }}</text>
          </view>
          <view class="progress-bar-bg">
            <view
              class="progress-bar-fill"
              :style="{ width: getProgressWidth(item.proficient) }"
              :class="getProgressClass(item.proficient)"
            ></view>
          </view>
        </view>
      </view>

      <!-- 平均熟练度 -->
      <view class="card-footer">
        <text class="footer-label">平均熟练度</text>
        <text class="footer-value">{{ getRadarProficient(currentCategory) }}</text>
      </view>
    </view>

    <!-- AI 智能分析报告入口 -->
    <view class="ai-report-section">
      <view class="section-title">
        <view class="title-icon">✨</view>
        <text class="title-text">AI 智能诊断报告</text>
      </view>
      <view class="report-types">
        <view
          v-for="type in reportTypes"
          :key="type.type"
          class="report-type-card"
          :class="{ active: currentReportType === type.type }"
          @click="selectReportType(type.type)"
        >
          <view class="type-icon">{{ type.icon }}</view>
          <text class="type-name">{{ type.name }}</text>
        </view>
      </view>

      <!-- 生成报告按钮 -->
      <view class="generate-section">
        <button
          class="generate-btn"
          @click="generateReport"
          :disabled="
            !currentReportType ||
            generating ||
            ((currentReportType === 'image' || currentReportType === 'expert') && !hasSentMessage)
          "
        >
          <text v-if="!generating">📊 生成{{ currentReportTypeName }}报告</text>
          <text v-else>⏳ AI 正在分析中...</text>
        </button>
      </view>
    </view>

    <!-- Markdown 报告展示区域 -->
    <view v-if="reportGenerated" class="report-modal" @click="closeReport">
      <view class="report-content" @click.stop>
        <!-- 报告头部 -->
        <view class="report-header">
          <view class="header-left">
            <text class="report-title">{{ currentReportTypeName }}报告</text>
            <text class="report-time">{{ generatedTime }}</text>
          </view>
          <view class="header-actions">
            <view class="action-btn" @click="exportReport">
              <text class="action-icon">📥</text>
              <text class="action-text">导出</text>
            </view>
            <view class="close-btn" @click="closeReport">
              <text class="close-icon">✕</text>
            </view>
          </view>
        </view>

        <!-- 诊断摘要统计栏（仅错题解析页显示） -->
        <view v-if="currentReportType === 'analysis'" class="diagnosis-summary">
          <view class="stat-item">
            <text class="stat-value error">{{ errorCount }}</text>
            <text class="stat-label">错题数</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value accuracy">{{ accuracyRate }}%</text>
            <text class="stat-label">正确率</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value weak">{{ weakPointCount }}</text>
            <text class="stat-label">薄弱点</text>
          </view>
        </view>

        <!-- Markdown 渲染区域 -->
        <scroll-view scroll-y class="report-scroll">
          <!-- Loading 状态 -->
          <view v-if="generating && isPolling" class="loading-state">
            <view class="loading-spinner-large"></view>
            <text class="loading-text-large">AI 正在分析中，请稍候...</text>
          </view>

          <!-- Markdown 内容 -->
          <MarkdownRenderer v-else :markdown="reportMarkdown" />

          <!-- 巩固练习页的特殊按钮（标记为已掌握/收藏） -->
          <view v-if="currentReportType === 'practice'" class="practice-actions">
            <view class="action-item" @click="markAsMastered">
              <view class="action-icon-box mastered">✓</view>
              <text class="action-label">标记为已掌握</text>
            </view>
            <view class="action-item" @click="addToFavorites">
              <view class="action-icon-box favorite">★</view>
              <text class="action-label">存入收藏夹</text>
            </view>
          </view>
        </scroll-view>

        <!-- 底部操作按钮组 -->
        <view class="report-footer-actions">
          <button class="footer-action-btn primary" @click="generatePractice">
            📝 生成对应练习
          </button>
          <button class="footer-action-btn secondary" @click="exportPDF">📄 导出 PDF 报告</button>
          <button class="footer-action-btn secondary" @click="syncToCalendar">
            📅 同步到学习日历
          </button>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-mask">
      <view class="loading-content">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>
    </view>

    <!-- 图文辅助/专家精改聊天对话框 -->
    <view v-if="showChatDialog" class="chat-dialog-mask" @click="closeChatDialog">
      <view class="chat-dialog-content" @click.stop>
        <!-- 对话框头部 -->
        <view class="chat-dialog-header">
          <view class="header-info">
            <text class="dialog-icon">{{ currentReportType === 'image' ? '🖼️' : '🎓' }}</text>
            <view class="header-text">
              <text class="dialog-title">{{
                currentReportType === 'image' ? '图文辅助实验室' : '专家级精改'
              }}</text>
              <text class="dialog-subtitle">AI 智能助手</text>
            </view>
          </view>
          <view class="dialog-close" @click="closeChatDialog">
            <text class="close-icon">✕</text>
          </view>
        </view>

        <!-- 聊天消息区域 -->
        <scroll-view scroll-y class="chat-messages-container" :scroll-into-view="scrollToView">
          <view class="chat-messages">
            <!-- 欢迎消息 -->
            <view class="message-item ai-message">
              <view class="message-avatar">
                <text class="avatar-icon">🤖</text>
              </view>
              <view class="message-content">
                <view class="message-bubble">
                  <text class="bubble-text">
                    {{
                      currentReportType === 'image'
                        ? '你好！我是图文辅助助手，请输入文章内容或段落，我会为您生成图文解析~'
                        : '你好！我是专家级助教，请输入您的作文或翻译，我会为您提供深度修改建议~'
                    }}
                  </text>
                </view>
                <text class="message-time">{{ currentTime }}</text>
              </view>
            </view>

            <!-- 用户发送的消息 -->
            <view v-for="(msg, index) in chatMessages" :key="index" class="message-item">
              <!-- 用户消息 -->
              <view v-if="msg.type === 'user'" class="message-item user-message">
                <view class="message-content user-content">
                  <view class="message-bubble user-bubble">
                    <text class="bubble-text">{{ msg.content }}</text>
                  </view>
                </view>
                <view class="message-avatar user-avatar">
                  <text class="avatar-icon">👤</text>
                </view>
              </view>

              <!-- AI 消息 -->
              <view v-else class="message-item ai-message">
                <view class="message-avatar">
                  <text class="avatar-icon">🤖</text>
                </view>
                <view class="message-content">
                  <view class="message-bubble">
                    <!-- Loading 状态 -->
                    <view v-if="msg.loading" class="loading-dots">
                      <view class="dot"></view>
                      <view class="dot"></view>
                      <view class="dot"></view>
                    </view>
                    <!-- Markdown 内容 -->
                    <MarkdownRenderer v-else :markdown="msg.content" class="bubble-markdown" />
                  </view>
                  <text class="message-time">{{ msg.time }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <!-- 输入区域 -->
        <view class="chat-input-area">
          <view class="input-wrapper">
            <textarea
              v-model="userInputText"
              class="chat-input"
              :placeholder="
                currentReportType === 'image' ? '请输入文章内容...' : '请输入作文或翻译...'
              "
              :maxlength="2000"
              :auto-height="true"
              :disabled="isSending"
            />
            <view class="char-indicator">{{ userInputText.length }}/2000</view>
          </view>
          <view class="send-button-wrapper">
            <button
              class="send-button"
              @click="sendMessage"
              :disabled="!userInputText.trim() || isSending"
            >
              <text v-if="!isSending">📤 发送</text>
              <text v-else>⏳ 发送中</text>
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  getProficientListAPI,
  chatErrorAnalysisAPI,
  chatCustomPracticeAPI,
  chatStudyPlanAPI,
  chatImageUnderstandingAPI,
  chatWritingCorrectionAPI,
  getChatResultAPI,
} from '@/api/question'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

// 主类型枚举（中文）
enum MainType {
  LISTENING = '听力',
  READING = '阅读',
  TRANSLATION = '翻译',
  WRITING = '写作',
}

// 难度级别 (1-9 级)
type DifficultyLevel =
  | '一级'
  | '二级'
  | '三级'
  | '四级'
  | '五级'
  | '六级'
  | '七级'
  | '八级'
  | '九级'

// 报告类型
type ReportType = 'analysis' | 'practice' | 'plan' | 'image' | 'expert'

// 本地存储的难度配置
interface DifficultyConfig {
  listening: DifficultyLevel
  reading: DifficultyLevel
  translation: DifficultyLevel
  writing: DifficultyLevel
}

// 雷达图数据接口
interface RadarChartData {
  mainType: MainType
  difficulty: DifficultyLevel
  dimensions: string[]
  scores: number[]
  proficient: number
}

// API 返回的数据结构
interface AbilityData {
  mainType: string
  subType: string
  difficulty: string
  questionIds: number[]
  userQuestionIds: number[]
  noUserQuestionIds: number[]
  proficient: number
}

// 主类型列表
const mainTypes = ref<MainType[]>([
  MainType.LISTENING,
  MainType.READING,
  MainType.TRANSLATION,
  MainType.WRITING,
])

// 难度选项
const difficultyOptions = ['一级', '二级', '三级', '四级', '五级', '六级', '七级', '八级', '九级']

// 难度配置
const difficultyConfig = ref<DifficultyConfig>({
  listening: '五级',
  reading: '五级',
  translation: '五级',
  writing: '五级',
})

// 雷达图数据
const radarData = ref<Record<MainType, RadarChartData>>({} as Record<MainType, RadarChartData>)

// 加载状态
const loading = ref(false)

// 当前选中的分类
const currentCategory = ref<MainType>(MainType.LISTENING)

// 报告相关状态
const currentReportType = ref<ReportType>('analysis')
const reportGenerated = ref(false)
const generating = ref(false)
const reportMarkdown = ref('')
const generatedTime = ref('')

// 图文辅助和专家精改相关状态
const showChatDialog = ref(false) // 显示聊天对话框
const userInputText = ref('') // 用户输入的文本
const currentChatId = ref('') // 当前聊天 ID
const currentConversationId = ref('') // 当前会话 ID
const pollingTimer = ref<any>(null) // 轮询定时器
const isPolling = ref(false) // 是否正在轮询
const isSending = ref(false) // 是否正在发送
const hasSentMessage = ref(false) // 是否已发送消息（用于启用底部按钮）
const chatMessages = ref<
  Array<{
    type: 'user' | 'ai'
    content: string
    time: string
    loading?: boolean
  }>
>([]) // 聊天消息列表
const scrollToView = ref('') // 滚动目标
const currentTime = ref('') // 当前时间

// 获取当前时间
const getCurrentTime = () => {
  const now = new Date()
  return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

// 统计数据
const errorCount = ref(0)
const accuracyRate = ref(0)
const weakPointCount = ref(0)

// 报告类型配置
const reportTypes = [
  {
    type: 'analysis' as ReportType,
    icon: '📊',
    name: '错题解析',
    description: '深度分析错题，找出知识薄弱点',
  },
  {
    type: 'practice' as ReportType,
    icon: '✍️',
    name: '巩固练习',
    description: '针对性练习，强化薄弱环节',
  },
  {
    type: 'plan' as ReportType,
    icon: '📅',
    name: '学习规划',
    description: '智能规划学习路径，高效提升',
  },
  {
    type: 'image' as ReportType,
    icon: '🖼️',
    name: '图文辅助',
    description: '上传文章，AI 生成图文解析',
  },
  {
    type: 'expert' as ReportType,
    icon: '🎓',
    name: '专家精改',
    description: '教授级深度修改意见',
  },
]

// 计算当前报告类型名称
const currentReportTypeName = computed(() => {
  const type = reportTypes.find((t) => t.type === currentReportType.value)
  return type?.name || ''
})

// 获取主类型中文名称
const getMainTypeName = (mainType: MainType) => {
  return mainType
}

// 获取雷达图平均熟练度
const getRadarProficient = (mainType: MainType) => {
  const data = radarData.value[mainType]
  return data ? (data.proficient * 100).toFixed(0) : '0'
}

// 获取子类型熟练度列表
const getSubTypeProficiency = (mainType: MainType) => {
  const data = radarData.value[mainType]
  if (!data || !data.dimensions || !data.scores) {
    return []
  }

  return data.dimensions.map((dimension, index) => ({
    subType: dimension,
    proficient: (data.scores[index] || 0) / 100,
  }))
}

// 获取进度条宽度
const getProgressWidth = (proficient: number) => {
  return `${Math.max(proficient * 100, 5)}%`
}

// 获取进度条样式类
const getProgressClass = (proficient: number) => {
  const percentage = proficient * 100
  if (percentage >= 80) return 'progress-high'
  if (percentage >= 60) return 'progress-medium'
  if (percentage >= 40) return 'progress-low'
  return 'progress-very-low'
}

// 获取 picker 的当前值
const getPickerValue = (mainType: MainType) => {
  const keyMap: Record<MainType, keyof DifficultyConfig> = {
    [MainType.LISTENING]: 'listening',
    [MainType.READING]: 'reading',
    [MainType.TRANSLATION]: 'translation',
    [MainType.WRITING]: 'writing',
  }
  const difficulty = difficultyConfig.value[keyMap[mainType]]
  return difficultyOptions.indexOf(difficulty)
}

// 获取显示的难度值
const getDifficultyValue = (mainType: MainType) => {
  const keyMap: Record<MainType, keyof DifficultyConfig> = {
    [MainType.LISTENING]: 'listening',
    [MainType.READING]: 'reading',
    [MainType.TRANSLATION]: 'translation',
    [MainType.WRITING]: 'writing',
  }
  return difficultyConfig.value[keyMap[mainType]]
}

// 将 API 数据转换为雷达图数据
const transformApiDataToRadarData = (
  apiData: AbilityData[],
  mainType: MainType,
  difficulty: DifficultyLevel,
): RadarChartData => {
  const filteredData = apiData.filter(
    (item) => item.mainType === mainType && item.difficulty === difficulty,
  )

  const dimensions: string[] = []
  const scores: number[] = []

  filteredData.forEach((item) => {
    dimensions.push(item.subType)
    scores.push(Math.round(item.proficient * 100))
  })

  const averageProficient =
    filteredData.length > 0
      ? filteredData.reduce((sum, item) => sum + item.proficient, 0) / filteredData.length
      : 0

  return {
    mainType,
    difficulty,
    dimensions,
    scores,
    proficient: averageProficient,
  }
}

// 难度改变事件
const onDifficultyChange = async (mainType: MainType, selectedValue: string) => {
  const index = parseInt(selectedValue)
  const newDifficulty = difficultyOptions[index] as DifficultyLevel

  const keyMap: Record<MainType, keyof DifficultyConfig> = {
    [MainType.LISTENING]: 'listening',
    [MainType.READING]: 'reading',
    [MainType.TRANSLATION]: 'translation',
    [MainType.WRITING]: 'writing',
  }
  difficultyConfig.value[keyMap[mainType]] = newDifficulty

  saveDifficultyConfig()
  await loadRadarData(mainType)
}

// 选择分类
const selectCategory = (mainType: MainType) => {
  currentCategory.value = mainType
}

// 保存难度配置到本地存储
const saveDifficultyConfig = () => {
  try {
    uni.setStorageSync('ability_difficulty_config', difficultyConfig.value)
  } catch (error) {
    console.error('保存难度配置失败:', error)
  }
}

// 从本地存储加载难度配置
const loadDifficultyConfig = () => {
  try {
    const config = uni.getStorageSync('ability_difficulty_config')
    if (config) {
      difficultyConfig.value = { ...difficultyConfig.value, ...config }
    }
  } catch (error) {
    console.error('加载难度配置失败:', error)
  }
}

// 加载雷达图数据
const loadRadarData = async (mainType: MainType) => {
  const keyMap: Record<MainType, keyof DifficultyConfig> = {
    [MainType.LISTENING]: 'listening',
    [MainType.READING]: 'reading',
    [MainType.TRANSLATION]: 'translation',
    [MainType.WRITING]: 'writing',
  }
  const difficulty = difficultyConfig.value[keyMap[mainType]]

  try {
    const response = await getProficientListAPI(mainType, `${difficulty}`)

    if (response && response.success && response.data) {
      const radarChartData = transformApiDataToRadarData(
        response.data as unknown as AbilityData[],
        mainType,
        difficulty,
      )
      radarData.value[mainType] = radarChartData
    } else {
      throw new Error((response as any).errorMsg || '获取数据失败')
    }
  } catch (error) {
    console.error(`加载${mainType}数据失败:`, error)
    await loadMockData(mainType)
  }
}

// 加载模拟数据（备选方案）
const loadMockData = async (mainType: MainType) => {
  const keyMap: Record<MainType, keyof DifficultyConfig> = {
    [MainType.LISTENING]: 'listening',
    [MainType.READING]: 'reading',
    [MainType.TRANSLATION]: 'translation',
    [MainType.WRITING]: 'writing',
  }
  const difficulty = difficultyConfig.value[keyMap[mainType]]

  const mockApiData: AbilityData[] = [
    {
      mainType: mainType,
      subType: '学术讲座',
      difficulty: `${difficulty}级`,
      questionIds: [1, 2, 3],
      userQuestionIds: [1, 2],
      noUserQuestionIds: [3],
      proficient: 0.85,
    },
    {
      mainType: mainType,
      subType: '短对话',
      difficulty: `${difficulty}级`,
      questionIds: [4, 5, 6],
      userQuestionIds: [4, 5],
      noUserQuestionIds: [6],
      proficient: 0.72,
    },
    {
      mainType: mainType,
      subType: '长对话',
      difficulty: `${difficulty}级`,
      questionIds: [7, 8, 9],
      userQuestionIds: [7],
      noUserQuestionIds: [8, 9],
      proficient: 0.68,
    },
    {
      mainType: mainType,
      subType: '新闻',
      difficulty: `${difficulty}级`,
      questionIds: [10, 11, 12],
      userQuestionIds: [10, 11, 12],
      noUserQuestionIds: [],
      proficient: 0.91,
    },
  ]

  const radarChartData = transformApiDataToRadarData(mockApiData, mainType, difficulty)
  radarData.value[mainType] = radarChartData
}

// 选择报告类型
const selectReportType = (type: ReportType) => {
  currentReportType.value = type

  // 如果是图文辅助或专家精改，显示聊天对话框
  if (type === 'image' || type === 'expert') {
    userInputText.value = ''
    chatMessages.value = [] // 清空消息
    currentTime.value = getCurrentTime()
    showChatDialog.value = true
    hasSentMessage.value = false // 重置发送状态
  } else if (reportGenerated.value) {
    generateReport()
  }
}

// 生成报告
const generateReport = async () => {
  generating.value = true
  reportGenerated.value = false

  try {
    console.log('开始生成报告，报告类型:', currentReportType.value)

    // 根据报告类型调用不同的 AI API
    let result
    switch (currentReportType.value) {
      case 'analysis':
        // 错题深度解析
        console.log('调用错题解析 API...')
        result = await chatErrorAnalysisAPI()
        break
      case 'practice':
        // 个性化出题/巩固练习
        console.log('调用巩固练习 API...')
        result = await chatCustomPracticeAPI()
        break
      case 'plan':
        // 学习规划
        console.log('调用学习规划 API...')
        result = await chatStudyPlanAPI()
        break
      case 'image':
        // 图文辅助实验室 (Type 3)
        console.log('调用图文辅助理解 API...')
        result = await chatImageUnderstandingAPI()
        break
      case 'expert':
        // 专家级精改 (Type 5)
        console.log('调用写作批改 API...')
        result = await chatWritingCorrectionAPI()
        break
      default:
        throw new Error('未知的报告类型')
    }

    console.log('API 返回结果:', result)
    console.log('result.success:', result.success)
    console.log('result.errorMsg:', result.errorMsg)
    console.log('result.data:', result.data)

    // 检查响应状态
    if (result.success && result.data) {
      // 设置 Markdown 内容
      reportMarkdown.value = result.data.text

      // 保存 chatId 和 conversationId
      currentChatId.value = result.data.chatId
      currentConversationId.value = result.data.conversationId

      // 设置生成时间
      const now = new Date()
      generatedTime.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(
        2,
        '0',
      )}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(
        2,
        '0',
      )}:${String(now.getMinutes()).padStart(2, '0')}`

      // 标记报告已生成
      reportGenerated.value = true

      // 如果是分析类报告，解析统计数据（如果后端返回的话）
      if (currentReportType.value === 'analysis') {
        // TODO: 如果后端返回错题数、正确率、薄弱点等数据，在这里解析
        // errorCount.value = result.data.errorCount || 0
        // accuracyRate.value = result.data.accuracyRate || 0
        // weakPointCount.value = result.data.weakPointCount || 0
      }
    } else {
      console.error('API 返回失败:', result.errorMsg)
      throw new Error(result.errorMsg || '报告生成失败')
    }
  } catch (error) {
    console.error('报告生成失败:', error)
    uni.showToast({
      title: '报告生成失败，请稍后重试',
      icon: 'none',
    })
  } finally {
    generating.value = false
  }
}

// 关闭报告
const closeReport = () => {
  // 停止轮询
  stopPolling()
  reportGenerated.value = false
}

// 导出报告
const exportReport = () => {
  uni.showToast({
    title: '导出功能开发中',
    icon: 'none',
  })
}

// 标记为已掌握
const markAsMastered = () => {
  uni.showToast({
    title: '已标记为掌握',
    icon: 'success',
  })
}

// 加入收藏夹
const addToFavorites = () => {
  uni.showToast({
    title: '已加入收藏夹',
    icon: 'success',
  })
}

// 生成对应练习
const generatePractice = () => {
  uni.showToast({
    title: '正在生成练习题...',
    icon: 'loading',
  })
}

// 导出 PDF 报告
const exportPDF = () => {
  uni.showToast({
    title: '导出 PDF 功能开发中',
    icon: 'none',
  })
}

// 同步到学习日历
const syncToCalendar = () => {
  uni.showToast({
    title: '同步成功',
    icon: 'success',
  })
}

// 关闭聊天对话框
const closeChatDialog = () => {
  showChatDialog.value = false
  userInputText.value = ''
  chatMessages.value = []
  hasSentMessage.value = false // 重置发送状态
  stopPolling()
}

// 发送消息
const sendMessage = async () => {
  if (!userInputText.value.trim()) {
    uni.showToast({
      title: '请输入内容',
      icon: 'none',
    })
    return
  }

  if (isSending.value) {
    return
  }

  // 添加用户消息
  const userMessage = {
    type: 'user' as const,
    content: userInputText.value.trim(),
    time: getCurrentTime(),
  }
  chatMessages.value.push(userMessage)

  // 清空输入框
  userInputText.value = ''
  isSending.value = true
  hasSentMessage.value = true // 标记已发送消息

  // 滚动到底部
  setTimeout(() => {
    scrollToView.value = 'message-' + (chatMessages.value.length - 1)
  }, 100)

  try {
    // 添加 AI 消息（loading 状态）
    const aiMessageIndex = chatMessages.value.length
    chatMessages.value.push({
      type: 'ai',
      content: '',
      time: getCurrentTime(),
      loading: true,
    })

    // 滚动到底部
    setTimeout(() => {
      scrollToView.value = 'message-' + aiMessageIndex
    }, 100)

    // 模拟延迟返回
    setTimeout(() => {
      if (currentReportType.value === 'image') {
        // 图文辅助 - 返回图片 + 解析
        chatMessages.value[
          aiMessageIndex
        ].content = `![Logo](/static/images/logo.png)\n\n## 📖 图文解析\n\n这篇文章主要讲述了**英语学习的重要性**。\n\n### 核心观点：\n1. 英语是国际通用语言\n2. 学习英语可以开阔视野\n3. 掌握英语有助于职业发展\n\n### 建议：\n- 每天坚持阅读英文文章\n- 多听英文播客和新闻\n- 勇于开口练习口语`
      } else if (currentReportType.value === 'expert') {
        // 专家精改 - 返回深度修改建议
        chatMessages.value[
          aiMessageIndex
        ].content = `## 🎓 专家级精改建议\n\n### 总体评价\n您的作文结构清晰，观点明确，但在以下几个方面还有提升空间：\n\n### 📝 语法改进\n1. **时态一致性**：文中多处时态混用，建议统一为一般现在时\n2. **冠词使用**：注意 a/an/the 的正确使用\n3. **主谓一致**：第三人称单数动词需加 -s\n\n### ✨ 词汇升级\n- "good" → "excellent", "outstanding", "remarkable"\n- "important" → "crucial", "vital", "significant"\n- "think" → "believe", "argue", "maintain"\n\n### 🏗️ 结构优化\n1. **开头段**：可以更直接地提出论点\n2. **主体段**：每段应有一个明确的主题句\n3. **结尾段**：应重申论点并提出展望\n\n### 💡 深度建议\n> 写作是一个需要长期积累的过程。建议您：\n> - 每天阅读英文外刊（如 The Economist）\n> - 背诵优秀范文的开头和结尾\n> - 多练习复杂句型的使用`
      }

      chatMessages.value[aiMessageIndex].loading = false
      isSending.value = false

      // 滚动到底部
      setTimeout(() => {
        scrollToView.value = 'message-' + aiMessageIndex
      }, 100)
    }, 1500)
  } catch (error) {
    console.error('发送消息失败:', error)
    uni.showToast({
      title: '发送失败，请稍后重试',
      icon: 'none',
    })
    isSending.value = false
  }
}

// 开始轮询
const startPolling = (messageIndex: number) => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
  }

  pollingTimer.value = setInterval(async () => {
    try {
      const result = await getChatResultAPI(currentChatId.value, currentConversationId.value)

      console.log('轮询结果:', result)

      if (result.success && result.data) {
        // 如果状态为 completed，停止轮询
        if (result.data.status === 'completed') {
          stopPolling()
          chatMessages.value[messageIndex].content = result.data.text
          chatMessages.value[messageIndex].loading = false
          isSending.value = false
          isPolling.value = false

          // 滚动到底部
          setTimeout(() => {
            scrollToView.value = 'message-' + messageIndex
          }, 100)
        } else {
          // 继续等待，保持 loading 状态
        }
      }
    } catch (error) {
      console.error('轮询失败:', error)
      stopPolling()
      isSending.value = false
      isPolling.value = false
      chatMessages.value[messageIndex].content = '抱歉，获取结果失败，请稍后重试~'
      chatMessages.value[messageIndex].loading = false
      uni.showToast({
        title: '获取结果失败',
        icon: 'none',
      })
    }
  }, 3000) // 每 3 秒轮询一次
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
  isPolling.value = false
}

// 页面加载
onMounted(() => {
  loadDifficultyConfig()

  loading.value = true
  Promise.all(mainTypes.value.map((mainType) => loadRadarData(mainType))).finally(() => {
    loading.value = false
  })
})
</script>

<style lang="scss" scoped>
.ability-page {
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
}

.header {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 0 0 24rpx 24rpx;
  padding: 24rpx 20rpx 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.title-section {
  text-align: center;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: block;
  margin-bottom: 6rpx;
}

.subtitle {
  font-size: 20rpx;
  color: #999;
  font-weight: 400;
}

/* 四个分类按钮 */
.category-buttons {
  display: flex;
  gap: 12rpx;
  padding: 16rpx;
  flex-shrink: 0;
}

.category-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12rpx;
  padding: 16rpx 12rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;

  &.active {
    background: linear-gradient(135deg, #667eea, #764ba2);
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
  }
}

.category-text {
  font-size: 24rpx;
  font-weight: 500;
  color: #667eea;

  .category-btn.active & {
    color: #fff;
  }
}

/* 熟练度展示框（固定高度） */
.proficiency-display {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16rpx;
  margin: 0 16rpx 16rpx;
  padding: 20rpx 16rpx 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  height: 450rpx;
  display: flex;
  flex-direction: column;
}

.display-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
  padding-bottom: 10rpx;
  border-bottom: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.display-title {
  font-size: 28rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.display-subtitle {
  font-size: 18rpx;
  color: #999;
  margin-top: 4rpx;
}

.difficulty-selector {
  flex-shrink: 0;
}

.difficulty-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 12rpx;
  padding: 8rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
}

.difficulty-text {
  font-size: 22rpx;
  font-weight: bold;
  color: #fff;
}

.proficiency-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  overflow: hidden;
  min-height: 0;
}

.proficiency-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  flex-shrink: 0;
}

.proficiency-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-type-name {
  font-size: 20rpx;
  color: #555;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
}

.proficiency-score {
  font-size: 22rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  min-width: 36rpx;
  text-align: right;
}

.progress-bar-bg {
  width: 100%;
  height: 10rpx;
  background: linear-gradient(to right, #f0f0f0, #e8e8e8);
  border-radius: 5rpx;
  overflow: hidden;
  box-shadow: inset 0 1rpx 2rpx rgba(0, 0, 0, 0.05);
}

.progress-bar-fill {
  height: 100%;
  border-radius: 5rpx;
  transition: width 0.5s ease;
  position: relative;
  overflow: hidden;

  &.progress-high {
    background: linear-gradient(90deg, #51cf66, #37b24d);
  }

  &.progress-medium {
    background: linear-gradient(90deg, #ffd43b, #fcc419);
  }

  &.progress-low {
    background: linear-gradient(90deg, #ff922b, #ff6b6b);
  }

  &.progress-very-low {
    background: linear-gradient(90deg, #ff6b6b, #fa5252);
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #f0f0f0;
  flex-shrink: 0;
}

.footer-label {
  font-size: 20rpx;
  color: #999;
}

.footer-value {
  font-size: 28rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* AI 报告区域 */
.ai-report-section {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx 24rpx 0 0;
  padding: 24rpx 20rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  gap: 12rpx;

  .title-icon {
    font-size: 32rpx;
  }

  .title-text {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
  }
}

.report-types {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.report-type-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  border: 2rpx solid transparent;
  cursor: pointer;

  &.active {
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.2);
  }
}

.type-icon {
  font-size: 56rpx;
  line-height: 1;
}

.type-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  text-align: center;
}

.generate-section {
  margin-top: auto;
  flex-shrink: 0;
}

.generate-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
  padding: 28rpx 0;
  border-radius: 16rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s;

  &:active {
    transform: scale(0.98);
  }

  &[disabled] {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

/* 报告弹窗 */
.report-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.report-content {
  width: 100%;
  height: 90vh;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 20rpx;
  border-bottom: 1rpx solid #e0e0e0;
  flex-shrink: 0;

  .header-left {
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .report-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .report-time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4rpx;
      padding: 8rpx 16rpx;
      background: #f8f9fa;
      border-radius: 8rpx;

      .action-icon {
        font-size: 32rpx;
      }

      .action-text {
        font-size: 22rpx;
        color: #666;
      }
    }

    .close-btn {
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f8f9fa;
      border-radius: 50%;

      .close-icon {
        font-size: 32rpx;
        color: #666;
      }
    }
  }
}

/* 诊断摘要 */
.diagnosis-summary {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  background: linear-gradient(135deg, #f8f9fa, #ffffff);
  border-bottom: 1rpx solid #e0e0e0;
  flex-shrink: 0;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;

    .stat-value {
      font-size: 40rpx;
      font-weight: bold;

      &.error {
        background: linear-gradient(135deg, #ff6b6b, #fa5252);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      &.accuracy {
        background: linear-gradient(135deg, #51cf66, #37b24d);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      &.weak {
        background: linear-gradient(135deg, #ffd43b, #fcc419);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 60rpx;
    background: #e0e0e0;
    margin: 0 20rpx;
  }
}

/* 滚动区域 */
.report-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20rpx;
}

/* 巩固练习页的特殊按钮 */
.practice-actions {
  display: flex;
  gap: 20rpx;
  padding: 24rpx 0;
  margin-top: 20rpx;
  border-top: 1rpx solid #e0e0e0;

  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    padding: 20rpx;
    background: #f8f9fa;
    border-radius: 12rpx;
    cursor: pointer;

    .action-icon-box {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      font-size: 32rpx;
      font-weight: bold;

      &.mastered {
        background: linear-gradient(135deg, #51cf66, #37b24d);
        color: #fff;
      }

      &.favorite {
        background: linear-gradient(135deg, #ffd43b, #fcc419);
        color: #fff;
      }
    }

    .action-label {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

/* 底部操作按钮组 */
.report-footer-actions {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
  border-top: 1rpx solid #e0e0e0;
  flex-shrink: 0;
  background: #ffffff;

  .footer-action-btn {
    flex: 1;
    padding: 24rpx 0;
    border-radius: 12rpx;
    font-size: 26rpx;
    font-weight: 600;
    border: none;

    &.primary {
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: #fff;
      box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
    }

    &.secondary {
      background: #f8f9fa;
      color: #333;
      border: 1rpx solid #e0e0e0;
    }
  }
}

/* 加载状态 */
.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.loading-content {
  background: #fff;
  padding: 40rpx 60rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}

/* 聊天对话框样式 - 提高优先级 */
.chat-dialog-mask {
  position: fixed !important;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6) !important;
  display: flex;
  align-items: flex-end;
  z-index: 1000 !important;
}

.chat-dialog-content {
  width: 100%;
  height: 85vh;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.2);
}

.chat-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 24rpx 20rpx;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  flex-shrink: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.dialog-icon {
  font-size: 48rpx;
}

.header-text {
  display: flex;
  flex-direction: column;
}

.dialog-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff !important;
}

.dialog-subtitle {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  margin-top: 4rpx;
}

.dialog-close {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
}

.close-icon {
  font-size: 32rpx;
  color: #fff;
}

/* 聊天消息区域 */
.chat-messages-container {
  flex: 1;
  padding: 24rpx;
  background: #f8f9fa;
  overflow-y: auto;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  box-shadow: 0 4rpx 12rpx rgba(245, 87, 108, 0.3);
}

.avatar-icon {
  font-size: 36rpx;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.user-content {
  align-items: flex-end;
}

.message-bubble {
  background: #fff;
  border-radius: 16rpx 16rpx 16rpx 4rpx;
  padding: 20rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.user-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border-radius: 16rpx 16rpx 4rpx 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(102, 126, 234, 0.3);
}

.bubble-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
}

.user-bubble .bubble-text {
  color: #fff !important;
}

.bubble-markdown {
  max-width: 100%;
}

.bubble-markdown :deep(.markdown-content) {
  font-size: 26rpx;
  color: #333;
}

.message-time {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
  text-align: right;
}

/* Loading 动画 */
.loading-dots {
  display: flex;
  gap: 8rpx;
  padding: 12rpx 0;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #999;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* 输入区域 */
.chat-input-area {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  gap: 16rpx;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border-radius: 24rpx;
  padding: 16rpx 24rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  border-color: #667eea;
  background: #fff;
  box-shadow: 0 2rpx 12rpx rgba(102, 126, 234, 0.1);
}

.chat-input {
  width: 100%;
  min-height: 44rpx;
  max-height: 200rpx;
  font-size: 28rpx;
  line-height: 1.5;
  background: transparent;
}

.char-indicator {
  display: block;
  text-align: right;
  font-size: 20rpx;
  color: #999;
  margin-top: 4rpx;
}

.send-button-wrapper {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.send-button {
  width: 140rpx !important;
  height: 80rpx !important;
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  color: #fff !important;
  border-radius: 40rpx !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 28rpx !important;
  font-weight: 600 !important;
  border: none !important;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.35) !important;
  transition: all 0.3s !important;
  padding: 0 !important;
  margin: 0 !important;
  line-height: 1 !important;
}

.send-button[disabled] {
  background: #e0e0e0 !important;
  color: #999 !important;
  box-shadow: none !important;
}

/* Loading 状态样式 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.loading-spinner-large {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text-large {
  margin-top: 24rpx;
  font-size: 28rpx;
  color: #999;
}
</style>
