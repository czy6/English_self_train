// 本地村春
export default {
  // 异步存储
  set(key: string, value: string) {
      return uni.setStorage({ key, data: value });
  },
  // 异步读取
  get(key: string, defaultValue = null) {
      return uni.getStorage({ key })
          .then(res => res.data)
          .catch(() => defaultValue);
  },
  // 同步存储
  setSync(key: string, value: string) {
    try {
        uni.setStorageSync(key, value);
        return true;
    } catch (e) {
        return false;
    }
  },
  // 同步读取
  getSync(key: string, defaultValue = null) {
      try {
          return uni.getStorageSync(key);
      } catch (e) {
          return defaultValue;
      }
  },
  // 删除
  remove(key: string) {
      return uni.removeStorage({ key });
  },
  // 清空
  clear() {
    return uni.clearStorage();
  }
};