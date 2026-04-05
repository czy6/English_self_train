<template>
  <view class="reading-page">
    <!-- 顶部操作栏 -->
    <view class="top-bar shadow">
      <picker
        mode="selector"
        :range="readStore.subTypeList"
        @change="handleTypeChange"
        :disabled="isDisabled"
      >
        <view class="selector type-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">题材</text>
          <text class="selector-value">{{
            readStore.selectedType || readStore.currentQuestionType
          }}</text>
        </view>
      </picker>
      <button class="refresh-btn" @click.stop="handleRefreshQuestion" :disabled="isDisabled">
        换一道
      </button>
      <picker
        mode="selector"
        :range="readStore.difficultyList"
        @change="handleDifficultyChange"
        :disabled="isDisabled"
      >
        <view class="selector difficulty-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">难度</text>
          <text class="selector-value">{{
            readStore.selectedDifficulty || readStore.currentQuestionDifficulty
          }}</text>
        </view>
      </picker>
    </view>

    <!-- 中间功能栏：建议时长 | 显示问题 | 花费时长 -->
    <view class="middle-bar shadow">
      <view class="time-box left">
        <text class="time-label">建议时长</text>
        <text class="time-value">{{ formattedSuggestedTime }}</text>
      </view>

      <button
        class="drawer-toggle"
        :class="{ active: readStore.drawerOpen }"
        @click.stop="readStore.toggleDrawer"
      >
        {{ readStore.drawerOpen ? '隐藏问题' : '显示问题' }}
      </button>

      <view class="time-box right" :class="{ overtime: isOvertime }">
        <text class="time-label">花费时长</text>
        <text class="time-value">{{ formattedElapsedTime }}</text>
      </view>
    </view>

    <!-- 阅读区域 -->
    <view class="reading-box shadow">
      <view class="article-content">
        <text v-if="readStore.currentQuestionTitle" class="article-title">{{
          readStore.currentQuestionTitle
        }}</text>
        <text v-if="readStore.currentQuestionAuthor" class="article-author">{{
          readStore.currentQuestionAuthor
        }}</text>
        <!-- <text class="article-paragraph" >{{ readStore.currentQuestionContent }}</text> -->
        <div v-html="readStore.processedQuestionContentHtml" class="question-container"></div>
        <!-- 收藏按钮 -->
        <view
          class="collect-btn"
          v-if="readStore.questionList.length > 0"
          @click="readStore.toggleFavorite"
        >
          <uni-icons
            :type="readStore.isFavorite ? 'star-filled' : 'star'"
            size="20"
            color="#FFCC00"
            class="star-icon"
          ></uni-icons>
        </view>
      </view>
    </view>

    <!-- 抽屉式问题面板 -->
    <view class="drawer-wrapper" :class="{ open: readStore.drawerOpen }">
      <!-- 题目区域 -->
      <view class="question-area shadow">
        <view class="question-header">
          <text class="question-count"
            >第{{ readStore.currentQuestionOption.questionNumber }}题 / 共{{
              readStore.currentQuestionTotalOption
            }}题</text
          >
          <text class="question-text">{{ readStore.currentQuestionOption.questionContent }}</text>
        </view>
        <view class="options-container">
          <view
            class="option"
            v-for="(opt, index) in readStore.currentQuestionOption.questionOptions"
            :key="index"
            @click.stop.prevent="readStore.selectOption(String.fromCharCode(65 + index))"
            :class="{ selected: readStore.selectedOption === String.fromCharCode(65 + index) }"
          >
            <text class="option-letter">{{ String.fromCharCode(65 + index) }}.</text>
            <text class="option-content">{{ opt.option }}</text>
          </view>
        </view>
      </view>

      <!-- 底部操作栏 -->
      <view class="bottom-bar shadow">
        <button
          class="nav-btn"
          :disabled="readStore.currentQuestionOptionIndex === 0"
          @click.stop="readStore.prevQuestion"
        >
          <uni-icons type="back" size="22"></uni-icons>
        </button>
        <view class="action-group">
          <button class="detail-btn" @click="readStore.toggleDetails">详情</button>
          <button
            class="submit-btn"
            :class="{ disabled: !allAnswered }"
            :disabled="
              !allAnswered || readStore.currentQuestionContent === '' || readStore.isSubmit
            "
            @click.stop="readStore.submitAnswer"
            v-if="!readStore.showAnswerButton"
          >
            提交答案
          </button>
          <button class="answer-btn" @click="readStore.hideResultPopup" v-else>显示结果</button>
        </view>
        <button
          class="nav-btn"
          :disabled="
            readStore.currentQuestionOptionIndex === readStore.currentQuestionTotalOption - 1
          "
          @click="readStore.nextQuestion"
        >
          <uni-icons type="forward" size="22"></uni-icons>
        </button>
      </view>
    </view>

    <!-- 结果弹窗 -->
    <view
      class="result-popup"
      v-if="readStore.showResultPopup"
      @click.self="readStore.hideResultPopup"
    >
      <view class="result-card">
        <view class="result-header">
          <text class="result-title">答题详情</text>
        </view>
        <view class="score-info"
          >得分：{{ readStore.userScore }} / 总分：{{ readStore.totalScore }}</view
        >
        <view class="result-list">
          <view class="result-item" v-for="(item, index) in readStore.explanations" :key="index">
            <text class="item-number">第{{ item.questionNumber }}题</text>
            <text
              class="user-ans"
              :class="{
                wrong: readStore.userAnswer[index] !== readStore.listenORReadAnswer[index],
              }"
            >
              你的答案：{{ readStore.userAnswer[index] || '未作答' }}
            </text>
            <text class="correct-ans">正确答案：{{ readStore.listenORReadAnswer[index] }}</text>
            <text class="explanation">{{ item.questionExplanation }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 详情弹窗 -->
    <view class="details-popup" v-if="readStore.showDetails" @click.self="readStore.toggleDetails">
      <view class="details-card">
        <view class="details-header">
          <text class="details-title">答题进度</text>
        </view>
        <view class="progress-dots">
          <view
            class="dot"
            v-for="(_, index) in readStore.currentQuestionTotalOption"
            :key="index"
            :class="{
              answered: readStore.userAnswer[index],
              current: index === readStore.currentQuestionOptionIndex,
            }"
            @click="readStore.goToQuestion(index)"
          >
            {{ index + 1 }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useQuestionStore } from '@/store'

