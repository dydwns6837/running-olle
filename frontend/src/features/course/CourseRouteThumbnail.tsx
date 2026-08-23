import type { CourseRouteCoordinate, CourseWaypoint } from './types'

type CourseRouteThumbnailProps = {
  routeCoordinates: CourseRouteCoordinate[]
  waypoints: CourseWaypoint[]
}

type Point = {
  x: number
  y: number
}

const WIDTH = 320
const HEIGHT = 156
const PADDING = 28

export function CourseRouteThumbnail({ routeCoordinates, waypoints }: CourseRouteThumbnailProps) {
  const waypointCoordinates = waypoints.map(({ lat, lng }) => ({ lat, lng }))
  const bounds = boundsFor([...routeCoordinates, ...waypointCoordinates])
  const routePoints = normalizePoints(routeCoordinates, bounds)
  const waypointPoints = normalizePoints(waypointCoordinates, bounds)
  const pathPoints = routePoints.map((point) => `${point.x.toFixed(1)},${point.y.toFixed(1)}`).join(' ')

  return (
    <div className="course-route-thumbnail" aria-hidden>
      <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img">
        <path className="thumbnail-land thumbnail-land-a" d="M0 112 C54 84 91 96 132 63 C184 21 227 46 320 18 L320 156 L0 156 Z" />
        <path className="thumbnail-land thumbnail-land-b" d="M0 38 C55 24 88 44 130 31 C184 13 222 8 320 28 L320 0 L0 0 Z" />
        <path className="thumbnail-road" d="M5 118 C74 105 88 76 132 76 C175 76 188 111 235 103 C270 97 290 81 315 88" />
        {routePoints.length > 1 && <polyline className="thumbnail-route" points={pathPoints} />}
        {waypointPoints.map((point, index) => (
          <g key={`${point.x}-${point.y}-${index}`} className="thumbnail-marker" transform={`translate(${point.x} ${point.y})`}>
            <circle r="15" />
            <text y="5">{index + 1}</text>
          </g>
        ))}
      </svg>
    </div>
  )
}

function boundsFor(coordinates: CourseRouteCoordinate[]) {
  if (coordinates.length === 0) {
    return { minLat: 33.35, maxLat: 33.55, minLng: 126.3, maxLng: 126.7 }
  }
  const minLat = Math.min(...coordinates.map((coordinate) => coordinate.lat))
  const maxLat = Math.max(...coordinates.map((coordinate) => coordinate.lat))
  const minLng = Math.min(...coordinates.map((coordinate) => coordinate.lng))
  const maxLng = Math.max(...coordinates.map((coordinate) => coordinate.lng))
  return { minLat, maxLat, minLng, maxLng }
}

function normalizePoints(
  coordinates: CourseRouteCoordinate[],
  bounds: { minLat: number; maxLat: number; minLng: number; maxLng: number },
): Point[] {
  if (coordinates.length === 0) return []

  const latRange = Math.max(bounds.maxLat - bounds.minLat, 0.0001)
  const lngRange = Math.max(bounds.maxLng - bounds.minLng, 0.0001)

  return coordinates.map((coordinate) => ({
    x: PADDING + ((coordinate.lng - bounds.minLng) / lngRange) * (WIDTH - PADDING * 2),
    y: HEIGHT - PADDING - ((coordinate.lat - bounds.minLat) / latRange) * (HEIGHT - PADDING * 2),
  }))
}
