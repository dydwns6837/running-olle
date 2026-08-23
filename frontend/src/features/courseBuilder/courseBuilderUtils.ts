import type { LatLng } from './types'

const EARTH_RADIUS_METERS = 6_371_000
const WALKING_SPEED_KM_PER_HOUR = 4

function toRadians(degrees: number) {
  return degrees * Math.PI / 180
}

export function distanceMeters(from: LatLng, to: LatLng) {
  const latitudeDelta = toRadians(to.lat - from.lat)
  const longitudeDelta = toRadians(to.lng - from.lng)
  const fromLatitude = toRadians(from.lat)
  const toLatitude = toRadians(to.lat)
  const haversine = Math.sin(latitudeDelta / 2) ** 2
    + Math.cos(fromLatitude) * Math.cos(toLatitude) * Math.sin(longitudeDelta / 2) ** 2

  return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
}

export function approximateWalkingMinutes(from: LatLng | null, to: LatLng) {
  if (!from) return null
  const distanceKm = distanceMeters(from, to) / 1_000
  return Math.max(1, Math.round(distanceKm / WALKING_SPEED_KM_PER_HOUR * 60))
}

export function difficultyLabel(value: string) {
  if (value === 'HIGH') return '상'
  if (value === 'MID') return '중'
  return '하'
}

export function formatDistanceKm(value: number) {
  return Number.isInteger(value) ? value.toFixed(0) : value.toFixed(1)
}

export function kakaoSearchUrl(name: string) {
  return `https://map.kakao.com/link/search/${encodeURIComponent(name)}`
}
