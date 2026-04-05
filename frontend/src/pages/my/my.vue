<template>
  <view class="container">
    <!-- AI 科技背景 -->
    <view class="ai-background">
      <view class="tech-circle circle-1"></view>
      <view class="tech-circle circle-2"></view>
      <view class="tech-circle circle-3"></view>
      <view class="tech-ring ring-1"></view>
      <view class="tech-ring ring-2"></view>
      <view class="tech-ring ring-3"></view>
      <view class="grid-overlay"></view>
      <view class="floating-dots"></view>
      <view class="aurora-effect"></view>
    </view>

    <!-- 用户信息卡片 - 悬浮科技风 -->
    <view class="user-card">
      <view class="card-glow"></view>
      <view class="card-border"></view>
      <view class="card-corner corner-tl"></view>
      <view class="card-corner corner-tr"></view>
      <view class="card-corner corner-bl"></view>
      <view class="card-corner corner-br"></view>
      <view class="user-content">
        <view class="avatar-wrapper" @click="uni.navigateTo({ url: '/pages/profile/profile' })">
          <view class="avatar-halo"></view>
          <view class="avatar-ring"></view>
          <view class="avatar-pulse"></view>
          <view class="avatar-shine"></view>
          <image :src="userStore.userInfo.avatar" mode="aspectFill" class="avatar" />
          <view class="avatar-status"></view>
          <view class="avatar-badge">
            <text class="badge-icon">✦</text>
          </view>
        </view>
        <view class="user-info" @click="uni.navigateTo({ url: '/pages/profile/profile' })">
          <text class="username">{{ userStore.userInfo.nickName || '未登录用户' }}</text>
          <view class="level-container">
            <view class="level-bar">
              <view class="level-fill"></view>
            </view>
            <text class="level-text"
              >Lv.{{ userStore.userInfo.level >= 0 ? userStore.userInfo.level : 0 }}</text
            >
          </view>
          <text class="welcome-text">探索 AI 学习的无限可能</text>
        </view>
      </view>
    </view>

    <!-- AI 功能菜单 -->
    <view class="ai-menu-section">
      <view class="section-title">
        <view class="title-icon">
          <view class="title-dot"></view>
          <view class="title-wave"></view>
        </view>
        <text class="title-text">AI 功能</text>
        <view class="title-line"></view>
        <view class="title-decoration">
          <view class="deco-point"></view>
          <view class="deco-line"></view>
          <view class="deco-point"></view>
        </view>
      </view>
      <view class="menu-card">
        <navigator
          url="/pages/favorites/favorites"
          class="ai-menu-item"
          hover-class="menu-hover"
          :hover-stay-time="200"
        >
          <view class="menu-icon-box star">
            <view class="icon-glow"></view>
            <view class="icon-ring"></view>
            <view class="icon-sparkle"></view>
            <text class="menu-icon">⭐</text>
          </view>
          <view class="menu-content">
            <text class="menu-title">我的收藏</text>
            <text class="menu-desc">AI 智能整理</text>
          </view>
          <view class="menu-action">
            <text class="menu-arrow">›</text>
            <view class="arrow-glow"></view>
          </view>
        </navigator>

        <navigator
          url="/pages/history/history"
          class="ai-menu-item"
          hover-class="menu-hover"
          :hover-stay-time="200"
        >
          <view class="menu-icon-box book">
            <view class="icon-glow"></view>
            <view class="icon-ring"></view>
            <view class="icon-sparkle"></view>
            <text class="menu-icon">📚</text>
          </view>
          <view class="menu-content">
            <text class="menu-title">学习历史</text>
            <text class="menu-desc">AI 学习分析</text>
          </view>
          <view class="menu-action">
            <text class="menu-arrow">›</text>
            <view class="arrow-glow"></view>
          </view>
        </navigator>

        <navigator
          url="/pages/set/set"
          class="ai-menu-item"
          hover-class="menu-hover"
          :hover-stay-time="200"
        >
          <view class="menu-icon-box gear">
            <view class="icon-glow"></view>
            <view class="icon-ring"></view>
            <view class="icon-sparkle"></view>
            <text class="menu-icon">⚙️</text>
          </view>
          <view class="menu-content">
            <text class="menu-title">设置</text>
            <text class="menu-desc">个性化配置</text>
          </view>
          <view class="menu-action">
            <text class="menu-arrow">›</text>
            <view class="arrow-glow"></view>
          </view>
        </navigator>

        <navigator
          url="/pages/about/about"
          class="ai-menu-item"
          hover-class="menu-hover"
          :hover-stay-time="200"
        >
          <view class="menu-icon-box info">
            <view class="icon-glow"></view>
            <view class="icon-ring"></view>
            <view class="icon-sparkle"></view>
            <text class="menu-icon">ℹ️</text>
          </view>
          <view class="menu-content">
            <text class="menu-title">关于我们</text>
            <text class="menu-desc">了解 AI 技术</text>
          </view>
          <view class="menu-action">
            <text class="menu-arrow">›</text>
            <view class="arrow-glow"></view>
          </view>
        </navigator>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-section">
      <view
        v-if="storage.getSync('token')"
        class="logout-btn"
        @click="logout"
        hover-class="btn-press"
        :hover-stay-time="200"
      >
        <view class="btn-glow"></view>
        <view class="btn-particles"></view>
        <view class="btn-shimmer"></view>
        <text class="btn-text">退出登录</text>
      </view>
      <navigator
        v-else
        url="/pages/login/login"
        class="login-btn"
        hover-class="btn-press"
        :hover-stay-time="200"
      >
        <view class="btn-glow"></view>
        <view class="btn-particles"></view>
        <view class="btn-shimmer"></view>
        <view class="btn-icon">
          <text class="btn-icon-text">✦</text>
        </view>
        <text class="btn-text">立即登录</text>
      </navigator>
    </view>
  </view>
