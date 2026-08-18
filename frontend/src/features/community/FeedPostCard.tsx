import { useMemo, useState } from 'react'
import { createFeedComment, deleteFeedComment, deleteFeedPost, toggleFeedLike, type FeedPost } from './api'

type FeedPostCardProps = {
  post: FeedPost
  onChange: (post: FeedPost | null) => void
  onEdit: (post: FeedPost) => void
  onOpenDetail: (post: FeedPost) => void
}

export function FeedPostCard({ post, onChange, onEdit, onOpenDetail }: FeedPostCardProps) {
  const [comment, setComment] = useState('')
  const [pending, setPending] = useState(false)
  const [showAllComments, setShowAllComments] = useState(false)
  const createdLabel = useMemo(() => formatRelativeTime(post.createdAt), [post.createdAt])
  const visibleComments = showAllComments ? post.comments : post.comments.slice(0, 2)

  const handleLike = async () => {
    const current = { ...post }
    const likedByMe = !current.likedByMe
    onChange({
      ...current,
      likedByMe,
      likeCount: current.likeCount + (likedByMe ? 1 : -1),
    })

    try {
      const result = await toggleFeedLike(post.id)
      onChange({
        ...current,
        likedByMe: result.liked,
        likeCount: result.likeCount,
      })
    } catch {
      onChange(current)
    }
  }

  const handleCommentSubmit = async () => {
    if (!comment.trim() || pending) return
    setPending(true)

    try {
      const nextComment = await createFeedComment(post.id, comment.trim())
      onChange({
        ...post,
        commentCount: post.commentCount + 1,
        comments: [...post.comments, nextComment],
      })
      setComment('')
      setShowAllComments(true)
    } finally {
      setPending(false)
    }
  }

  const handleDeleteComment = async (commentId: string) => {
    const nextComments = post.comments.filter((item) => item.id !== commentId)
    onChange({
      ...post,
      commentCount: Math.max(0, post.commentCount - 1),
      comments: nextComments,
    })

    try {
      await deleteFeedComment(commentId)
    } catch {
      onChange(post)
    }
  }

  const handleDeletePost = async () => {
    if (!window.confirm('이 게시글을 삭제할까요?')) return

    try {
      await deleteFeedPost(post.id)
      onChange(null)
    } catch {
      window.alert('게시글을 삭제하지 못했습니다.')
    }
  }

  return (
    <article className="rounded-[16px] bg-white p-4 shadow-[0px_4px_12px_rgba(0,0,0,0.05)]">
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-full bg-[linear-gradient(135deg,#FF6F0F_0%,#FD934C_100%)] text-[14px] font-bold text-white">
            {post.nickname.slice(0, 1)}
          </div>
          <div>
            <div className="text-[14px] font-bold text-[#261912]">{post.nickname}</div>
            <div className="mt-1 text-[11px] text-[#594136]">
              {createdLabel} · {post.region}
            </div>
          </div>
        </div>
        {post.mine ? (
          <div className="flex gap-2">
            <button type="button" onClick={() => onEdit(post)} className="text-[12px] font-bold text-[#8D7164]">
              수정
            </button>
            <button type="button" onClick={handleDeletePost} className="text-[12px] font-bold text-[#8D7164]">
              삭제
            </button>
          </div>
        ) : null}
      </div>

      {post.runningRecord ? (
        <div className="mt-4 rounded-[12px] bg-[#FFF1EA] p-3">
          <div className="text-[13px] font-bold text-[#261912]">{post.course?.name ?? '러닝 기록'}</div>
          <div className="mt-1 text-[11px] text-[#594136]">
            {post.runningRecord.distanceKm.toFixed(1)}km · {formatDuration(post.runningRecord.durationSeconds)}
          </div>
        </div>
      ) : null}

      <button type="button" onClick={() => onOpenDetail(post)} className="mt-4 block w-full text-left">
        <p className="whitespace-pre-wrap text-[14px] leading-6 text-[#261912]">{post.content}</p>
      </button>

      {post.imageUrls.length > 0 ? (
        <button type="button" onClick={() => onOpenDetail(post)} className="mt-4 block w-full">
          <div className="grid grid-cols-3 gap-1 overflow-hidden rounded-[12px]">
            {post.imageUrls.slice(0, 3).map((imageUrl) => (
              <div key={imageUrl} className="aspect-square bg-cover bg-center" style={{ backgroundImage: `url(${imageUrl})` }} />
            ))}
          </div>
        </button>
      ) : null}

      <div className="mt-4 flex items-center gap-3 text-[12px] font-bold text-[#594136]">
        <button type="button" onClick={handleLike} className={post.likedByMe ? 'text-[#A04100]' : ''}>
          좋아요 {post.likeCount}
        </button>
        <span>댓글 {post.commentCount}</span>
        {post.course ? (
          <span className="ml-auto text-[#A04100]">
            {post.course.courseType === 'RUNNING_COURSE' ? '러닝코스' : '스팟코스'} · {post.course.name}
          </span>
        ) : null}
      </div>

      <div className="mt-4 space-y-2">
        {visibleComments.map((item) => (
          <div key={item.id} className="rounded-[12px] bg-[#FFF8F6] px-3 py-3">
            <div className="flex items-center justify-between gap-3">
              <div className="text-[12px] font-bold text-[#261912]">{item.nickname}</div>
              {item.mine ? (
                <button type="button" onClick={() => handleDeleteComment(item.id)} className="text-[10px] font-bold text-[#8D7164]">
                  삭제
                </button>
              ) : null}
            </div>
            <div className="mt-1 text-[12px] leading-5 text-[#594136]">{item.content}</div>
          </div>
        ))}
        {post.comments.length > 2 ? (
          <button type="button" onClick={() => setShowAllComments((prev) => !prev)} className="text-[12px] font-bold text-[#A04100]">
            {showAllComments ? '댓글 접기' : `댓글 ${post.comments.length - 2}개 더보기`}
          </button>
        ) : null}
      </div>

      <div className="mt-4 flex gap-2">
        <input
          value={comment}
          onChange={(event) => setComment(event.target.value)}
          placeholder="댓글을 입력하세요."
          className="h-11 flex-1 rounded-full border border-[#E1BFB1] bg-[#FFF8F6] px-4 text-[13px] text-[#261912] outline-none"
        />
        <button
          type="button"
          onClick={handleCommentSubmit}
          disabled={pending || !comment.trim()}
          className="h-11 rounded-full bg-[linear-gradient(135deg,#FF6F0F_0%,#FD934C_100%)] px-4 text-[13px] font-bold text-white disabled:opacity-40"
        >
          등록
        </button>
      </div>
    </article>
  )
}

function formatDuration(seconds: number) {
  const minutes = Math.floor(seconds / 60)
  const remainSeconds = seconds % 60
  return `${minutes}:${String(remainSeconds).padStart(2, '0')}`
}

function formatRelativeTime(value: string) {
  const date = new Date(value)
  const diffMs = Date.now() - date.getTime()
  const diffMinutes = Math.max(1, Math.floor(diffMs / 60000))

  if (diffMinutes < 60) return `${diffMinutes}분 전`
  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}시간 전`
  return `${Math.floor(diffHours / 24)}일 전`
}
