import { useEffect, useState } from 'react'
import { FeedComposer } from '../../features/community/FeedComposer'
import { FeedDetailModal } from '../../features/community/FeedDetailModal'
import { FeedPostCard } from '../../features/community/FeedPostCard'
import { getFeed, type FeedPost } from '../../features/community/api'

type CommunityTab = 'feed' | 'meetup' | 'chat'
type FeedFilter = 'all' | 'running' | 'spot' | 'photo'

const tabs: Array<{ key: CommunityTab; label: string }> = [
  { key: 'feed', label: '피드' },
  { key: 'meetup', label: '번개' },
  { key: 'chat', label: '채팅' },
]

const filters: Array<{ key: FeedFilter; label: string }> = [
  { key: 'all', label: '전체' },
  { key: 'running', label: '🏃 러닝코스' },
  { key: 'spot', label: '🌊 스팟코스' },
  { key: 'photo', label: '📸 포토' },
]

export function CommunityPage() {
  const [activeTab, setActiveTab] = useState<CommunityTab>('feed')
  const [activeFilter, setActiveFilter] = useState<FeedFilter>('all')
  const [posts, setPosts] = useState<FeedPost[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [composerOpen, setComposerOpen] = useState(false)
  const [editingPost, setEditingPost] = useState<FeedPost | null>(null)
  const [detailPost, setDetailPost] = useState<FeedPost | null>(null)

  useEffect(() => {
    let active = true
    setLoading(true)
    setError('')

    getFeed()
      .then((data) => {
        if (active) {
          setPosts(data)
        }
      })
      .catch(() => {
        if (active) {
          setError('피드를 불러오지 못했습니다.')
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [])

  const filteredPosts = posts.filter((post) => {
    if (activeFilter === 'all') return true
    if (activeFilter === 'photo') return post.photoTagged
    if (activeFilter === 'running') return post.course?.courseType === 'RUNNING_COURSE'
    if (activeFilter === 'spot') return post.course?.courseType === 'SPOT_COURSE'
    return true
  })

  const updatePost = (targetId: string, nextPost: FeedPost | null) => {
    setPosts((current) =>
      current.flatMap((post) => {
        if (post.id !== targetId) return [post]
        return nextPost ? [nextPost] : []
      }),
    )

    if (detailPost?.id === targetId) {
      setDetailPost(nextPost)
    }
  }

  const upsertPost = (post: FeedPost) => {
    setPosts((current) => {
      const exists = current.some((item) => item.id === post.id)
      if (!exists) {
        return [post, ...current]
      }
      return current.map((item) => (item.id === post.id ? post : item))
    })

    setComposerOpen(false)
    setEditingPost(null)
    setDetailPost(post)
    setActiveTab('feed')
  }

  return (
    <>
      <section className="flex items-start justify-between">
        <h1 className="text-[20px] font-black leading-[28px] text-[#261912]">커뮤니티</h1>
        <button
          type="button"
          onClick={() => {
            setEditingPost(null)
            setComposerOpen(true)
          }}
          className="flex h-9 w-9 items-center justify-center rounded-[10px] bg-[#FF6F0F] text-[16px] text-white shadow-[0px_4px_12px_rgba(0,0,0,0.08)]"
          aria-label="피드 작성"
        >
          ✏️
        </button>
      </section>

      <div className="mt-4 flex gap-[22px] border-b border-[#E1BFB1]">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            onClick={() => setActiveTab(tab.key)}
            className={`border-b-[2.5px] pb-3 text-[14px] font-bold ${
              activeTab === tab.key
                ? 'border-[#FF6F0F] text-[#261912]'
                : 'border-transparent text-[#8D7164]'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'feed' ? (
        <>
          <div className="mt-4 flex gap-2 overflow-x-auto pb-1 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
            {filters.map((filter) => (
              <button
                key={filter.key}
                type="button"
                onClick={() => setActiveFilter(filter.key)}
                className={`shrink-0 rounded-full border px-4 py-2 text-[12px] font-bold ${
                  activeFilter === filter.key
                    ? 'border-[#FF6F0F] bg-[#FF6F0F] text-white'
                    : 'border-[#E1BFB1] bg-white text-[#594136]'
                }`}
              >
                {filter.label}
              </button>
            ))}
          </div>

          <div className="mt-3 rounded-[12px] border border-[#FFE4CC] bg-[#FFF5EE] px-4 py-2.5 text-[12px] font-bold text-[#A04100]">
            🕐 최근 7일 · 제주 지역 러너들의 실시간 기록
          </div>

          <div className="mt-4 flex flex-col gap-2">
            {loading ? <FeedStateBox message="피드를 불러오는 중입니다." /> : null}
            {!loading && error ? <FeedStateBox message={error} tone="error" /> : null}
            {!loading && !error && filteredPosts.length === 0 ? (
              <FeedStateBox message="조건에 맞는 피드가 없습니다." />
            ) : null}
            {!loading && !error
              ? filteredPosts.map((post) => (
                  <FeedPostCard
                    key={post.id}
                    post={post}
                    onChange={(nextPost) => updatePost(post.id, nextPost)}
                    onEdit={(target) => {
                      setEditingPost(target)
                      setComposerOpen(true)
                    }}
                    onOpenDetail={(target) => setDetailPost(target)}
                  />
                ))
              : null}
          </div>
        </>
      ) : (
        <div className="mt-6 rounded-[16px] bg-white p-5 shadow-[0px_4px_12px_rgba(0,0,0,0.05)]">
          <div className="text-[15px] font-bold text-[#261912]">
            {activeTab === 'meetup' ? '번개 기능은 다음 단계에서 붙입니다.' : '채팅 기능은 번개 다음 단계에서 붙입니다.'}
          </div>
          <p className="mt-2 text-[13px] leading-6 text-[#594136]">
            이번 단계에서는 피드 경험을 먼저 정리했습니다. 번개와 채팅은 같은 화면 구조를
            유지하면서 이어서 확장하면 됩니다.
          </p>
        </div>
      )}

      {composerOpen ? (
        <FeedComposer
          editingPost={editingPost}
          onCancel={() => {
            setComposerOpen(false)
            setEditingPost(null)
          }}
          onCreated={upsertPost}
        />
      ) : null}

      {detailPost ? (
        <FeedDetailModal
          feedPostId={detailPost.id}
          initialPost={detailPost}
          onClose={() => setDetailPost(null)}
          onChange={(nextPost) => updatePost(detailPost.id, nextPost)}
          onEdit={(target) => {
            setDetailPost(null)
            setEditingPost(target)
            setComposerOpen(true)
          }}
        />
      ) : null}
    </>
  )
}

function FeedStateBox({ message, tone = 'normal' }: { message: string; tone?: 'normal' | 'error' }) {
  return (
    <div
      className={`rounded-[16px] px-4 py-5 text-[13px] font-bold ${
        tone === 'error'
          ? 'bg-[#FFF1EE] text-[#B91C1C]'
          : 'bg-white text-[#594136] shadow-[0px_4px_12px_rgba(0,0,0,0.05)]'
      }`}
    >
      {message}
    </div>
  )
}
