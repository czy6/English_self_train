import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCodeAPI, postLoginAPI } from '@/api/login'
import type { LoginForm } from '@/types/login'

export const useLoginStore = defineStore('login', () => {
  // 1.登录表单信息
  const formData = ref<LoginForm>({
    phone: '18673751101',
    code: '',
  })

  const agreeAgreement = ref(false)

  // 2.表单验证规则
  const rules = {
    phone: {
      rules: [
        { required: true, errorMessage: '请输入手机号' },
        { pattern: /^1[3-9]\d{9}$/, errorMessage: '请输入正确的手机号' },
      ],
    },
    code: {
      rules: [
        { required: true, errorMessage: '请输入验证码' },
        { pattern: /^\d{6}$/, errorMessage: '验证码为6位数字' },
      ],
    },
  }

  // 3.获取验证码
  const getCode = async () => {
    const res = await getCodeAPI({ phone: formData.value.phone })
    formData.value.code = res.data.code
  }

  // 4.同意协议
  const onAgreeAgreement = () => {
    agreeAgreement.value = !agreeAgreement.value
  }

  // 5.显示协议
  const showAgreement = (type: string) => {
    let title = type === 'service' ? '用户服务协议' : '隐私政策'
    let content =
      type === 'service'
        ? `
      用户服务协议
      
      欢迎使用英语训练小程序！在使用本服务前，请仔细阅读以下条款：
      
      1. 服务内容
      本小程序提供英语听力、阅读、翻译和写作等学习功能，旨在帮助用户提高英语水平。
      
      2. 用户权利与义务
      用户有权使用本小程序的所有功能，但需遵守国家法律法规和本协议的规定。
      
      3. 隐私政策
      我们尊重用户隐私，具体信息请查看《隐私政策》。
      
      4. 免责声明
      本小程序内容仅供学习参考，不构成任何专业建议。
      
      5. 协议修改
      我们有权随时修改本协议，用户继续使用即视为同意修改后的条款。
      `
        : `
      隐私政策
      
      本隐私政策旨在帮助您了解我们如何收集、使用和保护您的个人信息：
      
      1. 信息收集
      我们可能收集您的手机号、学习记录等信息，用于提供更好的服务。
      
      2. 信息使用
      我们不会将您的信息分享给第三方，仅用于内部服务优化。
      
      3. 信息保护
      我们采用安全技术保护您的信息，防止未经授权的访问。
      
      4. 用户权利
      您有权访问、更正或删除您的个人信息。
      
      5. 政策更新
      我们可能会更新本政策，请定期查看。
      `

    uni.showModal({
      title: title,
      content: content,
      showCancel: false,
      confirmText: '我已阅读',
      confirmColor: '#409eff',
    })
  }

  // 6.登录请求
  const token = ref()
  const refreshToken = ref()
  const postLogin = async () => {
    const res = await postLoginAPI(formData.value)
    token.value = res.data.token
    // 保存 refreshToken（如果后端返回）
    if (res.data.refreshToken) {
      refreshToken.value = res.data.refreshToken
    }
  }

  // 7.重置
  const resetData = () => {
    ;(formData.value = {
      phone: '18673751101',
      code: '',
    }),
      (agreeAgreement.value = false)
  }

  return {
    formData,
    agreeAgreement,
    rules,
    getCode,
    onAgreeAgreement,
    showAgreement,
    postLogin,
    token,
    refreshToken,
    resetData,
  }
})
