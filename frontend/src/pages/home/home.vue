<template>
  <view class="container" :class="{ 'dark-mode': isDarkMode }">
    <!-- 背景装饰元素 -->
    <view class="background-decoration">
      <view class="decoration-circle circle-1"></view>
      <view class="decoration-circle circle-2"></view>
      <view class="decoration-circle circle-3"></view>
    </view>

    <!-- 顶部标题区 -->
    <view class="header">
      <view class="title-wrapper">
        <text class="title">英语学习助手</text>
        <text class="subtitle">让学习更高效，让进步更明显</text>
      </view>
      <!-- 日夜模式切换按钮 -->
      <view class="theme-toggle" @click="toggleTheme">
        <uni-icons :type="isDarkMode ? 'sun' : 'moon'" size="24" color="#667eea"></uni-icons>
      </view>
    </view>

    <!-- 每日一句卡片 -->
    <view class="daily-quote-card">
      <view class="quote-icon">
        <uni-icons type="star" size="20" color="#667eea"></uni-icons>
      </view>
      <view class="quote-content">
        <text class="quote-text">{{ dailyQuote.en }}</text>
        <text class="quote-trans">{{ dailyQuote.cn }}</text>
      </view>
      <view class="quote-action" @click="refreshQuote">
        <uni-icons type="refresh" size="20" color="#667eea"></uni-icons>
      </view>
    </view>

    <!-- 功能导航区 -->
    <view class="category-container">
      <!-- 听力 -->
      <navigator url="/pages/listening/listening?isHomeJump=true" class="category-item">
        <view class="item-wrapper">
          <view class="icon-circle listening-bg">
            <image src="/static/images/listening.png" mode="aspectFit" class="category-icon" />
          </view>
          <view class="icon-glow listening-glow"></view>
        </view>
        <text class="category-text">听力训练</text>
        <text class="category-desc">磨耳朵，练听力</text>
      </navigator>

      <!-- 阅读 -->
      <navigator url="/pages/reading/reading?isHomeJump=true" class="category-item">
        <view class="item-wrapper">
          <view class="icon-circle reading-bg">
            <image src="/static/images/reading.png" mode="aspectFill" class="category-icon" />
          </view>
          <view class="icon-glow reading-glow"></view>
        </view>
        <text class="category-text">阅读理解</text>
        <text class="category-desc">读文章，提速度</text>
      </navigator>

      <!-- 翻译 -->
      <navigator url="/pages/translation/translation?isHomeJump=true" class="category-item">
        <view class="item-wrapper">
          <view class="icon-circle translation-bg">
            <image src="/static/images/translation.png" mode="aspectFill" class="category-icon" />
          </view>
          <view class="icon-glow translation-glow"></view>
        </view>
        <text class="category-text">翻译练习</text>
        <text class="category-desc">中英互译，练表达</text>
      </navigator>

      <!-- 写作 -->
      <navigator url="/pages/writing/writing?isHomeJump=true" class="category-item">
        <view class="item-wrapper">
          <view class="icon-circle writing-bg">
            <image src="/static/images/writing.png" mode="aspectFill" class="category-icon" />
          </view>
          <view class="icon-glow writing-glow"></view>
        </view>
        <text class="category-text">写作提升</text>
        <text class="category-desc">写文章，学表达</text>
      </navigator>
    </view>

    <!-- 底部版权区 -->
    <view class="footer">
      <text class="footer-text">© 2026 英语学习助手 | 提升英语能力</text>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'
import { ref, reactive } from 'vue'

// 日夜模式
const isDarkMode = ref(false)

// 每日一句
const dailyQuote = reactive({
  en: 'Practice makes perfect.',
  cn: '熟能生巧。',
})

// 名言数据
const quotes = [
  { en: 'Practice makes perfect', cn: '熟能生巧' },
  { en: 'Where there is a will, there is a way', cn: '有志者，事竟成' },
  { en: 'Rome was not built in a day', cn: '罗马不是一天建成的' },
  { en: 'Knowledge is power', cn: '知识就是力量' },
  { en: 'Actions speak louder than words', cn: '行胜于言' },
  { en: 'No pain, no gain', cn: '一分耕耘，一分收获' },
  { en: 'Time and tide wait for no man', cn: '时不我待' },
]

// 切换主题
const toggleTheme = () => {
  isDarkMode.value = !isDarkMode.value
  uni.setStorageSync('isDarkMode', isDarkMode.value)
}

// 刷新名言
const refreshQuote = () => {
  const randomIndex = Math.floor(Math.random() * quotes.length)
  dailyQuote.en = quotes[randomIndex].en
  dailyQuote.cn = quotes[randomIndex].cn
}

onLoad(() => {
  // 禁止页面滚动（小程序兼容方案）
  uni.pageScrollTo({ scrollTop: 0, duration: 0 })

  // 加载主题设置
  const savedTheme = uni.getStorageSync('isDarkMode')
  if (savedTheme !== undefined) {
    isDarkMode.value = savedTheme
  }

  // 随机选择一句名言
  refreshQuote()
})
</script>

