import { axiosInstance } from '../../api/axiosInstance'
import type { CourseDetail, CourseListFilter, CourseListItem, CourseListScope } from './types'

type GetCoursesParams = {
  filter: CourseListFilter
  scope: CourseListScope
}

export const courseService = {
  getCourses({ filter, scope }: GetCoursesParams) {
    return axiosInstance.get<CourseListItem[]>('/courses', {
      params: { filter, scope },
    }).then(({ data }) => data)
  },

  getCourse(courseId: string) {
    return axiosInstance.get<CourseDetail>(`/courses/${courseId}`).then(({ data }) => data)
  },
}
