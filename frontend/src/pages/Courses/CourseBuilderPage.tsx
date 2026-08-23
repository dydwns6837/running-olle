import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { CourseBuilderMap } from '../../features/courseBuilder/CourseBuilderMap'
import { courseBuilderService } from '../../features/courseBuilder/courseBuilderService'
import { useCourseDraftStore } from '../../features/courseBuilder/courseDraftStore'
import { approximateWalkingMinutes, difficultyLabel, formatDistanceKm, kakaoSearchUrl } from '../../features/courseBuilder/courseBuilderUtils'
import { useRouteCalculation } from '../../features/courseBuilder/useRouteCalculation'
import type { CourseWaypointDraft, DraftRoute, LatLng, PlaceDetail, PlaceSearchResult } from '../../features/courseBuilder/types'
import { RunningIcon } from '../../features/running/RunningIcon'

const JEJU_CENTER: LatLng = {
  lat: 33.4996,
  lng: 126.5312,
}

type SearchStatus = 'idle' | 'loading' | 'success' | 'error'

function categoryBadgeClass(categoryGroupCode: string | null) {
  if (categoryGroupCode === 'AT4') return 'bg-[#E8F6E8] text-[#16833A]'
  if (categoryGroupCode === 'CE7') return 'bg-[#F6EEE7] text-[#8A5431]'
  if (categoryGroupCode === 'FD6') return 'bg-[#FFF0E5] text-[#E65E12]'
  if (categoryGroupCode === 'CS2') return 'bg-[#EAF3FF] text-[#2563EB]'
  if (categoryGroupCode === 'PK6') return 'bg-[#F1F5F9] text-[#475569]'
  return 'bg-[#F4F4F5] text-[#52525B]'
}

function categoryLabel(place: PlaceSearchResult | null, detail?: PlaceDetail | null) {
  if (place?.categoryGroupCode === 'AT4') return '관광지'
  if (place?.categoryGroupCode === 'CE7') return '카페'
  if (place?.categoryGroupCode === 'FD6') return '맛집'
  if (place?.categoryGroupCode === 'CS2') return '편의점'
  return detail?.categoryName || place?.categoryName || '장소'
}

function referencePoint(waypoints: ReturnType<typeof useCourseDraftStore.getState>['waypoints'], currentPosition: LatLng | null) {
  const lastWaypoint = waypoints.at(-1)
  if (lastWaypoint) return { lat: lastWaypoint.lat, lng: lastWaypoint.lng }
  return currentPosition
}

function walkingMinuteText(minutes: number | null) {
  return minutes === null ? '거리 계산 전' : `약 ${minutes}분`
}

