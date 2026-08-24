import { useEffect, useRef, useState } from 'react'
import { connectChatRoomRealtime } from './chatRealtime'
import type { ChatRoom, Meetup } from './communityTypes'

export function ChatRoomModal({
  chatRoom,
  meetup,
  onClose,
  onSendMessage,
  onDeleteMessage,
  onRealtimeRoom,
  onShare,
}: {
  chatRoom: ChatRoom
  meetup: Meetup | undefined
  onClose: () => void
  onSendMessage: (chatRoomId: string, content: string) => void
  onDeleteMessage: (chatRoomId: string, messageId: string) => void
  onRealtimeRoom: (chatRoom: ChatRoom) => void
  onShare: (chatRoom: ChatRoom) => void
}) {
  const [message, setMessage] = useState('')
  const realtimeHandlerRef = useRef(onRealtimeRoom)

  useEffect(() => {
    realtimeHandlerRef.current = onRealtimeRoom
  }, [onRealtimeRoom])

  useEffect(() => {
    return connectChatRoomRealtime(chatRoom.id, (room) => realtimeHandlerRef.current(room))
  }, [chatRoom.id])

  const send = () => {
    if (!message.trim()) {
      return
    }
    onSendMessage(chatRoom.id, message.trim())
    setMessage('')
  }

  const handleMessageKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key !== 'Enter' || event.shiftKey || !isDesktopBrowser()) {
      return
    }

    event.preventDefault()
    send()
  }

  const contextLabel = meetup?.course
    ? `${meetup.course.name} · ${meetup.course.distanceKm}km`
    : meetup
      ? `${meetup.title} 문의`
      : chatRoom.subtitle

  const headerMeta =
    chatRoom.type === 'group'
      ? `번개 채팅 · 참여 ${chatRoom.participantCountLabel ?? '-'} · ${meetup?.scheduleLabel ?? ''}`
      : chatRoom.subtitle

  return (
    <div className="fixed inset-0 z-40 bg-[rgba(38,25,18,0.45)]" onClick={onClose}>
      <div
        className="mx-auto flex h-dvh max-w-[430px] flex-col bg-[#F7F7F7]"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="border-b border-[#F0F0F0] bg-white px-4 py-3">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex h-[34px] w-[34px] items-center justify-center rounded-[10px] bg-[#F5F5F5] text-[17px]"
              aria-label="닫기"
            >
              ←
            </button>
            <div
              className={`flex items-center justify-center ${
                chatRoom.type === 'group' ? 'h-9 w-9 rounded-[10px]' : 'h-9 w-9 rounded-full'
              } text-[16px] text-white`}
              style={{ backgroundImage: chatRoom.gradient }}
            >
              {chatRoom.icon}
            </div>
            <div className="min-w-0 flex-1">
              <div className="truncate text-[16px] font-bold text-[#261912]">{chatRoom.title}</div>
              <div className="mt-0.5 truncate text-[11px] text-[#B0B0B0]">{headerMeta}</div>
            </div>
            <button
              type="button"
              onClick={() => onShare(chatRoom)}
              className="flex h-8 w-8 items-center justify-center rounded-[8px] bg-[#F5F5F5] text-[16px] text-[#594136]"
              aria-label="공유"
            >
              ↗
            </button>
          </div>
        </div>

        <div className="mx-4 mt-3 flex items-center justify-between rounded-[12px] bg-[#FFF5EE] px-4 py-3">
          <div className="truncate pr-3 text-[12px] font-semibold text-[#FF6F0F]">{contextLabel}</div>
          <div className="shrink-0 text-[11px] text-[#888]">{meetup?.scheduleLabel ?? ''}</div>
        </div>

        <div className="flex-1 overflow-y-auto px-4 py-3">
          {chatRoom.messages.map((item) =>
            item.system ? (
              <div key={item.id} className="mb-4 text-center">
                <span className="inline-block rounded-full bg-[#F0F0F0] px-4 py-1.5 text-[11px] text-[#888]">
                  {item.content}
                </span>
              </div>
            ) : (
              <div key={item.id} className={`mb-4 flex gap-2 ${item.mine ? 'flex-row-reverse' : ''}`}>
                <div
                  className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-[13px] text-white"
                  style={{ backgroundImage: item.senderGradient }}
                >
                  {item.senderAvatar}
                </div>
                <div className={`max-w-[234px] ${item.mine ? 'text-right' : ''}`}>
                  {!item.mine ? <div className="mb-1 text-[11px] text-[#B0B0B0]">{item.senderName}</div> : null}
                  <div
                    className={`whitespace-pre-wrap px-4 py-3 text-[13px] leading-6 ${
                      item.mine
                        ? 'rounded-[18px_4px_18px_18px] bg-[#FF6F0F] text-white'
                        : 'rounded-[4px_18px_18px_18px] bg-[#EFEFEF] text-[#261912]'
                    }`}
                  >
                    {item.content}
                  </div>
                  <div className={`mt-1 flex items-center gap-2 text-[10px] text-[#B0B0B0] ${item.mine ? 'justify-end' : ''}`}>
                    <span>{item.sentAtLabel}</span>
                    {item.mine ? (
                      <button
                        type="button"
                        onClick={() => onDeleteMessage(chatRoom.id, item.id)}
                        className="text-[10px] font-semibold text-[#8D7164]"
                      >
                        삭제
                      </button>
                    ) : null}
                  </div>
                </div>
              </div>
            ),
          )}
        </div>

        <div className="border-t border-[#F0F0F0] bg-white px-4 py-3">
          <div className="flex items-end gap-2">
            {chatRoom.type === 'group' ? (
              <button
                type="button"
                className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-[#F5F5F5] text-[18px]"
                aria-label="추가 기능"
              >
                +
              </button>
            ) : null}
            <textarea
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              onKeyDown={handleMessageKeyDown}
              placeholder="메시지를 입력하세요."
              rows={1}
              className="min-h-10 flex-1 resize-none rounded-[20px] border border-[#E8E8E8] bg-[#FAFAFA] px-4 py-2.5 text-[14px] leading-5 outline-none"
            />
            <button
              type="button"
              onClick={send}
              className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#FF6F0F] text-[17px] text-white"
              aria-label="전송"
            >
              ↑
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

function isDesktopBrowser() {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return false
  }

  return window.matchMedia('(hover: hover) and (pointer: fine)').matches
}
