<template>
  <view class="settings-container">
    <!-- AI 科技背景 -->
    <view class="ai-background">
      <view class="tech-circle circle-1"></view>
      <view class="tech-circle circle-2"></view>
      <view class="tech-ring ring-1"></view>
      <view class="tech-ring ring-2"></view>
      <view class="grid-overlay"></view>
      <view class="floating-dots"></view>
    </view>

    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-glow"></view>
      <view class="header-title">
        <view class="title-icon">
          <view class="icon-pulse"></view>
          <uni-icons type="gear" size="28" color="#ffffff"></uni-icons>
        </view>
        <text class="title-text">系统设置</text>
      </view>
    </view>

    <!-- 主要内容区域 - 固定不滚动 -->
    <view class="content-wrapper">
      <!-- 账户管理 -->
      <view class="ai-card">
        <view class="card-header">
          <view class="header-icon">
            <view class="icon-glow"></view>
            <uni-icons type="person" size="20" color="#667eea"></uni-icons>
          </view>
          <text class="card-title">账户管理</text>
        </view>
        <view class="card-content">
          <navigator url="/pages/profile/profile" class="setting-item" hover-class="item-hover">
            <view class="item-left">
              <view class="item-icon-box">
                <view class="item-glow"></view>
                <uni-icons type="contact" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">个人信息</text>
                <text class="item-desc">修改头像、昵称、密码等</text>
              </view>
            </view>
            <view class="item-arrow">
              <view class="arrow-glow"></view>
              <uni-icons type="forward" size="20" color="#667eea"></uni-icons>
            </view>
          </navigator>

          <view class="setting-item" @click="showAccountSecurity">
            <view class="item-left">
              <view class="item-icon-box danger">
                <view class="item-glow"></view>
                <uni-icons type="locked" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">账号安全</text>
                <text class="item-desc">绑定手机、修改密码</text>
              </view>
            </view>
            <view class="item-arrow">
              <view class="arrow-glow"></view>
              <uni-icons type="forward" size="20" color="#667eea"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 学习设置 -->
      <view class="ai-card">
        <view class="card-header">
          <view class="header-icon">
            <view class="icon-glow"></view>
            <uni-icons type="star" size="24" color="#667eea"></uni-icons>
          </view>
          <text class="card-title">学习设置</text>
        </view>
        <view class="card-content">
          <view class="setting-item">
            <view class="item-left">
              <view class="item-icon-box warning">
                <view class="item-glow"></view>
                <uni-icons type="info" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">学习提醒</text>
                <text class="item-desc">每日学习时间提醒</text>
              </view>
            </view>
            <view class="item-control">
              <switch
                :checked="notificationEnabled"
                @change="toggleNotification"
                class="tech-switch"
              ></switch>
            </view>
          </view>

          <view class="setting-item">
            <view class="item-left">
              <view class="item-icon-box purple">
                <view class="item-glow"></view>
                <uni-icons type="eye" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">深色模式</text>
                <text class="item-desc">切换深色/浅色主题</text>
              </view>
            </view>
            <view class="item-control">
              <switch
                :checked="darkModeEnabled"
                @change="toggleDarkMode"
                class="tech-switch"
              ></switch>
            </view>
          </view>
        </view>
      </view>

      <!-- 系统设置 -->
      <view class="ai-card">
        <view class="card-header">
          <view class="header-icon">
            <view class="icon-glow"></view>
            <uni-icons type="gear-filled" size="24" color="#667eea"></uni-icons>
          </view>
          <text class="card-title">系统设置</text>
        </view>
        <view class="card-content">
          <view class="setting-item" @click="clearCache">
            <view class="item-left">
              <view class="item-icon-box danger">
                <view class="item-glow"></view>
                <uni-icons type="trash" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">清除缓存</text>
                <text class="item-desc">{{ cacheSize }} MB</text>
              </view>
            </view>
            <view class="item-arrow">
              <view class="arrow-glow"></view>
              <uni-icons type="forward" size="20" color="#667eea"></uni-icons>
            </view>
          </view>

          <view class="setting-item" @click="showAbout">
            <view class="item-left">
              <view class="item-icon-box success">
                <view class="item-glow"></view>
                <uni-icons type="info" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">关于我们</text>
                <text class="item-desc">版本 v{{ appVersion }}</text>
              </view>
            </view>
            <view class="item-arrow">
              <view class="arrow-glow"></view>
              <uni-icons type="forward" size="20" color="#667eea"></uni-icons>
            </view>
          </view>

          <view class="setting-item" @click="showHelp">
            <view class="item-left">
              <view class="item-icon-box purple">
                <view class="item-glow"></view>
                <uni-icons type="help-filled" size="20" color="#ffffff"></uni-icons>
              </view>
              <view class="item-info">
                <text class="item-title">帮助中心</text>
                <text class="item-desc">常见问题与使用指南</text>
              </view>
            </view>
            <view class="item-arrow">
              <view class="arrow-glow"></view>
              <uni-icons type="forward" size="20" color="#667eea"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 退出登录 -->
      <view class="logout-wrapper">
        <view class="logout-btn" @click="confirmLogout">
          <view class="btn-glow"></view>
          <view class="btn-border"></view>
          <text class="btn-text">退出登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useUserStore } from '@/store'
