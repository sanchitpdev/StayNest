import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi, userApi } from '../api'
import toast from 'react-hot-toast'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user,    setUser]    = useState(null)
  const [loading, setLoading] = useState(true)

  // Restore session from localStorage on mount
  useEffect(() => {
    const token    = localStorage.getItem('staynest_token')
    const userData = localStorage.getItem('staynest_user')
    if (token && userData) {
      try { setUser(JSON.parse(userData)) } catch { logout() }
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    const res  = await authApi.login({ email, password })
    const data = res.data
    localStorage.setItem('staynest_token', data.token)
    localStorage.setItem('staynest_user',  JSON.stringify(data))
    setUser(data)
    return data
  }, [])

  const register = useCallback(async (formData) => {
    const res  = await authApi.register(formData)
    const data = res.data
    localStorage.setItem('staynest_token', data.token)
    localStorage.setItem('staynest_user',  JSON.stringify(data))
    setUser(data)
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('staynest_token')
    localStorage.removeItem('staynest_user')
    setUser(null)
    toast.success('Logged out successfully')
  }, [])

  const refreshUser = useCallback(async () => {
    try {
      const res      = await userApi.getMyProfile()
      const updated  = { ...user, ...res.data }
      localStorage.setItem('staynest_user', JSON.stringify(updated))
      setUser(updated)
    } catch (err) {
      console.error('Failed to refresh user', err)
    }
  }, [user])

  const isAuthenticated = !!user
  const isHost          = user?.role === 'HOST'
  const isAdmin         = user?.role === 'ADMIN'
  const isGuest         = user?.role === 'GUEST'

  return (
    <AuthContext.Provider value={{ user, loading, isAuthenticated, isHost, isAdmin, isGuest, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
