<template>
  <view class="markdown-renderer">
    <!-- Markdown 内容区域 -->
    <view class="markdown-content prose">
      <!-- 骨架屏 Loading -->
      <view v-if="loading" class="skeleton-loading">
        <view class="skeleton-line" v-for="i in 8" :key="i"></view>
      </view>

      <!-- 实际内容 -->
      <view v-else class="content-wrapper">
        <!-- 标题 -->
        <view v-for="(block, index) in parsedBlocks" :key="index" class="markdown-block">
          <!-- H1 -->
          <view v-if="block.type === 'h1'" class="heading h1">
            <text class="heading-text">{{ block.content }}</text>
            <view class="heading-divider"></view>
          </view>

          <!-- H2 -->
          <view v-else-if="block.type === 'h2'" :id="block.id" class="heading h2">
            <text class="heading-text">{{ block.content }}</text>
            <view class="heading-divider"></view>
          </view>

          <!-- H3 -->
          <view v-else-if="block.type === 'h3'" :id="block.id" class="heading h3">
            <text class="heading-text">{{ block.content }}</text>
          </view>

          <!-- 段落 -->
          <view v-else-if="block.type === 'paragraph'" class="paragraph">
            <text class="paragraph-text">{{ block.content }}</text>
          </view>

          <!-- 引用块 -->
          <view v-else-if="block.type === 'blockquote'" class="blockquote">
            <view class="blockquote-border"></view>
            <view class="blockquote-content">
              <text class="blockquote-text">{{ block.content }}</text>
            </view>
          </view>

          <!-- 代码块 -->
          <view v-else-if="block.type === 'code'" class="code-block">
            <view class="code-header">
              <text class="code-lang">{{ block.lang || 'Code' }}</text>
              <text class="code-copy" @click="copyCode(block.content)">复制</text>
            </view>
            <scroll-view scroll-x class="code-scroll">
              <text class="code-content">{{ block.content }}</text>
            </scroll-view>
          </view>

          <!-- 表格 -->
          <view v-else-if="block.type === 'table'" class="table-wrapper">
            <scroll-view scroll-x class="table-scroll">
              <view class="table">
                <view class="table-header">
                  <view class="table-row header-row">
                    <view
                      v-for="(header, hIndex) in block.headers"
                      :key="hIndex"
                      class="table-cell header-cell"
                    >
                      <text class="cell-text">{{ header }}</text>
                    </view>
                  </view>
                </view>
                <view class="table-body">
                  <view
                    v-for="(row, rIndex) in block.rows"
                    :key="rIndex"
                    :class="['table-row', Number(rIndex) % 2 === 0 ? 'even-row' : 'odd-row']"
                  >
                    <view v-for="(cell, cIndex) in row" :key="cIndex" class="table-cell">
                      <text class="cell-text">{{ cell }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>

          <!-- 折叠块 -->
          <view v-else-if="block.type === 'details'" class="details-block">
            <view class="details-summary" @click="toggleDetails(index)">
              <view class="details-arrow" :class="{ open: block.isOpen }"></view>
              <text class="details-summary-text">{{ block.summary }}</text>
            </view>
            <view v-if="block.isOpen" class="details-content">
              <view class="details-inner">
                <text class="details-text">{{ block.content }}</text>
              </view>
            </view>
          </view>

          <!-- 任务列表 -->
          <view v-else-if="block.type === 'taskList'" class="task-list">
            <view
              v-for="(task, tIndex) in block.tasks"
              :key="tIndex"
              class="task-item"
              @click="toggleTask(Number(tIndex))"
            >
              <view :class="['task-checkbox', { checked: task.checked }]">
                <text v-if="task.checked" class="check-icon">✓</text>
              </view>
              <text :class="['task-text', { completed: task.checked }]">{{ task.text }}</text>
            </view>
          </view>

          <!-- 列表 -->
          <view v-else-if="block.type === 'list'" class="list-block">
            <view v-for="(item, lIndex) in block.items" :key="lIndex" class="list-item">
              <view class="list-marker"></view>
              <text class="list-text">{{ item }}</text>
            </view>
          </view>

          <!-- 图片 -->
          <view v-else-if="block.type === 'image'" class="image-block">
            <image
              :src="block.src"
              :alt="block.alt"
              class="markdown-image"
              mode="widthFix"
              lazy-load
            />
            <text v-if="block.alt" class="image-caption">{{ block.alt }}</text>
          </view>

          <!-- 分隔线 -->
          <view v-else-if="block.type === 'hr'" class="divider"></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, getCurrentInstance } from 'vue'

interface Props {
  markdown: string
}

