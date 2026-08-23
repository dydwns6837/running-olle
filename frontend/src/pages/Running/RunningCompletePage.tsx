import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { FreeRunningMap } from '../../features/running/FreeRunningMap'
import { formatDistance, formatDuration, formatPace } from '../../features/running/runningUtils'
import type { SavedRunningRecord } from '../../features/running/types'

export function RunningCompletePage() {
  const navigate = useNavigate()
  const record = useLocation().state?.record as SavedRunningRecord | undefined
  if (!record) return <Navigate to="/running" replace />
  return (
    <main className="running-complete-page">
      <section className="complete-copy"><span>✓</span><p>러닝 완료</p><h1>오늘도 멋지게 달렸어요!</h1></section>
      <div className="complete-map"><FreeRunningMap currentPosition={record.route.at(-1) ?? null} recordedPath={record.route} followPosition={false} /></div>
      <section className="complete-stats">
        <div><strong>{formatDistance(record.distanceMeters)}</strong><span>km</span></div>
        <div><strong>{formatDuration(record.durationSeconds)}</strong><span>시간</span></div>
        <div><strong>{formatPace(record.averagePace)}</strong><span>평균 페이스</span></div>
      </section>
      {record.syncStatus === 'pending' && <p className="record-sync-notice">서버 연결에 실패해 기록을 이 기기에 임시 저장했어요.</p>}
      <button className="complete-home-button" type="button" onClick={() => navigate('/', { replace: true })}>홈으로</button>
    </main>
  )
}
