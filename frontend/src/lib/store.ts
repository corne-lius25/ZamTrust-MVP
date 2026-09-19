import { create } from 'zustand'
import { api } from './api'

type User = { username: string; roles: string[] }

type AuthState = {
  user: User | null
  token: string | null
  login: (username: string, password: string) => Promise<void>
  register: (payload: RegisterPayload) => Promise<void>
  logout: () => void
  hydrate: () => void
}

export type RegisterPayload = {
  username: string
  email: string
  password: string
  fullName: string
  organization: string
}

export const useAuth = create<AuthState>((set) => ({
  user: null,
  token: null,

  hydrate: () => {
    const token = localStorage.getItem('zamtrust_token')
    const username = localStorage.getItem('zamtrust_user')
    const rolesRaw = localStorage.getItem('zamtrust_roles')
    if (token && username) {
      set({ token, user: { username, roles: rolesRaw ? JSON.parse(rolesRaw) : [] } })
    }
  },

  login: async (username, password) => {
    const { data } = await api.post('/api/auth/login', { username, password })
    const { token, roles } = data
    localStorage.setItem('zamtrust_token', token)
    localStorage.setItem('zamtrust_user', username)
    localStorage.setItem('zamtrust_roles', JSON.stringify(roles))
    set({ token, user: { username, roles } })
  },

  register: async (payload) => {
    await api.post('/api/auth/register', payload)
  },

  logout: () => {
    localStorage.removeItem('zamtrust_token')
    localStorage.removeItem('zamtrust_user')
    localStorage.removeItem('zamtrust_roles')
    set({ token: null, user: null })
  },
}))
