<template>
  <view 
    class="robot-container" 
    :style="robotStyle" 
    @click="toggleMovement"
  >
    <!-- 对话气泡 -->
    <view class="chat-bubble" v-if="showChatBubble">
      <text class="bubble-text">您好！我是智能小英</text>
      <view class="bubble-arrow"></view>
    </view>
    
    <!-- 机器人头部 -->
    <view class="robot-head">
      <view class="eye left-eye">
        <view class="pupil"></view>
        <view class="eye-shine"></view>
        <view class="eye-reflection"></view>
      </view>
      <view class="eye right-eye">
        <view class="pupil"></view>
        <view class="eye-shine"></view>
        <view class="eye-reflection"></view>
      </view>
      <view class="mouth" :class="{ happy: isMoving }">
        <view class="mouth-inner"></view>
        <view class="teeth" v-if="isMoving"></view>
      </view>
      
      <!-- 头顶天线 -->
      <view class="top-antenna">
        <view class="antenna-stick"></view>
        <view class="antenna-top-ball">
          <view class="ball-highlight"></view>
        </view>
      </view>
      
      <!-- 头部装饰光环 -->
      <view class="head-glow"></view>
      
      <!-- 脸颊红晕 -->
      <view class="cheek left-cheek"></view>
      <view class="cheek right-cheek"></view>
    </view>
    
    <!-- 机器人手臂 -->
    <view class="robot-arms">
      <view class="arm left-arm" :class="{ waving: isWaving }">
        <view class="arm-joint"></view>
        <view class="hand-ball left-hand">
          <text class="hand-text">EN</text>
          <view class="hand-glow"></view>
        </view>
      </view>
      <view class="arm right-arm" :class="{ waving: isWaving }">
        <view class="arm-joint"></view>
        <view class="hand-ball right-hand">
          <text class="hand-text">GO</text>
          <view class="hand-glow"></view>
        </view>
      </view>
    </view>
    
    <!-- 浮动英语单词 -->
    <view class="floating-words" v-if="showWords">
      <text class="word word1">Hello</text>
      <text class="word word2">Learn</text>
      <text class="word word3">Fun</text>
    </view>
    
    <!-- 底部阴影 -->
    <view class="robot-shadow"></view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const robotStyle = ref({
  left: '50px',
  top: '50px',
  transform: 'scaleX(1)'
})

const isWaving = ref(false)
const showWords = ref(false)
const isMoving = ref(false)
const showChatBubble = ref(false)
const bubbleOnRight = ref(true) // 控制对话框显示在左边还是右边

let animationTimer = null
let wavingTimer = null
let wordsTimer = null
let currentX = 50
let currentY = 500
let direction = 1
let speed = 2

const getContainerBounds = () => {
  try {
    const windowInfo = uni.getWindowInfo()
    return {
      width: windowInfo.windowWidth || 375,
      height: windowInfo.windowHeight || 667,
      robotWidth: 120, // 考虑手臂伸出的总宽度
      robotHeight: 90
    }
  } catch (error) {
    return {
      width: 375,
      height: 667,
      robotWidth: 120, // 考虑手臂伸出的总宽度
      robotHeight: 90
    }
  }
}

const checkBubblePosition = () => {
  const bounds = getContainerBounds()
  const bubbleWidth = 130 // 对话框最大宽度
  const margin = 20 // 边距
  
  // 检查右边是否有足够空间
  const rightSpace = bounds.width - (currentX + bounds.robotWidth/2)
  const leftSpace = currentX + bounds.robotWidth/2
  
  if (rightSpace < bubbleWidth + margin) {
    bubbleOnRight.value = false // 右边空间不够，显示在左边
  } else if (leftSpace < bubbleWidth + margin) {
    bubbleOnRight.value = true // 左边空间不够，显示在右边
  } else {
    // 根据机器人朝向决定
    bubbleOnRight.value = direction === 1
  }
}

const toggleMovement = () => {
  isMoving.value = !isMoving.value
  if (isMoving.value) {
    showChatBubble.value = false // 移动时隐藏对话框
    startMoving()
  } else {
    stopMoving()
    // 暂停后检查位置并显示对话气泡
    checkBubblePosition()
    showChatBubble.value = true
  }
}

const startMoving = () => {
  if (!isMoving.value) return
  
  const animate = () => {
    if (!isMoving.value) return
    
    const bounds = getContainerBounds()
    
    currentX += speed * direction
    
    // 调整边界检测，考虑手臂宽度
    if (currentX >= bounds.width - bounds.robotWidth) {
      currentX = bounds.width - bounds.robotWidth
      direction = -1
      robotStyle.value.transform = 'scaleX(-1)'
    } else if (currentX <= 0) {
      currentX = 0
      direction = 1
      robotStyle.value.transform = 'scaleX(1)'
    }
    
    currentY = bounds.height - bounds.robotHeight - 50
    
    robotStyle.value.left = currentX + 'px'
    robotStyle.value.top = currentY + 'px'
  }
  
  animationTimer = setInterval(animate, 16)
}