const readStore = useQuestionStore()

// 时间格式化
const formattedSuggestedTime = computed(() => formatTime(readStore.currentQuestionTime))
const formattedElapsedTime = computed(() => formatTime(readStore.elapsedTime))

// 判断是否超时
const isOvertime = computed(() => readStore.elapsedTime >= readStore.currentQuestionTime)

function formatTime(seconds) {
  const min = Math.floor(seconds / 60)
    .toString()
    .padStart(2, '0')
  const sec = (seconds % 60).toString().padStart(2, '0')
  return `${min}:${sec}`
}

// 定时器列表管理
let timer = null

// 开启定时器
const startTimer = () => {
  if (timer) {
    stopTimer()
  }
  timer = setInterval(() => {
    readStore.elapsedTime++
    // 当花费时间达到建议时长时，添加提示
    if (readStore.elapsedTime === readStore.currentQuestionTime) {
      uni.showToast({
        title: '已达建议时长',
        icon: 'none',
        duration: 2000,
      })
    }
  }, 1000)
}

// 关闭列表中的定时器
const stopTimer = () => {
  clearInterval(timer)
  timer = null
}

const props = defineProps({
  questionId: Number,
  recordId: Number,
  isAIJump: String,
  isHistoryJump: String,
})

// 禁用顶部栏功能
const isDisabled = computed(() => props.isAIJump === 'true' || props.isHistoryJump === 'true')

onMounted(async () => {
  // 1.获取阅读类型以及收藏列表
  await readStore.getSubTypeList('阅读')
  await readStore.getFavoriteList()

  // 2.获取试题
  if (props.questionId) {
    // 2.1 从ai助手获取到的questionId(ai跳转)
    await readStore.getQuestionContentById(props.questionId)
  } else {
    // 2.2 TODO 从Home页面获取到的questionId(Home直接点进)--现写死
    await readStore.getQuestionContentList()
  }

  // 3.从历史跳转
  if (props.isHistoryJump === 'true') {
    await readStore.getRecordById(Number(props.recordId))
  } else {
    // 4.作答计时器
    if (readStore.questionList.length > 0) {
      startTimer()
    }

    // 5.监听提交状态变化
    watch(
      () => readStore.isSubmit,
      (newVal) => {
        if (newVal) {
          stopTimer()
        }
      },
    )
  }
})