export function CourseBuilderPage() {
  const navigate = useNavigate()
  const waypoints = useCourseDraftStore((state) => state.waypoints)
  const selectedPlace = useCourseDraftStore((state) => state.selectedPlace)
  const selectedPlaceDetail = useCourseDraftStore((state) => state.selectedPlaceDetail)
  const draftRoute = useCourseDraftStore((state) => state.draftRoute)
  const routeStatus = useCourseDraftStore((state) => state.routeStatus)
  const routeError = useCourseDraftStore((state) => state.routeError)
  const setSelectedPlace = useCourseDraftStore((state) => state.setSelectedPlace)
  const setSelectedPlaceDetail = useCourseDraftStore((state) => state.setSelectedPlaceDetail)
  const addWaypoint = useCourseDraftStore((state) => state.addWaypoint)
  const removeWaypoint = useCourseDraftStore((state) => state.removeWaypoint)
  const resetDraft = useCourseDraftStore((state) => state.resetDraft)
  const [currentPosition, setCurrentPosition] = useState<LatLng | null>(null)
  const [keyword, setKeyword] = useState('')
  const [searchResults, setSearchResults] = useState<PlaceSearchResult[]>([])
  const [searchStatus, setSearchStatus] = useState<SearchStatus>('idle')
  const [searchError, setSearchError] = useState<string | null>(null)
  const [detailStatus, setDetailStatus] = useState<SearchStatus>('idle')
  const [isOverviewExpanded, setIsOverviewExpanded] = useState(false)

  useRouteCalculation()

  useEffect(() => {
    if (!navigator.geolocation) return
    navigator.geolocation.getCurrentPosition((position) => {
      setCurrentPosition({
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      })
    }, () => {
      setCurrentPosition(null)
    }, {
      enableHighAccuracy: true,
      maximumAge: 5_000,
      timeout: 12_000,
    })
  }, [])

  const searchCenter = useMemo(() => {
    const lastWaypoint = waypoints.at(-1)
    if (lastWaypoint) return { lat: lastWaypoint.lat, lng: lastWaypoint.lng }
    return currentPosition ?? JEJU_CENTER
  }, [currentPosition, waypoints])

  const previousPoint = useMemo(() => referencePoint(waypoints, currentPosition), [currentPosition, waypoints])

  const handleSearch = useCallback((event?: FormEvent<HTMLFormElement>) => {
    event?.preventDefault()
    const trimmedKeyword = keyword.trim()
    if (!trimmedKeyword) return

    setSearchStatus('loading')
    setSearchError(null)
    setSelectedPlace(null)
    setSelectedPlaceDetail(null)
    courseBuilderService.searchPlaces(trimmedKeyword, searchCenter.lat, searchCenter.lng, 5_000)
      .then((places) => {
        setSearchResults(places)
        setSearchStatus('success')
      })
      .catch(() => {
        setSearchResults([])
        setSearchStatus('error')
        setSearchError('장소 검색에 실패했어요. 잠시 후 다시 시도해 주세요.')
      })
  }, [keyword, searchCenter.lat, searchCenter.lng, setSelectedPlace, setSelectedPlaceDetail])

  const handleSelectPlace = useCallback((place: PlaceSearchResult) => {
    setSelectedPlace(place)
    setSelectedPlaceDetail(null)
    setDetailStatus('loading')
    setIsOverviewExpanded(false)
    courseBuilderService.getPlaceDetail(place)
      .then((detail) => {
        setSelectedPlaceDetail(detail)
        setDetailStatus('success')
        setSearchResults([])
      })
      .catch(() => {
        setSelectedPlaceDetail({
          kakaoPlaceId: place.kakaoPlaceId,
          name: place.name,
          categoryName: place.categoryName,
          address: place.address,
          lat: place.lat,
          lng: place.lng,
          phone: null,
          tourApiMatched: false,
          tourContentId: null,
          tourContentTypeId: null,
          overview: null,
          firstImageUrl: null,
          useTime: null,
          tourDataRaw: null,
        })
        setDetailStatus('error')
        setSearchResults([])
      })
  }, [setSelectedPlace, setSelectedPlaceDetail])

  const handleAddWaypoint = useCallback(() => {
    if (!selectedPlace || !selectedPlaceDetail) return
    addWaypoint(selectedPlace, selectedPlaceDetail)
    setKeyword('')
    setSearchStatus('idle')
    setSearchResults([])
  }, [addWaypoint, selectedPlace, selectedPlaceDetail])

  function handleSave() {
    if (waypoints.length < 2) {
      window.alert('코스를 저장하려면 경유지를 2개 이상 추가해 주세요.')
      return
    }
    if (!draftRoute) {
      window.alert('경로 계산이 끝난 뒤 저장할 수 있어요.')
      return
    }
    window.alert('코스 저장 상세정보 화면은 다음 Phase에서 연결할게요.')
  }

  return (
    <main className="course-builder-page">
      <CourseBuilderMap
        currentPosition={currentPosition}
        waypoints={waypoints}
        draftRoute={draftRoute}
        selectedPlace={selectedPlace}
      />

      <header className="course-builder-header">
        <button type="button" aria-label="뒤로 가기" onClick={() => navigate('/running')}>
          <RunningIcon name="back" />
        </button>
        <strong>코스 만들기</strong>
        <button type="button" className="course-builder-save" onClick={handleSave}>저장</button>
      </header>

      <section className="course-builder-search-area" aria-label="장소 검색">
        <form className="course-builder-search" onSubmit={handleSearch}>
          <span aria-hidden="true">⌕</span>
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="관광지/맛집/숙소 검색"
            aria-label="관광지/맛집/숙소 검색"
          />
          {keyword && (
            <button type="button" aria-label="검색어 지우기" onClick={() => setKeyword('')}>×</button>
          )}
        </form>
        <SearchResultSheet
          currentPosition={previousPoint}
          places={searchResults}
          status={searchStatus}
          error={searchError}
          onSelect={handleSelectPlace}
        />
      </section>

      {selectedPlace && selectedPlaceDetail ? (
        <WaypointDetailSheet
          place={selectedPlace}
          detail={selectedPlaceDetail}
          walkingMinutes={approximateWalkingMinutes(previousPoint, {
            lat: selectedPlaceDetail.lat,
            lng: selectedPlaceDetail.lng,
          })}
          isAdded={waypoints.some((waypoint) => waypoint.kakaoPlaceId === selectedPlace.kakaoPlaceId)}
          detailStatus={detailStatus}
          isOverviewExpanded={isOverviewExpanded}
          onToggleOverview={() => setIsOverviewExpanded((value) => !value)}
          onClose={() => {
            setSelectedPlace(null)
            setSelectedPlaceDetail(null)
          }}
          onAdd={handleAddWaypoint}
        />
      ) : (
        <CourseDraftBottomSheet
          waypoints={waypoints}
          draftRoute={draftRoute}
          routeStatus={routeStatus}
          routeError={routeError}
          onRemove={removeWaypoint}
          onReset={resetDraft}
        />
      )}
    </main>
  )
}

