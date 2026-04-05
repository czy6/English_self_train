import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// https://vitejs.dev/config/
export default defineConfig({
  build: {
    // 开发阶段启用源码映射：https://uniapp.dcloud.net.cn/tutorial/migration-to-vue3.html#需主动开启-sourcemap
    sourcemap: process.env.NODE_ENV === 'development',
    rollupOptions: {
      external: [
        '@vueuse/core',
        '@vueuse/shared', // 新增 @vueuse/shared
        'marked'
      ]
    }
  },
  plugins: [uni()],
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})
