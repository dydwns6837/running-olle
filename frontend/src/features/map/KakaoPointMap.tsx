import { useEffect, useRef, useState } from 'react'
import { getKakaoMapAppKey, loadKakaoMapSdk } from './kakaoMaps'
import type { KakaoCustomOverlay, KakaoMap } from './kakaoMaps'

type KakaoPointMapProps = {
  lat: number
  lng: number
  label: string
  className?: string
}

export function KakaoPointMap({ lat, lng, label, className = '' }: KakaoPointMapProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<KakaoMap | null>(null)
  const markerRef = useRef<KakaoCustomOverlay | null>(null)
  const [ready, setReady] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const appKey = getKakaoMapAppKey()

  useEffect(() => {
    if (!containerRef.current || !appKey) return
    let disposed = false

    loadKakaoMapSdk(appKey)
      .then(() => {
        if (disposed || !containerRef.current || !window.kakao) return
        const maps = window.kakao.maps
        const position = new maps.LatLng(lat, lng)
        const map = new maps.Map(containerRef.current, {
          center: position,
          level: 4,
        })
        markerRef.current = new maps.CustomOverlay({
          map,
          position,
          content: '<div class="course-route-map-marker">집</div>',
          zIndex: 12,
        })
        mapRef.current = map
        setReady(true)
        window.setTimeout(() => map.relayout(), 0)
      })
      .catch((reason: Error) => setError(reason.message))

    return () => {
      disposed = true
      markerRef.current?.setMap(null)
      markerRef.current = null
    }
  // The map instance should be created once; the selected point updates below.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [appKey])

  useEffect(() => {
    if (!ready || !window.kakao || !mapRef.current || !markerRef.current) return
    const position = new window.kakao.maps.LatLng(lat, lng)
    markerRef.current.setPosition(position)
    mapRef.current.panTo(position)
  }, [lat, lng, ready])

  if (!appKey) {
    return (
      <div className={`grid place-items-center bg-[#F7DDD3] text-center text-[12px] font-semibold text-[#594136] ${className}`}>
        카카오맵 JavaScript 키를 설정해 주세요.
      </div>
    )
  }

  return (
    <div className={`relative overflow-hidden bg-[#E6E8EC] ${className}`} aria-label={`${label} 지도`}>
      <div ref={containerRef} className="h-full w-full" />
      <div className="pointer-events-none absolute left-3 top-3 rounded-full bg-white/95 px-3 py-1.5 text-[12px] font-bold text-[#261912] shadow-[0px_4px_12px_rgba(0,0,0,0.08)]">
        {label}
      </div>
      {error ? (
        <div className="absolute inset-0 grid place-items-center bg-[#E6E8EC] px-6 text-center text-[12px] leading-5 text-[#594136]">
          {error}
        </div>
      ) : null}
    </div>
  )
}
