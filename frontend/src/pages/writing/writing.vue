<template>
  <view class="writing-page">
    <!-- 顶部操作栏 -->
    <view class="top-bar shadow">
      <picker
        mode="selector"
        :range="writingStore.subTypeList"
        @change="handleTypeChange"
        :disabled="isDisabled"
      >
        <view class="selector type-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">题材</text>
          <text class="selector-value">{{
            writingStore.selectedType || writingStore.currentQuestionType
          }}</text>
        </view>
      </picker>
      <button class="refresh-btn" @click.stop="handleRefreshQuestion" :disabled="isDisabled">
        换一道
      </button>
      <picker
        mode="selector"
        :range="writingStore.difficultyList"
        @change="handleDifficultyChange"
        :disabled="isDisabled"
      >
        <view class="selector difficulty-selector" :class="{ disabled: isDisabled }">
          <text class="selector-label">难度</text>
          <text class="selector-value">{{
            writingStore.selectedDifficulty || writingStore.currentQuestionDifficulty
          }}</text>
        </view>
      </picker>
    </view>

    <!-- 中间功能栏：与阅读界面保持一致 -->
    <view class="middle-bar shadow">
      <view class="time-box left">
        <text class="time-label">建议时长</text>
        <text class="time-value">{{ formattedSuggestedTime }}</text>
      </view>

      <button
        class="drawer-toggle"
        :class="{ active: writingStore.drawerOpen }"
        @click.stop="writingStore.toggleDrawer"
      >
        {{ writingStore.drawerOpen ? '隐藏题目' : '显示题目' }}
      </button>

      <view class="time-box right" :class="{ overtime: isOvertime }">
        <text class="time-label">花费时长</text>
        <text class="time-value">{{ formattedElapsedTime }}</text>
      </view>
    </view>

    <!-- 写作输入区域 -->
    <view class="writing-container">
      <view class="answer-box shadow">
        <textarea
          class="writing-input"
          v-model="writingStore.userWriting"
          placeholder="请输入你的作文..."
          auto-height
          :maxlength="-1"
          @input="handleInput"
        />
        <!-- 答题盒子末尾显示的简洁字数统计 -->
        <view class="word-count-badge" v-if="writingStore.userWriting.trim().length > 0">
          <text class="count-text"
            >{{ wordCount }}/{{ writingStore.currentQuestionMinWordCount }}</text
          >
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-container">
        <button
          class="submit-button"
          @click="writingStore.submitAnswer"
          :disabled="
            !writingStore.userWriting.trim() ||
            writingStore.currentQuestionContent === '' ||
            writingStore.isSubmit
          "
          v-if="!writingStore.showAnswerButton"
        >
          提交练习
        </button>
        <button class="answer-button" @click="writingStore.hideResultPopup" v-else>显示结果</button>
      </view>
    </view>

    <!-- 抽屉式题目面板 -->
    <view class="drawer-wrapper" :class="{ open: writingStore.drawerOpen }">
      <view class="prompt-area shadow">
        <view class="prompt-header">
          <text class="prompt-type">
            {{ writingStore.currentQuestionType }} · {{ writingStore.currentQuestionDifficulty }} ·
            {{ writingStore.currentQuestionAuthor }}
          </text>
        </view>

        <!-- 图片展示区域 -->
        <view
          class="question-image-container"
          v-if="writingStore.currentQuestionAudioUrl.length > 0"
        >
          <view
            class="image-grid"
            :class="{
              'single-image': writingStore.currentQuestionAudioUrl.length === 1,
              'multi-images': writingStore.currentQuestionAudioUrl.length > 1,
            }"
          >
            <view
              class="image-item"
              v-for="(imageUrl, index) in writingStore.currentQuestionAudioUrl"
              :key="index"
              @click="previewQuestionImage(index)"
            >
              <image :src="imageUrl" mode="aspectFill" class="question-image" />
              <view class="image-index" v-if="writingStore.currentQuestionAudioUrl.length > 1">{{
                index + 1
              }}</view>
            </view>
          </view>
          <text class="image-tip">点击查看大图</text>
        </view>

        <scroll-view
          class="prompt-scroll"
          scroll-y
          show-scrollbar
          :style="{ height: 'calc(100% - 40px)' }"
        >
          <view class="prompt-content">
            <text class="prompt-details" v-if="writingStore.currentQuestionContent">
              {{ writingStore.currentQuestionContent }}
            </text>
          </view>
        </scroll-view>
        <!-- 新增：收藏按钮 -->
        <view
          class="collect-btn"
          v-if="writingStore.questionList.length > 0"
          @click="writingStore.toggleFavorite"
        >
          <uni-icons
            :type="writingStore.isFavorite ? 'star-filled' : 'star'"
            size="20"
            color="#FFCC00"
            class="star-icon"
          ></uni-icons>
        </view>
      </view>
    </view>

    <!-- 双页签结果弹窗 -->
    <view class="result-popup" v-if="writingStore.showResultPopup" @click="handlePopupClick">
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
            参考范文
          </view>
          <view
            class="tab-item"
            :class="{ active: activeTab === 'analysis' }"
            @click="handleTabClick('analysis')"
          >
            答案解析
          </view>
        </view>

        <!-- 参考范文页签 -->
        <view class="tab-content" v-if="activeTab === 'sample'">
          <!-- 写作技巧 -->
          <view class="writing-tips" v-if="writingStore.tips">
            <text class="tips-title">写作技巧：</text>
            <text class="tips-content article-paragraph">{{ writingStore.tips || 无 }}</text>
          </view>

          <!-- 参考范文 -->
          <view class="sample-answer" v-if="writingStore.translateORWriteAnswer">
            <text class="sample-title">参考范文：</text>
            <text
              class="sample-content article-paragraph"
              :class="{ letter: isLetterType }"
              style="white-space: pre-line"
            >
              {{ formatLetterContent(writingStore.translateORWriteAnswer) }}
            </text>
          </view>
        </view>

        <!-- 答案解析页签 -->
        <view class="tab-content" v-else>
          <view class="score-info"
            >得分：{{ writingStore.userScore }} / 总分：{{ writingStore.totalScore }}</view
          >
          <view class="analysis-content" v-if="writingStore.translateORWriteAnalysis">
            <text class="analysis-title">解析：</text>
            <text class="analysis-text">{{ writingStore.translateORWriteAnalysis }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useQuestionStore } from '@/store'

