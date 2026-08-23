import { axiosInstance } from '../../api/axiosInstance'
import type { CourseWaypointDraft, DraftRoute, PlaceDetail, PlaceSearchResult } from './types'

type DraftRouteWaypointRequest = {
  kakaoPlaceId: string | null
  name: string
  lat: number
  lng: number
  orderIndex: number
}

export const courseBuilderService = {
  searchPlaces(keyword: string, lat: number, lng: number, radius: number) {
    return axiosInstance.get<PlaceSearchResult[]>('/places/search', {
      params: { keyword, lat, lng, radius },
    }).then(({ data }) => data)
  },

  getPlaceDetail(place: PlaceSearchResult) {
    return axiosInstance.get<PlaceDetail>(`/places/${place.kakaoPlaceId}/detail`, {
      params: {
        name: place.name,
        lat: place.lat,
        lng: place.lng,
        categoryGroupCode: place.categoryGroupCode,
      },
    }).then(({ data }) => data)
  },

  calculateDraftRoute(waypoints: CourseWaypointDraft[]) {
    const body: { waypoints: DraftRouteWaypointRequest[] } = {
      waypoints: waypoints.map((waypoint, index) => ({
        kakaoPlaceId: waypoint.kakaoPlaceId,
        name: waypoint.name,
        lat: waypoint.lat,
        lng: waypoint.lng,
        orderIndex: index,
      })),
    }

    return axiosInstance.post<DraftRoute>('/courses/draft/route', body).then(({ data }) => data)
  },
}