</template>

<script setup>
import { useUserStore } from '@/store'
import storage from '@/utils/storage.ts'
import { onShow } from '@dcloudio/uni-app'

const userStore = useUserStore()

// 登录了才去获取用户信息
onShow(() => {
  storage.get('token').then((token) => {
    if (token !== null) {
      userStore.getUserInfo()
    }
  })
})

// 退出登录
const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '您确定要退出当前账号吗？',
    success: (res) => {
      if (res.confirm) {
        storage.remove('token')
        userStore.userInfo = userStore.defaultUserInfo
        uni.showToast({
          title: '退出成功',
          icon: 'success',
        })
      }
    },
  })
}
</script>

<style scoped>
/* 容器 - 固定布局不允许滚动 */
.container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f4ff 0%, #ffffff 50%, #f5f8ff 100%);
  overflow: hidden;
}

/* AI 科技背景 - 未来感浅色版 */
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
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.08) 0%,
    rgba(118, 75, 162, 0.04) 50%,
    transparent 70%
  );
}

.circle-2 {
  width: 250rpx;
  height: 250rpx;
  bottom: 20%;
  left: -100rpx;
  background: radial-gradient(
    circle,
    rgba(79, 172, 254, 0.06) 0%,
    rgba(0, 242, 254, 0.03) 50%,
    transparent 70%
  );
  animation-delay: 2s;
}

.circle-3 {
  width: 180rpx;
  height: 180rpx;
  top: 40%;
  right: 10%;
  background: radial-gradient(
    circle,
    rgba(255, 107, 107, 0.04) 0%,
    rgba(255, 195, 113, 0.02) 50%,
    transparent 70%
  );
  animation-delay: 4s;
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

.ring-3 {
  width: 600rpx;
  height: 600rpx;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border: 1rpx solid rgba(102, 126, 234, 0.04);
  border-left-color: rgba(102, 126, 234, 0.1);
  animation-duration: 30s;
  opacity: 0.3;
}

.aurora-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    120deg,
    rgba(102, 126, 234, 0.03) 0%,
    transparent 25%,
    rgba(118, 75, 162, 0.02) 50%,
    transparent 75%,
    rgba(79, 172, 254, 0.03) 100%
  );
  background-size: 200% 200%;
  animation: auroraFlow 10s ease-in-out infinite;
  opacity: 0.5;
}

@keyframes auroraFlow {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.floating-dots {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle, rgba(102, 126, 234, 0.12) 1rpx, transparent 1rpx);
  background-size: 80rpx 80rpx;
  background-position: 0 0;
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

@keyframes dotsFloat {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 80rpx 80rpx;
  }
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

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(80rpx, 80rpx);
  }
}