onUnmounted(() => {
  readStore.onExit()
  stopTimer()
})

// 选项切换处理
const handleDifficultyChange = (e) => {
  readStore.selectedDifficulty = readStore.difficultyList[e.detail.value]
}
const handleTypeChange = (e) => {
  readStore.selectedType = readStore.subTypeList[e.detail.value]
}
// 换题
const handleRefreshQuestion = async () => {
  await readStore.refreshQuestion()
  // 不存在题目则不启动定时器
  if (readStore.questionList.length !== 0) {
    startTimer()
  } else {
    stopTimer()
  }
}
// 提交状态判断
const allAnswered = computed(() => {
  return (
    readStore.userAnswer.length === readStore.currentQuestionTotalOption &&
    readStore.userAnswer.every((ans) => ans)
  )
})
</script>

<style lang="scss" scoped>
/* 全局变量，统一四页样式 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$accent-blue: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
$card-bg: rgba(255, 255, 255, 0.98);
$shadow-light: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
$shadow-hover: 0 12rpx 40rpx rgba(102, 126, 234, 0.25);

.reading-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f8f9ff 100%);
  overflow: hidden;
  padding: 16rpx;
  box-sizing: border-box;
  gap: 16rpx;
}

/* 主内容区域 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  flex: 1;
  overflow: hidden;
}

/* 通用阴影容器 */
.shadow {
  background: $card-bg;
  border-radius: 24rpx;
  box-shadow: $shadow-light;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  &:hover {
    box-shadow: $shadow-hover;
  }
}

/* 顶部操作栏 */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  height: 80rpx;
}

/* 选择器样式 */
.selector {
  padding: 12rpx 20rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 140rpx;
  min-height: 48rpx;
  transition: all 0.3s;
  border: 2rpx solid transparent;

  &.type-selector,
  &.difficulty-selector {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
    border-color: rgba(102, 126, 234, 0.3);
  }

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;
    background: rgba(102, 126, 234, 0.05);
    border-color: rgba(102, 126, 234, 0.2);

    .selector-label {
      color: #bbb;
    }

    .selector-value {
      color: #999;
    }
  }
}

.selector-label {
  font-size: 20rpx;
  color: #667eea;
  margin-bottom: 6rpx;
  display: block;
  font-weight: 500;
}

.selector-value {
  font-size: 22rpx;
  color: #333;
  font-weight: 600;
  display: block;
  text-align: center;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 换一道按钮 */
.refresh-btn {
  font-size: 24rpx;
  padding: 16rpx 32rpx;
  background: $primary-gradient;
  color: #fff;
  border-radius: 20rpx;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);

  &:disabled {
    background: #c0c4cc;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
  }

  &:active {
    transform: scale(0.95);
  }
}

/* 中间功能栏 */
.middle-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  height: 72rpx;
}

.time-box {
  flex: 1;
  max-width: 160rpx;
  padding: 12rpx;
  text-align: center;
  border-radius: 16rpx;
  transition: all 0.3s;

  &.left {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  }

  &.right {
    background: linear-gradient(135deg, rgba(245, 108, 108, 0.08), rgba(255, 122, 69, 0.08));
  }

  &.overtime .time-value {
    color: #f56c6c;
    animation: pulse 1.5s infinite;
  }
}

.time-label {
  font-size: 20rpx;
  color: #888;
  display: block;
  margin-bottom: 6rpx;
  font-weight: 400;
}

.time-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #667eea;
  font-family: 'Courier New', monospace;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.drawer-toggle {
  font-size: 24rpx;
  padding: 14rpx 28rpx;
  margin: 0 16rpx;
  background: $primary-gradient;
  color: #fff;
  border-radius: 20rpx;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);

  &.active {
    background: linear-gradient(135deg, #ff7a45 0%, #f56c6c 100%);
  }

  &:active {
    transform: scale(0.95);
  }
}

/* 阅读区域 */
.reading-box {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 文章内容容器 */
.article-content {
  padding: 32rpx;
  flex: 1;
  overflow-y: auto;
  max-height: 70vh;
  position: relative;
}

.article-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 16rpx;
  display: block;
}

.article-author {
  font-size: 24rpx;
  color: #909399;
  text-align: center;
  margin-bottom: 16rpx;
  display: block;
}

