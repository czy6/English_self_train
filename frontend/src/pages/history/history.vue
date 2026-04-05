<template>
  <view class="history-container">
    <view class="page-header">
      <text class="title">学习历史</text>
      <view class="header-actions">
        <uni-icons
          type="trash"
          size="24"
          color="#999"
          @click="showClearDialog"
          class="trash-icon"
        ></uni-icons>
      </view>
    </view>

    <!-- 学习统计概览 -->
    <view class="study-stats">
      <view class="stat-item">
        <text class="stat-value">{{ historyStore.totalCount }}</text>
        <text class="stat-label">总练习</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ historyStore.totalHours }}</text>
        <text class="stat-label">学习小时</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ historyStore.avgAccuracy }}%</text>
        <text class="stat-label">平均正确率</text>
      </view>
    </view>

    <!-- 历史记录列表 - 改用页面级滚动 -->
    <view class="history-list-wrapper">
      <view class="history-list">
        <view v-for="(day, index) in historyStore.groupedHistory" :key="index" class="day-group">
          <view class="date-header">
            <text class="date-text">{{ formatDate(day.date) }}</text>
            <text class="count-text">{{ day.list.length }} 项练习</text>
          </view>

          <view class="day-items">
            <view
              v-for="(item, itemIndex) in day.list"
              :key="`${day.date}-${itemIndex}`"
              class="history-item"
              @click="historyStore.handleJump(item.mainType, item.questionId, item.recordId)"
            >
              <view class="item-icon">
                <uni-icons
                  :type="historyStore.getCategoryIcon(item.mainType)"
                  size="24"
                  :color="historyStore.getCategoryColor(item.mainType)"
                ></uni-icons>
              </view>
              <view class="item-details">
                <text class="item-title"
                  >{{ item.mainType }} - {{ item.subType }} - {{ item.questionId }}号题</text
                >
                <view class="item-meta">
                  <text class="item-time">{{ item.time }}</text>
                  <text class="item-accuracy" :class="accuracyClass(item.accuracy)">
                    {{ item.accuracy }}% 正确率
                  </text>
                </view>
              </view>
              <uni-icons type="forward" size="20" color="#ccc" class="item-arrow"></uni-icons>
            </view>
          </view>
        </view>

        <!-- 加载中 -->
        <view v-if="historyStore.loading" class="loading-state">
          <text class="loading-text">加载中...</text>
        </view>

        <!-- 空状态 -->
        <view
          v-if="!historyStore.loading && historyStore.groupedHistory.length === 0"
          class="empty-state"
        >
          <uni-icons type="clock" size="60" color="#eee" class="empty-icon"></uni-icons>
          <text class="empty-text">暂无学习记录</text>
          <text class="empty-desc">开始你的英语学习之旅吧~</text>
        </view>

        <!-- 没有更多数据 -->
        <view
          v-if="!historyStore.hasMore && historyStore.groupedHistory.length > 0"
          class="no-more-state"
        >
          <text class="no-more-text">—— 已经到底啦，没有更多答题记录了 ——</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { defineComponent } from 'vue'
import { useHistoryStore } from '@/store/modules/history'

export default defineComponent({
  data() {
    return {
      lastScrollTop: 0,
      scrollTimeout: null,
      windowHeight: 0,
      cachedPageHeight: null, // 缓存页面高度，避免频繁查询
    }
  },
  computed: {
    historyStore() {
      return useHistoryStore()
    },
  },
  onLoad() {
    // 延迟获取系统信息，避免初始化问题
    setTimeout(() => {
      try {
        const sysInfo = uni.getSystemInfoSync()
        this.windowHeight = sysInfo.windowHeight || 600
      } catch (error) {
        console.error('获取系统信息失败:', error)
        this.windowHeight = 600 // 默认值
      }
    }, 100)

    // 重置状态并加载第一页
    this.historyStore.resetState()
    this.historyStore.getHistoryList()
  },
  onReachBottom() {
    this.handleLoadMore()
  },
  onPageScroll(e) {
    const currentScrollTop = e.scrollTop

    // 检测滚动方向（向下滚动）
    if (currentScrollTop > this.lastScrollTop) {
      // 增加防抖时间，减少频繁查询
      clearTimeout(this.scrollTimeout)
      this.scrollTimeout = setTimeout(() => {
        // 使用缓存的页面高度，避免频繁查询 DOM
        if (this.cachedPageHeight && this.windowHeight) {
          const pageHeight = this.cachedPageHeight
          const viewportHeight = this.windowHeight

          // 距离底部 100px 时触发
          if (pageHeight - currentScrollTop - viewportHeight < 100) {
            this.handleLoadMore()
          }
        } else {
          // 只在没有缓存时才查询 DOM
          const query = uni.createSelectorQuery()
          query
            .select('.history-container')
            .boundingClientRect((data) => {
              if (!data) return
              this.cachedPageHeight = data.height

              const pageHeight = data.height
              const viewportHeight = this.windowHeight

              if (pageHeight - currentScrollTop - viewportHeight < 100) {
                this.handleLoadMore()
              }
            })
            .exec()
        }
      }, 200) // 增加防抖时间到 200ms
    }

    this.lastScrollTop = currentScrollTop
  },
  methods: {
    // 格式化日期显示
    formatDate(dateString) {
      const date = new Date(dateString)
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)

      const currentYear = today.getFullYear()
      const itemYear = date.getFullYear()

      // 显示时间
      const timeStr = `${date.getMonth() + 1}月${date.getDate()}日`
      const yearStr = itemYear !== currentYear ? `${itemYear}年` : ''

      if (date >= today) return '今天'
      if (date >= yesterday) return '昨天'

      // 非今年数据带年份
      return `${yearStr}${timeStr}`
    },
    // 准确率样式
    accuracyClass(accuracy) {
      if (accuracy >= 80) return 'high'
      if (accuracy >= 60) return 'medium'
      return 'low'
    },
    // 清空历史记录
    showClearDialog() {
      uni.showModal({
        title: '确认清空',
        content: '确定要清空所有历史记录吗？此操作不可恢复~',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({ icon: 'none', title: '测试中....' })
          }
        },
      })
    },
    // 统一的加载处理函数
    handleLoadMore() {
      if (this.historyStore.loading || !this.historyStore.hasMore) {
        return
      }
      this.historyStore.getHistoryList()
    },
  },
})
</script>