const stopMoving = () => {
  if (animationTimer) {
    clearInterval(animationTimer)
    animationTimer = null
  }
}

const startWaving = () => {
  wavingTimer = setInterval(() => {
    isWaving.value = !isWaving.value
  }, 4000)
}

const startFloatingWords = () => {
  wordsTimer = setInterval(() => {
    showWords.value = true
    setTimeout(() => {
      showWords.value = false
    }, 2000)
  }, 6000)
}

const stopAllAnimations = () => {
  isWaving.value = false
  showWords.value = false
  
  if (animationTimer) clearInterval(animationTimer)
  if (wavingTimer) clearInterval(wavingTimer)
  if (wordsTimer) clearInterval(wordsTimer)
}

onMounted(() => {
  const bounds = getContainerBounds()
  currentY = bounds.height - bounds.robotHeight - 50
  robotStyle.value.top = currentY + 'px'
  
  setTimeout(() => {
    startWaving()
    startFloatingWords()
  }, 1000)
})

onUnmounted(() => {
  stopAllAnimations()
})
</script>

<style scoped>
.robot-container {
  position: fixed;
  width: 100px;
  height: 130px;
  z-index: 1000;
  transition: transform 0.5s ease;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  user-select: none;
  transform-style: preserve-3d;
}

.robot-head {
  width: 80px;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 40px;
  position: relative;
  margin: 0 auto 10px;
  box-shadow: 
    0 15px 30px rgba(102, 126, 234, 0.5), 
    inset 0 4px 8px rgba(255,255,255,0.3),
    0 0 0 5px rgba(79, 172, 254, 0.4);
  border: 3px solid #4facfe;
  overflow: visible;
}

.robot-head::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255,255,255,0.2), transparent);
  animation: shine 5s infinite;
  border-radius: 50%;
}

.head-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(79, 172, 254, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: glow-pulse 4s infinite alternate;
}

