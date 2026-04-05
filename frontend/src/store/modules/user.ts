import { defineStore } from 'pinia'
import type { UserDetail } from '@/types/user'
import { ref } from 'vue'
import { getUserInfoAPI, updateUserInfoAPI } from '@/api/user'
import { uploadImageToOSS } from '@/api/chat'

export const useUserStore = defineStore('user', () => {
  // 1.默认用户信息
  const defaultUserInfo = ref<UserDetail>({
    avatar: '/static/images/default-avatar.png',
    level: -1,
    nickName: '未登录',
    gender: '男',
    birthday: ''
  })
  const userInfo = ref<UserDetail>({
    avatar: '',
    level: -1,
    nickName: '',
    gender: '',
    birthday: ''
  })

  // 2.获取用户信息
  const getUserInfo = async () => {
    const res = await getUserInfoAPI()
    userInfo.value = res.data
  }

  // 3.修改头像
  const onAvatarChange = () => {
    // 选择拍照or相册
    uni.chooseMedia({
      // 选择的文件个数
      count: 1,
      // 选择的文件类型
      mediaType: ['image'],
      // 选中后
      success(res) {
        const { tempFilePath } = res.tempFiles[0]
        userInfo.value.avatar = tempFilePath
      },
    })
  }

  // 4.修改性别
  const onGenderChange: UniHelper.RadioGroupOnChange = (e) => {
    userInfo.value.gender = e.detail.value
  }

  // 5.修改生日
  const onBirthdayChange: UniHelper.DatePickerOnChange = (e) => {
    userInfo.value.birthday = e.detail.value
  }

  // 6.修改用户信息
  const onUpdateUserInfo = async () => {
    uni.showModal({
      title: '确认修改',
      content: '确定要修改个人信息吗？',
      success: async (res) => {
        if (res.confirm) {
          uni.showLoading({title: '提交中...',})
          // 6.1 先提交图片值aliyun的oss中
          userInfo.value.avatar = await uploadImageToOSS(userInfo.value.avatar)
          const { success } = await updateUserInfoAPI({
            avatar: userInfo.value.avatar,
            nickName: userInfo.value.nickName,
            gender: userInfo.value.gender,
            birthday: userInfo.value.birthday
          })
          if (success) {
            uni.hideLoading()
            uni.showToast({
              title: '修改成功',
              icon: 'success',
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 1000)
          }
        }
      }
     })
  }

  return {
    /** 默认用户信息 */
    defaultUserInfo,
    /** 用户信息 */
    userInfo,
    /** 获取用户信息 */
    getUserInfo,
    /** 修改性别 */
    onGenderChange,
    /** 修改生日 */
    onBirthdayChange,
    /** 修改用户信息 */
    onUpdateUserInfo,
    /** 修改头像 */
    onAvatarChange
  }
})