<style lang="scss" scoped>
/* 全局变量 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$card-bg: rgba(255, 255, 255, 0.98);

.history-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f8f9ff 100%);
  padding-bottom: 120rpx;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: $card-bg;
  backdrop-filter: blur(20rpx);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.trash-icon {
  transition: all 0.2s ease;
  padding: 12rpx;
  border-radius: 50%;

  &:hover {
    background: rgba(245, 63, 63, 0.1);
    color: #f53f3f;
  }
}

.study-stats {
  display: flex;
  justify-content: space-around;
  background: $card-bg;
  backdrop-filter: blur(20rpx);
  padding: 30rpx 20rpx;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 126, 234, 0.05);
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 历史记录列表容器 */
.history-list-wrapper {
  padding: 0 20rpx 20rpx;
}

.history-list {
  padding-bottom: 20rpx;
}

.day-group {
  margin-bottom: 40rpx;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  margin-bottom: 16rpx;
}

.date-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.count-text {
  font-size: 24rpx;
  color: #999;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  border: 1px solid rgba(102, 126, 234, 0.15);
}

.history-item {
  display: flex;
  align-items: center;
  padding: 28rpx;
  background: $card-bg;
  backdrop-filter: blur(20rpx);
  border-radius: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(102, 126, 234, 0.05);

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.12);
    border-color: rgba(102, 126, 234, 0.2);
  }

  &:active {
    transform: translateY(-2rpx) scale(0.99);
  }
}

.item-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border: 1px solid rgba(102, 126, 234, 0.15);
}

.item-details {
  flex: 1;
}

.item-title {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  font-weight: 500;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #999;
}

.item-accuracy {
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: 500;
  transition: all 0.3s;
}

.item-accuracy.high {
  color: #00b42a;
  background: linear-gradient(135deg, rgba(0, 180, 42, 0.1), rgba(0, 180, 42, 0.05));
  border: 1px solid rgba(0, 180, 42, 0.2);
}

.item-accuracy.medium {
  color: #ff7d00;
  background: linear-gradient(135deg, rgba(255, 125, 0, 0.1), rgba(255, 125, 0, 0.05));
  border: 1px solid rgba(255, 125, 0, 0.2);
}

.item-accuracy.low {
  color: #f53f3f;
  background: linear-gradient(135deg, rgba(245, 63, 63, 0.1), rgba(245, 63, 63, 0.05));
  border: 1px solid rgba(245, 63, 63, 0.2);
}

.item-arrow {
  margin-left: 16rpx;
  transition: transform 0.3s;
}

.history-item:hover .item-arrow {
  transform: translateX(4rpx);
  color: #667eea;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 150rpx 0;
  color: #999;
}

.empty-icon {
  margin-bottom: 30rpx;
  opacity: 0.6;
}

.empty-text {
  font-size: 30rpx;
  margin-bottom: 15rpx;
  color: #666;
  font-weight: 500;
}

.empty-desc {
  font-size: 26rpx;
  color: #bbb;
}

/* 加载中状态 */
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
}

.loading-text {
  font-size: 26rpx;
  color: #999;
}

/* 没有更多数据 */
.no-more-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
}

.no-more-text {
  font-size: 24rpx;
  color: #ccc;
  text-align: center;
}

/* 动画效果 */
.slide-in {
  animation: slideIn 0.4s ease forwards;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