type SearchResultSheetProps = {
  currentPosition: LatLng | null
  places: PlaceSearchResult[]
  status: SearchStatus
  error: string | null
  onSelect: (place: PlaceSearchResult) => void
}

function SearchResultSheet({ currentPosition, places, status, error, onSelect }: SearchResultSheetProps) {
  if (status === 'idle') return null

  return (
    <div className="course-search-results">
      {status === 'loading' && <p className="course-search-state">검색 중이에요…</p>}
      {status === 'error' && <p className="course-search-state">{error}</p>}
      {status === 'success' && places.length === 0 && <p className="course-search-state">검색 결과가 없어요.</p>}
      {status === 'success' && places.map((place) => {
        const minutes = approximateWalkingMinutes(currentPosition, { lat: place.lat, lng: place.lng })
        return (
          <button key={place.kakaoPlaceId} type="button" className="course-search-result-item" onClick={() => onSelect(place)}>
            <span className={`course-category-badge ${categoryBadgeClass(place.categoryGroupCode)}`}>
              {categoryLabel(place)}
            </span>
            <strong>{place.name}</strong>
            <small>{place.address || place.categoryName || '주소 정보 없음'}</small>
            <em>{walkingMinuteText(minutes)}</em>
          </button>
        )
      })}
    </div>
  )
}

type WaypointDetailSheetProps = {
  place: PlaceSearchResult
  detail: PlaceDetail
  walkingMinutes: number | null
  isAdded: boolean
  detailStatus: SearchStatus
  isOverviewExpanded: boolean
  onToggleOverview: () => void
  onClose: () => void
  onAdd: () => void
}

function WaypointDetailSheet({
  place,
  detail,
  walkingMinutes,
  isAdded,
  detailStatus,
  isOverviewExpanded,
  onToggleOverview,
  onClose,
  onAdd,
}: WaypointDetailSheetProps) {
  const isTourism = place.categoryGroupCode === 'AT4'
  const isTourApiMatched = isTourism && detail.tourApiMatched
  const overview = detail.overview?.trim()
  const shouldClampOverview = Boolean(overview && overview.length > 96)

  return (
    <section className="course-place-detail-sheet">
      <span className="course-sheet-handle" />
      <div className="course-place-detail-header">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className={`course-category-badge ${categoryBadgeClass(place.categoryGroupCode)}`}>
              {categoryLabel(place, detail)}
            </span>
            {isTourApiMatched && <span className="course-source-badge">한국관광공사</span>}
          </div>
          <h2>{detail.name}</h2>
          <p>{detail.address || '주소 정보 없음'}</p>
        </div>
        <button type="button" aria-label="상세 닫기" onClick={onClose}>×</button>
      </div>

      <div className="course-place-meta">
        <span>{walkingMinuteText(walkingMinutes)}</span>
        {detail.phone && <span>{detail.phone}</span>}
      </div>

      {isTourApiMatched && (
        <div className="course-tour-card">
          {detail.firstImageUrl && <img src={detail.firstImageUrl} alt="" />}
          {overview && (
            <div>
              <p className={isOverviewExpanded ? '' : 'line-clamp-3'}>{overview}</p>
              {shouldClampOverview && (
                <button type="button" onClick={onToggleOverview}>
                  {isOverviewExpanded ? '접기' : '더보기'}
                </button>
              )}
            </div>
          )}
          {detail.useTime && <small>이용 시간 {detail.useTime}</small>}
          <em>정보 제공: 한국관광공사</em>
        </div>
      )}

      {isTourism && !detail.tourApiMatched && (
        <div className="course-tour-fallback">
          <strong>상세 정보를 준비 중인 장소예요</strong>
          <p>한국관광공사 데이터와 아직 매칭되지 않았어요.</p>
          <a href={kakaoSearchUrl(detail.name)} target="_blank" rel="noreferrer">카카오맵에서 보기</a>
        </div>
      )}

      {!isTourism && (
        <div className="course-kakao-only-card">
          <strong>{detail.categoryName || place.categoryName || '카카오 장소 정보'}</strong>
          <p>{detail.address || '주소 정보 없음'}</p>
          {detail.phone && <small>{detail.phone}</small>}
        </div>
      )}

      {detailStatus === 'error' && <p className="course-detail-notice">카카오 상세 조회가 불안정해서 기본 정보만 표시해요.</p>}

      <button type="button" className="course-add-button" disabled={isAdded} onClick={onAdd}>
        {isAdded ? '이미 추가된 장소' : '+ 코스에 추가하기'}
      </button>
    </section>
  )
}

