import { useEffect, useState } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { axiosInstance } from '../../api/axiosInstance'

const TOKEN_KEY = 'runningOlleAccessToken'

export function RequireAuth() {
  const token = localStorage.getItem(TOKEN_KEY)
  const [authenticated, setAuthenticated] = useState<boolean | null>(token ? null : false)

  useEffect(() => {
    if (!token) return

    let active = true

    axiosInstance.get('/api/users/me')
      .then(() => {
        if (active) setAuthenticated(true)
      })
      .catch(() => {
        localStorage.removeItem(TOKEN_KEY)
        if (active) setAuthenticated(false)
      })

    return () => {
      active = false
    }
  }, [token])

  if (authenticated === null) {
    return <main className="center-page"><div className="spinner" /><p>로그인 정보를 확인하고 있습니다…</p></main>
  }

  return authenticated ? <Outlet /> : <Navigate to="/login" replace />
}
