<template>
  <view class="login-container">
    <!-- 顶部图标区域 -->
    <view class="logo-container">
      <image src="/static/images/logo.png" mode="aspectFit" class="logo" />
      <text class="app-name">英语训练</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-container">
      <uniForm ref="loginForm" :modelValue="loginStore.formData" :rules="loginStore.rules">
        <uniFormItem name="phone" label="">
          <input
            class="phone-input"
            type="number"
            placeholder="请输入手机号"
            v-model="loginStore.formData.phone"
            maxlength="11"
            placeholder-class="placeholder-style"
          ></input>
        </uniFormItem>

        <uniFormItem name="code" label="">
          <view class="code-input-container">
            <input
              class="code-input"
              type="number"
              placeholder="请输入验证码"
              v-model="loginStore.formData.code"
              maxlength="6"
              placeholder-class="placeholder-style"
            ></input>
            <button class="get-code-btn" :disabled="countdown > 0" @click="getCode">
              {{ countdown > 0 ? `${countdown}s后重新获取` : '获取验证码' }}
            </button>
          </view>
        </uniFormItem>

        <!-- 协议勾选 -->
        <view class="agreement-container">
          <checkbox-group
            :modelValue="loginStore.agreeAgreement"
            @change="loginStore.onAgreeAgreement"
          >
            <checkbox name="agree" class="checkbox-size"/>
            <text class="agreement-text">我已阅读并同意</text>
            <text class="agreement-link" @click="loginStore.showAgreement('service')">《用户服务协议》</text>
            <text class="agreement-text">和</text>
            <text class="agreement-link" @click="loginStore.showAgreement('privacy')">《隐私政策》</text>

          </checkbox-group>
        </view>

        <!-- 登录按钮 -->
        <button class="login-btn"  @click="submitForm">登录</button>

        <!-- 提示文字 -->
        <text class="register-tip">
          <text class="tip-icon">ℹ️</text>
          未注册的手机号会自动注册
        </text>

      </uniForm>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import storage from '@/utils/storage.ts';
import uniForm from '@dcloudio/uni-ui/lib/uni-forms/uni-forms.vue'
import uniFormItem from '@dcloudio/uni-ui/lib/uni-forms-item/uni-forms-item.vue'
import { useLoginStore } from '@/store/modules/login'
import { onShow, onLoad } from '@dcloudio/uni-app';

// 倒计时
const countdown = ref(0)
// 表单引用
const loginForm = ref(null)
// 仓库
const loginStore = useLoginStore()

// 默认表单
onShow(() => {
  loginStore.resetData()
})

// 获取验证码
const getCode = () => {
  // 验证手机号
  loginForm.value
    .validateField('phone')
    .then(() => {
      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)

      // 获取验证码
      loginStore.getCode()
      uni.showToast({
        title: '验证码已发送',
        icon: 'none',
      })
    })
    .catch((err) => {
      uni.showToast({
        title: err.message,
        icon: 'none',
      })
    })
}

// 登录
const submitForm = () => {
  loginForm.value
    .validate()
    .then(async () => {
      if (!loginStore.agreeAgreement) {
        uni.showToast({
          title: '请同意用户协议',
          icon: 'none',
        })
        return
      }
      // 登录
      uni.showLoading({
        title: '登录中...',
      })
      await loginStore.postLogin()
      uni.hideLoading()
      if(loginStore.token !== null) {
        // 存储 token 和 refreshToken 至本地
        storage.setSync('token', loginStore.token)
        if (loginStore.refreshToken) {
          storage.setSync('refreshToken', loginStore.refreshToken)
        }
        setTimeout(() => {
          uni.showToast({
          title: '登录成功',
          icon: 'success',
        })
        }, 200)
        if (backUrl && backUrl !== '/pages/login/login') {
          // 返回来源页
          uni.redirectTo({ url: backUrl })
        } else {
          // 无来源则回首页
          uni.switchTab({ url: '/pages/home/home' })
        }
      }
    })
    .catch((err) => {
      uni.showToast({
        title: err.message,
        icon: 'none',
      })
    })
}
let backUrl = ''
onLoad((query) => {
  loginStore.backUrl = decodeURIComponent(query.redirect || '')
})

</script>

<style lang="scss" scoped>
/* 全局变量 */
$primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$card-bg: rgba(255, 255, 255, 0.98);

.login-container {
  padding: 60rpx 40rpx;
  height: 100vh;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 50%, #f8f9ff 100%);
  display: flex;
  flex-direction: column;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 80rpx;
  animation: fadeInDown 0.6s ease-out;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 32rpx;
  border-radius: 32rpx;
  box-shadow: 0 16rpx 48rpx rgba(102, 126, 234, 0.2);
}

.app-name {
  font-size: 40rpx;
  font-weight: bold;
  background: $primary-gradient;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2rpx;
}

.form-container {
  padding: 0 10rpx;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.phone-input {
  padding: 24rpx 0;
  border: none;
  border-bottom: 2rpx solid rgba(102, 126, 234, 0.1);
  font-size: 28rpx;
  transition: all 0.3s;
  
  &:focus {
    border-bottom-color: #667eea;
  }
}

.placeholder-style {
  color: #bbb;
}

.code-input-container {
  padding: 24rpx 0;
  display: flex;
  align-items: center;
  border: none;
  border-bottom: 2rpx solid rgba(102, 126, 234, 0.1);
  
  &:focus-within {
    border-bottom-color: #667eea;
  }
}

.code-input {
  flex: 1;
  font-size: 28rpx;
}

.get-code-btn {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  color: #667eea;
  border: 2rpx solid rgba(102, 126, 234, 0.3);
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  transition: all 0.3s;
  
  &:disabled {
    background: rgba(102, 126, 234, 0.05);
    color: #bbb;
    border-color: rgba(102, 126, 234, 0.1);
  }
}

.agreement-container {
  margin: 40rpx 0;
  font-size: 24rpx;
  color: #666;
  display: flex;
  align-items: flex-start;
  line-height: 1.6;
}

.checkbox-size {
  transform: scale(0.9);
  margin-right: 8rpx;
}

.agreement-link {
  color: #667eea;
  text-decoration: none;
  transition: opacity 0.3s;
  
  &:active {
    opacity: 0.7;
  }
}

.login-btn {
  width: 100%;
  height: 88rpx;
  background: $primary-gradient;
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
  margin-top: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.2);
  }
  
  &[disabled] {
    background: #c0c4cc;
    color: #999;
    box-shadow: none;
    transform: none;
  }
}

.other-login-container {
  margin-top: 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeInUp 0.6s ease-out 0.4s both;
}

.divider-text {
  color: #999;
  font-size: 24rpx;
  margin-bottom: 40rpx;
}

.login-methods {
  display: flex;
  gap: 60rpx;
}

.login-method {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.method-icon {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 16rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.login-method:hover .method-icon {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.method-text {
  font-size: 22rpx;
  color: #666;
}

.register-tip {
  margin-top: 24rpx;
  font-size: 22rpx;
  color: #999;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .tip-icon {
    margin-right: 8rpx;
    font-size: 24rpx;
  }
}

/* 动画 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