.article-paragraph {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 24rpx;
  text-indent: 2em;
  text-align: justify;
  text-justify: inter-word;
  overflow-wrap: break-word;
  display: block;
  break-inside: avoid;
}

/* 文章内容 HTML 样式 */
::v-deep .article-paragraph {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 24rpx;
  text-indent: 2em;
  text-align: justify;
  text-justify: inter-word;
  overflow-wrap: break-word;
  display: block;
  break-inside: avoid;
}

/* 问题内容容器 */
.question-container {
  width: 100%;
}

/* 收藏按钮 */
.collect-btn {
  position: absolute;
  top: 8rpx;
  left: 8rpx;
  z-index: 2;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.9);
  padding: 8rpx;
  border-radius: 50%;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;

  &:active {
    transform: scale(1.1);
  }
}

/* 抽屉式问题面板 */
.drawer-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: auto;
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  background: #fff;
  transform: translateY(100%);
  transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  border-top-left-radius: 32rpx;
  border-top-right-radius: 32rpx;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.1);
  z-index: 998;
}

.drawer-wrapper.open {
  transform: translateY(0);
}

/* 题目区域 */
.question-area {
  padding: 32rpx;
  min-height: 160px;
}

.question-header {
  margin-bottom: 32rpx;
}

.question-count {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 12rpx;
  display: block;
}

.question-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 24rpx;
  display: block;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.option {
  padding: 20rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  &:active {
    background: rgba(102, 126, 234, 0.1);
  }

  &.selected {
    background: rgba(102, 126, 234, 0.15);
    color: #667eea;
    border-color: #667eea;
  }
}

.option-letter {
  font-weight: 700;
  color: #667eea;
}

/* 底部操作栏 */
.bottom-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  height: 80rpx;
}

.nav-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  transition: all 0.3s;

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  &:active {
    transform: scale(0.9);
  }
}

.action-group {
  display: flex;
  gap: 16rpx;
  flex: 1;
  justify-content: center;
}

.detail-btn,
.submit-btn,
.answer-btn {
  font-size: 24rpx;
  padding: 14rpx 32rpx;
  border-radius: 20rpx;
  border: none;
  transition: all 0.3s;
  background: $primary-gradient;
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);

  &:disabled {
    background: #c0c4cc;
    cursor: not-allowed;
    box-shadow: none;
  }

  &:active {
    transform: scale(0.95);
  }
}

.answer-btn {
  background: linear-gradient(135deg, #ff7a45 0%, #f56c6c 100%);
}

/* 结果弹窗 */
.result-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40rpx;
}

.result-card {
  background: #fff;
  border-radius: 32rpx;
  padding: 40rpx;
  width: 100%;
  max-width: 600rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.2);
}

.result-header {
  text-align: center;
  margin-bottom: 32rpx;
}

.result-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #333;
  display: block;
}

.score-info {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 600;
  text-align: center;
  margin-bottom: 32rpx;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.result-item {
  padding: 20rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 16rpx;
}

.item-number {
  font-size: 24rpx;
  color: #667eea;
  font-weight: 600;
  display: block;
  margin-bottom: 12rpx;
}

.user-ans {
  font-size: 24rpx;
  color: #333;

  &.wrong {
    color: #f56c6c;
    text-decoration: line-through;
  }
}

.correct-ans {
  font-size: 24rpx;
  color: #67c23a;
  font-weight: 600;
  display: block;
  margin-top: 8rpx;
}

.explanation {
  font-size: 22rpx;
  color: #999;
  line-height: 1.6;
}

/* 详情弹窗 */
.details-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.details-card {
  background: #fff;
  border-radius: 32rpx;
  width: 90%;
  max-width: 500rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.15);
}

.details-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  border-bottom: 2rpx solid #ebeef5;
}

.details-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #333;
}

.close-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: $primary-gradient;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: transform 0.3s;
  border: none;

  &:active {
    transform: rotate(90deg);
  }
}

.progress-dots {
  padding: 32rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16rpx;
}

.dot {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700;
  color: #666;
  border: 2rpx solid #ebeef5;
  transition: all 0.3s;
  cursor: pointer;

  &.answered {
    background: #667eea;
    color: #fff;
    border-color: #667eea;
  }

  &.current {
    background: #ff7a45;
    color: #fff;
    border-color: #ff7a45;
    transform: scale(1.1);
  }
}
</style>
