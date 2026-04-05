<template>
  <view class="translation-page">
    <!-- 顶部操作栏 -->
    <view class="top-bar shadow">
      <picker
        mode="selector"
        :range="translateStore.subTypeList"
        @change="handleTypeChange"
        :disabled="isDisabled"
      >
        <view class="selector type-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">题材</text>
          <text class="selector-value">{{
            translateStore.selectedType || translateStore.currentQuestionType
          }}</text>
        </view>
      </picker>
      <button class="refresh-btn" @click.stop="handleRefreshQuestion" :disabled="isDisabled">
        换一道
      </button>
      <picker
        mode="selector"
        :range="translateStore.difficultyList"
        @change="handleDifficultyChange"
        :disabled="isDisabled"
      >
        <view class="selector difficulty-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">难度</text>
          <text class="selector-value">{{
            translateStore.selectedDifficulty || translateStore.currentQuestionDifficulty
          }}</text>
        </view>
      </picker>
    </view>

    <!-- 中间功能栏：两个时间对称分布 -->
    <view class="middle-bar shadow">
      <view class="time-box left">
        <text class="time-label">建议时长</text>
        <text class="time-value">{{ formattedSuggestedTime }}</text>
      </view>

      <view class="time-box right" :class="{ overtime: isOvertime }">
        <text class="time-label">花费时长</text>
        <text class="time-value">{{ formattedElapsedTime }}</text>
      </view>
    </view>

    <!-- 主内容区域：固定高度的题目和作答区域 -->
    <view class="main-content">
      <!-- 题目盒子：高度固定，内容超出时内部滚动 -->
      <view class="question-box shadow">
        <scroll-view class="question-scroll" scroll-y show-scrollbar>
          <view class="question-content">
            <text class="question-text">{{ translateStore.currentQuestionContent }}</text>
          </view>
        </scroll-view>
        <!-- 新增：收藏按钮 -->
        <view
          class="collect-btn"
          v-if="translateStore.questionList.length > 0"
          @click="translateStore.toggleFavorite"
        >
          <uni-icons
            :type="translateStore.isFavorite ? 'star-filled' : 'star'"
            size="20"
            color="#FFCC00"
            class="star-icon"
          ></uni-icons>
        </view>
      </view>

      <!-- 翻译输入盒子：高度固定，内容超出时内部滚动 -->
      <view class="translation-box shadow">
        <textarea
          class="translation-input"
          v-model="translateStore.userWriting"
          placeholder="请输入您的翻译..."
          :maxlength="-1"
          @input="handleInput"
        />
      </view>

      <!-- 提交按钮 -->
      <view class="submit-container">
        <button
          class="submit-button"
          @click="translateStore.submitAnswer"
          :disabled="
            !translateStore.userWriting.trim() ||
            translateStore.currentQuestionContent === '' ||
            translateStore.isSubmit
          "
          v-if="!translateStore.showAnswerButton"
        >
          提交答案
        </button>
        <button class="answer-button" @click="translateStore.showResultPopup = true" v-else>
          显示结果
        </button>
      </view>
    </view>

    <!-- 双页签结果弹窗 -->
    <view class="result-popup" v-if="translateStore.showResultPopup" @click="handlePopupClick">
      <view class="result-card" @click.stop>
        <!-- 头部：标题 -->
        <view class="result-header">
          <text class="result-title">答题详情</text>
        </view>

        <!-- 页签切换栏 -->
        <view class="tab-bar">
          <view
            class="tab-item"
            :class="{ active: activeTab === 'sample' }"
            @click="handleTabClick('sample')"
          >
            参考翻译
          </view>
          <view
            class="tab-item"
            :class="{ active: activeTab === 'analysis' }"
            @click="handleTabClick('analysis')"
          >
            答案解析
          </view>
        </view>

        <!-- 参考翻译页签 -->
        <view class="tab-content" v-if="activeTab === 'sample'">
          <view class="translate-tips" v-if="translateStore.tips">
            <text class="tips-title">翻译技巧：</text>
            <text class="tips-content article-paragraph">{{ translateStore.tips }}</text>
          </view>

          <view class="sample-answer" v-if="translateStore.translateORWriteAnswer">
            <text class="sample-title">参考翻译：</text>
            <text class="sample-content article-paragraph" style="white-space: pre-line">
              {{ translateStore.translateORWriteAnswer }}
            </text>
          </view>
        </view>

        <!-- 答案解析页签 -->
        <view class="tab-content" v-else>
          <view class="score-info"
            >得分：{{ translateStore.userScore }} / 总分：{{ translateStore.totalScore }}</view
          >
          <view class="analysis-content" v-if="translateStore.translateORWriteAnalysis">
            <text class="analysis-title">解析：</text>
            <text class="analysis-text">{{ translateStore.translateORWriteAnalysis }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useQuestionStore } from '@/store'