import storage from '@/utils/storage.ts'

// 设置状态
const notificationEnabled = ref(true)
const darkModeEnabled = ref(false)
const cacheSize = ref(2.4)
const appVersion = ref('1.0.0')

// 切换通知
const toggleNotification = (e) => {
  notificationEnabled.value = e.detail.value
  const status = e.detail.value ? '开启' : '关闭'
  uni.showToast({
    title: `已${status}学习提醒`,
    icon: 'none',
  })
}

// 切换深色模式
const toggleDarkMode = (e) => {
  darkModeEnabled.value = e.detail.value
  const mode = e.detail.value ? 'dark' : 'light'
  uni.setStorageSync('themeMode', mode)
  uni.showToast({
    title: `已切换到${mode === 'dark' ? '深色' : '浅色'}模式`,
    icon: 'none',
  })
}

// 清除缓存
const clearCache = () => {
  uni.showModal({
    title: '清除缓存',
    content: `确定要清除${cacheSize.value}MB缓存吗？`,
    confirmColor: '#3a7bd5',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '清除中...' })

        // 模拟清除缓存（实际项目需替换为真实逻辑）
        setTimeout(() => {
          cacheSize.value = 0
          uni.hideLoading()
          uni.showToast({ title: '缓存已清除', icon: 'success' })
        }, 1000)
      }
    },
  })
}

// 显示账号安全（开发中提示）
const showAccountSecurity = () => {
  uni.showToast({
    title: '功能开发中...',
    icon: 'none',
    mask: true,
  })
}

// 显示关于我们（开发中提示）
const showAbout = () => {
  uni.navigateTo({ url: '/pages/about/about' })
}

// 跳转帮助中心
const showHelp = () => {
  uni.showToast({
    title: '功能开发中...',
    icon: 'none',
    mask: true,
  })
}

// 退出登录逻辑
const userStore = useUserStore()
const confirmLogout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    confirmColor: '#f53f3f',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '退出中...' })
        setTimeout(() => {
          storage.remove('token') // 清除登录态
          userStore.userInfo = userStore.defaultUserInfo // 重置用户信息
          uni.hideLoading()
          uni.reLaunch({ url: '/pages/login/login' }) // 跳转到登录页
        }, 1000)
      }
    },
  })
}
</script>

<style scoped>
/* ==================== 容器与背景 ==================== */
.settings-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, #f0f4ff 0%, #ffffff 50%, #f5f8ff 100%);
  overflow: hidden;
}

/* ==================== AI 科技背景 ==================== */
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
  width: 600rpx;
  height: 600rpx;
  top: -200rpx;
  right: -100rpx;
}

.circle-2 {
  width: 400rpx;
  height: 400rpx;
  bottom: -100rpx;
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
  top: 10%;
  left: 20%;
  border-width: 2rpx;
}

.ring-2 {
  width: 700rpx;
  height: 700rpx;
  bottom: -100rpx;
  right: -150rpx;
  animation-delay: 3s;
  border-width: 1rpx;
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

/* ==================== 页面标题 ==================== */
.page-header {
  position: relative;
  padding: 60rpx 30rpx 30rpx;
  text-align: center;
  z-index: 10;
}

.header-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 300rpx;
  height: 300rpx;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: headerPulse 4s ease-in-out infinite;
}

.header-title {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 16rpx;
}

.title-icon {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
}

.icon-pulse {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: iconPulse 2s ease-in-out infinite;
  opacity: 0.6;
}

.title-text {
  font-size: 40rpx;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* ==================== 内容区域 - 固定不滚动 ==================== */
.content-wrapper {
  height: calc(100vh - 120rpx);
  padding: 0 20rpx 10rpx;
  box-sizing: border-box;
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

/* ==================== AI 卡片 ==================== */
.ai-card {
  margin-bottom: 16rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(102, 126, 234, 0.06);
  border: 1rpx solid rgba(102, 126, 234, 0.06);
  animation: cardSlideUp 0.5s ease-out;
  position: relative;
  flex-shrink: 0;
}

.ai-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3rpx;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #667eea 100%);
  background-size: 200% 100%;
  animation: gradientSlide 3s ease-in-out infinite;
}

.ai-card:nth-child(2) {
  animation-delay: 0.1s;
}
.ai-card:nth-child(3) {
  animation-delay: 0.2s;
}
.ai-card:nth-child(4) {
  animation-delay: 0.3s;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 24rpx 30rpx 16rpx;
  gap: 14rpx;
}

.header-icon {
  position: relative;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);
}

.icon-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: white;
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  letter-spacing: 0.5rpx;
}

.card-content {
  padding: 0 6rpx 6rpx;
}

