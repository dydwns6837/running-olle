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
import { FreeRunReadyPage } from './pages/Running/FreeRunReadyPage'
import { LiveRunningPage } from './pages/Running/LiveRunningPage'
import { RunningCompletePage } from './pages/Running/RunningCompletePage'
import { RunningSelectPage } from './pages/Running/RunningSelectPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/oauth/callback" element={<OAuthCallbackPage />} />

      <Route element={<RequireAuth onboarding="incomplete" />}>
        <Route path="/onboarding" element={<OnboardingPage />} />
      </Route>

      <Route element={<RequireAuth onboarding="required" />}>
        <Route element={<AppLayout leftSlot={<span>📍 {user.location}</span>} />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/community" element={<CommunityPage />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/running" element={<RunningSelectPage />} />
        </Route>
        <Route path="/running/free" element={<FreeRunReadyPage />} />
        <Route path="/running/live" element={<LiveRunningPage />} />
        <Route path="/running/complete" element={<RunningCompletePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