/* 用户卡片 - 未来科技悬浮风 */
.user-card {
  position: relative;
  margin: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: 28rpx;
  padding: 44rpx 34rpx;
  border: 1rpx solid rgba(102, 126, 234, 0.12);
  box-shadow: 0 12rpx 60rpx rgba(102, 126, 234, 0.1), 0 2rpx 20rpx rgba(102, 126, 234, 0.05),
    inset 0 0 40rpx rgba(255, 255, 255, 0.8);
  overflow: hidden;
  z-index: 1;
  transform: translateY(0);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 28rpx;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.4) 0%,
    transparent 50%,
    rgba(255, 255, 255, 0.2) 100%
  );
  pointer-events: none;
  z-index: 3;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.08) 0%, transparent 60%);
  animation: glowRotate 12s linear infinite;
  pointer-events: none;
}

.card-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, transparent, #667eea, #764ba2, #667eea, transparent);
  background-size: 200% 100%;
  animation: borderFlow 3s ease-in-out infinite;
  border-radius: 4rpx 4rpx 0 0;
}

.card-corner {
  position: absolute;
  width: 30rpx;
  height: 30rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  pointer-events: none;
  z-index: 4;
}

.corner-tl {
  top: 10rpx;
  left: 10rpx;
  border-right: none;
  border-bottom: none;
  border-radius: 28rpx 0 0 0;
}

.corner-tr {
  top: 10rpx;
  right: 10rpx;
  border-left: none;
  border-bottom: none;
  border-radius: 0 28rpx 0 0;
}

.corner-bl {
  bottom: 10rpx;
  left: 10rpx;
  border-right: none;
  border-top: none;
  border-radius: 0 0 0 28rpx;
}

.corner-br {
  bottom: 10rpx;
  right: 10rpx;
  border-left: none;
  border-top: none;
  border-radius: 0 0 28rpx 0;
}

@keyframes glowRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes borderFlow {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.user-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.avatar-wrapper {
  position: relative;
  width: 130rpx;
  height: 130rpx;
  margin-right: 34rpx;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3rpx solid rgba(102, 126, 234, 0.35);
  box-shadow: 0 6rpx 24rpx rgba(102, 126, 234, 0.2), inset 0 0 20rpx rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 2;
}

.avatar-halo {
  position: absolute;
  top: -8rpx;
  left: -8rpx;
  right: -8rpx;
  bottom: -8rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  border-top-color: rgba(102, 126, 234, 0.6);
  border-right-color: rgba(102, 126, 234, 0.4);
  animation: haloSpin 3s linear infinite;
  z-index: 1;
}

.avatar-ring {
  position: absolute;
  top: -14rpx;
  left: -14rpx;
  right: -14rpx;
  bottom: -14rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(102, 126, 234, 0.15);
  animation: ringSpin 5s linear infinite;
  z-index: 0;
}

.avatar-pulse {
  position: absolute;
  top: -20rpx;
  left: -20rpx;
  right: -20rpx;
  bottom: -20rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.12) 0%, transparent 70%);
  animation: pulse 2.5s ease-in-out infinite;
  z-index: 0;
}

.avatar-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.4) 0%,
    transparent 50%,
    rgba(255, 255, 255, 0.2) 100%
  );
  animation: shineRotate 4s linear infinite;
  z-index: 3;
  pointer-events: none;
}

.avatar-status {
  position: absolute;
  bottom: 4rpx;
  right: 4rpx;
  width: 24rpx;
  height: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  border: 3rpx solid #ffffff;
  box-shadow: 0 2rpx 12rpx rgba(102, 126, 234, 0.4);
  z-index: 3;
  animation: statusPulse 2s ease-in-out infinite;
}

.avatar-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.35);
  z-index: 4;
  animation: badgeFloat 3s ease-in-out infinite;
}

.badge-icon {
  font-size: 24rpx;
  color: #ffffff;
  animation: badgeTwinkle 2s ease-in-out infinite;
}

@keyframes shineRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes badgeFloat {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-8rpx) rotate(10deg);
  }
}

@keyframes badgeTwinkle {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

@keyframes haloSpin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes ringSpin {
  from {
    transform: rotate(0deg) scale(1);
  }
  50% {
    transform: rotate(180deg) scale(1.05);
  }
  to {
    transform: rotate(360deg) scale(1);
  }
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.3;
  }
}

@keyframes statusPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  font-size: 38rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #1a1a1a 0%, #4a4a4a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 18rpx;
  letter-spacing: 1.5rpx;
}

