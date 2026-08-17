import { Navigate, Outlet } from 'react-router-dom'

const TOKEN_KEY = 'runningOlleAccessToken'

export function RequireAuth() {
  return localStorage.getItem(TOKEN_KEY) ? <Outlet /> : <Navigate to="/" replace />
}