const props = defineProps<Props>()

// 加载状态
const loading = ref(true)
const parsedBlocks = ref<any[]>([])
const detailsOpenState = ref<boolean[]>([])
const taskStates = ref<boolean[]>([])

// 获取当前组件实例
const instance = getCurrentInstance()

// 简单的 Markdown 解析器（适配微信小程序）
const parseMarkdownText = (markdown: string) => {
  const blocks: any[] = []
  let headingCounter = 0

  // 处理字面量的 \n，将其转换为真正的换行符
  const normalizedMarkdown = markdown.replace(/\\n/g, '\n')

  const lines = normalizedMarkdown.split('\n')
  let i = 0

  while (i < lines.length) {
    const line = lines[i]
    const trimmedLine = line.trim()

    // 标题 (# H1, ## H2, ### H3)
    const headingMatch = trimmedLine.match(/^(#{1,6})\s+(.+)$/)
    if (headingMatch) {
      headingCounter++
      const depth = headingMatch[1].length
      const id = `heading-${headingCounter}`
      blocks.push({
        type: `h${depth}`,
        content: headingMatch[2],
        id,
      })
      i++
      continue
    }

    // 引用块 (> text)
    if (trimmedLine.startsWith('>')) {
      const quoteLines: string[] = []
      while (i < lines.length && lines[i].trim().startsWith('>')) {
        quoteLines.push(lines[i].replace(/^>\s?/, ''))
        i++
      }
      blocks.push({
        type: 'blockquote',
        content: quoteLines.join('\n'),
      })
      continue
    }

    // 代码块 (```)
    if (trimmedLine.startsWith('```')) {
      const lang = trimmedLine.replace('```', '').trim()
      const codeLines: string[] = []
      i++
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        codeLines.push(lines[i])
        i++
      }
      blocks.push({
        type: 'code',
        content: codeLines.join('\n'),
        lang: lang || 'code',
      })
      i++
      continue
    }

    // 表格
    if (trimmedLine.includes('|') && trimmedLine.startsWith('|')) {
      const tableRows: string[][] = []
      let hasHeader = false

      while (i < lines.length && lines[i].trim().includes('|') && lines[i].trim().startsWith('|')) {
        const currentLine = lines[i].trim()
        // 跳过分隔符行 (|---|---|)
        if (!currentLine.match(/^[\|\-:\s]+$/)) {
          const cells = currentLine
            .split('|')
            .filter((_, idx, arr) => idx !== 0 && idx !== arr.length - 1)
            .map((cell) => cell.trim())

          if (cells.length > 0) {
            if (!hasHeader) {
              hasHeader = true
              tableRows.push(cells) // 表头
            } else {
              tableRows.push(cells) // 数据行
            }
          }
        }
        i++
      }

      if (tableRows.length > 1) {
        blocks.push({
          type: 'table',
          headers: tableRows[0],
          rows: tableRows.slice(1),
        })
      }
      continue
    }

    // 折叠块 (<details>)
    if (trimmedLine.includes('<details>')) {
      const detailsLines: string[] = []
      i++
      while (i < lines.length && !lines[i].includes('</details>')) {
        detailsLines.push(lines[i])
        i++
      }
      const detailsContent = detailsLines.join('\n')
      const summaryMatch = detailsContent.match(/<summary>(.*?)<\/summary>/s)
      const contentMatch = detailsContent.match(/<\/summary>(.*)/s)

      if (summaryMatch) {
        blocks.push({
          type: 'details',
          summary: summaryMatch[1].trim(),
          content: contentMatch ? contentMatch[1].trim() : '',
          isOpen: false,
        })
      }
      i++
      continue
    }

    // 任务列表 (- [ ] 或 - [x])
    if (trimmedLine.match(/^-\s+\[([ x])\]\s+(.+)$/)) {
      const tasks: any[] = []
      while (i < lines.length) {
        const taskMatch = lines[i].trim().match(/^-\s+\[([ x])\]\s+(.+)$/)
        if (taskMatch) {
          tasks.push({
            text: taskMatch[2],
            checked: taskMatch[1] === 'x',
          })
          i++
        } else {
          break
        }
      }
      if (tasks.length > 0) {
        blocks.push({
          type: 'taskList',
          tasks,
        })
        taskStates.value = tasks.map((t: any) => t.checked)
      }
      continue
    }

    // 普通列表 (- text)
    if (trimmedLine.startsWith('- ') && !trimmedLine.match(/^-\s+\[/)) {
      const listItems: string[] = []
      while (
        i < lines.length &&
        lines[i].trim().startsWith('- ') &&
        !lines[i].trim().match(/^-\s+\[/)
      ) {
        listItems.push(lines[i].replace(/^- /, '').trim())
        i++
      }
      blocks.push({
        type: 'list',
        items: listItems,
      })
      continue
    }

    // 分隔线 (---)
    if (trimmedLine.match(/^(-{3,}|\*{3,})$/)) {
      blocks.push({
        type: 'hr',
      })
      i++
      continue
    }

    // 图片 (![alt](src))
    const imageMatch = trimmedLine.match(/^!\[(.*?)\]\((.*?)\)$/)
    if (imageMatch) {
      blocks.push({
        type: 'image',
        alt: imageMatch[1],
        src: imageMatch[2],
      })
      i++
      continue
    }

    // 段落（非空行）
    if (trimmedLine) {
      blocks.push({
        type: 'paragraph',
        content: trimmedLine,
      })
    }

    i++
  }

  return { blocks }
}

// 切换折叠块
const toggleDetails = (index: number) => {
  if (parsedBlocks.value[index]) {
    parsedBlocks.value[index].isOpen = !parsedBlocks.value[index].isOpen
  }
}

// 切换任务状态
const toggleTask = (index: number) => {
  taskStates.value[index] = !taskStates.value[index]
  if (parsedBlocks.value.find((b) => b.type === 'taskList')?.tasks[index]) {
    parsedBlocks.value.find((b) => b.type === 'taskList').tasks[index].checked =
      taskStates.value[index]
  }
}

// 复制代码
const copyCode = (code: string) => {
  uni.setClipboardData({
    data: code,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success',
      })
    },
  })
}