.welcome-text {
  font-size: 24rpx;
  color: rgba(102, 126, 234, 0.7);
  margin-top: 12rpx;
  font-weight: 400;
  letter-spacing: 0.5rpx;
}

.level-container {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.level-bar {
  width: 140rpx;
  height: 10rpx;
  background: rgba(102, 126, 234, 0.12);
  border-radius: 5rpx;
  overflow: hidden;
  position: relative;
}

.level-bar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s ease-in-out infinite;
}

.level-fill {
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #667eea 100%);
  background-size: 200% 100%;
  border-radius: 5rpx;
  animation: levelProgress 2s ease-out, levelShine 3s ease-in-out infinite;
  position: relative;
}

.level-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: fillShine 2s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes levelProgress {
  from {
    width: 0;
  }
}

@keyframes levelShine {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes fillShine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.level-text {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 600;
  text-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.2);
}

/* AI 菜单区域 */
.ai-menu-section {
  position: relative;
  margin: 0 30rpx 30rpx;
  z-index: 1;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  padding-left: 10rpx;
  position: relative;
}

.title-icon {
  position: relative;
  width: 12rpx;
  height: 12rpx;
  margin-right: 12rpx;
}

.title-dot {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  animation: dotPulse 2s ease-in-out infinite;
}

.title-wave {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border: 2rpx solid rgba(102, 126, 234, 0.4);
  border-radius: 50%;
  animation: waveExpand 2s ease-out infinite;
}

.title-text {
  font-size: 32rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1rpx;
}

.title-line {
  width: 100rpx;
  height: 4rpx;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2rpx;
  margin: 0 20rpx;
  position: relative;
  overflow: hidden;
}

.title-line::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 40rpx;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
  animation: lineGlow 2s ease-in-out infinite;
}

.title-decoration {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-left: auto;
}

.deco-point {
  width: 8rpx;
  height: 8rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  animation: decoTwinkle 3s ease-in-out infinite;
}

.deco-line {
  width: 30rpx;
  height: 2rpx;
  background: linear-gradient(
    90deg,
    rgba(102, 126, 234, 0.3),
    rgba(118, 75, 162, 0.5),
    rgba(102, 126, 234, 0.3)
  );
  border-radius: 1rpx;
  animation: decoPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

@keyframes waveExpand {
  0% {
    width: 100%;
    height: 100%;
    opacity: 1;
  }
  100% {
    width: 200%;
    height: 200%;
    opacity: 0;
  }
}

@keyframes lineGlow {
  0%,
  100% {
    width: 40rpx;
    opacity: 0.8;
  }
  50% {
    width: 60rpx;
    opacity: 1;
  }
}

@keyframes decoTwinkle {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.3);
  }
}

@keyframes decoPulse {
  0%,
  100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.8;
  }
}

.menu-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: 24rpx;
  border: 1rpx solid rgba(102, 126, 234, 0.1);
  overflow: hidden;
  box-shadow: 0 8rpx 40rpx rgba(102, 126, 234, 0.08), 0 2rpx 12rpx rgba(102, 126, 234, 0.04);
}

.ai-menu-item {
  display: flex;
  align-items: center;
  padding: 36rpx 32rpx;
  border-bottom: 1rpx solid rgba(102, 126, 234, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.ai-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4rpx;
  background: linear-gradient(180deg, #667eea, #764ba2);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.ai-menu-item:last-child {
  border-bottom: none;
}

.menu-hover {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.06) 0%, rgba(118, 75, 162, 0.04) 100%);
  transform: translateX(8rpx);
}

.menu-hover::before {
  transform: scaleY(1);
}

.menu-icon-box {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 28rpx;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.08) 100%);
  border: 1rpx solid rgba(102, 126, 234, 0.18);
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.12), inset 0 0 20rpx rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.menu-icon-box::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 60%);
  animation: iconShine 3s linear infinite;
  pointer-events: none;
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 48rpx;
  height: 48rpx;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(102, 126, 234, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: iconGlow 2.5s ease-in-out infinite;
}

.icon-ring {
  position: absolute;
  top: -6rpx;
  left: -6rpx;
  right: -6rpx;
  bottom: -6rpx;
  border-radius: 22rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.2);
  border-top-color: rgba(102, 126, 234, 0.5);
  animation: ringRotate 4s linear infinite;
  z-index: 1;
}