<style lang="scss" scoped>
/* 全局样式变量 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$card-bg: rgba(255, 255, 255, 0.95);
$shadow-light: 0 8rpx 32rpx rgba(102, 126, 234, 0.12);
$shadow-hover: 0 16rpx 48rpx rgba(102, 126, 234, 0.2);

/* 主题色 */
$listening-gradient: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
$reading-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
$translation-gradient: linear-gradient(135deg, #4facfe 0%, #43e97b 100%);
$writing-gradient: linear-gradient(135deg, #fa709a 0%, #fee140 100%);

/* 日夜模式变量 */
$dark-bg: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
$dark-card-bg: rgba(30, 30, 50, 0.8);
$dark-text: #e0e0e0;
$dark-subtitle: #a0a0a0;

/* 页面容器 */
.container {
  height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f0f2ff 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transition: all 0.5s ease;

  &.dark-mode {
    background: $dark-bg;

    .header {
      .title {
        background: linear-gradient(135deg, #a8c0ff, #3f2b96, #a8c0ff);
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      .subtitle {
        color: $dark-subtitle;
      }
    }

    .category-item {
      background: $dark-card-bg;
      border-color: rgba(255, 255, 255, 0.1);
    }

    .category-text {
      color: $dark-text;
    }

    .daily-quote-card {
      background: $dark-card-bg;
      border-color: rgba(255, 255, 255, 0.1);

      .quote-text {
        color: $dark-text;
      }

      .quote-trans {
        color: $dark-subtitle;
      }
    }

    .footer-text {
      color: $dark-subtitle;
    }
  }
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.3;
    filter: blur(60rpx);
  }

  .circle-1 {
    width: 400rpx;
    height: 400rpx;
    background: linear-gradient(135deg, rgba(79, 172, 254, 0.2), rgba(0, 242, 254, 0.1));
    top: -100rpx;
    right: -100rpx;
    animation: float 8s ease-in-out infinite;
  }

  .circle-2 {
    width: 300rpx;
    height: 300rpx;
    background: linear-gradient(135deg, rgba(240, 147, 251, 0.2), rgba(245, 87, 108, 0.1));
    bottom: 20%;
    left: -80rpx;
    animation: float 6s ease-in-out infinite reverse;
  }

  .circle-3 {
    width: 250rpx;
    height: 250rpx;
    background: linear-gradient(135deg, rgba(79, 172, 254, 0.15), rgba(67, 233, 123, 0.1));
    bottom: 10%;
    right: 10%;
    animation: float 7s ease-in-out infinite 1s;
  }
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30rpx, -30rpx) scale(1.05);
  }
  66% {
    transform: translate(-20rpx, 20rpx) scale(0.95);
  }
}

/* 顶部标题区 */
.header {
  padding: 40rpx 30rpx 16rpx;
  text-align: center;
  flex-shrink: 0;
  position: relative;
  z-index: 1;

  .title-wrapper {
    position: relative;
    display: inline-block;
  }

  .title {
    font-size: 44rpx;
    font-weight: 800;
    background: linear-gradient(135deg, #667eea, #764ba2, #667eea);
    background-size: 200% auto;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 6rpx;
    display: block;
    margin-bottom: 12rpx;
    animation: shimmer 3s linear infinite;
  }

  .subtitle {
    font-size: 22rpx;
    color: #888;
    font-weight: 400;
    letter-spacing: 2rpx;
    display: block;
  }

  .theme-toggle {
    position: absolute;
    top: 10rpx;
    right: 0;
    width: 50rpx;
    height: 50rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(20rpx);
    border-radius: 50%;
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.2);
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: pulse 2s ease-in-out infinite;

    &:active {
      transform: scale(0.9);
    }
  }
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}

/* 每日一句卡片 */
.daily-quote-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20rpx);
  margin: 8rpx 30rpx;
  padding: 14rpx 18rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.15);
  border: 2rpx solid rgba(102, 126, 234, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideDown 0.6s ease-out;
  text-align: center;
  flex-shrink: 0;

  &:active {
    transform: scale(0.98);
  }

  .quote-icon {
    width: 32rpx;
    height: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
    border-radius: 50%;
    margin-bottom: 8rpx;
    flex-shrink: 0;
  }

  .quote-content {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    align-items: center;
  }

  .quote-text {
    font-size: 24rpx;
    font-weight: 600;
    color: #333;
    line-height: 1.4;
    word-break: break-all;
  }

  .quote-trans {
    font-size: 20rpx;
    color: #999;
    line-height: 1.3;
  }

  .quote-action {
    margin-top: 8rpx;
    width: 32rpx;
    height: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
    border-radius: 50%;
    cursor: pointer;
    transition: all 0.3s ease;

    &:active {
      transform: rotate(180deg) scale(0.9);
    }
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-40rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 功能导航容器 */
.category-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  padding: 16rpx 30rpx;
  flex: 1;
  position: relative;
  z-index: 1;
}

/* 功能项卡片 - 增强动画效果 */
.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32rpx 16rpx;
  background: $card-bg;
  backdrop-filter: blur(20rpx);
  border-radius: 32rpx;
  box-shadow: $shadow-light;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: visible;
  border: 2rpx solid rgba(255, 255, 255, 0.8);

  // 光晕流动效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 32rpx;
    background: conic-gradient(
      from 0deg,
      transparent,
      rgba(102, 126, 234, 0.1),
      rgba(118, 75, 162, 0.1),
      rgba(102, 126, 234, 0.1),
      transparent
    );
    opacity: 0;
    transition: opacity 0.5s ease;
    pointer-events: none;
    animation: rotateGlow 3s linear infinite paused;
  }

  &:hover {
    &::before {
      animation-play-state: running;
      opacity: 1;
    }
  }

  &:active {
    transform: translateY(-12rpx) scale(0.98);
    box-shadow: $shadow-hover;

    .icon-circle {
      transform: scale(1.05) rotate(5deg);
    }

    .category-text {
      transform: translateY(-4rpx);
    }
  }
}

