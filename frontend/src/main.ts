import { createSSRApp } from "vue";
import App from "./App.vue";
import pinia from "./store";

export function createApp() {
  const app = createSSRApp(App);
  
  // 全局错误处理
  app.config.errorHandler = (err, instance, info) => {
    console.error('Vue应用错误:', err);
    console.error('错误信息:', info);
  };
  
  app.use(pinia);
  
  return { app };
}
