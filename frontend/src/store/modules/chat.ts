import {
  deleteChatAPI,
  getChatListAPI,
  getChatIdAPI,
  getChatHistoryListAPI,
  sendMessageAPI,
  deleteAllChatAPI,
  sendMessageWithPictureAPI,
  uploadImageToOSS,
} from '@/api/chat.ts'
import type { AiChatListType, ChatListType } from '@/types/chat'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAiStore = defineStore('ai', () => {
  // 1.请求发送
  // 1.1 创建对话id
  const activeChat = ref<string>('') // 当前激活会话ID
  const currentChat = ref<ChatListType>({
    id: '',
    type: '',
    title: '',
    messages: [],
    startTime: '',
  }) // 当前会话
  const chatList = ref<ChatListType[]>([]) // 会话列表
  const createChatId = async () => {
    try {
      const response = await getChatIdAPI()
      console.log('[Chat Store] createChatId response:', response)

      // 检查响应是否有效
      if (!response) {
        console.error('[Chat Store] createChatId: 空响应')
        throw new Error('服务器无响应')
      }

      if (response.success && response.data) {
        uni.showToast({ icon: 'none', title: '创建新会话成功！' })
        activeChat.value = response.data.chatId
        currentChat.value!.id = response.data.chatId
        currentChat.value!.title = response.data.title
        currentChat.value!.type = response.data.chatType
      } else {
        console.error('创建会话失败:', response)
        throw new Error(response?.errorMsg || '创建会话失败')
      }
    } catch (error: any) {
      console.error('创建会话错误:', error)
      // 如果是网络错误，response 可能是 undefined
      if (error.message && !error.message.includes('服务器')) {
        uni.showToast({ icon: 'none', title: '网络错误，请稍后重试' })
      } else {
        uni.showToast({ icon: 'none', title: error.message || '网络错误，请稍后重试' })
      }
      throw error
    }
  }

  // 1.2 获取对话列表
  const aiChatList = ref<AiChatListType[]>()
  const getChatList = async () => {
    try {
      const response = await getChatListAPI()
      if (response.success) {
        aiChatList.value = response.data
        chatList.value =
          aiChatList.value?.map((aiChat) => ({
            id: aiChat.chatId,
            type: aiChat.chatType,
            title: aiChat.title,
            startTime: aiChat.startTime,
            messages: [],
            firstMessageSet: false,
          })) || []
      } else {
        console.error('获取对话列表失败:', response.errorMsg)
        aiChatList.value = []
        chatList.value = []
      }
    } catch (error) {
      console.error('获取对话列表错误:', error)
      aiChatList.value = []
      chatList.value = []
    }
  }

  // 1.3 删除对话
  const deleteChat = async (chatId: string) => {
    const { success } = await deleteChatAPI(chatId)
    // 重新获取列表
    getChatList()
    if (success) {
      uni.showToast({ icon: 'none', title: '删除成功' })
    }
    if (activeChat.value === chatId) {
      activeChat.value = ''
      currentChat.value = {
        id: '',
        type: '',
        title: '',
        messages: [],
        startTime: '',
      }
    }
  }
  const deleteAllChat = async () => {
    const { success } = await deleteAllChatAPI()
    getChatList()
    if (success) {
      activeChat.value = ''
      currentChat.value = {
        id: '',
        type: '',
        title: '',
        messages: [],
        startTime: '',
      }
      uni.showToast({ icon: 'none', title: '已清空' })
    }
  }

  // 2.顶部固定栏(历史 + 新对话)
  // 2.1 历史记录
  const showHistory = ref(false) // 历史面板显隐
  const showMenu = ref(true) // 菜单按钮显隐
  const toggleHistory = () => {
    showHistory.value = !showHistory.value
    showMenu.value = !showHistory.value
    if (showHistory.value) {
      getChatList()
    }
  }
  // 2.2 创建新对话
  const createNewChat = async () => {
    try {
      stopStreaming()
      await createChatId()
      getChatList()
      currentChat.value.messages = []
      inputValue.value = ''
      isSending.value = false
    } catch (error) {
      console.error('创建新对话失败:', error)
      // 重要：创建失败时不要清空消息，不要重置状态
      // throw error // 不再抛出错误，避免影响调用者
    }
  }

  // 3.发送对话
  // 3.1 计算属性：当前会话的消息列表
  const messages = computed(() => {
    return activeChat ? currentChat.value.messages : []
  })
  // 3.2 时间格式化函数（统一为 HH:mm 格式）
  const formatTime = (date: Date) => {
    const hour = date.getHours().toString().padStart(2, '0')
    const minute = date.getMinutes().toString().padStart(2, '0')
    return `${hour}:${minute}`
  }
  // 3.3 发送消息（核心：先加加载态，再更新回复）
  const inputValue = ref('') // 输入框内容
  const isSending = ref(false) // 全局加载状态（控制发送按钮）
  const selectedImages = ref<string[]>([]) // 选中的图片列表
  // 流式输出控制变量
  let streamingInterval: number | null = null
  const isStreaming = ref(false)
  const isPaused = ref(false)
  const MAX_STREAMING_TIME = 5000 // 最大流式时间5秒

  // 停止流式输出
  const stopStreaming = () => {
    if (streamingInterval) {
      clearInterval(streamingInterval)
      streamingInterval = null
    }
    isStreaming.value = false
    isPaused.value = false
  }

  // 暂停/恢复流式输出
  const toggleStreamingPause = () => {
    isPaused.value = !isPaused.value
  }

  const sendMessage = async () => {
    // 1.不能发送空消息
    const content = inputValue.value.trim()
    if (!content || isSending.value) {
      uni.showToast({ icon: 'none', title: '消息不能为空' })
      return
    }
    isSending.value = true

    // 2.判断会话列表是不是空
    if (activeChat.value === '') {
      // 为空则创建会话 - activeChat.value = newChat.id
      await createNewChat()
    }

    // 3. 添加用户消息（时间格式统一为 HH:mm）
    const userMessage = {
      role: 'user',
      content,
      sendTime: formatTime(new Date()),
      imageList: selectedImages.value,
      isLoading: false,
      hasNavigator: false,
    }
    currentChat.value.messages.push(userMessage)
    inputValue.value = ''
    // 清空选中的图片列表
    selectedImages.value = []

    // 4. 添加AI加载消息（时间格式统一为 HH:mm）
    const loadingMsg = {
      role: 'assistant',
      content: '',
      imageList: [],
      sendTime: formatTime(new Date()),
      isLoading: true,
      hasNavigator: false,
    }
    currentChat.value.messages.push(loadingMsg)
    const loadingIndex = currentChat.value.messages.length - 1

    try {
      if (selectedImages.value && selectedImages.value.length > 0) {
        // 处理带图片的请求（暂时使用原有逻辑）
        const imageUrlList = await uploadImageListToOSS(selectedImages.value)
        selectedImages.value = []
        const res = await sendMessageWithPictureAPI({
          prompt: content,
          chatId: activeChat.value,
          imageUrls: imageUrlList,
        })
        const aiReply = res && res.data && typeof res.data === 'string' ? res.data : ''

        if (!aiReply || aiReply.trim() === '') {
          throw new Error('空响应')
        }

        // 使用模拟流式输出
        const split = splitMessage(aiReply)
        const finalContent = split ? split.replyContent : aiReply

        let currentText = ''
        const startTime = Date.now()
        isStreaming.value = true
        isPaused.value = false

        streamingInterval = setInterval(() => {
          if (isPaused.value) return

          if (Date.now() - startTime > MAX_STREAMING_TIME) {
            currentText = finalContent
          } else if (currentText.length < finalContent.length) {
            currentText += finalContent[currentText.length]
          }

          if (split) {
            currentChat.value.messages.splice(loadingIndex, 1, {
              ...currentChat.value.messages[loadingIndex],
              isLoading: false,
              content: currentText,
              nvgQuestionType: split.questionType,
              nvgQuestionId: split.questionId,
            })
          } else {
            currentChat.value.messages.splice(loadingIndex, 1, {
              ...currentChat.value.messages[loadingIndex],
              isLoading: false,
              content: currentText,
            })
          }

          if (currentText.length >= finalContent.length) {
            stopStreaming()
          }
        }, 30)
      } else {
        // 处理文本请求（使用 SSE 流式处理）
        let accumulatedContent = ''
        let currentEvent = ''
        let currentData = ''
        let buffer = ''

        // 直接使用 uni.request 发送请求
        const task = uni.request({
          url: `/ai/chat?chatId=${activeChat.value}&prompt=${encodeURIComponent(content)}`,
          method: 'GET', // SSE 使用 GET 请求
          enableChunked: true, // 启用分块传输
          responseType: 'text',
          header: {
            Accept: 'text/event-stream',
            'Cache-Control': 'no-cache',
          },
          success: (res) => {},
          fail: (err) => {
            isSending.value = false
            stopStreaming()
            currentChat.value.messages[loadingIndex] = {
              ...currentChat.value.messages[loadingIndex],
              content: '请求出错，请稍后尝试',
              isLoading: false,
            }
            uni.showToast({ icon: 'none', title: '发送失败' })
          },
          complete: () => {},
        })

        // 监听数据块
        ;(task as any).onChunkReceived?.((res: any) => {
          let chunk = res.data

          // 转换 ArrayBuffer 为字符串
          if (chunk instanceof ArrayBuffer) {
            chunk = new TextDecoder('utf-8').decode(chunk)
          }

          // 累积数据
          buffer += chunk

          // 按行处理
          const lines = buffer.split('\n')
          buffer = lines.pop() || '' // 保存最后不完整的行

          for (const line of lines) {
            if (line.startsWith('event:')) {
              currentEvent = line.substring(6).trim()
            } else if (line.startsWith('data:')) {
              currentData += line.substring(5).trim()
            } else if (line === '') {
              // 空行表示事件结束
              if (currentData) {
                if (currentEvent === 'done' || currentData === '[DONE]') {
                  // 完成事件
                  stopStreaming()
                } else {
                  // 正常数据块
                  accumulatedContent += currentData

                  // 实时更新 UI
                  const split = splitMessage(accumulatedContent)
                  const finalContent = split ? split.replyContent : accumulatedContent

                  if (split) {
                    currentChat.value.messages.splice(loadingIndex, 1, {
                      ...currentChat.value.messages[loadingIndex],
                      isLoading: false,
                      content: finalContent,
                      nvgQuestionType: split.questionType,
                      nvgQuestionId: split.questionId,
                    })
                  } else {
                    currentChat.value.messages.splice(loadingIndex, 1, {
                      ...currentChat.value.messages[loadingIndex],
                      isLoading: false,
                      content: finalContent,
                    })
                  }
                }
                currentData = ''
              }
            }
          }
        })

        // 监听任务完成
        ;(task as any).onComplete?.((res: any) => {
          isSending.value = false
          stopStreaming()
        })
      }
    } catch (error) {
      uni.showToast({ icon: 'none', title: '发送失败，请重试' })
      // 通过数组索引更新消息，确保响应式更新
      const loadingIndex = currentChat.value.messages.length - 1
      if (loadingIndex >= 0) {
        currentChat.value.messages[loadingIndex] = {
          ...currentChat.value.messages[loadingIndex],
          content: '请求出错，请稍后尝试',
          isLoading: false,
        }
      }
      stopStreaming()
    } finally {
      // 这里不再设置 isSending.value = false，因为 SSE 是异步的
      // 会在 task.onComplete 中设置
    }
  }

  // 3.4 处理跳转
  // 3.4.1 分割信息
  const jumpRegex = /<!--\s*JUMP:([\u4e00-\u9fa5a-zA-Z0-9_-]+)-(\d+)\s*-->/ // 匹配跳转标记
  const splitMessage = (message: string) => {
    if (!message || typeof message !== 'string') return null
    const matchMessage = message.match(jumpRegex)
    if (matchMessage) {
      const questionType = matchMessage[1]
      const questionId = parseInt(matchMessage[2])
      const replyContent = message.replace(jumpRegex, '')
      return { questionType, questionId, replyContent }
    }
    return null
  }
  // 3.4.2 跳转
  enum QuestionType {
    '听力' = 'listening',
    '阅读' = 'reading',
    '翻译' = 'translation',
    '写作' = 'writing',
  }
  const handleJump = (questionType: string, questionId: number) => {
    // 停止当前流式输出，避免干扰
    stopStreaming()
    // 直接使用 questionType 作为路径（因为传入的就是 '听力', '阅读' 等）
    const pagePath = QuestionType[questionType as keyof typeof QuestionType]
    if (!pagePath) {
      uni.showToast({ title: '未知的跳转类型', icon: 'none' })
      return
    }
    uni.navigateTo({
      url: `/pages/${pagePath}/${pagePath}?questionId=${questionId}&isAIJump=true`,
      success: () => {
        uni.showToast({ title: '跳转成功', icon: 'none' })
      },
      fail: (err) => {
        console.error('跳转失败:', err)
        uni.showToast({ title: '跳转失败', icon: 'none' })
      },
    })
  }

  // 4.获取对话记录
  const selectChat = async (chatId: string) => {
    // 停止当前流式输出
    stopStreaming()
    try {
      const response = await getChatHistoryListAPI(chatId)
      if (response.success) {
        const selectMessageList = response.data
          .filter((chat: { content: string }) => Boolean(chat.content))
          .map((chat: { role: string; content: string; [key: string]: any }) => {
            if (chat.role === 'assistant') {
              const split = splitMessage(chat.content)
              if (split) {
                return {
                  ...chat,
                  isLoading: false,
                  content: split.replyContent,
                  nvgQuestionType: split.questionType,
                  nvgQuestionId: split.questionId,
                }
              }
            } else if (chat.role === 'user') {
              // 处理用户消息中的图片URL列表（末尾的[url1, url2, ...]格式）
              const urlListRegex = /\[([^\]]+)\]$/ // 匹配末尾的方括号内容
              const match = chat.content.match(urlListRegex)
              let imageList: string[] = []
              let textContent = chat.content

              if (match) {
                const urlString = match[1]
                // 检查是否包含URL（包含http://或https://）
                if (urlString.includes('http')) {
                  // 分割URL并清理空格
                  imageList = urlString.split(',').map((url) => url.trim())
                  // 移除末尾的URL列表，保留纯文本内容
                  textContent = chat.content.replace(urlListRegex, '').trim()
                }
              }

              return {
                ...chat,
                isLoading: false,
                content: textContent,
                imageList: imageList,
              }
            }
            return {
              ...chat,
              isLoading: false,
              imageList: [],
            }
          })
        activeChat.value = chatId
        const chat = chatList.value.find((chat) => chat.id === chatId)
        currentChat.value = {
          id: chatId,
          type: chat?.type || '',
          title: chat?.title || '',
          messages: selectMessageList,
        }
      } else {
        console.error('获取对话记录失败:', response.errorMsg)
      }
    } catch (error) {
      console.error('获取对话记录错误:', error)
    } finally {
      showHistory.value = false
      showMenu.value = true
    }
  }

  // 5.上传多张图片到阿里云OSS并返回URL列表
  const uploadImageListToOSS = async (localImagePaths: string[]) => {
    try {
      // 依次上传每张图片并收集URL
      const urlList = (await Promise.all(
        localImagePaths.map((path) => uploadImageToOSS(path)),
      )) as string[]
      return urlList
    } catch (err) {
      console.error('上传图片失败:', err)
      uni.showToast({ icon: 'none', title: '图片上传失败，请重试' })
      throw err
    }
  }

  return {
    /** 当前激活会话ID */
    activeChat,
    /** 当前会话对象 */
    currentChat,
    /** 输入框内容 */
    inputValue,
    /** 是否正在发送消息 */
    isSending,
    /** 是否正在流式输出 */
    isStreaming,
    /** 是否暂停流式输出 */
    isPaused,
    /** AI会话列表 */
    aiChatList,
    /** 会话列表 */
    chatList,
    /** 创建新对话id */
    createChatId,
    /** 创建新对话 */
    createNewChat,
    /** 获取对话列表 */
    getChatList,
    /** 获取对话记录 */
    selectChat,
    /** 删除对话 */
    deleteChat,
    /** 删除所有对话 */
    deleteAllChat,
    /** 历史记录面板显隐 */
    showHistory,
    /** 菜单按钮显隐 */
    showMenu,
    /** 切换历史记录面板 */
    toggleHistory,
    /** 发送消息 */
    sendMessage,
    /** 消息列表 */
    messages,
    /** 处理跳转 */
    handleJump,
    /** 选中的图片列表 */
    selectedImages,
    /** 停止流式输出 */
    stopStreaming,
    /** 暂停/恢复流式输出 */
    toggleStreamingPause,
  }
})
