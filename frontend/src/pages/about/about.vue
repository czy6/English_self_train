<template>
  <view class="container">
    <!-- AI 科技背景 -->
    <view class="ai-background">
      <view class="tech-circle circle-1"></view>
      <view class="tech-circle circle-2"></view>
      <view class="tech-ring ring-1"></view>
      <view class="tech-ring ring-2"></view>
      <view class="grid-overlay"></view>
      <view class="floating-dots"></view>
    </view>

    <!-- 头部信息 -->
    <view class="app-header">
      <view class="logo-wrapper">
        <view class="logo-glow"></view>
        <view class="logo-ring"></view>
        <image src="/static/images/logo.png" mode="aspectFit" class="logo" />
      </view>
      <text class="app-name">英语自主训练平台</text>
      <text class="app-version">v{{ appVersion }}</text>
      <text class="app-slogan">AI 赋能 · 智慧学习 · 从此刻开始</text>
    </view>

    <!-- 主要内容 - 精简版，确保一页显示 -->
    <view class="content-wrapper">
      <!-- 精简功能特点 - 单行显示 -->
      <view class="features-section">
        <view class="feature-item-small">
          <view class="feature-icon-small">
            <uni-icons type="star" size="18" color="#fff"></uni-icons>
          </view>
          <text class="feature-text-small">AI 智能推荐</text>
        </view>
        <view class="feature-item-small">
          <view class="feature-icon-small">
            <uni-icons type="map-pin-ellipse" size="18" color="#fff"></uni-icons>
          </view>
          <text class="feature-text-small">智能分析</text>
        </view>
        <view class="feature-item-small">
          <view class="feature-icon-small">
            <uni-icons type="closeempty" size="18" color="#fff"></uni-icons>
          </view>
          <text class="feature-text-small">错题本</text>
        </view>
        <view class="feature-item-small">
          <view class="feature-icon-small">
            <uni-icons type="list" size="18" color="#fff"></uni-icons>
          </view>
          <text class="feature-text-small">学习报告</text>
        </view>
      </view>

      <!-- 联系我们 - 精简版 -->
      <view class="contact-section">
        <view class="contact-item-simple" @click="contactUs('email')">
          <view class="contact-icon-simple">
            <uni-icons type="paperplane" size="20" color="#667eea"></uni-icons>
          </view>
          <text class="contact-text-simple">意见反馈</text>
          <uni-icons type="forward" size="16" color="#ccc"></uni-icons>
        </view>
        <view class="contact-item-simple" @click="contactUs('weixin')">
          <view class="contact-icon-simple green">
            <uni-icons type="weixin" size="20" color="#fff"></uni-icons>
          </view>
          <text class="contact-text-simple">微信公众号</text>
          <uni-icons type="forward" size="16" color="#ccc"></uni-icons>
        </view>
        <view class="contact-item-simple" @click="gotoPrivacyPolicy">
          <view class="contact-icon-simple purple">
            <uni-icons type="paperclip" size="20" color="#fff"></uni-icons>
          </view>
          <text class="contact-text-simple">隐私政策</text>
          <uni-icons type="forward" size="16" color="#ccc"></uni-icons>
        </view>
      </view>

      <!-- 版权信息 -->
      <view class="copyright-simple">
        <text class="copyright-text">© 2026 英语自主训练平台</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'

const appVersion = ref('1.0.0')
const hasUpdate = ref(false)

const checkUpdate = () => {
  uni.showLoading({ title: '检查中...' })
  setTimeout(() => {
    uni.hideLoading()
    if (hasUpdate.value) {
      uni.showModal({
        title: '发现新版本',
        content: '是否立即更新到最新版本？',
        confirmText: '立即更新',
        cancelText: '稍后',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ title: '正在更新...', icon: 'none' })
          }
        },
      })
    } else {
      uni.showToast({ title: '当前已是最新版本', icon: 'none' })
    }
  }, 1500)
}

const shareApp = () => {
  uni.showActionSheet({
    itemList: ['微信好友', '朋友圈', 'QQ 好友', 'QQ 空间'],
    success: (res) => {
      const platforms = ['微信好友', '朋友圈', 'QQ 好友', 'QQ 空间']
      uni.showToast({ title: `已选择分享到${platforms[res.tapIndex]}`, icon: 'none' })
    },
  })
}

const contactUs = (type) => {
  if (type === 'email') {
    uni.navigateTo({ url: '/pages/feedback/feedback' })
  } else if (type === 'weixin') {
    uni.showModal({
      title: '公众号',
      content: '英语自主训练平台',
      showCancel: false,
    })
  }
}

const gotoPrivacyPolicy = () => {
  uni.navigateTo({ url: '/pages/webview/webview?url=/pages/privacy/privacy.html' })
}
</script>

<style scoped>
.container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #f0f4ff 0%, #ffffff 50%, #f5f8ff 100%);
  overflow: hidden;
}

.ai-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.tech-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.06) 0%, transparent 70%);
  animation: techFloat 8s ease-in-out infinite;
}