type CourseDraftBottomSheetProps = {
  waypoints: CourseWaypointDraft[]
  draftRoute: DraftRoute | null
  routeStatus: 'idle' | 'loading' | 'success' | 'error'
  routeError: string | null
  onRemove: (orderIndex: number) => void
  onReset: () => void
}

function CourseDraftBottomSheet({
  waypoints,
  draftRoute,
  routeStatus,
  routeError,
  onRemove,
  onReset,
}: CourseDraftBottomSheetProps) {
  const distanceKm = draftRoute ? formatDistanceKm(draftRoute.distanceKm) : '0'
  const estimatedMinutes = draftRoute?.estimatedDurationMinutes ?? 0
  const elevationGainM = draftRoute ? Math.round(draftRoute.elevationGainM) : 0

  return (
    <section className="course-draft-bottom-sheet">
      <span className="course-sheet-handle" />
      <div className="course-draft-stats">
        <Stat label="총 거리" value={distanceKm} unit="km" />
        <Stat label="예상 시간" value={String(estimatedMinutes)} unit="분" />
        <Stat label="누적 고도" value={String(elevationGainM)} unit="m" />
      </div>
      {draftRoute?.surface && (
        <div className="course-surface-row">
          <span>포장 {Math.round(draftRoute.surface.asphaltPct)}%</span>
          <span>흙길 {Math.round(draftRoute.surface.dirtPct)}%</span>
          <span>계단 {Math.round(draftRoute.surface.stairsPct)}%</span>
        </div>
      )}
      {routeStatus === 'loading' && <p className="course-route-status">경로를 다시 계산하고 있어요…</p>}
      {routeError && <p className="course-route-status is-error">{routeError}</p>}
      {draftRoute && <p className="course-route-status">난이도 {difficultyLabel(draftRoute.suggestedDifficulty)}</p>}
      <div className="course-waypoint-list">
        {waypoints.length === 0 && (
          <p className="course-empty-waypoints">검색해서 러닝 코스에 넣을 장소를 추가해 주세요.</p>
        )}
        {waypoints.map((waypoint, index) => (
          <div key={`${waypoint.kakaoPlaceId}-${waypoint.orderIndex}`} className="course-waypoint-item">
            <span>{index + 1}</span>
            <div>
              <strong>{waypoint.name}</strong>
              {waypoint.categoryName && <small>{waypoint.categoryName}</small>}
            </div>
            <button type="button" aria-label={`${waypoint.name} 삭제`} onClick={() => onRemove(waypoint.orderIndex)}>×</button>
          </div>
        ))}
      </div>
      {waypoints.length > 0 && (
        <button type="button" className="course-reset-button" onClick={onReset}>경유지 모두 지우기</button>
      )}
    </section>
  )
}

function Stat({ label, value, unit }: { label: string; value: string; unit: string }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}<small>{unit}</small></strong>
    </div>
  )
}