const translateStore = useQuestionStore()
const activeTab = ref('sample')

const props = defineProps({
  questionId: Number,
  recordId: Number,
  isAIJump: String,
  isHistoryJump: String,
})

// 禁用顶部栏功能
const isDisabled = computed(() => props.isAIJump === 'true' || props.isHistoryJump === 'true')

// 替代 closest 的兼容方法
function isInside(target, className) {
  let el = target
  while (el) {
    if (el.classList && el.classList.contains(className)) {
      return true
    }
    el = el.parentElement
  }
  return false
}

// 弹窗点击处理
const handlePopupClick = (e) => {
  if (!isInside(e.target, 'result-card')) {
    translateStore.showResultPopup = false
  }
}

// 页签点击处理
const handleTabClick = (tabName) => {
  activeTab.value = tabName
}

// 选项切换处理
const handleDifficultyChange = (e) => {
  translateStore.selectedDifficulty = translateStore.difficultyList[e.detail.value]
}
const handleTypeChange = (e) => {
  translateStore.selectedType = translateStore.subTypeList[e.detail.value]
}

// 换题
const handleRefreshQuestion = async () => {
  await translateStore.refreshQuestion()
  if (translateStore.questionList.length !== 0) {
    startTimer()
  } else {
    stopTimer()
  }
}

// 时间格式化
const formattedSuggestedTime = computed(() => formatTime(translateStore.currentQuestionTime))
const formattedElapsedTime = computed(() => formatTime(translateStore.elapsedTime))
const isOvertime = computed(() => translateStore.elapsedTime >= translateStore.currentQuestionTime)

function formatTime(seconds) {
  const min = Math.floor(seconds / 60)
    .toString()
    .padStart(2, '0')
  const sec = (seconds % 60).toString().padStart(2, '0')
  return `${min}:${sec}`
}

// 定时器管理
let timer = null
const startTimer = () => {
  if (timer) stopTimer()
  timer = setInterval(() => {
    translateStore.elapsedTime++
    if (translateStore.elapsedTime === translateStore.currentQuestionTime) {
      uni.showToast({ title: '已达建议时长', icon: 'none', duration: 2000 })
    }
  }, 1000)
}
const stopTimer = () => {
  clearInterval(timer)
  timer = null
}

// 初始化
onMounted(async () => {
  await translateStore.getSubTypeList('翻译')
  await translateStore.getFavoriteList()
  if (props.questionId) {
    await translateStore.getQuestionContentById(props.questionId)
  } else {
    await translateStore.getQuestionContentList()
  }

  if (props.isHistoryJump === 'true') {
    await translateStore.getRecordById(Number(props.recordId))
  } else {
    if (translateStore.questionList.length > 0) {
      startTimer()
    }
    watch(
      () => translateStore.isSubmit,
      (newVal) => {
        if (newVal) {
          stopTimer()
          translateStore.showAnswerButton = true
        }
      },
    )
  }
})

onUnmounted(() => {
  translateStore.onExit()
  stopTimer()
})

// 输入处理
const handleInput = () => {
  // 可选：添加字数统计逻辑
}
</script>