.circle-1 {
  width: 400rpx;
  height: 400rpx;
  top: -150rpx;
  right: -150rpx;
}

.circle-2 {
  width: 250rpx;
  height: 250rpx;
  bottom: 20%;
  left: -100rpx;
  animation-delay: 2s;
}

.tech-ring {
  position: absolute;
  border-radius: 50%;
  border: 2rpx solid rgba(102, 126, 234, 0.08);
  animation: techRotate 15s linear infinite;
}

.ring-1 {
  width: 500rpx;
  height: 500rpx;
  top: -200rpx;
  right: -200rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.06);
  border-top-color: rgba(102, 126, 234, 0.15);
}

.ring-2 {
  width: 350rpx;
  height: 350rpx;
  bottom: -150rpx;
  left: -150rpx;
  border: 2rpx solid rgba(79, 172, 254, 0.05);
  border-bottom-color: rgba(79, 172, 254, 0.12);
  animation-direction: reverse;
  animation-duration: 20s;
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: linear-gradient(rgba(102, 126, 234, 0.03) 1rpx, transparent 1rpx),
    linear-gradient(90deg, rgba(102, 126, 234, 0.03) 1rpx, transparent 1rpx);
  background-size: 80rpx 80rpx;
  animation: gridMove 25s linear infinite;
}

.floating-dots {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle, rgba(102, 126, 234, 0.12) 1rpx, transparent 1rpx);
  background-size: 80rpx 80rpx;
  animation: dotsFloat 30s linear infinite;
  opacity: 0.4;
}

@keyframes techFloat {
  0%,
  100% {
    transform: translateY(0) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-40rpx) scale(1.05);
    opacity: 0.7;
  }
}

@keyframes techRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(80rpx, 80rpx);
  }
}

@keyframes dotsFloat {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 80rpx 80rpx;
  }
}

.content-scroll {
  position: relative;
  height: 100vh;
  z-index: 1;
}

.app-header {
  position: relative;
  padding: 60rpx 30rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 4rpx 24rpx rgba(102, 126, 234, 0.08);
}

.logo-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: 24rpx;
  position: relative;
  z-index: 2;
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140rpx;
  height: 140rpx;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: logoGlow 3s ease-in-out infinite;
}

.logo-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 160rpx;
  height: 160rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.2);
  border-top-color: rgba(102, 126, 234, 0.5);
  border-radius: 50%;
  animation: ringSpin 4s linear infinite;
}

@keyframes logoGlow {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.9;
  }
}

@keyframes ringSpin {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

.app-name {
  font-size: 40rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12rpx;
  letter-spacing: 1rpx;
}

.app-version {
  font-size: 24rpx;
  color: rgba(102, 126, 234, 0.7);
  margin-bottom: 16rpx;
  padding: 6rpx 20rpx;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 20rpx;
}

.app-slogan {
  font-size: 24rpx;
  color: #666;
  text-align: center;
  line-height: 1.5;
  margin-top: 8rpx;
}

/* 内容包装器 - 不滚动 */
.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 20rpx 30rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

/* 功能特点区域 - 单行显示 */
.features-section {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  padding: 24rpx;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20rpx);
  border-radius: 20rpx;
  border: 1rpx solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.08);
}

.feature-item-small {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  flex: 1;
}

.feature-icon-small {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);
}

.feature-text-small {
  font-size: 22rpx;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
}

/* 联系区域 - 精简版 */
.contact-section {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20rpx);
  border-radius: 20rpx;
  border: 1rpx solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.08);
  padding: 8rpx 0;
}

.contact-item-simple {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid rgba(102, 126, 234, 0.06);
  transition: all 0.2s ease;
}

.contact-item-simple:last-child {
  border-bottom: none;
}

.contact-item-simple:active {
  background: rgba(102, 126, 234, 0.05);
}

.contact-icon-simple {
  width: 44rpx;
  height: 44rpx;
  border-radius: 12rpx;
  background: rgba(102, 126, 234, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  transition: all 0.3s ease;
}

.contact-icon-simple.green {
  background: linear-gradient(135deg, #51cf66 0%, #69db7c 100%);
  box-shadow: 0 2rpx 8rpx rgba(81, 207, 102, 0.25);
}

.contact-icon-simple.purple {
  background: linear-gradient(135deg, #b197fc 0%, #c4b5fd 100%);
  box-shadow: 0 2rpx 8rpx rgba(177, 151, 252, 0.25);
}

.contact-icon-simple:active {
  transform: scale(0.95);
}

.contact-text-simple {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 版权信息 - 精简版 */
.copyright-simple {
  text-align: center;
  padding: 10rpx 0;
}

.copyright-text {
  font-size: 22rpx;
  color: rgba(102, 126, 234, 0.6);
}

/* 强制图标显示样式 */
.feature-icon-small uni-icons {
  position: relative;
  z-index: 10;
}

.contact-icon-simple uni-icons {
  position: relative;
  z-index: 10;
}
</style>
