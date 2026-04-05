/** 对话列表 */
export type AiChatListType = {
  /** 对话id */
  chatId: string
  /** 对话类型 */
  chatType: string
  /** 对话标题 */
  title: string
  /** 开始时间 */
  startTime: string
}

/** 单条消息类型 */
type Message = {
  /** 消息发送者类型：用户或机器人 */
  role: string;
  /** 消息内容 */
  content: string;
  /** 消息发送时间 */
  sendTime: string;
  /** 查询到的图片列表JSON字符串 */
  imageUrls?: string;
};

/** 单条消息类型 */
type Messages = Message & {
  /** 图片列表 */
  imageList?: string[];
  /** 是否处于加载状态（可选） */
  isLoading: boolean;
  /** 是否包含跳转类型(听力, 阅读，翻译，写作) */
  nvgQuestionType?: string;
  /** 是否包含跳转id */
  nvgQuestionId?: number;
}

export type ChatListType = {
  /** 对话id */
  id: string
  /** 对话类型 */
  type: string 
  /** 对话标题 */
  title: string, 
  /** 对话消息列表 */
  messages: Messages[],
  /** 开始时间 */
  startTime?: string
}

/** 带图片的发送消息格式 */
export type MsgAndPicRequestType = {
  /** 文本消息 */
  prompt: string,
  /** 会话id */
  chatId: string,
  /** 图片文件 */
  imageUrls: string[]
}