/* ==================== 设置项 ==================== */
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
  margin: 0 6rpx;
  border-radius: 14rpx;
  transition: all 0.25s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.setting-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.04) 0%, rgba(118, 75, 162, 0.04) 100%);
  opacity: 0;
  transition: opacity 0.25s ease;
  border-radius: 14rpx;
}

.setting-item:hover::before,
.item-hover::before {
  opacity: 1;
}

.setting-item:active {
  transform: scale(0.98);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  flex: 1;
  position: relative;
  z-index: 1;
}

.item-icon-box {
  position: relative;
  width: 42rpx;
  height: 42rpx;
  border-radius: 14rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 10rpx rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.item-icon-box.danger {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
  box-shadow: 0 4rpx 10rpx rgba(255, 107, 107, 0.2);
}

.item-icon-box.warning {
  background: linear-gradient(135deg, #ffd43b 0%, #ffe066 100%);
  box-shadow: 0 4rpx 10rpx rgba(255, 212, 59, 0.2);
}

.item-icon-box.success {
  background: linear-gradient(135deg, #51cf66 0%, #69db7c 100%);
  box-shadow: 0 4rpx 10rpx rgba(81, 207, 102, 0.2);
}

.item-icon-box.purple {
  background: linear-gradient(135deg, #b197fc 0%, #c4b5fd 100%);
  box-shadow: 0 4rpx 10rpx rgba(177, 151, 252, 0.2);
}

.item-icon-box:active {
  transform: scale(0.95);
}

.item-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 14rpx;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  animation: itemGlow 2s ease-in-out infinite;
  opacity: 0.4;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 3rpx;
}

.item-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.item-desc {
  font-size: 22rpx;
  color: #999;
}

.item-arrow {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  transition: all 0.3s ease;
  z-index: 1;
}

.item-arrow:active {
  transform: translateX(8rpx) scale(0.95);
}

.arrow-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  animation: arrowGlow 2s ease-in-out infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.item-arrow:active .arrow-glow {
  opacity: 1;
}

.item-control {
  position: relative;
  z-index: 1;
}

/* ==================== 自定义开关 ==================== */
.tech-switch {
  transform: scale(0.85);
}

/* 微信小程序 switch 组件样式覆盖 */
.tech-switch.wx-switch-input {
  border-color: transparent !important;
  background-color: #dee2e6 !important;
}

.tech-switch.wx-switch-input.wx-switch-input-checked {
  border-color: transparent !important;
  background-color: #51cf66 !important;
}

/* uni-app switch 组件样式 */
uni-switch[checked] .wx-switch-input {
  background-color: #51cf66 !important;
  border-color: transparent !important;
}

uni-switch:not([checked]) .wx-switch-input {
  background-color: #dee2e6 !important;
  border-color: transparent !important;
}

/* ==================== 退出登录按钮 ==================== */
.logout-wrapper {
  padding: 6rpx 30rpx 10rpx;
  position: relative;
  z-index: 10;
  flex-shrink: 0;
}

.logout-btn {
  position: relative;
  padding: 24rpx 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20rpx);
  border-radius: 16rpx;
  text-align: center;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(245, 63, 63, 0.12);
  border: 1rpx solid rgba(245, 63, 63, 0.15);
  transition: all 0.3s ease;
  cursor: pointer;
}

.logout-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(245, 63, 63, 0.08) 0%, rgba(255, 125, 0, 0.08) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.logout-btn:hover::before {
  opacity: 1;
}

.logout-btn:active {
  transform: scale(0.98);
}

.btn-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba(245, 63, 63, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: btnGlow 3s ease-in-out infinite;
  pointer-events: none;
}

.btn-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 20rpx;
  border: 2rpx solid transparent;
  background: linear-gradient(135deg, #f53f3f, #ff7d00) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.6;
}

.btn-text {
  position: relative;
  z-index: 10;
  font-size: 30rpx;
  font-weight: 500;
  color: #f53f3f;
  letter-spacing: 2rpx;
}

/* ==================== 动画关键帧 ==================== */
@keyframes techFloat {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(20rpx, -20rpx) scale(1.05);
  }
}

@keyframes techRotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 80rpx 80rpx;
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

@keyframes headerPulse {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.15;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.25;
  }
}

@keyframes iconPulse {
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

@keyframes cardSlideUp {
  0% {
    opacity: 0;
    transform: translateY(40rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes gradientSlide {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

@keyframes iconGlow {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.2;
  }
}

@keyframes itemGlow {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.2;
  }
}

@keyframes arrowGlow {
  0%,
  100% {
    transform: scale(1);
    opacity: 0;
  }
  50% {
    transform: scale(1.3);
    opacity: 1;
  }
}

@keyframes btnGlow {
  0%,
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.15;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.3);
    opacity: 0.25;
  }
}

/* 强制图标显示样式 */
.item-icon-box uni-icons {
  position: relative;
  z-index: 10;
}

.header-icon uni-icons {
  position: relative;
  z-index: 10;
}
</style>
