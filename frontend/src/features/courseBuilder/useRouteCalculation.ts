import { useEffect } from 'react'
import { courseBuilderService } from './courseBuilderService'
import { useCourseDraftStore } from './courseDraftStore'

export function useRouteCalculation() {
  const waypoints = useCourseDraftStore((state) => state.waypoints)
  const setDraftRoute = useCourseDraftStore((state) => state.setDraftRoute)
  const setRouteStatus = useCourseDraftStore((state) => state.setRouteStatus)

  useEffect(() => {
    if (waypoints.length < 2) {
      setDraftRoute(null)
      setRouteStatus('idle')
      return
    }

    let disposed = false
    setRouteStatus('loading')
    const timer = window.setTimeout(() => {
      courseBuilderService.calculateDraftRoute(waypoints)
        .then((draftRoute) => {
          if (disposed) return
          setDraftRoute(draftRoute)
          setRouteStatus('success')
        })
        .catch(() => {
          if (disposed) return
          setDraftRoute(null)
          setRouteStatus('error', '경로를 계산하지 못했어요. 경유지 위치를 다시 확인해 주세요.')
        })
    }, 500)

    return () => {
      disposed = true
      window.clearTimeout(timer)
    }
  }, [setDraftRoute, setRouteStatus, waypoints])
}