const writingStore = useQuestionStore()
const activeTab = ref('sample')

// 检测是否为书信类题目
const isLetterType = computed(() => {
  return /信/.test(writingStore.currentQuestionType)
})

// 书信类内容格式化（增强版）
const formatLetterContent = (content) => {
  if (!isLetterType.value) return content

  let lines = content.split('\n').filter((line) => line.trim() !== '')

  // 确保有Dear开头
  if (lines.length === 0 || !lines[0].trim().startsWith('Dear')) {
    lines.unshift('Dear [Name],')
  }

  // 确保有标准结尾
  if (lines.length > 0 && !lines[lines.length - 1].trim().startsWith('Yours,')) {
    lines.push('Yours sincerely,')
    lines.push('[Your Name]')
  }

  return lines.join('\n')
}

// 替代closest的兼容方法
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
    writingStore.showResultPopup = false
  }
}

// 页签点击处理（阻止冒泡）
const handleTabClick = (tabName) => {
  activeTab.value = tabName
}

// 选项切换处理
const handleDifficultyChange = (e) => {
  writingStore.selectedDifficulty = writingStore.difficultyList[e.detail.value]
}
const handleTypeChange = (e) => {
  writingStore.selectedType = writingStore.subTypeList[e.detail.value]
}

// 换题
const handleRefreshQuestion = async () => {
  await writingStore.refreshQuestion()
  if (writingStore.questionList.length !== 0) {
    startTimer()
  } else {
    stopTimer()
  }
}

// 时间格式化
const formattedSuggestedTime = computed(() => formatTime(writingStore.currentQuestionTime))
const formattedElapsedTime = computed(() => formatTime(writingStore.elapsedTime))
const isOvertime = computed(() => writingStore.elapsedTime >= writingStore.currentQuestionTime)

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
    writingStore.elapsedTime++
    if (writingStore.elapsedTime === writingStore.currentQuestionTime) {
      uni.showToast({ title: '已达建议时长', icon: 'none', duration: 2000 })
    }
  }, 1000)
}
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

