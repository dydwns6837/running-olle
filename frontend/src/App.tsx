import { BrowserRouter, Route, Routes } from 'react-router-dom'

function HomePage() {
  return (
    <main className="min-h-screen p-6">
      <h1 className="text-2xl font-bold">Running Olle</h1>
      <p className="mt-2">러닝과 여행을 연결하는 위치 기반 서비스</p>
    </main>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
