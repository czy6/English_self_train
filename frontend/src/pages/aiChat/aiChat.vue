<template>
  <view class="chat-container">
    <!-- AI 粒子背景 -->
    <view class="ai-background">
      <view class="particle particle-1"></view>
      <view class="particle particle-2"></view>
      <view class="particle particle-3"></view>
      <view class="particle particle-4"></view>
      <view class="particle particle-5"></view>
      <view class="ai-grid"></view>
    </view>

    <!-- 顶部栏 -->
    <view class="top-bar">
      <view class="menu-btn" v-if="aiStore.showMenu" @click="aiStore.toggleHistory">☰</view>
      <view class="new-chat-btn" @click="aiStore.createNewChat">新会话</view>
    </view>

    <!-- 历史记录面板（优化美观版） -->
    <view class="history-panel" :class="{ show: aiStore.showHistory }">
      <view class="history-header">
        <text class="history-title">聊天记录</text>
        <view class="clear-btn" @click="clearAllChats">清空</view>
      </view>
      <view class="chat-list">
        <!-- 日期分组遍历 -->
        <template v-for="(group, groupIdx) in groupedChats" :key="group.date">
          <view
            class="chat-group-title"
            :style="{ borderTopLeftRadius: groupIdx === 0 ? '24rpx' : '0' }"
          >
            {{ formatDate(group.date) }}
          </view>
          <view
            v-for="(chat, idx) in group.chats"
            :key="chat.id"
            class="chat-item"
            :class="{ active: aiStore.activeChat === chat.id }"
            @click="switchChat(chat.id)"
          >
            <text class="chat-title ellipsis">{{ chat.title }}</text>
            <text class="delete-btn" @click.stop="deleteChat(chat.id)">×</text>
          </view>
        </template>
      </view>
    </view>

    <!-- 遮罩层 -->
    <view class="mask" v-show="aiStore.showHistory" @click="aiStore.toggleHistory"></view>

    <!-- 主聊天区 -->
    <view class="main-panel">
      <scroll-view
        class="chat-content"
        scroll-y="true"
        :scroll-top="scrollTop"
        :scroll-with-animation="true"
        :enhanced="true"
        @scroll="handleScroll"
      >
        <!-- 欢迎消息 - 高科技 AI 版 -->
        <view v-if="aiStore.currentChat.messages.length === 0" class="welcome-message">
          <view class="ai-avatar-container">
            <view class="ai-avatar-ring ring-1"></view>
            <view class="ai-avatar-ring ring-2"></view>
            <view class="ai-avatar-ring ring-3"></view>
            <image class="welcome-avatar" :src="botAvatar" mode="aspectFill" />
          </view>
          <view class="welcome-text">
            <view class="welcome-title">
              <text class="title-gradient">你好！有什么可以帮助你的吗？</text>
            </view>
            <view class="welcome-subtitle">
              <text class="subtitle-text">可以输入文字或上传图片进行交流</text>
            </view>
            <view class="ai-features">
              <view class="feature-item" @click="jumpToPage('listening')">
                <view class="feature-icon-box">
                  <text class="feature-icon">🎧</text>
                </view>
                <text class="feature-text">听力训练</text>
              </view>
              <view class="feature-item" @click="jumpToPage('reading')">
                <view class="feature-icon-box">
                  <text class="feature-icon">📖</text>
                </view>
                <text class="feature-text">阅读理解</text>
              </view>
              <view class="feature-item" @click="jumpToPage('translation')">
                <view class="feature-icon-box">
                  <text class="feature-icon">💬</text>
                </view>
                <text class="feature-text">翻译助手</text>
              </view>
              <view class="feature-item" @click="jumpToPage('writing')">
                <view class="feature-icon-box">
                  <text class="feature-icon">📝</text>
                </view>
                <text class="feature-text">智能写作</text>
              </view>
            </view>
          </view>
        </view>

        <view
          v-for="(msg, index) in aiStore.currentChat.messages"
          :key="index"
          class="message-item"
          :class="{ user: msg.role === 'user' }"
        >
          <!-- 头像 -->
          <image
            class="avatar"
            :src="msg.role === 'user' ? userAvatar || defaultUserAvatar : botAvatar"
            mode="aspectFill"
            object-fit="cover"
          />
          <view class="message-bubble">
            <view class="message-content" :title="msg.content">
              <!-- 复制按钮（仅 AI 回复显示） -->
              <view
                v-if="msg.role === 'assistant' && !msg.isLoading && msg.content"
                class="copy-btn"
                @click="copyMessage(msg.content)"
                hover-class="copy-btn-hover"
                :hover-start-time="100"
                :hover-stay-time="100"
              >
                <text class="copy-icon">📋</text>
              </view>

              <!-- 文本内容 -->
              <template v-if="typeof msg.content === 'string' && msg.content.trim()">
                <span v-html="formatMessage(msg.content)"></span>
              </template>

              <!-- 图片内容 -->
              <view v-if="msg.imageList && msg.imageList.length" class="image-group">
                <image
                  v-for="(img, imgIdx) in msg.imageList"
                  :key="imgIdx"
                  :src="img"
                  mode="aspectFill"
                  class="message-image"
                  @click="previewImage(msg.imageList, imgIdx)"
                />
              </view>

              <!-- 加载动画 -->
              <template v-if="msg.isLoading && msg.role === 'assistant'">
                <view class="loading-indicator">
                  <text class="dot" :style="{ animationDelay: '0s' }"></text>
                  <text class="dot" :style="{ animationDelay: '0.2s' }"></text>
                  <text class="dot" :style="{ animationDelay: '0.4s' }"></text>
                </view>
              </template>

              <!-- 跳转按钮 -->
              <view
                v-if="msg.nvgQuestionType && msg.nvgQuestionId"
                class="ai-jump-btn"
                @click.stop="aiStore.handleJump(msg.nvgQuestionType, msg.nvgQuestionId)"
              >
                <view class="jump-label">点击开始训练</view>
                <view class="jump-sub">跳转至{{ msg.nvgQuestionType }}训练页</view>
              </view>
            </view>
            <text class="message-time">{{ msg.sendTime }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 输入栏 - 恢复原来的结构 -->
      <view class="input-bar">
        <!-- 图片预览容器 - 美化版 -->
        <view
          class="image-preview-container"
          v-if="aiStore.selectedImages && aiStore.selectedImages.length > 0"
        >
          <view
            class="image-item"
            v-for="(img, index) in aiStore.selectedImages"
            :key="index"
            hover-class="image-item-hover"
            :hover-start-time="150"
            :hover-stay-time="100"
          >
            <image
              :src="img"
              mode="aspectFill"
              class="preview-image"
              @click="handlePreviewImage(index)"
            />
            <view
              class="delete-image"
              @click.stop="removeImage(index)"
              hover-class="delete-image-hover"
              :hover-start-time="100"
              :hover-stay-time="100"
              >×</view
            >
          </view>
        </view>

        <!-- 输入控件 - 多行输入 -->
        <view class="input-controls">
          <view class="attach-btn" @tap="showMoreOptions">+</view>

          <textarea
            class="input-box"
            v-model="aiStore.inputValue"
            placeholder="输入消息..."
            :maxlength="500"
            :auto-height="true"
            :fixed="true"
            @confirm="sendMessageWithScroll"
          />

          <button
            class="send-btn"
            :class="{
              disabled: aiStore.isSending && !aiStore.isStreaming,
              pause: aiStore.isStreaming,
            }"
            @tap="handleSendOrPause"
          >
            {{ getSendButtonText() }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, onMounted, computed, watch, reactive, toRefs } from 'vue'