// 生命周期
onMounted(async () => {
  await writingStore.getSubTypeList('写作')
  await writingStore.getFavoriteList()
  if (props.questionId) {
    await writingStore.getQuestionContentById(props.questionId)
  } else {
    await writingStore.getQuestionContentList()
  }

  if (props.isHistoryJump === 'true') {
    await writingStore.getRecordById(Number(props.recordId))
  } else {
    if (writingStore.questionList.length > 0) startTimer()

    watch(
      () => writingStore.isSubmit,
      (newVal) => {
        if (newVal) {
          stopTimer()
          writingStore.showAnswerButton = true
        }
      },
    )
  }
})

onUnmounted(() => {
  writingStore.onExit()
  stopTimer()
})

// 字数统计
const wordCount = computed(() => {
  if (!writingStore.userWriting.trim()) return 0
  const separators = /[\s.,;:!?'"()[\]{}<>\\/+-=*&^%$#@~`|]+/
  const words = writingStore.userWriting.trim().split(separators)
  return words.filter((word) => word.length > 0).length
})

// 预览题目图片
const previewQuestionImage = (currentIndex) => {
  uni.previewImage({
    urls: writingStore.currentQuestionAudioUrl,
    current: writingStore.currentQuestionAudioUrl[currentIndex],
    indicator: 'number',
  })
}
</script>

<style lang="scss" scoped>
/* 全局变量，统一四页样式 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$accent-green: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
$card-bg: rgba(255, 255, 255, 0.98);
$shadow-light: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
$shadow-hover: 0 12rpx 40rpx rgba(102, 126, 234, 0.25);

.writing-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f8f9ff 100%);
  overflow: hidden;
  padding: 16rpx;
  box-sizing: border-box;
  gap: 16rpx;
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

/* 写作输入区域容器：核心修改点 */
.writing-container {
  flex: 1; /* 占据顶部和中间栏后的剩余空间 */
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden; /* 禁止容器整体滚动，避免挤压抽屉 */
}

/* 作答盒子：核心修改点 */
.answer-box {
  flex: 1; /* 占据writing-container的全部剩余高度 */
  display: flex;
  flex-direction: column;
  max-width: 100%;
  position: relative;
  overflow-y: auto; /* 内容超出时内部滚动（仅作答区域滚动） */
}

.writing-input {
  flex: 1;
  width: 100%;
  padding: 16px;
  font-size: 16px;
  color: var(--gray);
  line-height: 1.8;
  border-radius: 16px;
  word-wrap: break-word;
  word-break: break-all;
  background-color: var(--white);
  box-sizing: border-box;
  padding-bottom: 40px; /* 预留字数统计空间 */
  text-align: justify;
  overflow-wrap: break-word;
  display: block;
}

/* 简洁的字数统计显示 */
.word-count-badge {
  position: absolute;
  bottom: 12px;
  right: 16px;
  font-size: 13px;
  color: var(--primary);
  background: rgba(103, 114, 229, 0.1);
  padding: 4px 8px;
  border-radius: 12px;
}

/* 提交按钮 */
.submit-container {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.submit-button,
.answer-button {
  font-size: 16px;
  padding: 10px 30px;
  background: var(--primary);
  color: var(--white);
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(103, 114, 229, 0.2);
  transition: all 0.3s;
  margin: 0 8px;
}

.submit-button:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
  box-shadow: none;
}

/* 抽屉式题目面板：固定底部，默认隐藏 */
.drawer-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  background: var(--white);
  transform: translateY(100%); /* 默认隐藏（滑出屏幕外） */
  transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.27, 1.55);
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  box-shadow: 0 -6px 20px rgba(0, 0, 0, 0.08);
  z-index: 998;
}

.drawer-wrapper.open {
  transform: translateY(0); /* 打开时滑入屏幕 */
}

