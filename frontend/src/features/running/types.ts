export type GeoPoint = {
  latitude: number
  longitude: number
  accuracy?: number
  timestamp?: number
}

export type RunningPhase = 'running' | 'paused'

export type SavedRunningRecord = {
  localId: string
  serverId?: string
  distanceMeters: number
  durationSeconds: number
  averagePace: number | null
  calories: number
  startedAt: string
  endedAt: string
  route: GeoPoint[]
  syncStatus: 'synced' | 'pending'
}
