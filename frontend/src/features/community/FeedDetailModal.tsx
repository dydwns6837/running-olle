import { useEffect, useState } from 'react'
import { getFeedPost, type FeedPost } from './api'

export function FeedDetailModal({
  feedPostId,
  initialPost,
  onClose,
}: {
  feedPostId: string
  initialPost: FeedPost
  onClose: () => void
}) {
  const [post, setPost] = useState<FeedPost>(initialPost)

  useEffect(() => {
    let active = true
    getFeedPost(feedPostId)
      .then((data) => {
        if (active) setPost(data)
      })
      .catch(() => {})

    return () => {
      active = false
    }
  }, [feedPostId])

  return (
    <div className="fixed inset-0 z-30 bg-[rgba(38,25,18,0.45)]" onClick={onClose}>
      <div
        className="mx-auto mt-10 max-w-[430px] rounded-[20px] bg-[#FFF8F6] p-5 shadow-[0px_10px_30px_rgba(0,0,0,0.18)]"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-center justify-between">
          <div>
            <div className="text-[18px] font-black text-[#261912]">피드 상세</div>
            <div className="mt-1 text-[12px] text-[#594136]">{post.nickname} · {post.region}</div>
          </div>
          <button type="button" onClick={onClose} className="text-[13px] font-bold text-[#8D7164]">
            닫기
          </button>
        </div>

        <div className="mt-4 rounded-[16px] bg-white p-4">
          <div className="whitespace-pre-wrap text-[14px] leading-6 text-[#261912]">{post.content}</div>
          {post.imageUrls.length > 0 ? (
            <div className="mt-4 grid grid-cols-1 gap-2">
              {post.imageUrls.map((imageUrl) => (
                <div
                  key={imageUrl}
                  className="aspect-[4/3] rounded-[12px] bg-cover bg-center"
                  style={{ backgroundImage: `url(${imageUrl})` }}
                />
              ))}
            </div>
          ) : null}
          <div className="mt-4 text-[12px] font-bold text-[#594136]">
            좋아요 {post.likeCount} · 댓글 {post.commentCount}
          </div>
        </div>
      </div>
    </div>
  )
}
