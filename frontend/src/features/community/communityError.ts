import axios from 'axios'

export function getCommunityErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) {
    return fallback
  }

  const message = typeof error.response?.data?.message === 'string' ? error.response.data.message : ''

  switch (message) {
    case 'Meetup is already full.':
      return '모집이 마감된 번개입니다.'
    case 'Completed meetup cannot be joined.':
      return '종료된 번개에는 참여할 수 없습니다.'
    case 'Cancelled meetup cannot be joined.':
      return '취소된 번개에는 참여할 수 없습니다.'
    case 'Already joined or requested this meetup.':
      return '이미 신청했거나 참여 중인 번개입니다.'
    case 'Participant is no longer pending.':
      return '이미 처리된 신청입니다.'
    case 'Completed meetup cannot be updated.':
      return '종료된 번개는 신청을 처리할 수 없습니다.'
    case 'Cancelled meetup cannot be updated.':
      return '취소된 번개는 신청을 처리할 수 없습니다.'
    default:
      return fallback
  }
}