.icon-sparkle {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle, rgba(255, 255, 255, 0.5) 1rpx, transparent 1rpx);
  background-size: 8rpx 8rpx;
  background-position: 0 0;
  animation: sparkleFloat 2s ease-in-out infinite;
  opacity: 0.6;
  pointer-events: none;
}

@keyframes iconShine {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes sparkleFloat {
  0%,
  100% {
    opacity: 0.4;
    background-position: 0 0;
  }
  50% {
    opacity: 0.8;
    background-position: 4rpx 4rpx;
  }
}

@keyframes iconGlow {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.6);
    opacity: 0.9;
  }
}

@keyframes ringRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.menu-icon {
  font-size: 36rpx;
  line-height: 1;
  z-index: 2;
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.menu-title {
  font-size: 30rpx;
  color: #2a2a2a;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}

.menu-desc {
  font-size: 24rpx;
  color: rgba(102, 126, 234, 0.65);
  font-weight: 400;
}

.menu-arrow {
  font-size: 44rpx;
  color: rgba(102, 126, 234, 0.35);
  font-weight: 300;
  transition: all 0.3s ease;
}

.menu-action {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
}

.arrow-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60rpx;
  height: 60rpx;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: arrowGlowPulse 2s ease-in-out infinite;
  opacity: 0;
}

.menu-hover .menu-arrow {
  color: rgba(102, 126, 234, 0.6);
  transform: translateX(6rpx);
}

.menu-hover .arrow-glow {
  opacity: 1;
  animation: arrowGlowExpand 0.3s ease-out;
}

@keyframes arrowGlowPulse {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.6;
  }
}

@keyframes arrowGlowExpand {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
  }
}

/* 操作按钮区域 */
.action-section {
  position: relative;
  margin: 0 30rpx 30rpx;
  z-index: 1;
}

.logout-btn,
.login-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 1.5rpx;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08), 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.logout-btn {
  background: rgba(255, 255, 255, 0.98);
  color: #ff6b6b;
  border: 1rpx solid rgba(255, 107, 107, 0.18);
  box-shadow: 0 6rpx 24rpx rgba(255, 107, 107, 0.1), 0 2rpx 8rpx rgba(255, 107, 107, 0.05);
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 12rpx 40rpx rgba(102, 126, 234, 0.3), 0 4rpx 16rpx rgba(102, 126, 234, 0.2);
  position: relative;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    120deg,
    transparent 0%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 100%
  );
  background-size: 200% 100%;
  animation: btnGradient 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes btnGradient {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.btn-glow {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.25), transparent);
  animation: btnShine 3s ease-in-out infinite;
}

.btn-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(circle, rgba(255, 255, 255, 0.3) 1rpx, transparent 1rpx);
  background-size: 20rpx 20rpx;
  background-position: 0 0;
  opacity: 0;
  animation: particlesFloat 2s ease-in-out infinite;
}

.btn-shimmer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    transparent 0%,
    rgba(255, 255, 255, 0.15) 50%,
    transparent 100%
  );
  background-size: 200% 200%;
  animation: shimmerMove 4s ease-in-out infinite;
  pointer-events: none;
}

@keyframes shimmerMove {
  0%,
  100% {
    background-position: 0% 0%;
  }
  50% {
    background-position: 100% 100%;
  }
}

.btn-icon {
  position: relative;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.btn-icon-text {
  font-size: 28rpx;
  color: #ffffff;
  animation: iconSpin 3s linear infinite;
}

@keyframes iconSpin {
  0%,
  100% {
    transform: rotate(0deg) scale(1);
  }
  25% {
    transform: rotate(-10deg) scale(1.1);
  }
  75% {
    transform: rotate(10deg) scale(1.1);
  }
}

@keyframes btnShine {
  0%,
  100% {
    left: -100%;
  }
  50% {
    left: 100%;
  }
}

@keyframes particlesFloat {
  0% {
    opacity: 0;
    background-position: 0 0;
  }
  50% {
    opacity: 0.4;
  }
  100% {
    opacity: 0;
    background-position: 20rpx 20rpx;
  }
}

.btn-press {
  transform: scale(0.97);
  opacity: 0.95;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.btn-text {
  position: relative;
  z-index: 2;
  line-height: 1;
}
</style>