<style lang="scss" scoped>
/* 全局变量 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$accent-orange: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
$card-bg: rgba(255, 255, 255, 0.98);
$shadow-light: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
$shadow-hover: 0 12rpx 40rpx rgba(102, 126, 234, 0.25);

.translation-page {
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
  flex-shrink: 0;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);

  &:disabled {
    background: #c0c4cc;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
  }
}

/* 中间功能栏 */
.middle-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  height: 64rpx;
}

.time-box {
  flex: 1;
  max-width: 160rpx;
  padding: 12rpx;
  text-align: center;
  border-radius: 16rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));

  &.right {
    background: linear-gradient(135deg, rgba(245, 108, 108, 0.08), rgba(246, 134, 86, 0.08));

    &.overtime .time-value {
      color: #f56c6c;
      animation: pulse 1.5s infinite;
    }
  }
}

.time-label {
  font-size: 20rpx;
  color: #999;
  display: block;
  margin-bottom: 6rpx;
  font-weight: 400;
}

.time-value {
  font-size: 26rpx;
  font-weight: bold;
  color: #667eea;
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

/* 题目盒子 */
.question-box {
  height: 35vh;
  overflow: hidden;
  position: relative;
}

.question-scroll {
  width: 100%;
  height: 100%;
  padding: 24rpx;
  box-sizing: border-box;
}

.question-content {
  width: 100%;
}

.question-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
  text-align: justify;
  text-indent: 2em;
  overflow-wrap: break-word;
  display: block;
}

/* 新增：收藏按钮样式 */
.collect-btn {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  z-index: 2;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.8);
  padding: 10rpx;
  border-radius: 50%;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;

  &:hover {
    transform: scale(1.1);
  }
}

/* 翻译输入盒子 */
.translation-box {
  height: 35vh;
  overflow: hidden;
  position: relative;
}

.translation-input {
  width: 100%;
  height: 100%;
  padding: 24rpx;
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
  border-radius: 24rpx;
  word-wrap: break-word;
  word-break: break-all;
  background-color: $card-bg;
  box-sizing: border-box;
  text-align: justify;
  overflow-wrap: break-word;
  overflow-y: auto;
}

/* 提交按钮 */
.submit-container {
  display: flex;
  justify-content: center;
  padding: 16rpx 0;
}

.submit-button,
.answer-button {
  font-size: 28rpx;
  padding: 16rpx 48rpx;
  background: $primary-gradient;
  color: #fff;
  border-radius: 32rpx;
  box-shadow: 0 6rpx 20rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 8rpx 28rpx rgba(102, 126, 234, 0.4);
  }
}

.submit-button:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

/* 双页签结果弹窗 */
.result-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.result-card {
  width: 90%;
  max-width: 600rpx;
  background: $card-bg;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.15);
  animation: modalEnter 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes modalEnter {
  from {
    opacity: 0;
    transform: scale(0.8) translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.result-header {
  display: flex;
  justify-content: center;
  margin-bottom: 24rpx;
}

.result-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tab-bar {
  display: flex;
  border-bottom: 2rpx solid #eee;
  margin-bottom: 24rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  color: #999;
  cursor: pointer;
  transition: all 0.3s;

  &.active {
    color: #667eea;
    border-bottom: 4rpx solid #667eea;
    font-weight: 600;
  }
}

.tab-content {
  max-height: 400rpx;
  overflow-y: auto;
  padding-right: 16rpx;
}

.article-paragraph {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 24rpx;
  text-indent: 2em;
  text-align: justify;
  overflow-wrap: break-word;
  display: block;
}

.translate-tips,
.sample-answer,
.analysis-content {
  margin-bottom: 24rpx;
}

.tips-title,
.sample-title,
.analysis-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
  display: block;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.analysis-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  text-align: justify;
  text-indent: 0;
  overflow-wrap: break-word;
  display: block;
}

.score-info {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 24rpx;
  display: block;
  text-align: center;
}
</style>
