import { createPinia } from 'pinia'
import persist from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(persist)

export default pinia

export * from './modules/user'
export * from './modules/login'
export * from './modules/chat'
export * from './modules/question'
export * from './modules/history'
export * from './modules/favorite'