import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AppLayout } from './components/layout/AppLayout'
import { user } from './mocks/home'
import { CommunityPage } from './pages/Community/CommunityPage'
import { CoursesPage } from './pages/Courses/CoursesPage'
import { HomePage } from './pages/Home/HomePage'
import { MyPage } from './pages/MyPage/MyPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout leftSlot={<span>📍 {user.location}</span>} />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/courses" element={<CoursesPage />} />
          <Route path="/community" element={<CommunityPage />} />
          <Route path="/mypage" element={<MyPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
