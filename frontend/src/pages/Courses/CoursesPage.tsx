import { CourseListView } from '../../features/course/CourseListView'

export function CoursesPage() {
  return (
    <CourseListView
      scope="AVAILABLE"
      title="코스 선택 달리기"
      subtitle="저장한 코스와 공개 코스 중에서 오늘 달릴 제주 코스를 고르세요."
      emptyTitle="아직 달릴 수 있는 코스가 없어요"
      emptyDescription="코스 만들고 달리기에서 제주 거점을 추가해 첫 코스를 저장해 보세요."
    />
  )
}