// 解析 Markdown 内容
const parseMarkdown = () => {
  try {
    const { blocks } = parseMarkdownText(props.markdown)
    parsedBlocks.value = blocks

    // 模拟打字机效果的延迟
    setTimeout(() => {
      loading.value = false
    }, 800)
  } catch (error) {
    console.error('Markdown 解析失败:', error)
    loading.value = false
  }
}

onMounted(() => {
  parseMarkdown()
})
</script>

<style lang="scss" scoped>
.markdown-renderer {
  width: 100%;
}

/* Markdown 内容区域 */
.markdown-content {
  width: 100%;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 40rpx 32rpx;
  box-sizing: border-box;
}

/* 骨架屏 */
.skeleton-loading {
  .skeleton-line {
    height: 32rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 4rpx;
    margin-bottom: 16rpx;

    &:nth-child(2n) {
      width: 80%;
    }

    &:nth-child(3n) {
      width: 60%;
    }
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 标题样式 */
.heading {
  margin: 40rpx 0 24rpx;

  &.h1 {
    margin-top: 0;

    .heading-text {
      font-size: 40rpx;
      font-weight: bold;
      color: #1a1a1a;
      display: block;
    }

    .heading-divider {
      height: 4rpx;
      background: linear-gradient(90deg, #667eea, #764ba2);
      margin-top: 16rpx;
      border-radius: 2rpx;
    }
  }

  &.h2 {
    .heading-text {
      font-size: 32rpx;
      font-weight: 600;
      color: #2a2a2a;
      display: block;
    }

    .heading-divider {
      height: 3rpx;
      background: linear-gradient(90deg, #667eea 0%, transparent 100%);
      margin-top: 12rpx;
      border-radius: 2rpx;
    }
  }

  &.h3 {
    .heading-text {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      display: block;
    }
  }
}

/* 段落 */
.paragraph {
  margin: 24rpx 0;
  line-height: 1.8;

  .paragraph-text {
    font-size: 32rpx;
    color: #333;
    line-height: 1.8;
  }
}

/* 引用块 */
.blockquote {
  display: flex;
  margin: 32rpx 0;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;

  .blockquote-border {
    width: 6rpx;
    background: linear-gradient(180deg, #667eea, #764ba2);
    border-radius: 3rpx;
    margin-right: 20rpx;
    flex-shrink: 0;
  }

  .blockquote-content {
    flex: 1;
  }

  .blockquote-text {
    font-size: 30rpx;
    color: #555;
    line-height: 1.7;
    font-style: italic;
  }
}

/* 代码块 */
.code-block {
  margin: 32rpx 0;
  border-radius: 12rpx;
  overflow: hidden;
  background: #282c34;

  .code-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 24rpx;
    background: #21252b;
    border-bottom: 1rpx solid #3e4451;

    .code-lang {
      font-size: 24rpx;
      color: #abb2bf;
      font-weight: 500;
    }

    .code-copy {
      font-size: 24rpx;
      color: #61afef;
      cursor: pointer;
      padding: 8rpx 16rpx;
      background: rgba(97, 175, 239, 0.1);
      border-radius: 6rpx;
    }
  }

  .code-scroll {
    max-height: 400rpx;
    overflow-x: auto;

    .code-content {
      display: block;
      padding: 24rpx;
      font-size: 28rpx;
      color: #abb2bf;
      font-family: 'Courier New', monospace;
      line-height: 1.6;
      white-space: nowrap;
    }
  }
}

/* 表格 */
.table-wrapper {
  margin: 32rpx 0;
  border-radius: 12rpx;
  overflow: hidden;
  border: 2rpx solid #e0e0e0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .table-scroll {
    width: 100%;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .table {
    min-width: 100%;
    display: flex;
    flex-direction: column;
  }

  .table-header {
    display: flex;
    background: linear-gradient(180deg, #f5f7fa 0%, #e8ebf0 100%);
    border-bottom: 2rpx solid #d0d0d0;
  }

  .table-row {
    display: flex;
    border-bottom: 1rpx solid #e0e0e0;
    transition: background 0.2s;

    &.header-row {
      .table-cell {
        font-weight: 600;
        background: linear-gradient(180deg, #f0f2f5 0%, #e8ebee 100%);
      }
    }

    &.even-row {
      background: #fafbfc;
    }

    &.odd-row {
      background: #ffffff;
    }

    &:last-child {
      border-bottom: none;
    }
  }

  .table-cell {
    flex: 1;
    padding: 24rpx 20rpx;
    border-right: 1rpx solid #e8e8e8;
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 120rpx;
    word-break: break-word;

    &:last-child {
      border-right: none;
    }

    .cell-text {
      font-size: 28rpx;
      color: #333;
      line-height: 1.8;
      display: block;
    }
  }

  .header-cell {
    .cell-text {
      font-weight: 600;
      color: #2a2a2a;
      font-size: 28rpx;
    }
  }
}

/* 图片 */
.image-block {
  margin: 32rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;

  .markdown-image {
    width: 100%;
    max-width: 100%;
    border-radius: 12rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  }

  .image-caption {
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;
    text-align: center;
  }
}

/* 折叠块 */
.details-block {
  margin: 24rpx 0;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  overflow: hidden;

  .details-summary {
    display: flex;
    align-items: center;
    padding: 24rpx;
    background: #f8f9fa;
    cursor: pointer;
    transition: background 0.3s;

    &:active {
      background: #f0f0f0;
    }

    .details-arrow {
      width: 0;
      height: 0;
      border-style: solid;
      border-width: 10rpx 0 10rpx 16rpx;
      border-color: transparent transparent transparent #667eea;
      margin-right: 16rpx;
      transition: transform 0.3s;

      &.open {
        transform: rotate(90deg);
      }
    }

    .details-summary-text {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      flex: 1;
    }
  }

  .details-content {
    padding: 24rpx;
    background: #ffffff;
    border-top: 1rpx solid #e0e0e0;

    .details-inner {
      .details-text {
        font-size: 30rpx;
        color: #555;
        line-height: 1.7;
      }
    }
  }
}

/* 任务列表 */
.task-list {
  margin: 24rpx 0;

  .task-item {
    display: flex;
    align-items: center;
    padding: 16rpx 0;
    cursor: pointer;
    transition: all 0.3s;

    &:active {
      opacity: 0.7;
    }

    .task-checkbox {
      width: 36rpx;
      height: 36rpx;
      border: 2rpx solid #667eea;
      border-radius: 6rpx;
      margin-right: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: all 0.3s;

      &.checked {
        background: #667eea;
        border-color: #667eea;

        .check-icon {
          color: #ffffff;
          font-size: 24rpx;
          font-weight: bold;
        }
      }
    }

    .task-text {
      flex: 1;
      font-size: 30rpx;
      color: #333;
      line-height: 1.6;

      &.completed {
        text-decoration: line-through;
        color: #999;
      }
    }
  }
}

/* 列表 */
.list-block {
  margin: 24rpx 0;

  .list-item {
    display: flex;
    align-items: flex-start;
    padding: 12rpx 0;

    .list-marker {
      width: 12rpx;
      height: 12rpx;
      background: #667eea;
      border-radius: 50%;
      margin-top: 20rpx;
      margin-right: 16rpx;
      flex-shrink: 0;
    }

    .list-text {
      flex: 1;
      font-size: 30rpx;
      color: #333;
      line-height: 1.6;
    }
  }
}

/* 分隔线 */
.divider {
  height: 2rpx;
  background: #e0e0e0;
  margin: 40rpx 0;
}
</style>
