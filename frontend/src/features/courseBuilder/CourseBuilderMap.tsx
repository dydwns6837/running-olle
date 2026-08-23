import { useEffect, useRef, useState } from 'react'
import { JEJU_CENTER } from './courseBuilderUtils'
import { getKakaoMapAppKey, loadKakaoMapSdk } from '../map/kakaoMaps'
import type { KakaoCustomOverlay, KakaoMap, KakaoPolyline } from '../map/kakaoMaps'
import type { CourseWaypointDraft, DraftRoute, LatLng, PlaceSearchResult } from './types'

type Props = {
  currentPosition: LatLng | null
  waypoints: CourseWaypointDraft[]
  draftRoute: DraftRoute | null
  selectedPlace: PlaceSearchResult | null
  className?: string
  onMapPress?: () => void
  onSelectedPlaceMarkerClick?: () => void
}

function markerContent(index: number) {
  return `<div class="course-builder-marker">${index}</div>`
}

function selectedMarkerContent(onClick?: () => void) {
  const button = document.createElement('button')
  button.type = 'button'
  button.className = 'course-builder-selected-marker'
  button.setAttribute('aria-label', '선택한 장소 상세 보기')
  button.addEventListener('click', (event) => {
    event.stopPropagation()
    onClick?.()
  })
  return button
}

function currentMarkerContent() {
  return '<div class="course-builder-current-marker"><span></span></div>'
}

export function CourseBuilderMap({
  currentPosition,
  waypoints,
  draftRoute,
  selectedPlace,
  className = '',
  onMapPress,
  onSelectedPlaceMarkerClick,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<KakaoMap | null>(null)
  const routeRef = useRef<KakaoPolyline | null>(null)
  const waypointOverlayRefs = useRef<KakaoCustomOverlay[]>([])
  const selectedOverlayRef = useRef<KakaoCustomOverlay | null>(null)
  const currentOverlayRef = useRef<KakaoCustomOverlay | null>(null)
  const onMapPressRef = useRef(onMapPress)
  const [ready, setReady] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const appKey = getKakaoMapAppKey()

  useEffect(() => {
    onMapPressRef.current = onMapPress
  }, [onMapPress])

  useEffect(() => {
    if (!containerRef.current || !appKey) return
    let disposed = false

    loadKakaoMapSdk(appKey)
      .then(() => {
        if (disposed || !containerRef.current || !window.kakao) return
        const maps = window.kakao.maps
        const center = JEJU_CENTER
        const map = new maps.Map(containerRef.current, {
          center: new maps.LatLng(center.lat, center.lng),
          level: 6,
        })
        routeRef.current = new maps.Polyline({
          map,
          path: [],
          strokeWeight: 5,
          strokeColor: '#FF6F0F',
          strokeOpacity: 0.96,
          strokeStyle: 'solid',
        })
        maps.event.addListener(map, 'click', () => onMapPressRef.current?.())
        mapRef.current = map
        setReady(true)
        window.setTimeout(() => map.relayout(), 0)
      })
      .catch((reason: Error) => setError(reason.message))

    return () => {
      disposed = true
    }
  }, [appKey])

  useEffect(() => {
    if (!ready || !mapRef.current || !currentPosition || !window.kakao) return
    const position = new window.kakao.maps.LatLng(currentPosition.lat, currentPosition.lng)
    if (!currentOverlayRef.current) {
      currentOverlayRef.current = new window.kakao.maps.CustomOverlay({
        map: mapRef.current,
        position,
        content: currentMarkerContent(),
        zIndex: 8,
      })
    } else {
      currentOverlayRef.current.setPosition(position)
    }
  }, [currentPosition, ready])

  useEffect(() => {
    if (!ready || !routeRef.current || !window.kakao) return
    const path = draftRoute?.routeCoordinates.map((coordinate) => (
      new window.kakao!.maps.LatLng(coordinate.lat, coordinate.lng)
    )) ?? []
    routeRef.current.setPath(path)
  }, [draftRoute, ready])

  useEffect(() => {
    if (!ready || !mapRef.current || !window.kakao) return
    waypointOverlayRefs.current.forEach((overlay) => overlay.setMap(null))
    waypointOverlayRefs.current = waypoints.map((waypoint, index) => (
      new window.kakao!.maps.CustomOverlay({
        map: mapRef.current!,
        position: new window.kakao!.maps.LatLng(waypoint.lat, waypoint.lng),
        content: markerContent(index + 1),
        zIndex: 12,
      })
    ))
  }, [ready, waypoints])

  useEffect(() => {
    if (!ready || !mapRef.current || !window.kakao) return
    selectedOverlayRef.current?.setMap(null)
    selectedOverlayRef.current = null
    if (!selectedPlace) return

    const position = new window.kakao.maps.LatLng(selectedPlace.lat, selectedPlace.lng)
    selectedOverlayRef.current = new window.kakao.maps.CustomOverlay({
      map: mapRef.current,
      position,
      content: selectedMarkerContent(onSelectedPlaceMarkerClick),
      zIndex: 11,
      yAnchor: 1,
    })
    mapRef.current.panTo(position)
  }, [onSelectedPlaceMarkerClick, ready, selectedPlace])

  function zoomBy(delta: number) {
    if (!mapRef.current) return
    const nextLevel = Math.max(1, Math.min(14, mapRef.current.getLevel() + delta))
    mapRef.current.setLevel(nextLevel)
  }

  if (!appKey) {
    return (
      <div className={`course-builder-map-fallback ${className}`}>
        카카오맵 JavaScript 키를 설정해 주세요.
      </div>
    )
  }

  return (
    <div className={`course-builder-map ${className}`}>
      <div ref={containerRef} className="h-full w-full" />
      {error && (
        <div className="course-builder-map-fallback">
          {error}
          <br />
          JavaScript 키와 등록 도메인을 확인해 주세요.
        </div>
      )}
      <div className="course-builder-zoom-controls" aria-label="지도 확대/축소">
        <button type="button" aria-label="지도 확대" onClick={() => zoomBy(-1)}>+</button>
        <button type="button" aria-label="지도 축소" onClick={() => zoomBy(1)}>-</button>
      </div>
    </div>
  )
}
