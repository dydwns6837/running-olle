import { Navigate, Route, Routes } from 'react-router-dom'
import { RequireAuth } from './components/auth/RequireAuth'
import { AppLayout } from './components/layout/AppLayout'
import { user } from './mocks/home'
import { LoginPage } from './pages/Auth/LoginPage'
import { OAuthCallbackPage } from './pages/Auth/OAuthCallbackPage'
import { OnboardingPage } from './pages/Auth/OnboardingPage'
import { CommunityPage } from './pages/Community/CommunityPage'
import { CoursesPage } from './pages/Courses/CoursesPage'
import { HomePage } from './pages/Home/HomePage'
import { MyPage } from './pages/MyPage/MyPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/oauth/callback" element={<OAuthCallbackPage />} />

      <Route element={<RequireAuth />}>
        <Route path="/onboarding" element={<OnboardingPage />} />
        <Route element={<AppLayout leftSlot={<span>📍 {user.location}</span>} />}>
          <Route path="/main" element={<HomePage />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/community" element={<CommunityPage />} />
          <Route path="/mypage" element={<MyPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
