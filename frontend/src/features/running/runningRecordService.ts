import { axiosInstance } from '../../api/axiosInstance'
import type { GeoPoint, SavedRunningRecord } from './types'

const RECORDS_KEY = 'runningOlleRunningRecords'

type RecordInput = Omit<SavedRunningRecord, 'localId' | 'serverId' | 'syncStatus'>

function readRecords(): SavedRunningRecord[] {
  try {
    return JSON.parse(localStorage.getItem(RECORDS_KEY) ?? '[]') as SavedRunningRecord[]
  } catch {
    return []
  }
}

function writeRecord(record: SavedRunningRecord) {
  const records = readRecords()
  const index = records.findIndex(({ localId }) => localId === record.localId)
  if (index >= 0) records[index] = record
  else records.unshift(record)
  localStorage.setItem(RECORDS_KEY, JSON.stringify(records.slice(0, 50)))
}

function routeForApi(route: GeoPoint[]) {
  return route.map(({ latitude, longitude }) => ({ latitude, longitude }))
}

export async function saveRunningRecord(input: RecordInput) {
  const pending: SavedRunningRecord = {
    ...input,
    localId: crypto.randomUUID(),
    syncStatus: 'pending',
  }
  writeRecord(pending)

  try {
    const { data } = await axiosInstance.post<{ id: string }>('/running-records', {
      route: routeForApi(input.route),
      totalDistanceMeters: input.distanceMeters,
      totalDurationSeconds: input.durationSeconds,
      averagePace: input.averagePace,
      calories: input.calories,
      startedAt: input.startedAt,
      endedAt: input.endedAt,
    })
    const synced = { ...pending, serverId: data.id, syncStatus: 'synced' as const }
    writeRecord(synced)
    return synced
  } catch {
    return pending
  }
}