import { useAiStore, useUserStore } from '@/store'

// 状态管理
const aiStore = useAiStore()
const userStore = useUserStore()
const scrollTop = ref(0)

// 流式输出相关
let streamingInterval = null
const MAX_STREAMING_TIME = 5000

// 停止流式输出
const stopStreaming = () => {
  if (streamingInterval) {
    clearInterval(streamingInterval)
    streamingInterval = null
  }
}

// 静态资源
const defaultUserAvatar = '/static/images/user-avatar.png'
const botAvatar = '/static/images/bot-avatar.png'

// 用户头像
const userAvatar = computed(() => userStore.userInfo?.avatar || defaultUserAvatar)

// 动画状态
const state = reactive({
  sendButtonTransform: 'scale(1)',
  isButtonPressed: false,
})

// 时间格式化
const formatTime = (date) => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 跳转到指定页面
const jumpToPage = (page) => {
  const pageMap = {
    writing: '/pages/writing/writing',
    listening: '/pages/listening/listening',
    reading: '/pages/reading/reading',
    translation: '/pages/translation/translation',
  }
  const targetPage = pageMap[page]
  if (targetPage) {
    uni.navigateTo({
      url: targetPage,
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({
          title: '跳转失败',
          icon: 'none',
        })
      },
    })
  }
}