.eye {
  width: 18px;
  height: 18px;
  background: radial-gradient(circle at 35% 35%, #fff, #f8f9fa, #e9ecef);
  border-radius: 50%;
  position: absolute;
  top: 15px;
  animation: blink 7s infinite;
  transition: all 0.3s ease;
  box-shadow: 
    inset 0 4px 8px rgba(0,0,0,0.1), 
    0 4px 15px rgba(79, 172, 254, 0.5),
    0 0 0 3px #4facfe;
  border: 2px solid rgba(255,255,255,0.9);
}

.pupil {
  width: 11px;
  height: 11px;
  background: radial-gradient(circle at 30% 30%, #2c3e50, #34495e, #1a252f);
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 4px rgba(0,0,0,0.4);
}

.eye-shine {
  width: 6px;
  height: 6px;
  background: radial-gradient(circle, rgba(255,255,255,0.95), rgba(255,255,255,0.7));
  border-radius: 50%;
  position: absolute;
  top: 15%;
  left: 20%;
  animation: twinkle 5s infinite;
}

.eye-reflection {
  width: 4px;
  height: 4px;
  background: rgba(255,255,255,0.6);
  border-radius: 50%;
  position: absolute;
  bottom: 20%;
  right: 25%;
  animation: reflection-glow 3s infinite alternate;
}

.left-eye { left: 18px; }
.right-eye { right: 18px; }

.mouth {
  width: 24px;
  height: 12px;
  background: linear-gradient(135deg, #2c3e50, #34495e);
  border-radius: 0 0 24px 24px;
  position: absolute;
  bottom: 18px;
  left: 50%;
  transform: translateX(-50%);
  transition: all 0.5s ease;
  box-shadow: inset 0 3px 6px rgba(0,0,0,0.4);
  overflow: hidden;
}

.mouth.happy {
  width: 30px;
  height: 16px;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  border-radius: 0 0 30px 30px;
  box-shadow: 
    inset 0 3px 6px rgba(0,0,0,0.3),
    0 3px 12px rgba(231, 76, 60, 0.4);
  animation: happy-bounce 0.6s ease-out;
}

.mouth-inner {
  width: 100%;
  height: 60%;
  background: linear-gradient(135deg, #c0392b, #a93226);
  border-radius: 0 0 25px 25px;
  position: absolute;
  bottom: 0;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.mouth.happy .mouth-inner {
  opacity: 1;
}

.teeth {
  width: 20px;
  height: 4px;
  background: #fff;
  border-radius: 2px;
  position: absolute;
  top: 3px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.cheek {
  width: 8px;
  height: 8px;
  background: radial-gradient(circle, rgba(255, 182, 193, 0.8), transparent);
  border-radius: 50%;
  position: absolute;
  top: 32px;
  animation: cheek-blush 4s infinite alternate;
}

.left-cheek { left: 8px; }
.right-cheek { right: 8px; }

.robot-body {
  width: 64px;
  height: 44px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 50%, #667eea 100%);
  border-radius: 16px;
  margin: 0 auto 8px;
  position: relative;
  box-shadow: 
    0 8px 20px rgba(79, 172, 254, 0.4),
    inset 0 3px 8px rgba(255,255,255,0.3),
    0 0 0 3px rgba(102, 126, 234, 0.2);
  border: 2px solid rgba(255,255,255,0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.robot-body::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
  animation: body-shine 4s infinite;
}

.chest-screen {
  width: 28px;
  height: 16px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #4facfe;
  box-shadow: 
    inset 0 2px 4px rgba(0,0,0,0.6),
    0 0 12px rgba(79, 172, 254, 0.5);
  margin-bottom: 4px;
}

.screen-text {
  font-size: 8px;
  color: #00ff88;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  text-shadow: 0 0 6px #00ff88;
  animation: text-glow 2s infinite alternate;
}

.body-decoration {
  display: flex;
  gap: 8px;
  align-items: center;
}

.deco-line {
  width: 16px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #4facfe, transparent);
  border-radius: 1px;
  animation: line-pulse 3s infinite;
}

.line1 { animation-delay: 0s; }
.line2 { animation-delay: 1s; }

.top-antenna {
  position: absolute;
  top: -32px;
  left: 50%;
  transform: translateX(-50%);
}

.antenna-stick {
  width: 4px;
  height: 26px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  border-radius: 2px;
  position: relative;
  box-shadow: 0 3px 10px rgba(79, 172, 254, 0.5);
}

.antenna-top-ball {
  width: 18px;
  height: 18px;
  background: radial-gradient(circle at 30% 30%, #ff6b6b, #ee5a52, #dc3545);
  border-radius: 50%;
  position: absolute;
  top: -9px;
  left: -7px;
  animation: antenna-glow 3s infinite;
  box-shadow: 
    0 0 25px rgba(255, 107, 107, 0.9),
    0 5px 15px rgba(255, 107, 107, 0.5),
    inset 0 3px 6px rgba(255,255,255,0.4);
}

.ball-highlight {
  width: 7px;
  height: 7px;
  background: radial-gradient(circle, rgba(255,255,255,0.9), transparent);
  border-radius: 50%;
  position: absolute;
  top: 3px;
  left: 3px;
  animation: highlight-shimmer 2s infinite;
}

.robot-arms {
  position: absolute;
  top: 55px;
  left: 50%;
  transform: translateX(-50%);
  width: 120px;
  height: 35px;
}

.arm {
  position: absolute;
  width: 22px;
  height: 35px;
  transform-origin: top center;
}

.arm-joint {
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
}

.hand-ball {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 50%;
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 
    0 8px 20px rgba(79, 172, 254, 0.6),
    inset 0 4px 8px rgba(255,255,255,0.4),
    0 0 0 4px rgba(102, 126, 234, 0.3);
  border: 3px solid #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.hand-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: radial-gradient(circle, rgba(79, 172, 254, 0.3), transparent);
  border-radius: 50%;
  animation: hand-pulse 3s infinite alternate;
}

.hand-text {
  font-size: 10px;
  color: #fff;
  font-weight: bold;
  font-family: 'Arial', sans-serif;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  z-index: 1;
}

.left-arm {
  left: -18px;
  transform: rotate(-20deg);
}

.right-arm {
  right: -18px;
  transform: rotate(20deg);
}

.arm.waving {
  animation: wave-arm 2s ease-in-out infinite;
}

.floating-words {
  position: absolute;
  top: -85px;
  left: -45px;
  width: 190px;
  height: 80px;
  z-index: 3;
}

.word {
  position: absolute;
  font-size: 16px;
  color: #4facfe;
  font-weight: bold;
  font-family: 'Arial', sans-serif;
  animation: float-up 3s ease-out;
  text-shadow: 0 4px 8px rgba(79, 172, 254, 0.5);
}

.word1 {
  left: 25px;
  animation-delay: 0s;
}

.word2 {
  left: 85px;
  animation-delay: 0.6s;
}

.word3 {
  left: 145px;
  animation-delay: 1.2s;
}

.robot-shadow {
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  width: 90px;
  height: 12px;
  background: radial-gradient(ellipse, rgba(102, 126, 234, 0.4) 0%, transparent 70%);
  border-radius: 50%;
  animation: shadow-pulse 4s infinite alternate;
}

@keyframes shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  100% { transform: translateX(100%) translateY(100%) rotate(45deg); }
}

@keyframes glow-pulse {
  0% { opacity: 0.7; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.08); }
}

@keyframes blink {
  0%, 85%, 100% { opacity: 1; }
  90% { opacity: 0.1; }
}

@keyframes twinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.7); }
}

@keyframes reflection-glow {
  0% { opacity: 0.4; }
  100% { opacity: 0.8; }
}

@keyframes happy-bounce {
  0% { transform: translateX(-50%) scale(1); }
  50% { transform: translateX(-50%) scale(1.1); }
  100% { transform: translateX(-50%) scale(1); }
}

@keyframes cheek-blush {
  0% { opacity: 0.6; transform: scale(1); }
  100% { opacity: 1; transform: scale(1.2); }
}

@keyframes led-blink {
  0%, 50% { opacity: 1; }
  25% { opacity: 0.3; }
}

@keyframes antenna-glow {
  0% { 
    box-shadow: 
      0 0 25px rgba(255, 107, 107, 0.9),
      0 5px 15px rgba(255, 107, 107, 0.5),
      inset 0 3px 6px rgba(255,255,255,0.4);
  }
  50% { 
    box-shadow: 
      0 0 35px rgba(255, 107, 107, 1),
      0 8px 25px rgba(255, 107, 107, 0.7),
      inset 0 3px 6px rgba(255,255,255,0.5);
  }
  100% { 
    box-shadow: 
      0 0 25px rgba(255, 107, 107, 0.9),
      0 5px 15px rgba(255, 107, 107, 0.5),
      inset 0 3px 6px rgba(255,255,255,0.4);
  }
}

@keyframes highlight-shimmer {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 1; }
}

@keyframes hand-pulse {
  0% { opacity: 0.4; transform: scale(1); }
  100% { opacity: 0.7; transform: scale(1.1); }
}

@keyframes wave-arm {
  0%, 100% { transform: rotate(-20deg); }
  25% { transform: rotate(-45deg); }
  50% { transform: rotate(-20deg); }
  75% { transform: rotate(-45deg); }
}

.right-arm.waving {
  animation: wave-arm-right 2s ease-in-out infinite;
}

@keyframes wave-arm-right {
  0%, 100% { transform: rotate(20deg); }
  25% { transform: rotate(45deg); }
  50% { transform: rotate(20deg); }
  75% { transform: rotate(45deg); }
}

@keyframes float-up {
  0% {
    opacity: 0;
    transform: translateY(0px) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translateY(-30px) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-60px) scale(0.8);
  }
}

@keyframes shadow-pulse {
  0% { opacity: 0.4; transform: translateX(-50%) scale(1); }
  100% { opacity: 0.7; transform: translateX(-50%) scale(1.15); }
}

@keyframes body-shine {
  0% { left: -100%; }
  100% { left: 100%; }
}

@keyframes text-glow {
  0% { text-shadow: 0 0 6px #00ff88; }
  100% { text-shadow: 0 0 12px #00ff88, 0 0 18px #00ff88; }
}

@keyframes line-pulse {
  0%, 100% { opacity: 0.3; transform: scaleX(0.8); }
  50% { opacity: 1; transform: scaleX(1.2); }
}

.chat-bubble {
  position: absolute;
  top: -45px;
  left: 140%;
  transform: translateX(-50%) translateZ(0);
  transform-style: flat;
  backface-visibility: visible;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(79, 172, 254, 0.25);
  border-radius: 14px;
  padding: 8px 12px;
  min-width: 100px;
  max-width: 130px;
  box-shadow: 
    0 6px 20px rgba(0,0,0,0.08),
    0 1px 4px rgba(79, 172, 254, 0.15);
  z-index: 6;
  animation: bubble-appear 0.35s ease-out;
  transform-origin: center;
}

.chat-bubble.bubble-left {
  left: -40%;
}

.bubble-text {
  font-size: 10px;
  color: #2c3e50;
  line-height: 1.4;
  text-align: center;
  word-wrap: break-word;
  font-weight: 500;
  letter-spacing: 0.3px;
}

.bubble-arrow {
  position: absolute;
  bottom: -5px;
  left: 10%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 5px solid transparent;
  border-right: 5px solid transparent;
  border-top: 5px solid rgba(79, 172, 254, 0.25);
}

.robot-container[style*="scaleX(-1)"] .chat-bubble {
  left: -40%;
  transform: translateX(-50%) scaleX(-1);
}

.robot-container[style*="scaleX(-1)"] .bubble-arrow {
  left: 10%;
  transform: translateX(-50%);
}

@keyframes bubble-appear {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(12px) scale(0.88);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}
</style>







