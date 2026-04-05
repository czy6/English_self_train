<template>
  <view class="favorites-container">
    <!-- 分类标签 -->
    <view class="category-tabs">
      <view
        v-for="(category, index) in favoriteStore.categories"
        :key="index"
        @click="favoriteStore.switchCategory(index)"
        class="tab-item"
        :class="{ active: favoriteStore.currentMainType === index }"
      >
        <uni-icons :type="category.icon" size="18" class="tab-icon"></uni-icons>
        <text class="tab-text">{{ category.name }}</text>
      </view>
    </view>

    <!-- 收藏列表 -->
    <view class="favorites-list">
      <view v-if="favoriteStore.filteredFavorites.length > 0">
        <view
          v-for="(item, index) in favoriteStore.filteredFavorites"
          :key="index"
          class="favorite-item"
          @click="favoriteStore.handleJump(item.questionId)"
          :animation="itemAnimation(index)"
        >
          <view class="item-info">
            <view class="item-category">
              <text class="subcategory">{{ item.subType }}</text>
            </view>
            <text class="item-title">{{ item.subType }} - {{ item.questionId }}号题</text>
          </view>
          <view class="item-actions">
            <uni-icons type="star-filled" size="20" color="#FFCC00" class="star-icon"></uni-icons>
            <uni-icons type="forward" size="18" color="#999" class="arrow-icon"></uni-icons>
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <uni-icons type="heart" size="60" color="#eee" class="empty-icon"></uni-icons>
        <text class="empty-text">暂无收藏内容</text>
        <text class="empty-desc">快去收藏你喜欢的题目吧~</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import uniIcons from '@dcloudio/uni-ui/lib/uni-icons/uni-icons.vue'
import { useFavoriteStore } from '@/store'

// 仓库
const favoriteStore = useFavoriteStore()

// 项目动画
const itemAnimation = (index) => {
  return {
    enterClass: 'fade-in',
    duration: 300 + index * 100,
  }
}

onShow(async () => {
  await favoriteStore.getFavoriteList()
})
</script>

<style lang="scss" scoped>
/* 全局变量 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$card-bg: rgba(255, 255, 255, 0.98);

.favorites-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f8f9ff 100%);
  padding-bottom: 20rpx;
}

.page-header {
  padding: 20rpx 30rpx;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.category-tabs {
  display: flex;
  justify-content: space-between;
  background: $card-bg;
  backdrop-filter: blur(20rpx);
  padding: 20rpx 20rpx;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  overflow-x: auto;
  white-space: nowrap;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
}

.tab-item {
  display: inline-flex;
  align-items: center;
  padding: 16rpx 32rpx;
  margin: 0 8rpx;
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(102, 126, 234, 0.05);
  border: 2rpx solid transparent;

  &:hover {
    background: rgba(102, 126, 234, 0.1);
  }
}

.tab-item.active {
  background: $primary-gradient;
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
  border-color: transparent;
}

.tab-icon {
  margin-right: 10rpx;
}

.favorites-list {
  padding: 20rpx;
}

.favorite-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: $card-bg;
  backdrop-filter: blur(20rpx);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(102, 126, 234, 0.05);

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 12rpx 32rpx rgba(102, 126, 234, 0.15);
    border-color: rgba(102, 126, 234, 0.2);
  }

  &:active {
    transform: translateY(-2rpx) scale(0.99);
  }
}

.item-info {
  flex: 1;
}

.item-category {
  margin-bottom: 12rpx;
}

.subcategory {
  font-size: 24rpx;
  color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  font-weight: 500;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.item-title {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  font-weight: 500;
}

.item-actions {
  display: flex;
  align-items: center;
}

.star-icon {
  margin-right: 20rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(255, 204, 0, 0.3));
}

.arrow-icon {
  transition: transform 0.3s;
}

.favorite-item:hover .arrow-icon {
  transform: translateX(4rpx);
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

/* 动画效果 */
.fade-in {
  animation: fadeIn 0.5s ease forwards;
  opacity: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
