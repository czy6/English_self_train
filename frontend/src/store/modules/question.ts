import { getSubTypeListAPI, getQuestionContentListAPI, getQuestionContentByIdAPI, submitAnswerRecordAPI, getRecordByIdAPI, favoriteAPI, getFavoriteListAPI } from "@/api/question"
import type { AnswerRecordType, QuestionExplanation, QuestionContentType } from "@/types/question"
import { defineStore } from "pinia"
import { ref, computed } from 'vue'

export const useQuestionStore = defineStore('question', () => {
  // 1.获取阅读分类
  const subTypeList = ref<string[]>([]) // 子分类列表
  const mainType = ref('') // 主类型('听力'，'阅读'，'翻译'，'写作')
  const selectedType = ref('') // 选中的子分类
  const getSubTypeList = async (type: string) => {
    const { data } = await getSubTypeListAPI(type)
    subTypeList.value = data
    mainType.value = type
  }

  // 2.试题难度(九级量表)
  const difficultyList = ref(['一级', '二级', '三级', '四级', '五级', '六级', '七级', '八级', '九级']);
  const selectedDifficulty = ref('') // 选中的难度

  // 3.获取试题列表
  const questionList = ref<QuestionContentType[]>([])
  const currentQuestionIndex = ref(0); // 当前阅读试题索引
  const currentQuestionOptionIndex = ref(0); // 当前试题问题索引
  // 根据分类和难度获取试题列表
  const getQuestionContentList = async () => {
    const type = selectedType.value || currentQuestionType.value
    const difficulty = selectedDifficulty.value || currentQuestionDifficulty.value  
    const { success, data } = await getQuestionContentListAPI(type, difficulty)
    if (success) {
      questionList.value = data.map(item => {
        if (typeof item.options === 'string') {
          item.options = JSON.parse(item.options); // 将 JSON 字符串转为对象(选项使用JSON返回的)
        }
        if (typeof item.audioUrl === 'string' && item.audioUrl) {
          item.audioUrl = JSON.parse(item.audioUrl); // 将 JSON 字符串转为对象(选项使用JSON返回的)
        }
        return item;
      });
      currentQuestionIndex.value = 0
    }
    console.log(questionList.value)
  }
  // 根据questionId获取试题
  const getQuestionContentById = async (questionId: number) => {
    const { success, data } = await getQuestionContentByIdAPI(questionId)
    if (success) {
      if (typeof data.options === 'string') {
        data.options = JSON.parse(data.options); // 将 JSON 字符串转为对象(选项使用JSON返回的)
      }
      if (typeof data.audioUrl === 'string' && data.audioUrl) {
        data.audioUrl = JSON.parse(data.audioUrl); // 将 JSON 字符串转为对象(选项使用JSON返回的)
      }
      questionList.value = [data]
      currentQuestionIndex.value = 0
    }
    console.log(questionList.value)
  }
  // 根据recordId获取作答详情
  const record = ref<AnswerRecordType>()
  const getRecordById = async (recordId: number) => {
    const { data } = await getRecordByIdAPI(recordId)
    record.value = data
    console.log(data)
    restore()
  }

  // 3.0.0 试题分类
  const currentQuestionType = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].subType : '');
  
  // 3.0.1 试题难度
  const currentQuestionDifficulty = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].difficulty : '');

  // 3.1 试题标题
  const currentQuestionTitle = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].questionTitle : false)

  // 3.2 试题作者
  const currentQuestionAuthor = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].questionAuthor : false)

  // 3.3 试题内容
  const currentQuestionContent = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].questionText : '')

  // 3.3.1 处理试题内容为带 <p> 标签的 HTML
  const processedQuestionContentHtml = computed(() => {
    const text = currentQuestionContent.value;
    if (!text) return ''; // 文本为空时返回空

    // 1. 分割段落：用正则匹配 1 个或多个换行符（处理 \n、\n\n、\n\n\n 等）
    const paragraphs = text.split(/\r?\n+/).filter(para => {
      // 过滤空段落（去掉纯空格的段落）
      return para.trim() !== '';
    });

    // 2. 拼接成带 <p> 标签的 HTML 字符串
    return paragraphs.map(para => `<p class="article-paragraph">${para}</p>`).join('');
  });

  // 3.4 试题选项个数
  const currentQuestionTotalOption = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].options.questionTotal : 0);

  // 3.4 试题问题(第一题开始)
  const currentQuestionOption = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].options.questions[currentQuestionOptionIndex.value] : {});

  // 3.5 试题时间(秒)
  const currentQuestionTime = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].requiredTime: 0);
  
  // 3.6 作答时间(秒)
  const elapsedTime = ref(0);

  // 3.7 写作最少要求词量
  const currentQuestionMinWordCount = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].options.min_word_count : 0);

  // 3.8 当前试题音频URL
  const currentQuestionAudioUrl = computed(() => questionList.value[currentQuestionIndex.value] ?
    questionList.value[currentQuestionIndex.value].audioUrl : '');

  // 4.切换题目
  // 当前题目选择的选项
  const selectedOption = ref<string>('');
  // 当前题目作答答案列表---听力与阅读的选择题
  const userAnswer = ref<string[]>([]);
  // 当前题目作答答案列表---翻译与写作的主观题
  const userWriting = ref<string>('');

  // 4.1 切换题目时重置变量
  const reset = () => {
    currentQuestionOptionIndex.value = 0;
    selectedOption.value = '';
    userAnswer.value = [];
    answer.value = undefined
    isSubmit.value = false
    showResultPopup.value = false
    drawerOpen.value = true
    listenORReadAnswer.value = []
    translateORWriteAnswer.value = ''
    userWriting.value = ''
    showAnswerButton.value = false
    explanations.value = []
    userScore.value = 0
    totalScore.value = 0
    elapsedTime.value = 0;
  };

  // 回显作答记录
  const restore = () => {
    if(mainType.value === '听力' || mainType.value === '阅读') {
      userAnswer.value = JSON.parse(record.value!.userAnswer)
      listenORReadAnswer.value = JSON.parse(record.value!.answerText)
      selectedOption.value = userAnswer.value[0]
    } else if (mainType.value === '翻译'||mainType.value === '写作') {
      userWriting.value = record.value!.userAnswer
      translateORWriteAnswer.value = record.value!.answerText
      translateORWriteAnalysis.value = record.value!.analysis
    }
    if(typeof record.value!.explanation === 'string') {
      const explanation = JSON.parse(record.value!.explanation);
      explanations.value = explanation.explanations
      tips.value = explanation.tips
    }
    userScore.value = record.value!.userScore
    totalScore.value = record.value!.score
    elapsedTime.value = record.value!.timeSpent
    isSubmit.value = true
    showAnswerButton.value = true
    showResultPopup.value = true
  }

  // 退出界面
  const onExit = () => {
    reset();
    questionList.value = []
    currentQuestionIndex.value = 0
    selectedType.value = ''
    selectedDifficulty.value = ''
  }

  // 4.2 切换题目
  const refreshQuestion = async () => {
    // TODO 根据用户实际情况推送
    await getQuestionContentList()
    if(questionList.value.length === 0) {
      uni.showToast({icon: 'none', title: '暂无该类型更多试题'});
    }
    reset();
  };

  // 4.3 选择当前题目选项
  const selectOption = (selected: string) => {
    selectedOption.value = selected;
    // 实时更新当前题的作答状态
    userAnswer.value[currentQuestionOptionIndex.value] = selected;
  };

  // 4.4 跳转上一题
  const prevQuestion = () => {
    if (currentQuestionOptionIndex.value > 0) {
      // 区分(题号)readStore.currentQuestionOption.questionNumber = (索引)readStore.currentQuestionOptionIndex + 1
      // 保存当前答案
      if (selectedOption.value !== '') {
        userAnswer.value[currentQuestionOptionIndex.value] = selectedOption.value;
      }
      // 索引减一
      currentQuestionOptionIndex.value--;
      // 选中的题目选项切换到上一题的选择
      selectedOption.value = userAnswer.value[currentQuestionOptionIndex.value];
    }
  };

  // 4.5 跳转下一题
  const nextQuestion = () => {
    if (currentQuestionOptionIndex.value < currentQuestionTotalOption.value - 1) {
      // 保存当前答案
      if (selectedOption.value !== '') {
        userAnswer.value[currentQuestionOptionIndex.value] = selectedOption.value;
      }
      // 索引加一
      currentQuestionOptionIndex.value++;
      // 选中的题目选项切换到下一题的选择
      selectedOption.value = userAnswer.value[currentQuestionOptionIndex.value];
    }
  };

  // 5. 显示作答详情
  // 详情弹窗
  const showDetails = ref(false);
  const toggleDetails = () => {
    showDetails.value = !showDetails.value;
  };
  const goToQuestion = (index: number) => {
    currentQuestionOptionIndex.value = index;
    selectedOption.value = userAnswer.value[index];
    showDetails.value = false;
  };

  // 6.提交答案
  const isSubmit = ref(false) // 是否提交
  const answer = ref<AnswerRecordType>()
  const drawerOpen = ref(false)// 抽屉展开状态
  const showResultPopup = ref(false)// 结果弹窗
  const listenORReadAnswer = ref<string[]>([]) // 听力阅读正确答案列表 
  const translateORWriteAnswer = ref<string>('') // 翻译写作参考范文
  const translateORWriteAnalysis = ref<string>('') // 翻译作文批改结果分析，听力阅读默认为空
  const explanations = ref<QuestionExplanation[]>([])// 听力阅读解析内容
  const tips = ref<string>() // 写作/翻译技巧
  const userScore = ref(0) // 用户得分
  const totalScore = ref(0) // 总分
  const showAnswerButton = ref(false)

  const submitAnswer = () => {
    uni.showModal({
      title: '确认提交',
      content: '提交后无法修改，确定吗？',
      success: async (res) => {
        if (res.confirm) {
          uni.showLoading({title: '提交中...',})
          // 6.1 修改标示符
          isSubmit.value = true
          const submitUserAnswer = ref<string>('')
          // 6.2 提交客观题 or 主观题
          if(mainType.value === '听力' || mainType.value === '阅读') {
            submitUserAnswer.value = JSON.stringify(userAnswer.value)
          } else if (mainType.value === '翻译'||mainType.value === '写作') {
            submitUserAnswer.value = userWriting.value
          }
          // 6.3 获取提交结果
          const { success, data } = await submitAnswerRecordAPI({
            mainType: mainType.value,
            questionId: questionList.value[currentQuestionIndex.value].questionId,
            userAnswer: submitUserAnswer.value,
            timeSpent: elapsedTime.value
          })
          if(!success) {
            uni.showToast({icon: 'none', title: '提交失败'});
            return;
          }
          uni.hideLoading()
          // 6.4 解析返回数据
          if(typeof data.explanation === 'string') {
            data.explanation = JSON.parse(data.explanation);
          }
          answer.value = data
          console.log(answer.value)
          if(mainType.value === '听力' || mainType.value === '阅读') {
            listenORReadAnswer.value = JSON.parse(data.answerText)
            explanations.value = data.explanation.explanations
          } else if (mainType.value === '翻译'||mainType.value === '写作') {
            translateORWriteAnswer.value = data.answerText
            translateORWriteAnalysis.value = data.analysis
            tips.value = data.explanation.tips
          }
          userScore.value = data.userScore
          totalScore.value = data.score
          uni.showToast({icon: 'none', title: '提交成功'});
          // 6.5 后续操作
          uni.showModal({
            title: '作答完成',
            content: '选择后续操作',
            confirmText: '做下一题',
            cancelText: '查看答案',
            success: (res) => {
              if (res.confirm) {
                uni.showToast({
                  title: '作答详情已保存至学习历史',
                  icon: 'none',
                  duration: 2000
                });
              }
              if (res.cancel) {
                showAnswerButton.value = true
                showResultPopup.value = true
              }
            }
          });
        }
      }
    });
  };

  // 7.收藏
  const isFavorite = computed(() =>
    questionList.value.length > 0 ? 
    favoriteList.value.includes(questionList.value[currentQuestionIndex.value].questionId) : false
  )
  const favoriteList = ref<number[]>([])
  // 7.1 收藏 or 取消收藏
  const toggleFavorite = async () => {
    try {
      if (questionList.value.length > 0) {
        const { success } = await favoriteAPI(questionList.value[currentQuestionIndex.value].questionId)
        if (success) {
          getFavoriteList()
          uni.showToast({icon: 'none', title: isFavorite.value ? '取消收藏' : '收藏成功' });
        }
      } else {
        uni.showToast({icon: 'none', title: '试题为空'});
      }
    } catch (error) {
      uni.showToast({icon: 'none', title: '操作失败'});
    }
  }
  // 7.2 获取收藏列表
  const getFavoriteList = async () => {
    const { success, data } = await getFavoriteListAPI()
    if (success) {
      favoriteList.value = data
    }
  }

  // 8. 抽屉
  const toggleDrawer = () => {
    drawerOpen.value = !drawerOpen.value
  }

  // 9. 结果弹窗
  const hideResultPopup = () => {
    showResultPopup.value = !showResultPopup.value
  }


  return {
    /** 阅读分类 */
    subTypeList,
    /** 选中的分类 */
    selectedType,
    /** 选中的难度 */
    selectedDifficulty,
    /** 试题难度列表 */
    difficultyList,
    /** 试题列表 */
    questionList,
    /** 作答详情 */
    record,
    /** 当前试题分类 */
    currentQuestionType,
    /** 当前试题难度 */
    currentQuestionDifficulty,
    /** 当前阅读试题索引 */
    currentQuestionIndex,
    /** 当前试题标题 */
    currentQuestionTitle,
    /** 当前试题作者 */
    currentQuestionAuthor,
    /** 当前试题内容 */
    currentQuestionContent,
    /** 当前试题内容(带 <p> 标签的 HTML) */
    processedQuestionContentHtml,
    /** 当前试题选项个数 */
    currentQuestionTotalOption,
    /** 当前试题问题索引 */
    currentQuestionOptionIndex,
    /** 当前试题问题 */
    currentQuestionOption,
    /** 当前试题时间(秒) */
    currentQuestionTime,
    /** 作答时间(秒) */
    elapsedTime,
    /** 当前试题最少要求词量 */
    currentQuestionMinWordCount,
    /** 当前试题音频URL */
    currentQuestionAudioUrl,
    /** 获取阅读分类 */
    getSubTypeList,
    /** 根据id获取试题 */
    getQuestionContentById,
    /** 根据recordId获取作答详情 */
    getRecordById,
    /** 获取试题列表 */
    getQuestionContentList,
    /** 当前题目选择的选项 */
    selectedOption,
    /** 当前题目作答答案列表---听力与阅读的选择题 */
    userAnswer,
    /** 当前题目作答答案列表---翻译与写作的主观题 */
    userWriting,
    /** 切换题目时重置变量 */
    reset,
    /** 切换题目 */
    refreshQuestion,
    /** 选择当前题目选项 */
    selectOption,
    /** 跳转上一题 */
    prevQuestion,
    /** 跳转下一题 */
    nextQuestion,
    /** 显示作答详情 */
    showDetails,
    /** 切换详情弹窗 */
    toggleDetails,
    /** 跳转到指定题目 */
    goToQuestion,
    /** 是否提交 */
    isSubmit,
    /** 提交答案 */
    submitAnswer,
    /** 抽屉展开状态 */
    drawerOpen,
    /** 结果弹窗 */
    showResultPopup,
    /** 听力正确答案列表 */
    listenORReadAnswer,
    /** 翻译或写作参考范文 */
    translateORWriteAnswer,
    /** 翻译作文批改结果分析，听力阅读默认为空 */
    translateORWriteAnalysis,
    /** 听力阅读解析内容 */
    explanations,
    /** 写作/翻译技巧 */
    tips,
    /** 用户得分 */
    userScore,
    /** 总分 */
    totalScore,
    /** 是否收藏 */
    isFavorite,
    /** 获取收藏列表 */
    getFavoriteList,
    /** 切换收藏 */
    toggleFavorite,
    /** 切换抽屉 */
    toggleDrawer,
    /** 隐藏结果弹窗 */
    hideResultPopup,
    /** 显示答案按钮 */
    showAnswerButton,
    /** 退出界面 */
    onExit
  }
})