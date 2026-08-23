import { useEffect, useRef, useState } from 'react'
import { getKakaoMapAppKey, loadKakaoMapSdk } from '../map/kakaoMaps'
import type { KakaoCircle, KakaoCustomOverlay, KakaoMap, KakaoPolyline } from '../map/kakaoMaps'
import { JEJU_FALLBACK_POSITION } from './runningUtils'
import type { GeoPoint } from './types'

type Props = {
  currentPosition: GeoPoint | null
  recordedPath?: GeoPoint[]
  followPosition?: boolean
  className?: string
}

export function FreeRunningMap({ currentPosition, recordedPath = [], followPosition = true, className = '' }: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<KakaoMap | null>(null)
  const markerRef = useRef<KakaoCustomOverlay | null>(null)
  const accuracyRef = useRef<KakaoCircle | null>(null)
  const routeRef = useRef<KakaoPolyline | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [ready, setReady] = useState(false)
  const appKey = getKakaoMapAppKey()

  useEffect(() => {
    if (!containerRef.current || !appKey) return
    let disposed = false
    loadKakaoMapSdk(appKey).then(() => {
      if (disposed || !containerRef.current || !window.kakao) return
      const maps = window.kakao.maps
      const center = currentPosition ?? JEJU_FALLBACK_POSITION
      const map = new maps.Map(containerRef.current, {
        center: new maps.LatLng(center.latitude, center.longitude),
        level: 4,
      })
      routeRef.current = new maps.Polyline({
        map,
        path: [],
        strokeWeight: 6,
        strokeColor: '#FF6F0F',
        strokeOpacity: 0.95,
        strokeStyle: 'solid',
      })
      mapRef.current = map
      setReady(true)
      window.setTimeout(() => map.relayout(), 0)
    }).catch((reason: Error) => setError(reason.message))
    return () => { disposed = true }
  // The map instance is intentionally created only once per mounted screen.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appKey])

  useEffect(() => {
    if (!ready || !mapRef.current || !window.kakao || !currentPosition) return
    const maps = window.kakao.maps
    const position = new maps.LatLng(currentPosition.latitude, currentPosition.longitude)
    if (!markerRef.current) {
      markerRef.current = new maps.CustomOverlay({
        map: mapRef.current,
        position,
        content: '<div class="running-current-marker"><span></span></div>',
        zIndex: 10,
      })
      accuracyRef.current = new maps.Circle({
        map: mapRef.current,
        center: position,
        radius: currentPosition.accuracy ?? 0,
        strokeWeight: 1,
        strokeColor: '#2F80ED',
        strokeOpacity: 0.35,
        fillColor: '#71A7F7',
        fillOpacity: 0.18,
      })
    } else {
      markerRef.current.setPosition(position)
      const accuracy = accuracyRef.current
      if (accuracy) {
        accuracy.setPosition(position)
        accuracy.setRadius(currentPosition.accuracy ?? 0)
      }
    }
    if (followPosition) mapRef.current.panTo(position)
  }, [currentPosition, followPosition, ready])

  useEffect(() => {
    if (!ready || !routeRef.current || !window.kakao) return
    routeRef.current.setPath(recordedPath.map((point) => new window.kakao!.maps.LatLng(point.latitude, point.longitude)))
  }, [ready, recordedPath])

  if (!appKey) return <div className={`running-map-fallback ${className}`}>카카오맵 API 키를 설정해 주세요.</div>
  return <div className={`running-map ${className}`}><div ref={containerRef} className="running-map-canvas" />{error && <div className="running-map-fallback">{error}<br />JavaScript 키와 등록 도메인을 확인해 주세요.</div>}</div>
}
