import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FreeRunningMap } from '../../features/running/FreeRunningMap'
import { RunningIcon } from '../../features/running/RunningIcon'
import { getLocationErrorMessage, GPS_OPTIONS, positionToPoint } from '../../features/running/runningUtils'
import type { GeoPoint } from '../../features/running/types'

export function FreeRunReadyPage() {
  const navigate = useNavigate()
  const [position, setPosition] = useState<GeoPoint | null>(null)
  const [locationMessage, setLocationMessage] = useState('현재 위치를 찾고 있어요…')
  const [countdown, setCountdown] = useState<number | null>(null)

  const findLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setLocationMessage('이 기기에서는 위치 기능을 사용할 수 없어요.')
      return
    }
    setLocationMessage('현재 위치를 찾고 있어요…')
    navigator.geolocation.getCurrentPosition((result) => {
      setPosition(positionToPoint(result))
      setLocationMessage('현재 위치를 확인했어요')
    }, (error) => setLocationMessage(getLocationErrorMessage(error)), GPS_OPTIONS)
  }, [])

  useEffect(() => { findLocation() }, [findLocation])

  useEffect(() => {
    if (countdown === null) return
    if (countdown === 0) {
      navigate('/running/live', { replace: true, state: { startPosition: position } })
      return
    }
    const timer = window.setTimeout(() => setCountdown((value) => value === null ? null : value - 1), 1_000)
    return () => window.clearTimeout(timer)
  }, [countdown, navigate, position])

  return (
    <main className="free-ready-page">
      <header className="running-flow-header">
        <button type="button" aria-label="뒤로 가기" onClick={() => navigate('/running')}><RunningIcon name="back" /></button>
        <strong>즉시 달리기</strong>
        <span />
      </header>
      <section className="free-ready-content">
        <FreeRunningMap currentPosition={position} />
        <button className="map-location-button" type="button" aria-label="현재 위치 다시 찾기" onClick={findLocation}><RunningIcon name="location" /></button>
        <div className="ready-location-card">
          <span className={position ? 'is-ready' : ''} />
          <div><strong>{position ? '달릴 준비가 됐어요' : '위치를 확인하고 있어요'}</strong><p>{locationMessage}</p></div>
        </div>
      </section>
      <footer className="free-ready-footer">
        <p>안전한 장소에서 시작해 주세요</p>
        <button type="button" disabled={!position || countdown !== null} onClick={() => setCountdown(3)}>러닝 시작</button>
      </footer>
      {countdown !== null && countdown > 0 && <div className="running-countdown" role="status"><span key={countdown}>{countdown}</span><p>준비하세요!</p></div>}
    </main>
  )
}
