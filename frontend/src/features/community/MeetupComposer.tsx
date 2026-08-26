import { useMemo, useState } from 'react'
import type { Meetup, MeetupTheme } from './communityTypes'
import type { MeetupCreatePayload } from './meetupApi'

export function MeetupComposer({
  editingMeetup,
  onClose,
  onSubmit,
}: {
  editingMeetup?: Meetup | null
  onClose: () => void
  onSubmit: (payload: MeetupCreatePayload) => void | Promise<void>
}) {
  const isEditMode = !!editingMeetup
  const [title, setTitle] = useState(editingMeetup?.title ?? '')
  const [description, setDescription] = useState(editingMeetup?.description ?? '')
  const [meetupDate, setMeetupDate] = useState(toDateTimeLocal(editingMeetup?.meetupDate ?? '2026-08-25T07:00:00'))
  const [meetingPlace, setMeetingPlace] = useState(editingMeetup?.locationLabel ?? '함덕해수욕장')
  const [latitude, setLatitude] = useState(String(editingMeetup?.meetingLatitude ?? 33.5432))
  const [longitude, setLongitude] = useState(String(editingMeetup?.meetingLongitude ?? 126.6695))
  const [maxParticipants, setMaxParticipants] = useState(String(editingMeetup?.maxParticipants ?? 6))
  const [targetPaceLabel, setTargetPaceLabel] = useState(
    editingMeetup?.targetPaceValue != null ? formatPaceValue(editingMeetup.targetPaceValue) : `6'30"`,
  )
  const [joinMethod, setJoinMethod] = useState<'INSTANT' | 'APPROVAL'>(
    editingMeetup?.joinMethod === 'approval' ? 'APPROVAL' : 'INSTANT',
  )
  const [theme, setTheme] = useState<MeetupTheme>(editingMeetup?.theme ?? 'coast')
  const [error, setError] = useState('')

  const titleText = useMemo(() => (isEditMode ? '번개 수정' : '번개 만들기'), [isEditMode])

  const submit = () => {
    if (!title.trim() || !description.trim()) {
      setError('제목과 설명을 입력해 주세요.')
      return
    }

    const parsedLatitude = Number(latitude)
    const parsedLongitude = Number(longitude)
    const parsedMax = Number(maxParticipants)

    if (Number.isNaN(parsedLatitude) || Number.isNaN(parsedLongitude)) {
      setError('좌표 형식을 확인해 주세요.')
      return
    }

    if (Number.isNaN(parsedMax) || parsedMax < 2) {
      setError('최대 인원은 2명 이상이어야 합니다.')
      return
    }

    onSubmit({
      title: title.trim(),
      description: description.trim(),
      meetupDate,
      maxParticipants: parsedMax,
      targetPace: parsePace(targetPaceLabel),
      meetingPlace: meetingPlace.trim(),
      latitude: parsedLatitude,
      longitude: parsedLongitude,
      joinMethod,
      themeCode: theme,
      courseId: editingMeetup?.course?.id ?? null,
    })
  }

  return (
    <div className="fixed inset-0 z-40 bg-[rgba(38,25,18,0.45)]">
      <div className="mx-auto flex h-dvh max-w-[430px] flex-col bg-[#FFF8F6]">
        <div className="flex items-center justify-between border-b border-[#E1BFB1] bg-[#FFF8F6] px-5 py-4">
          <button
            type="button"
            onClick={onClose}
            className="rounded-full border border-[#E1BFB1] px-4 py-2 text-[13px] font-bold text-[#594136]"
          >
            취소
          </button>
          <strong className="text-[16px] font-bold text-[#261912]">{titleText}</strong>
          <button
            type="button"
            onClick={submit}
            className="rounded-full bg-[#FF6F0F] px-4 py-2 text-[13px] font-bold text-white"
          >
            저장
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-5 py-5">
          <Field label="제목">
            <input value={title} onChange={(event) => setTitle(event.target.value)} className={inputClassName} />
          </Field>
          <Field label="설명">
            <textarea
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              className={`${inputClassName} min-h-[120px] py-3`}
            />
          </Field>
          <Field label="일시">
            <input
              type="datetime-local"
              value={meetupDate}
              onChange={(event) => setMeetupDate(event.target.value)}
              className={inputClassName}
            />
          </Field>
          <Field label="집결 장소">
            <input value={meetingPlace} onChange={(event) => setMeetingPlace(event.target.value)} className={inputClassName} />
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="위도">
              <input value={latitude} onChange={(event) => setLatitude(event.target.value)} className={inputClassName} />
            </Field>
            <Field label="경도">
              <input value={longitude} onChange={(event) => setLongitude(event.target.value)} className={inputClassName} />
            </Field>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <Field label="최대 인원">
              <input value={maxParticipants} onChange={(event) => setMaxParticipants(event.target.value)} className={inputClassName} />
            </Field>
            <Field label="목표 페이스">
              <input value={targetPaceLabel} onChange={(event) => setTargetPaceLabel(event.target.value)} className={inputClassName} />
            </Field>
          </div>
          <Field label="참여 방식">
            <div className="grid grid-cols-2 gap-3">
              <ChipButton active={joinMethod === 'INSTANT'} onClick={() => setJoinMethod('INSTANT')}>
                즉시 참여
              </ChipButton>
              <ChipButton active={joinMethod === 'APPROVAL'} onClick={() => setJoinMethod('APPROVAL')}>
                수락 후 참여
              </ChipButton>
            </div>
          </Field>
          <Field label="테마">
            <div className="flex flex-wrap gap-2">
              {(['coast', 'forest', 'oreum', 'photo', 'food'] as MeetupTheme[]).map((item) => (
                <ChipButton key={item} active={theme === item} onClick={() => setTheme(item)}>
                  {themeToLabel(item)}
                </ChipButton>
              ))}
            </div>
          </Field>
          {error ? (
            <div className="mt-4 rounded-[12px] bg-[#FFF1EE] px-4 py-3 text-[12px] text-[#B91C1C]">{error}</div>
          ) : null}
        </div>
      </div>
    </div>
  )
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="mt-4">
      <div className="mb-2 text-[13px] font-bold text-[#261912]">{label}</div>
      {children}
    </div>
  )
}

function ChipButton({
  active,
  children,
  onClick,
}: {
  active: boolean
  children: React.ReactNode
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-[12px] border px-4 py-3 text-[13px] font-bold ${
        active ? 'border-[#FF6F0F] bg-[#FFF1EA] text-[#A04100]' : 'border-[#E1BFB1] bg-white text-[#594136]'
      }`}
    >
      {children}
    </button>
  )
}

function themeToLabel(theme: MeetupTheme) {
  switch (theme) {
    case 'forest':
      return '숲길'
    case 'oreum':
      return '오름'
    case 'photo':
      return '포토'
    case 'food':
      return '맛집'
    default:
      return '해안'
  }
}

function parsePace(value: string) {
  const match = value.match(/(\d+)\D+(\d{1,2})/)
  if (!match) {
    return null
  }
  const minutes = Number(match[1])
  const seconds = Number(match[2])
  if (Number.isNaN(minutes) || Number.isNaN(seconds)) {
    return null
  }
  return minutes + seconds / 60
}

function formatPaceValue(value: number) {
  const totalSeconds = Math.round(value * 60)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}'${String(seconds).padStart(2, '0')}"`
}

function toDateTimeLocal(value: string) {
  return value.slice(0, 16)
}

const inputClassName =
  'h-12 w-full rounded-[12px] border border-[#E1BFB1] bg-white px-4 text-[13px] text-[#261912] outline-none'
