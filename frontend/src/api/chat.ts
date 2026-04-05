import { request } from '@/utils/request'
import type { AiChatListType, Message, MsgAndPicRequestType } from '@/types/chat'

/**
 * 获取对话id
 */
export const getChatIdAPI = () => {
  return request<AiChatListType>({
    method: 'GET',
    url: '/ai/chat/id'
  })
}

/**
 * 获取对话列表
 */
export const getChatListAPI = () => {
  return request<AiChatListType[]>({
    method: 'GET',
    url: '/ai/chat/list'
  })
}

/**
 * 获取对话列表
 */
export const getChatHistoryListAPI = (chatId: string) => {
  return request<Message[]>({
    method: 'GET',
    url: `/ai/chat/history?chatId=${chatId}`
  })
}

/**
 * 删除对话
 */
export const deleteChatAPI = (chatId: string) => {
  return request({
    method: 'DELETE',
    url: `/ai/chat/delete/${chatId}`
  })
}

/**
 * 删除对话
 */
export const deleteAllChatAPI = () => {
  return request({
    method: 'DELETE',
    url: '/ai/chat/delete/all'
  })
}

/**
 * 发送对话
 */
export const sendMessageAPI = (prompt: string, chatId: string) => {
  return request<string>({
    method: 'POST',
    url: `/ai/chat${chatId ? `?chatId=${chatId}&prompt=${prompt}` : `?prompt=${prompt}`}`
  })
}

function isLocalTmpPath(path: string): boolean {
  // 微信小程序：http://tmp/ 或 http://usr/
  // 支付宝小程序：https://resource/ 等
  return /^(http|https):\/\/(tmp|usr|resource)\//.test(path);
}

export const uploadImageToOSS = (path: string): Promise<string> => {
  return new Promise((resolve, reject) => {
    // 1. 不是本地临时路径 → 直接返回
    if (!isLocalTmpPath(path)) {
      resolve(path);
      return;
    }

    // 2. 本地临时路径 → 正常上传
    uni.uploadFile({
      url: '/upload',
      filePath: path,
      name: 'file',
      success(res) {
        if (res.statusCode === 200) {
          const { data } = JSON.parse(res.data);
          resolve(data);
        } else {
          reject(new Error(`上传失败: HTTP ${res.statusCode}`));
        }
      },
      fail: reject
    });
  });
};


/**
 * 发送对话(带图片)
 */
export const sendMessageWithPictureAPI = (data: MsgAndPicRequestType) => {
  return request<string>({
    method: 'POST',
    url: `/ai/chat/image`,
    data
  });
};