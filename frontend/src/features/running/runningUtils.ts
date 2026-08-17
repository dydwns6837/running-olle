import type { GeoPoint } from './types'

const EARTH_RADIUS_METERS = 6_371_000

function toRadians(degrees: number) {
  return degrees * Math.PI / 180
}

export function distanceBetween(from: GeoPoint, to: GeoPoint) {
  const latitudeDelta = toRadians(to.latitude - from.latitude)
  const longitudeDelta = toRadians(to.longitude - from.longitude)
  const fromLatitude = toRadians(from.latitude)
  const toLatitude = toRadians(to.latitude)
  const haversine = Math.sin(latitudeDelta / 2) ** 2
    + Math.cos(fromLatitude) * Math.cos(toLatitude) * Math.sin(longitudeDelta / 2) ** 2

  return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
}

export function positionToPoint(position: GeolocationPosition): GeoPoint {
  return {
    latitude: position.coords.latitude,
    longitude: position.coords.longitude,
    accuracy: position.coords.accuracy,
    timestamp: position.timestamp,
  }
}

export function getLocationErrorMessage(error: GeolocationPositionError) {
  if (error.code === error.PERMISSION_DENIED) {
    return '위치 권한이 꺼져 있어요. 브라우저 설정에서 위치 권한을 허용해 주세요.'
  }
  if (error.code === error.POSITION_UNAVAILABLE) {
    return '현재 위치를 찾을 수 없어요. GPS를 켜고 다시 시도해 주세요.'
  }
  return '위치 확인 시간이 초과됐어요. 잠시 후 다시 시도해 주세요.'
}

export function formatDuration(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3_600)
  const minutes = Math.floor(totalSeconds % 3_600 / 60)
  const seconds = totalSeconds % 60
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

export function formatDistance(distanceMeters: number) {
  return (distanceMeters / 1_000).toFixed(2)
}

export function formatPace(minutesPerKilometer: number | null) {
  if (!minutesPerKilometer || !Number.isFinite(minutesPerKilometer)) return `0'00\"`
  const seconds = Math.round(minutesPerKilometer * 60)
  return `${Math.floor(seconds / 60)}'${String(seconds % 60).padStart(2, '0')}\"`
}

export const GPS_OPTIONS: PositionOptions = {
  enableHighAccuracy: true,
  maximumAge: 1_000,
  timeout: 15_000,
}

export const JEJU_FALLBACK_POSITION: GeoPoint = {
  latitude: 33.4996,
  longitude: 126.5312,
}
