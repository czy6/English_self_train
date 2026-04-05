<template>
  <view class="test-page">
    <text class="title">设备信息测试</text>
    <view class="info-section">
      <text v-if="deviceInfo">设备品牌: {{ deviceInfo.brand || '未知' }}</text>
      <text v-else>获取设备信息中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const deviceInfo = ref<any>(null)

onMounted(() => {
  try {
    uni.getSystemInfo({
      success: (res) => {
        console.log('设备信息:', res)
        deviceInfo.value = res
      },
      fail: (err) => {
        console.error('获取设备信息失败:', err)
      }
    })
  } catch (err) {
    console.error('获取设备信息时出错:', err)
  }
})
</script>

<style lang="scss" scoped>
.test-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  display: block;
}

.info-section {
  background: white;
  padding: 20rpx;
  border-radius: 10rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.1);
}
</style>