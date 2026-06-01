import { ref, type Ref } from 'vue'
import { defineStore } from 'pinia'
import type { Role } from './enums'

export const useUserStore = defineStore('user', () => {
  const user: Ref<User | null> = ref(null)
  const register = () => {

  }
  const login = () => {

  }
  const logout = () => {

  }
  const contact = (id: Number) => {

  }

  return { user, register, login, logout, contact }
})

export interface User {
  username: string;
  forename: string;
  lastname: string;
  email: string;
}
