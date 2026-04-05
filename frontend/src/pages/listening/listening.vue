<template>
  <view class="listening-page">
    <!-- 顶部操作栏 -->
    <view class="top-bar shadow">
      <picker
        mode="selector"
        :range="listenStore.subTypeList"
        @change="handleTypeChange"
        :disabled="isDisabled"
      >
        <view class="selector type-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">题材</text>
          <text class="selector-value">{{
            listenStore.selectedType || listenStore.currentQuestionType
          }}</text>
        </view>
      </picker>
      <button class="refresh-btn" @click.stop="handleRefreshQuestion" :disabled="isDisabled">
        换一道
      </button>
      <picker
        mode="selector"
        :range="listenStore.difficultyList"
        @change="handleDifficultyChange"
        :disabled="isDisabled"
      >
        <view class="selector difficulty-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">难度</text>
          <text class="selector-value">{{
            listenStore.selectedDifficulty || listenStore.currentQuestionDifficulty
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
        :class="{ active: listenStore.drawerOpen }"
        @click.stop="listenStore.toggleDrawer"
      >
        {{ listenStore.drawerOpen ? '隐藏问题' : '显示问题' }}
      </button>

      <view class="time-box right" :class="{ overtime: isOvertime }">
        <text class="time-label">花费时长</text>
        <text class="time-value">{{ formattedElapsedTime }}</text>
      </view>
    </view>

    <!-- 听力要求盒子 -->
    <view class="instructions-box shadow">
      <text class="instruction-text"
        >请仔细听下面的音频内容，然后根据听到的信息回答问题，请注意控制时间。</text
      >
      <!-- 新增：收藏按钮 -->
      <view
        class="collect-btn"
        v-if="listenStore.questionList.length > 0"
        @click="listenStore.toggleFavorite"
      >
        <uni-icons
          :type="listenStore.isFavorite ? 'star-filled' : 'star'"
          size="20"
          color="#FFCC00"
          class="star-icon"
        ></uni-icons>
      </view>
    </view>

    <!-- 听力播放区域 -->
    <view class="audio-player shadow">
      <view class="progress-container">
        <text class="time">{{ currentTime }}</text>
        <progress
          :percent="progressPercent"
          stroke-width="4"
          activeColor="#6772e5"
          backgroundColor="#E5E9F2"
        ></progress>
        <text class="time">{{ duration }}</text>
      </view>
      <button class="play-button" :class="{ disabled: !audioReady }" @click="togglePlay">
        <uniIcons :type="isPlaying ? 'mic' : 'micoff'" size="30" color="#FFFFFF"></uniIcons>
      </button>
    </view>

    <!-- 抽屉式问题面板 -->
    <view class="drawer-wrapper" :class="{ open: listenStore.drawerOpen }">
      <!-- 题目区域 -->
      <view class="question-area shadow">
        <view class="question-header">
          <text class="question-count"
            >第{{ listenStore.currentQuestionOption.questionNumber }}题 / 共{{
              listenStore.currentQuestionTotalOption
            }}题</text
          >
          <text class="question-text">{{ listenStore.currentQuestionOption.questionContent }}</text>
        </view>
        <view class="options-container">
          <view
            class="option"
            v-for="(opt, index) in listenStore.currentQuestionOption.questionOptions"
            :key="index"
            @click.stop="listenStore.selectOption(String.fromCharCode(65 + index))"
            :class="{ selected: listenStore.selectedOption === String.fromCharCode(65 + index) }"
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
          :disabled="listenStore.currentQuestionOptionIndex === 0"
          @click.stop="listenStore.prevQuestion"
        >
          <uniIcons type="back" size="22"></uniIcons>
        </button>
        <view class="action-group">
          <button class="detail-btn" @click="listenStore.toggleDetails">详情</button>
          <button
            class="submit-btn"
            :class="{ disabled: !allAnswered }"
            :disabled="
              !allAnswered || listenStore.currentQuestionContent === '' || listenStore.isSubmit
            "
            @click.stop="listenStore.submitAnswer"
            v-if="!listenStore.showAnswerButton"
          >
            提交答案
          </button>
          <button class="answer-btn" @click="listenStore.hideResultPopup" v-else>显示结果</button>
        </view>
        <button
          class="nav-btn"
          :disabled="
            listenStore.currentQuestionOptionIndex === listenStore.currentQuestionTotalOption - 1
          "
          @click="listenStore.nextQuestion"
        >
          <uniIcons type="forward" size="22"></uniIcons>
        </button>
      </view>
    </view>

    <!-- 结果弹窗 -->
    <view
      class="result-popup"
      v-if="listenStore.showResultPopup"
      @click.self="listenStore.hideResultPopup"
    >
      <view class="result-card">
        <view class="result-header">
          <text class="result-title">答题详情</text>
        </view>
        <view class="score-info"
          >得分：{{ listenStore.userScore }} / 总分：{{ listenStore.totalScore }}</view
        >
        <view class="result-list">
          <view class="result-item" v-for="(item, index) in listenStore.explanations" :key="index">
            <text class="item-number">第{{ item.questionNumber }}题</text>
            <text
              class="user-ans"
              :class="{ wrong: listenStore.userAnswer[index] !== listenStore.answerText[index] }"
            >
              你的答案：{{ listenStore.userAnswer[index] || '未作答' }}
            </text>
            <text class="correct-ans">正确答案：{{ listenStore.answerText[index] }}</text>
            <text class="explanation">{{ item.questionExplanation }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 详情弹窗 -->
    <view
      class="details-popup"
      v-if="listenStore.showDetails"
      @click.self="listenStore.toggleDetails"
    >
      <view class="details-card">
        <view class="details-header">
          <text class="details-title">答题进度</text>
        </view>
        <view class="progress-dots">
          <view
            class="dot"
            v-for="(_, index) in listenStore.currentQuestionTotalOption"
            :key="index"
            :class="{
              answered: listenStore.userAnswer[index],
              current: index === listenStore.currentQuestionOptionIndex,
            }"
            @click="listenStore.goToQuestion(index)"
          >
            {{ index + 1 }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useQuestionStore } from '@/store'

// 引入仓库并命名为listenStore
const listenStore = useQuestionStore()

const props = defineProps({
  questionId: Number,
  recordId: Number,
  isAIJump: String,
  isHistoryJump: String,
})

// 禁用顶部栏功能
const isDisabled = computed(() => props.isAIJump === 'true' || props.isHistoryJump === 'true')

// 音频控制
const innerAudioContext = uni.createInnerAudioContext()
const isPlaying = ref(false)
const currentTime = ref('00:00')
const duration = ref('00:00')
const progressPercent = ref(0)
const audioReady = ref(false)

// 时间格式化
const formattedSuggestedTime = computed(() => formatTime(listenStore.currentQuestionTime))
const formattedElapsedTime = computed(() => formatTime(listenStore.elapsedTime))

// 判断是否超时
const isOvertime = computed(() => listenStore.elapsedTime >= listenStore.currentQuestionTime)

// 时间格式化（改造后）
function formatTime(seconds) {
  // 关键：先对秒数取整，消除小数
  const intSeconds = Math.floor(seconds)
  const min = Math.floor(intSeconds / 60)
    .toString()
    .padStart(2, '0')
  const sec = (intSeconds % 60).toString().padStart(2, '0')
  return `${min}:${sec}`
}

// 定时器管理
let timer = null

// 开启定时器
const startTimer = () => {
  if (timer) {
    stopTimer()
  }
  timer = setInterval(() => {
    listenStore.elapsedTime++
    // 当花费时间达到建议时长时，添加提示
    if (listenStore.elapsedTime === listenStore.currentQuestionTime) {
      uni.showToast({
        title: '已达建议时长',
        icon: 'none',
        duration: 2000,
      })
    }
  }, 1000)
}

// 关闭定时器
const stopTimer = () => {
  clearInterval(timer)
  timer = null
}

// 初始化音频
const initAudio = () => {
  // 从仓库获取当前音频地址
  innerAudioContext.src = listenStore.currentQuestionAudioUrl[0]

  innerAudioContext.onCanplay(() => {
    audioReady.value = true
    // 获取音频时长
    duration.value = formatTime(innerAudioContext.duration)
  })

  innerAudioContext.onTimeUpdate(() => {
    currentTime.value = formatTime(innerAudioContext.currentTime)
    progressPercent.value = Math.min(
      (innerAudioContext.currentTime / innerAudioContext.duration) * 100,
      100,
    )
  })

  innerAudioContext.onEnded(() => {
    isPlaying.value = false
  })

  innerAudioContext.onError((err) => {
    console.error('Audio error:', err)
    uni.showToast({ title: '音频加载失败', icon: 'none' })
  })
}

// 播放控制
const togglePlay = () => {
  if (!audioReady.value) return

  isPlaying.value = !isPlaying.value
  isPlaying.value ? innerAudioContext.play() : innerAudioContext.pause()
}

// 初始化
onMounted(async () => {
  // 1.获取听力类型
  await listenStore.getSubTypeList('听力')
  await listenStore.getFavoriteList()
  // 2.获取试题
  if (props.questionId) {
    // 从ai助手获取到的questionId(ai跳转)
    await listenStore.getQuestionContentById(props.questionId)
  } else {
    // 从Home页面获取试题
    await listenStore.getQuestionContentList()
  }

  // 3.初始化音频
  initAudio()

  if (props.isHistoryJump === 'true') {
    await listenStore.getRecordById(Number(props.recordId))
  } else {
    // 4.作答计时器
    if (listenStore.questionList.length > 0) {
      startTimer()
    }

    // 5.监听提交状态变化
    watch(
      () => listenStore.isSubmit,
      (newVal) => {
        if (newVal) {
          stopTimer()
          innerAudioContext.pause()
          isPlaying.value = false
        }
      },
    )

    // 6.监听题目变化，重新加载音频
    watch(
      () => listenStore.currentQuestionAudioUrl,
      () => {
        initAudio()
      },
    )
  }
})

onUnmounted(() => {
  listenStore.onExit()
  stopTimer()
  innerAudioContext.destroy()
})

// 选项切换处理
const handleDifficultyChange = (e) => {
  listenStore.selectedDifficulty = listenStore.difficultyList[e.detail.value]
}
const handleTypeChange = (e) => {
  listenStore.selectedType = listenStore.subTypeList[e.detail.value]
}

// 换题
const handleRefreshQuestion = async () => {
  await listenStore.refreshQuestion()
  // 重新加载音频
  initAudio()
  // 不存在题目则不启动定时器
  if (listenStore.questionList.length !== 0) {
    startTimer()
  } else {
    stopTimer()
  }
}

// 提交状态判断
const allAnswered = computed(() => {
  return (
    listenStore.userAnswer.length === listenStore.currentQuestionTotalOption &&
    listenStore.userAnswer.every((ans) => ans)
  )
})
</script>

<style lang="scss" scoped>
/* 全局变量 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$accent-blue: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
$card-bg: rgba(255, 255, 255, 0.98);
$shadow-light: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
$shadow-hover: 0 12rpx 40rpx rgba(102, 126, 234, 0.25);

.listening-page {
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

/* 听力要求盒子 */
.instructions-box {
  padding: 24rpx 32rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.instruction-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  flex: 1;
}

.collect-btn {
  padding: 12rpx;
  margin-left: 16rpx;
  background: transparent;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;

  &:active {
    transform: scale(0.9);
  }
}

.star-icon {
  transition: all 0.3s;
}

/* 听力播放区域 */
.audio-player {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.progress-container {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.time {
  font-size: 22rpx;
  color: #667eea;
  font-weight: 600;
  min-width: 80rpx;
  text-align: center;
  font-family: 'Courier New', monospace;
}

progress {
  flex: 1;
  border-radius: 8rpx;
}

.play-button {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $primary-gradient;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;

  &.disabled {
    background: #c0c4cc;
    cursor: not-allowed;
    box-shadow: none;
  }

  &:active {
    transform: scale(0.9);
  }
}

/* 抽屉式问题面板 */
.drawer-wrapper {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  overflow: hidden;
}

.question-area {
  padding: 32rpx;
  flex: 1;
  overflow-y: auto;
}

.question-header {
  margin-bottom: 32rpx;
}

.question-count {
  font-size: 22rpx;
  color: #888;
  display: block;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.question-text {
  font-size: 30rpx;
  color: #333;
  font-weight: 600;
  line-height: 1.6;
  display: block;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.option {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 16rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.2);
  transition: all 0.3s;
  cursor: pointer;

  &:active {
    transform: scale(0.98);
  }

  &.selected {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
    border-color: #667eea;
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.2);
  }
}

.option-letter {
  font-size: 28rpx;
  color: #667eea;
  font-weight: 700;
  margin-right: 16rpx;
  min-width: 40rpx;
}

.option-content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  flex: 1;
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
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
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
  font-size: 32rpx;
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
</style>