/* 题目区域 */
.prompt-area {
  padding: 16px;
  min-height: 160px;
}

.prompt-header {
  margin-bottom: 16px;
}

.prompt-type {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  display: block;
}

.prompt-details {
  font-size: 14px;
  color: var(--gray);
  line-height: 1.8;
  word-break: normal; /* 改为normal，不强制拆分单词 */
  text-align: justify;
  text-justify: inter-word; /* 保持单词间对齐，不拆分单词 */
  text-indent: 2em;
  overflow-wrap: break-word; /* 只在必要时换行 */
  word-wrap: break-word; /* 兼容性 */
  hyphens: none; /* 禁用连字符 */
  display: block;
}

.prompt-scroll {
  padding-right: 8px;
}

/* 新增：收藏按钮样式 */
.collect-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  z-index: 2; /* 确保在文章内容上方 */
  cursor: pointer;
  background: rgba(255, 255, 255, 0.8); /* 半透明背景 */
  padding: 5px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}
.collect-btn:hover {
  transform: scale(1.1);
}

/* 双页签结果弹窗核心样式 */
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
  max-width: 500px;
  background: var(--white);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  position: relative;
}

/* 头部：标题 */
.result-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}
.result-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--gray);
}

/* 页签切换栏 */
.tab-bar {
  display: flex;
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 15px;
  color: var(--gray);
  cursor: pointer;
  transition: color 0.3s;
}
.tab-item.active {
  color: var(--primary);
  border-bottom: 2px solid var(--primary);
}

/* 内容区域通用样式 */
.tab-content {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 12px; /* 解决滚动条遮挡文字问题 */
}

/* 标准段落样式（写作技巧、范文） */
.article-paragraph {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 16px;
  text-align: justify; /* 两端对齐 */
  text-justify: inter-word; /* 单词不拆分 */
  overflow-wrap: break-word;
  display: block;
}

/* 写作技巧样式 */
.writing-tips {
  margin-bottom: 16px;
}
.tips-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gray);
  margin-bottom: 8px;
  display: block;
}

/* 参考范文样式 */
.sample-answer {
  margin-bottom: 16px;
}
.sample-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gray);
  margin-bottom: 8px;
  display: block;
}
.sample-content {
  white-space: pre-line; /* 保留换行 */
}
/* 书信类特殊排版 */
.sample-content.letter {
  text-indent: 0; /* 取消整体缩进 */
}
.sample-content.letter line:first-child {
  text-indent: 0; /* 首行（Dear...）顶格 */
}
.sample-content.letter line:not(:first-child):not(:last-child):not(:nth-last-child(2)) {
  text-indent: 2em; /* 正文段落首行缩进 */
}
.sample-content.letter line:nth-last-child(2),
.sample-content.letter line:last-child {
  text-align: right; /* 结尾两行右对齐 */
}

/* 答案解析样式（无首行缩进） */
.analysis-content {
  margin-bottom: 16px;
}
.analysis-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gray);
  margin-bottom: 8px;
  display: block;
}
.analysis-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  text-align: justify;
  text-justify: inter-word;
  text-indent: 0; /* 解析取消首行缩进 */
  overflow-wrap: break-word;
  display: block;
}
.score-info {
  font-size: 14px;
  color: #999;
  margin-bottom: 16px;
  display: block;
}

/* 题目图片容器 */
.question-image-container {
  margin: 16px 0;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  text-align: center;
}

/* 图片网格布局 */
.image-grid {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.image-grid.single-image {
  justify-content: center;
}

.image-grid.single-image .image-item {
  width: 100%;
  max-width: 300px;
}

.image-grid.multi-images .image-item {
  width: calc(50% - 6px);
  max-width: 140px;
}

/* 图片项容器 */
.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
  cursor: pointer;
}

.image-item:active {
  transform: scale(0.98);
}

/* 图片样式 */
.question-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
}

.image-grid.single-image .question-image {
  height: 180px;
}

/* 图片序号标识 */
.image-index {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
}

/* 提示文字 */
.image-tip {
  display: block;
  margin-top: 12px;
  font-size: 12px;
  color: #6c757d;
  font-style: italic;
}
</style>
