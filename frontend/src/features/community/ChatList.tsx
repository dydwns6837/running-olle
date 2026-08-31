import type { ChatRoom } from './communityTypes'

export function ChatList({
  groupChats,
  inquiryChats,
  onOpenChat,
  searchValue,
  onSearchChange,
  searchOpen,
}: {
  groupChats: ChatRoom[]
  inquiryChats: ChatRoom[]
  onOpenChat: (chat: ChatRoom) => void
  searchValue: string
  onSearchChange: (value: string) => void
  searchOpen: boolean
}) {
  return (
    <>
      {searchOpen ? (
        <div className="mt-4">
          <input
            value={searchValue}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="채팅방 이름이나 메시지로 검색"
            className="h-11 w-full rounded-full border border-[#E1BFB1] bg-white px-4 text-[13px] text-[#261912] outline-none"
          />
        </div>
      ) : null}

      <div className="mt-4 rounded-[12px] border border-[#EBEBEB] bg-[#F5F5F5] px-5 py-3 text-[13px] font-semibold text-[#888]">
        참여 중인 번개 채팅방 {groupChats.length}개
      </div>

      <div className="mt-3 overflow-hidden rounded-[18px] bg-white shadow-[0px_2px_12px_rgba(0,0,0,0.06)]">
        {groupChats.length === 0 ? (
          <div className="px-5 py-5 text-[13px] text-[#8D7164]">참여 중인 그룹 채팅방이 없습니다.</div>
        ) : (
          groupChats.map((chat) => <ChatRow key={chat.id} chat={chat} onOpen={onOpenChat} />)
        )}
      </div>

      <div className="mt-5 px-1 text-[12px] font-bold text-[#B0B0B0]">1:1 문의</div>
      <div className="mt-2 overflow-hidden rounded-[18px] bg-white shadow-[0px_2px_12px_rgba(0,0,0,0.06)]">
        {inquiryChats.length === 0 ? (
          <div className="px-5 py-5 text-[13px] text-[#8D7164]">문의 채팅이 없습니다.</div>
        ) : (
          inquiryChats.map((chat) => <ChatRow key={chat.id} chat={chat} onOpen={onOpenChat} />)
        )}
      </div>
    </>
  )
}

function ChatRow({ chat, onOpen }: { chat: ChatRoom; onOpen: (chat: ChatRoom) => void }) {
  return (
    <button
      type="button"
      onClick={() => onOpen(chat)}
      className="flex w-full items-center gap-3 border-b border-[#F5F5F5] px-5 py-4 text-left last:border-b-0"
    >
      <div className="relative">
        <div
          className={`flex items-center justify-center ${
            chat.type === 'group' ? 'h-[50px] w-[50px] rounded-[14px]' : 'h-[50px] w-[50px] rounded-full'
          } text-[22px] text-white`}
          style={{ backgroundImage: chat.gradient }}
        >
          {chat.icon}
        </div>
        {chat.activeDot ? (
          <div className="absolute bottom-[2px] right-[2px] h-[10px] w-[10px] rounded-full border-2 border-white bg-[#34C759]" />
        ) : null}
      </div>
      <div className="min-w-0 flex-1">
        <div className="truncate text-[15px] font-bold text-[#261912]">{chat.title}</div>
        <div className="mt-1 truncate text-[13px] text-[#888]">{chat.lastMessage}</div>
      </div>
      <div className="text-right">
        <div className="text-[11px] text-[#B0B0B0]">{chat.lastMessageAt}</div>
        {chat.unreadCount > 0 ? (
          <div className="ml-auto mt-1 flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-[#FF6F0F] px-1 text-[10px] font-bold text-white">
            {chat.unreadCount}
          </div>
        ) : null}
      </div>
    </button>
  )
}
