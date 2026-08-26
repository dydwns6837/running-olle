import type { Meetup } from './communityTypes'

export function MeetupApplicantsModal({
  meetup,
  onClose,
  onAccept,
  onReject,
}: {
  meetup: Meetup
  onClose: () => void
  onAccept: (meetupId: string, participantId: string) => void
  onReject: (meetupId: string, participantId: string) => void
}) {
  const pendingApplicants = meetup.applicants.filter((item) => item.status === 'pending')
  const acceptedApplicants = meetup.applicants.filter((item) => item.status === 'accepted')

  return (
    <div className="fixed inset-0 z-60 bg-[rgba(38,25,18,0.45)]" onClick={onClose}>
      <div className="mx-auto flex h-dvh max-w-[430px] flex-col bg-white" onClick={(event) => event.stopPropagation()}>
        <div className="border-b border-[#E1BFB1] px-5 py-4">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex h-[34px] w-[34px] items-center justify-center rounded-[10px] bg-[#F5F5F5] text-[17px]"
            >
              ←
            </button>
            <div className="text-[17px] font-bold text-[#261912]">요청자 관리</div>
          </div>
        </div>

        <div className="border-b border-[#FFE4CC] bg-[#FFF5EE] px-5 py-4">
          <div className="text-[14px] font-bold text-[#261912]">{meetup.title}</div>
          <div className="mt-1 text-[12px] text-[#8D7164]">
            {meetup.scheduleLabel} · 모집 {meetup.participantIds.length}/{meetup.maxParticipants}
          </div>
        </div>

        <div className="flex-1 overflow-y-auto bg-[#F5F5F5] px-5 py-4">
          <div className="mb-3 text-[13px] font-bold text-[#8D7164]">대기 중인 요청 {pendingApplicants.length}명</div>

          {pendingApplicants.length === 0 ? (
            <div className="rounded-[16px] bg-white p-4 text-[12px] text-[#8D7164]">현재 대기 중인 요청이 없습니다.</div>
          ) : null}

          {pendingApplicants.map((applicant) => (
            <div key={applicant.id} className="mb-3 rounded-[16px] bg-white p-4">
              <div className="flex items-center gap-3">
                <div
                  className="flex h-10 w-10 items-center justify-center rounded-full text-[14px] text-white"
                  style={{ backgroundImage: applicant.gradient }}
                >
                  {applicant.avatar}
                </div>
                <div className="flex-1">
                  <div className="text-[14px] font-bold text-[#261912]">{applicant.nickname}</div>
                  <div className="mt-1 text-[12px] text-[#8D7164]">참여 요청 대기</div>
                </div>
              </div>
              <div className="mt-4 grid grid-cols-3 rounded-[12px] border border-[#F0F0F0]">
                <StatItem label="누적 거리" value={`${applicant.stats.totalDistanceKm}km`} />
                <StatItem label="평균 페이스" value={applicant.stats.averagePaceText} />
                <StatItem label="번개 참여" value={`${applicant.stats.meetupCount}회`} />
              </div>
              <div className="mt-4 flex gap-2">
                <button
                  type="button"
                  onClick={() => onReject(meetup.id, applicant.id)}
                  className="flex-1 rounded-[12px] bg-[#FFE8E5] py-3 text-[14px] font-bold text-[#FF3B30]"
                >
                  거절
                </button>
                <button
                  type="button"
                  onClick={() => onAccept(meetup.id, applicant.id)}
                  className="flex-[1.7] rounded-[12px] bg-[#FF6F0F] py-3 text-[14px] font-bold text-white"
                >
                  수락하기
                </button>
              </div>
            </div>
          ))}

          <div className="mt-4 text-[13px] font-bold text-[#8D7164]">확정 멤버 {acceptedApplicants.length}명</div>
          <div className="mt-2 rounded-[16px] bg-white p-4">
            {acceptedApplicants.length === 0 ? (
              <div className="text-[12px] text-[#8D7164]">아직 확정된 멤버가 없습니다.</div>
            ) : (
              acceptedApplicants.map((member) => (
                <div key={member.id} className="flex items-center gap-3 py-2">
                  <div
                    className="flex h-10 w-10 items-center justify-center rounded-full text-[14px] text-white"
                    style={{ backgroundImage: member.gradient }}
                  >
                    {member.avatar}
                  </div>
                  <div className="flex-1">
                    <div className="text-[14px] font-bold text-[#261912]">{member.nickname}</div>
                    <div className="mt-1 text-[12px] text-[#8D7164]">참여 확정</div>
                  </div>
                  <span className="rounded-full bg-[#FFF5EE] px-3 py-1 text-[11px] font-bold text-[#FF6F0F]">멤버</span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function StatItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="border-r border-[#F0F0F0] px-2 py-3 text-center last:border-r-0">
      <div className="text-[14px] font-bold text-[#261912]">{value}</div>
      <div className="mt-1 text-[10px] text-[#B0B0B0]">{label}</div>
    </div>
  )
}