@keyframes rotateGlow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 图标包装器 */
.item-wrapper {
  position: relative;
  margin-bottom: 28rpx;
  animation: bounceIn 0.8s ease-out backwards;

  .category-item:nth-child(1) & {
    animation-delay: 0.1s;
  }

  .category-item:nth-child(2) & {
    animation-delay: 0.2s;
  }

  .category-item:nth-child(3) & {
    animation-delay: 0.3s;
  }

  .category-item:nth-child(4) & {
    animation-delay: 0.4s;
  }
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 图标圆形容器 - 半径为边长一半 */
.icon-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.15);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);

  &.listening-bg {
    background: $listening-gradient;
  }

  &.reading-bg {
    background: $reading-gradient;
  }

  &.translation-bg {
    background: $translation-gradient;
  }

  &.writing-bg {
    background: $writing-gradient;
  }

  .category-item:active & {
    transform: scale(1.05) rotate(5deg);
  }

  // 悬停时轻微旋转
  .category-item:hover & {
    transform: rotate(3deg) scale(1.02);
  }
}

/* 功能图标 - 正方形 */
.category-icon {
  width: 160rpx;
  height: 160rpx;
}

/* 阅读图标 - 放大 1.3 倍以裁剪掉白色背景，只保留蓝色圆形 */
.reading-bg .category-icon {
  width: 210rpx;
  height: 210rpx;
}

/* 写作图标 - 放大 1.5 倍以裁剪掉黑色背景，只保留黑白圆形 */
.writing-bg .category-icon {
  width: 240rpx;
  height: 240rpx;
}

/* 光晕效果 */
.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 180rpx;
  height: 180rpx;
  border-radius: 50%;
  opacity: 0.3;
  filter: blur(40rpx);
  z-index: 0;
  transition: all 0.4s ease;
  pointer-events: none;

  &.listening-glow {
    background: linear-gradient(135deg, #4facfe, #00f2fe);
  }

  &.reading-glow {
    background: linear-gradient(135deg, #f093fb, #f5576c);
  }

  &.translation-glow {
    background: linear-gradient(135deg, #4facfe, #43e97b);
  }

  &.writing-glow {
    background: linear-gradient(135deg, #fa709a, #fee140);
  }

  .category-item:active & {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

/* 功能文字 */
.category-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 700;
  letter-spacing: 2rpx;
  text-align: center;
  margin-bottom: 12rpx;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out backwards;

  .category-item:nth-child(1) & {
    animation-delay: 0.3s;
  }

  .category-item:nth-child(2) & {
    animation-delay: 0.4s;
  }

  .category-item:nth-child(3) & {
    animation-delay: 0.5s;
  }

  .category-item:nth-child(4) & {
    animation-delay: 0.6s;
  }
}

.category-desc {
  font-size: 24rpx;
  color: #999;
  font-weight: 400;
  text-align: center;
  animation: fadeInUp 0.6s ease-out backwards;
  animation-delay: 0.5s;

  .category-item:nth-child(1) & {
    animation-delay: 0.4s;
  }

  .category-item:nth-child(2) & {
    animation-delay: 0.5s;
  }

  .category-item:nth-child(3) & {
    animation-delay: 0.6s;
  }

  .category-item:nth-child(4) & {
    animation-delay: 0.7s;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 底部版权区 */
.footer {
  padding: 12rpx 30rpx 20rpx;
  text-align: center;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  margin-top: auto;

  .footer-text {
    font-size: 20rpx;
    color: #aaa;
    font-weight: 400;
    letter-spacing: 1rpx;
  }
}
</style>
