<template>
  <view class="viewport">
    <!-- 头像区域 -->
    <view class="avatar-section">
      <view class="avatar-content" @click="userStore.onAvatarChange">
        <image 
          class="avatar-img" 
          :src="userStore.userInfo?.avatar || '/static/images/avatar-default.png'" 
          mode="aspectFill" 
        />
      </view>
      <text class="avatar-tip" @click="userStore.onAvatarChange">点击修改头像</text>
    </view>

    <!-- 表单区域 -->
    <view class="form">
      <view class="form-card">
        <!-- 手机号（不可修改） -->
        <view class="form-item">
          <text class="label">手机号</text>
          <text class="phone">{{ userStore.userInfo?.phone || '未绑定' }}</text>
        </view>

        <!-- 昵称 -->
        <view class="form-item">
          <text class="label">昵称</text>
          <input
            class="input"
            type="text"
            placeholder="请填写昵称"
            v-model="userStore.userInfo!.nickName"
          />
        </view>

        <!-- 性别 -->
        <view class="form-item">
          <text class="label">性别</text>
          <radio-group @change="userStore.onGenderChange" class="radio-group">
            <label class="radio">
              <radio
                value="男"
                color="#36BFFA"
                :checked="userStore.userInfo?.gender === '男'"
              />
              男
            </label>
            <label class="radio">
              <radio
                value="女"
                color="#36BFFA"
                :checked="userStore.userInfo?.gender === '女'"
              />
              女
            </label>
            <text v-if="!userStore.userInfo?.gender" class="noSetGender">(未设置)</text>
          </radio-group>
        </view>

        <!-- 生日 -->
        <view class="form-item">
          <text class="label">生日</text>
          <picker
            class="picker"
            mode="date"
            start="1900-01-01"
            :end="new Date().toISOString().split('T')[0]"
            :value="userStore.userInfo?.birthday"
            @change="userStore.onBirthdayChange"
          >
            <view v-if="userStore.userInfo?.birthday" class="picker-value">{{
              userStore.userInfo?.birthday
            }}</view>
            <view class="placeholder" v-else>请选择日期</view>
          </picker>
        </view>
      </view>

      <!-- 保存按钮 -->
      <button @click="userStore.onUpdateUserInfo()" class="save-btn">保 存</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store'
import { onLoad } from '@dcloudio/uni-app'

const userStore = useUserStore()

onLoad(() => {
  userStore.getUserInfo()
})
</script>

<style lang="scss">
page {
  background-color: #fff;
}

.viewport {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 0 30rpx;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0;

  .avatar-content {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    overflow: hidden;
    box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.1);
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }

  .avatar-img {
    width: 100%;
    height: 100%;
    background-color: #f5f5f5;
  }

  .avatar-tip {
    margin-top: 20rpx;
    font-size: 26rpx;
    color: #666;
  }
}

/* 表单区域 */
.form {
  margin-top: 20rpx;

  .form-card {
    background: #fff;
    border-radius: 20rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
    overflow: hidden;
  }

  .form-item {
    display: flex;
    align-items: center;
    padding: 0 30rpx;
    height: 120rpx;
    border-bottom: 1rpx solid #f2f2f2;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 140rpx;
      color: #333;
      font-size: 30rpx;
      font-weight: 500;
    }

    .phone {
      color: #666;
      flex: 1;
      font-size: 30rpx;
    }

    .input {
      flex: 1;
      height: 80rpx;
      padding: 0;
      border: none;
      background: transparent;
      font-size: 30rpx;
      color: #333;
    }

    .radio-group {
      flex: 1;
      display: flex;
      align-items: center;
    }

    .radio {
      margin-right: 40rpx;
      display: flex;
      align-items: center;
      color: #666;
      font-size: 30rpx;

      radio {
        margin-right: 10rpx;
        transform: scale(1.2);
      }
    }

    .noSetGender {
      color: #999;
      font-size: 28rpx;
    }

    .picker {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: #333;
      font-size: 30rpx;
    }

    .placeholder {
      color: #ccc;
    }
  }

  .save-btn {
    height: 100rpx;
    line-height: 100rpx;
    margin: 40rpx 0;
    background: linear-gradient(135deg, #36BFFA 0%, #2296ED 100%);
    color: #fff;
    border-radius: 50rpx;
    font-size: 32rpx;
    font-weight: 500;
    box-shadow: 0 6rpx 18rpx rgba(54, 191, 250, 0.3);
    transition: all 0.3s;
    border: none;

    &:active {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba(54, 191, 250, 0.2);
      background: linear-gradient(135deg, #2DA8E4 0%, #1E87D1 100%);
    }
  }
}
</style>