// 日期分组计算属性
const groupedChats = computed(() => {
  const groups = {}
  aiStore.chatList.forEach((chat) => {
    const dateKey = chat.startTime.split('T')[0]
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(chat)
  })
  return Object.entries(groups)
    .sort((a, b) => new Date(b[0]) - new Date(a[0]))
    .map(([date, chats]) => ({ date, chats }))
})

// 日期格式化
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}月${day}日`
}

// 监听消息变化，自动滚动到底部
watch(
  () => aiStore.currentChat?.messages || [],
  (newMessages, oldMessages) => {
    if (!newMessages || !oldMessages) return

    if (newMessages.length > oldMessages.length) {
      // 新消息添加时滚动到底部
      setTimeout(() => {
        scrollToBottom()
      }, 100)
    } else if (newMessages.length === oldMessages.length && newMessages.length > 0) {
      // 消息内容更新时（如流式输出）也滚动
      const lastMessage = newMessages[newMessages.length - 1]
      if (lastMessage && lastMessage.role === 'assistant') {
        setTimeout(() => {
          scrollToBottom()
        }, 50)
      }
    }
  },
  { deep: true, immediate: true },
)

// 监听聊天切换，切换后滚动到底部
watch(
  () => aiStore.activeChat,
  () => {
    setTimeout(() => {
      scrollToBottom()
    }, 200)
  },
)

// 简化的滚动方法
const scrollToBottom = () => {
  scrollTop.value = scrollTop.value + 1 // 先触发变化
  nextTick(() => {
    scrollTop.value = 99999 // 再设置大值
  })
}

// 消息格式化
const formatMessage = (content) => {
  return typeof content === 'string' ? content.replace(/\n/g, '<br>') : ''
}

// 复制消息
const copyMessage = (content) => {
  if (!content) {
    uni.showToast({
      title: '复制内容为空',
      icon: 'none',
    })
    return
  }
  uni.setClipboardData({
    data: content,
    success: () => {
      uni.showToast({
        title: '复制成功',
        icon: 'success',
        duration: 1500,
      })
    },
    fail: () => {
      uni.showToast({
        title: '复制失败',
        icon: 'none',
      })
    },
  })
}

// 移除图片
const removeImage = (index) => {
  aiStore.selectedImages.splice(index, 1)
}

// 处理图片预览
const handlePreviewImage = (index) => {
  previewImage(aiStore.selectedImages, index)
}

// 预览图片
const previewImage = (images, current) => {
  try {
    if (!images || images.length === 0) {
      return
    }
    uni.previewImage({
      urls: images,
      current: current,
      indicator: 'default',
      loop: true,
    })
  } catch (error) {
    console.error('预览图片失败:', error)
    uni.showToast({
      title: '预览失败',
      icon: 'none',
    })
  }
}

// 附件菜单
const showMoreOptions = () => {
  uni.showActionSheet({
    itemList: ['图片', '拍照', '文件', '位置'],
    itemColor: '#333',
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          chooseImageFromAlbum()
          break
        case 1:
          takePhoto()
          break
        default:
          uni.showToast({ title: '功能开发中', icon: 'none' })
      }
    },
  })
}

// 选择图片（相册）
const chooseImageFromAlbum = () => {
  const maxCount = 9 - aiStore.selectedImages.length
  if (maxCount <= 0) return
  uni.chooseImage({
    count: maxCount,
    sizeType: ['compressed'],
    sourceType: ['album'],
    success: (res) => {
      aiStore.selectedImages = [...aiStore.selectedImages, ...res.tempFilePaths]
    },
  })
}

// 拍照
const takePhoto = () => {
  if (aiStore.selectedImages.length >= 9) {
    uni.showToast({ title: '最多选择9张图片', icon: 'none' })
    return
  }
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['camera'],
    success: (res) => {
      aiStore.selectedImages.push(...res.tempFilePaths)
    },
  })
}

// 清空聊天
const clearAllChats = () => {
  uni.showModal({
    title: '确认清空',
    content: '所有聊天记录将被删除，不可恢复',
    success: (res) => {
      if (res.confirm) aiStore.deleteAllChat()
    },
  })
}

// 删除单条聊天
const deleteChat = (chatId) => {
  uni.showModal({
    title: '删除会话',
    content: '确定删除该会话吗？',
    success: (res) => {
      if (res.confirm) aiStore.deleteChat(chatId)
    },
  })
}

// 切换聊天
const switchChat = (chatId) => {
  aiStore.selectChat(chatId)
  setTimeout(() => {
    scrollToBottom()
  }, 300)
}

// 发送消息后立即滚动
const sendMessageWithScroll = async () => {
  if (aiStore.isSending) return
  if (aiStore.inputValue.trim() || aiStore.selectedImages.length > 0) {
    // 判断会话列表是不是空，如果是空先创建会话
    if (aiStore.activeChat.value === '') {
      await aiStore.createNewChat()
      // 如果创建失败，activeChat 还是空，直接返回
      if (aiStore.activeChat.value === '') {
        return
      }
    }

    // 调用 store 的 sendMessage 发送消息
    await aiStore.sendMessage()
  }
}

// 发送按钮释放
const onSendButtonRelease = () => {
  if (state.isButtonPressed) {
    state.isButtonPressed = false
    state.sendButtonTransform = 'scale(1)'
  }
}

// 滚动状态
const showScrollToBottom = ref(false)

// 滚动事件处理
const handleScroll = (e) => {
  const { scrollTop, scrollHeight } = e.detail
  const threshold = 100
  showScrollToBottom.value = scrollTop + 600 < scrollHeight - threshold // 使用固定高度估算
}

// 移除 onMounted 中的滚动监听，改用 scroll-view 的 @scroll 事件
onMounted(() => {
  userStore.getUserInfo()
}) // 初始化
onMounted(() => {
  userStore.getUserInfo()

  watch(
    () => aiStore.currentChat.messages,
    () => scrollToBottom(),
    { deep: true, immediate: true },
  )
  watch(
    () => aiStore.activeChat,
    () => scrollToBottom(true),
  )
})

// 解构响应式对象
const { sendButtonTransform } = toRefs(state)

// 处理发送或暂停
const handleSendOrPause = () => {
  if (aiStore.isStreaming) {
    // 如果正在流式输出，则暂停/继续
    aiStore.toggleStreamingPause()
  } else {
    // 否则发送消息
    sendMessageWithScroll()
  }
}

// 获取按钮文本
const getSendButtonText = () => {
  if (aiStore.isStreaming) {
    return aiStore.isPaused ? '继续' : '暂停'
  } else if (aiStore.isSending) {
    return '发送中'
  } else {
    return '发送'
  }
}
</script>

<style scoped>
/* 全局布局 - 现代渐变背景 */
.chat-container {
  position: relative;
  height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f8f9ff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 顶部栏 - 现代简约设计 */
.top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  padding: 0 36rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.08);
}
.menu-btn {
  font-size: 36rpx;
  color: #667eea;
  transition: all 0.3s ease;
  padding: 12rpx;
  border-radius: 12rpx;
}
.menu-btn:active {
  transform: scale(0.9);
  background: rgba(102, 126, 234, 0.1);
}
.new-chat-btn {
  font-size: 28rpx;
  color: #fff;
  padding: 16rpx 32rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}
.new-chat-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.2);
}

/* 历史记录面板 - 高级玻璃效果 */
.history-panel {
  position: fixed;
  top: 88rpx;
  left: -380rpx;
  width: 380rpx;
  height: calc(100% - 88rpx);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(30rpx);
  border-radius: 0 32rpx 32rpx 0;
  box-shadow: 0 16rpx 48rpx rgba(102, 126, 234, 0.15);
  transition: left 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 101;
  overflow: hidden;
  border: 1px solid rgba(102, 126, 234, 0.1);
}
.history-panel.show {
  left: 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 36rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}
.history-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.clear-btn {
  font-size: 26rpx;
  color: #ff6b6b;
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  background: rgba(255, 107, 107, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 107, 107, 0.2);
}
.clear-btn:active {
  background: rgba(255, 107, 107, 0.2);
  transform: scale(0.95);
}

/* 聊天列表 */
.chat-list {
  max-height: calc(100% - 92rpx);
  overflow-y: auto;
  padding: 12rpx 0 140rpx 0;
}
.chat-list::-webkit-scrollbar {
  width: 8rpx;
}
.chat-list::-webkit-scrollbar-track {
  background: transparent;
}
.chat-list::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8rpx;
  transition: background 0.2s;
}
.chat-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

/* 日期分组标题 */
.chat-group-title {
  padding: 20rpx 30rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #666;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  transition: background 0.2s;
}
.chat-group-title:first-child {
  border-top-left-radius: 24rpx;
}

/* 聊天项 - 现代卡片设计 */
.chat-item {
  position: relative;
  padding: 24rpx 36rpx;
  margin: 12rpx 20rpx;
  font-size: 28rpx;
  color: #555;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10rpx);
  border: 1px solid rgba(102, 126, 234, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.08);
}
.chat-item:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
  background: rgba(255, 255, 255, 0.95);
}
.chat-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border-color: #667eea;
  color: #667eea;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.25);
}

/* 删除按钮 */
.delete-btn {
  width: 36rpx;
  height: 36rpx;
  font-size: 28rpx;
  color: #ff6b6b;
  border-radius: 50%;
  background: rgba(255, 107, 107, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0.8;
  transition: all 0.2s;
  border: 1px solid rgba(255, 107, 107, 0.2);
}
.delete-btn:active {
  background: rgba(255, 107, 107, 0.2);
  opacity: 1;
  transform: scale(1.1);
}

/* 遮罩层 - 柔和效果 */
.mask {
  position: fixed;
  top: 88rpx;
  left: 0;
  width: 100%;
  height: calc(100% - 88rpx);
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(5rpx);
  z-index: 100;
  transition: all 0.3s ease;
  opacity: 1;
}
.mask:active {
  background: rgba(0, 0, 0, 0.4);
}

/* 主聊天区 - 固定布局 */
.main-panel {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: transparent;
  z-index: 10;
}
.chat-content {
  flex: 1;
  /* 使用固定高度，避免 flex 布局在微信小程序中的兼容性问题 */
  height: calc(100vh - 228rpx);
  /* 左右对称 padding，滚动条在容器内部右侧 */
  padding: 20rpx 20rpx 120rpx 20rpx; /* 底部增加 padding，避免被输入框遮挡 */
  overflow-y: scroll !important;
  overflow-x: hidden;
  min-height: 0;
  z-index: 1;
  position: relative;
  box-sizing: border-box;
  /* 确保滚动条在容器内部 */
  scrollbar-width: thin;
}
.chat-content::-webkit-scrollbar {
  width: 8rpx;
}
.chat-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4rpx;
}

/* 欢迎消息 - 现代设计 */
.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 40rpx 30rpx 20rpx;
  color: #333;
  text-align: center;
  width: 100%;
  position: relative;
  z-index: 2;
}
.welcome-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  margin-bottom: 40rpx;
  box-shadow: 0 16rpx 48rpx rgba(102, 126, 234, 0.3);
  animation: pulse 3s infinite;
  border: 6rpx solid rgba(255, 255, 255, 0.8);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.welcome-title {
  font-size: 36rpx;
  margin-bottom: 20rpx;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.welcome-subtitle {
  font-size: 28rpx;
  color: #999;
}

/* 消息项：对称布局 */
.message-item {
  display: flex;
  flex-direction: row; /* AI 默认：头像左，气泡右 */
  margin-bottom: 40rpx;
  max-width: 90%;
  /* 移除动画初始状态，直接显示，避免微信小程序兼容性问题 */
  opacity: 1;
  transform: translateY(0);
  align-items: flex-start;
  position: relative;
  z-index: 2;
}
.message-item.user {
  flex-direction: row-reverse; /* 用户反转：头像右，气泡左 */
  margin-left: auto; /* 右对齐 */
}

/* 头像 - 添加光晕效果 */
.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  /* 左右对称 margin */
  margin: 8rpx 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
  border: 3rpx solid rgba(255, 255, 255, 0.9);
}
.message-item.user .avatar {
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  border-color: rgba(102, 126, 234, 0.3);
}

/* 消息气泡 */
.message-bubble {
  max-width: calc(100% - 160rpx);
  position: relative;
  word-wrap: break-word;
  word-break: break-all;
}
.message-item.user .message-bubble {
  max-width: 75%;
}

.message-content {
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  min-height: 40rpx;
  word-break: break-word;
  word-wrap: break-word;
  white-space: pre-wrap;
  position: relative;
  transition: all 0.3s ease;
  text-align: justify;
  text-justify: inter-ideograph;
  overflow-wrap: break-word;
  hyphens: none;
  word-spacing: normal;
  letter-spacing: normal;
}
.message-item:not(.user) .message-content {
  background: #fff;
  color: #333;
  border-top-left-radius: 8rpx;
  border: 1px solid rgba(102, 126, 234, 0.1);
}
.message-item.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-top-right-radius: 8rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

/* 消息图片 */
.image-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  margin-top: 15rpx;
}
.message-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  object-fit: cover;
  margin: 5rpx;
  transition: all 0.2s;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.15);
  border: 2rpx solid rgba(255, 255, 255, 0.8);
}
.message-image:hover {
  transform: scale(1.05);
  box-shadow: 0 6rpx 16rpx rgba(102, 126, 234, 0.25);
}

/* 加载动画 */
.loading-indicator {
  display: flex;
  justify-content: center;
  padding: 10rpx 0;
}
.dot {
  width: 12rpx;
  height: 12rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  margin: 0 8rpx;
  animation: dotPulse 1.4s infinite ease-in-out both;
}
.message-item.user .dot {
  background: rgba(255, 255, 255, 0.8);
}

/* 时间戳 */
.message-time {
  font-size: 22rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
  text-align: left;
  opacity: 0.8;
  padding: 0 8rpx;
}
.message-item.user .message-time {
  text-align: right;
}

/* 复制按钮 */
.copy-btn {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0;
  transform: scale(0.8);
}
.message-content:hover .copy-btn {
  opacity: 1;
  transform: scale(1);
}
.copy-btn-hover {
  background: rgba(102, 126, 234, 0.15) !important;
  transform: scale(1.1) !important;
}
.copy-icon {
  font-size: 28rpx;
  line-height: 1;
}

/* 输入栏 - 强制固定在底部 */
.input-bar {
  position: fixed !important;
  bottom: 0 !important;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20rpx);
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 -4rpx 16rpx rgba(102, 126, 234, 0.08);
  z-index: 999 !important;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-height: 140rpx;
  max-height: 400rpx;
  width: 100%;
}

/* 图片预览容器 - 科技感美化样式 */
.image-preview-container {
  padding: 16rpx 24rpx 8rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  border-bottom: 2rpx solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(180deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.05) 100%);
  max-height: 172rpx;
  overflow-y: auto;
  box-sizing: border-box;
  align-content: flex-start;
  flex-shrink: 0;
  justify-content: flex-start;
  position: relative;
  backdrop-filter: blur(10rpx);
  box-shadow: inset 0 2rpx 8rpx rgba(102, 126, 234, 0.1);
}

.image-preview-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1rpx;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(102, 126, 234, 0.8) 50%,
    transparent 100%
  );
  box-shadow: 0 0 10rpx rgba(102, 126, 234, 0.5);
}

.image-item {
  position: relative;
  width: 150rpx;
  height: 150rpx;
  flex-shrink: 0;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3), 0 0 20rpx rgba(102, 126, 234, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2rpx solid rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(240, 240, 255, 0.9) 100%);
}

.image-item::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(102, 126, 234, 0.1) 50%,
    transparent 70%
  );
  transform: rotate(45deg);
  transition: all 0.6s ease;
  opacity: 0;
}

.image-item:hover {
  transform: translateY(-6rpx) scale(1.03);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.5), 0 0 30rpx rgba(102, 126, 234, 0.3),
    inset 0 0 20rpx rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.8);
}

.image-item:hover::after {
  opacity: 1;
  animation: scan 2s linear infinite;
}

.image-item-hover {
  transform: scale(0.96);
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.4);
}

.image-item:active {
  transform: scale(0.95);
}

.delete-image-hover {
  transform: scale(1.2) rotate(90deg);
  background: linear-gradient(135deg, #ff4757 0%, #ff3742 100%);
  box-shadow: 0 6rpx 20rpx rgba(255, 71, 87, 0.6), 0 0 30rpx rgba(255, 71, 87, 0.4);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
  display: block;
}

.image-item:active .preview-image {
  transform: scale(0.95);
}

.delete-image {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 44rpx;
  height: 44rpx;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.95) 0%, rgba(255, 71, 87, 0.95) 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: bold;
  border: 2rpx solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 4rpx 16rpx rgba(255, 75, 87, 0.5), 0 0 20rpx rgba(255, 75, 87, 0.3);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
  backdrop-filter: blur(10rpx);
}

.delete-image:hover {
  transform: scale(1.2) rotate(90deg);
  background: linear-gradient(135deg, #ff4757 0%, #ff3742 100%);
  box-shadow: 0 6rpx 24rpx rgba(255, 75, 87, 0.6), 0 0 30rpx rgba(255, 75, 87, 0.4);
}

.delete-image:active {
  transform: scale(0.9);
}

/* 滚动条科技感美化 */
.image-preview-container::-webkit-scrollbar {
  width: 8rpx;
}

.image-preview-container::-webkit-scrollbar-track {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 8rpx;
}

.image-preview-container::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 8rpx;
  box-shadow: 0 0 10rpx rgba(102, 126, 234, 0.5);
}

.image-preview-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #5a6fd8 0%, #6a4190 100%);
  box-shadow: 0 0 16rpx rgba(102, 126, 234, 0.8);
}

@keyframes scan {
  0% {
    transform: translateX(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) rotate(45deg);
  }
}

/* 输入控件 - 多行自适应 */
.input-controls {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 24rpx 30rpx;
  min-height: 88rpx;
  width: 100%;
  box-sizing: border-box;
  flex-shrink: 0;
}

.attach-btn {
  width: 68rpx;
  height: 68rpx;
  min-height: 68rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #667eea;
  flex-shrink: 0;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.1);
}

.attach-btn:active {
  transform: scale(0.95);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15), rgba(118, 75, 162, 0.15));
}

.input-box {
  flex: 1;
  min-height: 68rpx;
  max-height: 200rpx;
  padding: 16rpx 24rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  border-radius: 34rpx;
  font-size: 28rpx;
  background: rgba(255, 255, 255, 0.95);
  line-height: 1.5;
  word-wrap: break-word;
  word-break: break-all;
  white-space: pre-wrap;
  overflow-y: auto;
  transition: all 0.3s ease;
  box-sizing: border-box;
  font-family: inherit;
}

.input-box:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4rpx rgba(102, 126, 234, 0.1);
  background: #fff;
}

.input-box::-webkit-scrollbar {
  width: 4rpx;
}

.input-box::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 4rpx;
}

.send-btn {
  min-width: 120rpx;
  height: 68rpx;
  min-height: 68rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 34rpx;
  font-size: 28rpx;
  padding: 0 28rpx;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.send-btn.disabled {
  background: #d9d9d9;
  color: #999;
  box-shadow: none;
}

.send-btn.pause {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4757 100%);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 107, 0.3);
}

.send-btn:active:not(.disabled) {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.2);
}

/* 跳转按钮 - 美化样式 */
::v-deep .ai-jump-btn {
  display: block !important;
  margin-top: 20rpx;
  padding: 24rpx 36rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  color: #fff;
  font-size: 28rpx;
  text-align: center;
  cursor: pointer;
  box-shadow: 0 6rpx 18rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  border: none;
}
::v-deep .ai-jump-btn:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
}
::v-deep .ai-jump-btn:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);
}

/* 动画 */
@keyframes fadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes dotPulse {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
@keyframes spin {
  to {
    transform: rotate(360deg);
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
@keyframes bounceIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  70% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 省略号 */
.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 触底悬浮按钮 */
.scroll-to-bottom-btn {
  position: fixed;
  bottom: 200rpx;
  right: 30rpx;
  width: 80rpx;
  height: 80rpx;
  background: #1677ff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}
.scroll-to-bottom-btn:hover {
  background: #0958d9;
}
.scroll-to-bottom-btn:active {
  transform: scale(0.95);
}

/* ========== 高科技 AI 背景样式 ========== */
.ai-background {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.3) 0%, transparent 70%);
  animation: float 20s infinite ease-in-out;
  filter: blur(20rpx);
}

.particle-1 {
  width: 200rpx;
  height: 200rpx;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.particle-2 {
  width: 300rpx;
  height: 300rpx;
  top: 60%;
  right: 10%;
  animation-delay: 5s;
}

.particle-3 {
  width: 150rpx;
  height: 150rpx;
  bottom: 20%;
  left: 20%;
  animation-delay: 10s;
}

.particle-4 {
  width: 250rpx;
  height: 250rpx;
  top: 30%;
  right: 30%;
  animation-delay: 15s;
}

.particle-5 {
  width: 180rpx;
  height: 180rpx;
  bottom: 40%;
  left: 60%;
  animation-delay: 8s;
}

.ai-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: linear-gradient(rgba(102, 126, 234, 0.05) 1rpx, transparent 1rpx),
    linear-gradient(90deg, rgba(102, 126, 234, 0.05) 1rpx, transparent 1rpx);
  background-size: 100rpx 100rpx;
  animation: gridMove 20s linear infinite;
  opacity: 0.3;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30rpx, -30rpx) scale(1.1);
  }
  50% {
    transform: translate(-20rpx, 20rpx) scale(0.9);
  }
  75% {
    transform: translate(20rpx, 30rpx) scale(1.05);
  }
}

@keyframes gridMove {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 100rpx 100rpx;
  }
}

/* ========== 高科技 AI 欢迎界面样式 ========== */
.ai-avatar-container {
  position: relative;
  width: 240rpx;
  height: 240rpx;
  margin: 0 auto 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  animation: ringExpand 3s ease-out infinite;
}

.ring-1 {
  width: 240rpx;
  height: 240rpx;
  animation-delay: 0s;
}

.ring-2 {
  width: 300rpx;
  height: 300rpx;
  animation-delay: 1s;
}

.ring-3 {
  width: 360rpx;
  height: 360rpx;
  animation-delay: 2s;
}

.welcome-avatar {
  position: absolute;
  top: 17%;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  box-shadow: 0 0 40rpx rgba(102, 126, 234, 0.4), 0 0 80rpx rgba(102, 126, 234, 0.2),
    inset 0 0 20rpx rgba(102, 126, 234, 0.1);
  border: 4rpx solid rgba(255, 255, 255, 0.9);
  z-index: 10;
  background: #fff;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
  border-radius: 50%;
  animation: statusPulse 2s ease-in-out infinite;
  box-shadow: 0 0 10rpx rgba(74, 222, 128, 0.6);
}

.status-text {
  font-size: 20rpx;
  color: #4ade80;
  font-weight: 600;
}

@keyframes ringExpand {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 1;
    border-color: rgba(102, 126, 234, 0.5);
  }
  100% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0;
    border-color: rgba(102, 126, 234, 0);
  }
}

@keyframes statusPulse {
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

.welcome-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 16rpx;
  max-width: 600rpx;
}

.title-gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #667eea 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientShift 3s ease infinite;
  display: inline-block;
}

@keyframes gradientShift {
  0%,
  100% {
    background-position: 0% center;
  }
  50% {
    background-position: 100% center;
  }
}

.welcome-subtitle {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin-bottom: 40rpx;
}

.subtitle-text {
  background: linear-gradient(90deg, #999, #666, #999);
  background-size: 200% auto;
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: shimmer 2s linear infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}

.ai-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
  max-width: 600rpx;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  padding: 48rpx 24rpx;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 28rpx;
  backdrop-filter: blur(20rpx);
  border: 2rpx solid rgba(102, 126, 234, 0.25);
  box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.15), 0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: featureFloat 3s ease-in-out infinite;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.feature-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  transition: left 0.5s;
}

.feature-item:hover::before {
  left: 100%;
}

.feature-item:nth-child(1) {
  animation-delay: 0s;
}
.feature-item:nth-child(2) {
  animation-delay: 0.3s;
}
.feature-item:nth-child(3) {
  animation-delay: 0.6s;
}
.feature-item:nth-child(4) {
  animation-delay: 0.9s;
}

.feature-item:hover {
  transform: translateY(-12rpx) scale(1.05) rotate(2deg);
  box-shadow: 0 16rpx 48rpx rgba(102, 126, 234, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.8) inset;
  border-color: rgba(102, 126, 234, 0.5);
  background: rgba(255, 255, 255, 0.95);
}

.feature-item:active {
  transform: translateY(-6rpx) scale(0.98) rotate(0deg);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.2);
}

.feature-icon-box {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
  transition: all 0.3s ease;
  animation: iconPulse 2s ease-in-out infinite;
}

.feature-item:hover .feature-icon-box {
  transform: scale(1.15) rotate(-5deg);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 12rpx 32rpx rgba(102, 126, 234, 0.25);
}

.feature-icon {
  font-size: 44rpx;
  filter: drop-shadow(0 4rpx 8rpx rgba(102, 126, 234, 0.3));
  animation: iconBounce 2s ease-in-out infinite;
}

@keyframes featureFloat {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10rpx) rotate(-1deg);
  }
}

@keyframes iconPulse {
  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.15);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 12rpx 32rpx rgba(102, 126, 234, 0.25);
  }
}

@keyframes iconBounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6rpx);
  }
}

.feature-text {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 600;
  text-shadow: 0 2rpx 4rpx rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.feature-item:hover .feature-text {
  color: #764ba2;
  text-shadow: 0 4rpx 8rpx rgba(118, 75, 162, 0.3);
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(40rpx);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 加载动画增强 ========== */
.loading-indicator {
  display: flex;
  justify-content: center;
  padding: 20rpx 0;
  gap: 12rpx;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  animation: dotPulse 1.4s infinite ease-in-out both;
  box-shadow: 0 0 10rpx rgba(102, 126, 234, 0.5);
}

.message-item.user .dot {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 10rpx rgba(255, 255, 255, 0.8);
}

@keyframes dotPulse {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
    box-shadow: 0 0 20rpx rgba(102, 126, 234, 0.8);
  }
}
</style>
