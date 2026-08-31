import {
  createFeedComment,
  deleteFeedComment,
  deleteFeedPost,
  toggleFeedLike,
  type FeedComment,
  type FeedPost,
} from './api'

export function applyToggleLike(post: FeedPost, liked: boolean, likeCount: number): FeedPost {
  return {
    ...post,
    likedByMe: liked,
    likeCount,
  }
}

export function applyAppendComment(post: FeedPost, nextComment: FeedComment): FeedPost {
  return {
    ...post,
    commentCount: post.commentCount + 1,
    comments: [...post.comments, nextComment],
  }
}

export function applyRemoveComment(post: FeedPost, commentId: string): FeedPost {
  return {
    ...post,
    commentCount: Math.max(0, post.commentCount - 1),
    comments: post.comments.filter((item) => item.id !== commentId),
  }
}

export async function toggleLikeWithOptimistic(post: FeedPost) {
  const optimistic = applyToggleLike(post, !post.likedByMe, post.likeCount + (post.likedByMe ? -1 : 1))
  const result = await toggleFeedLike(post.id)
  const confirmed = applyToggleLike(post, result.liked, result.likeCount)
  return { optimistic, confirmed }
}

export async function createCommentWithPost(post: FeedPost, content: string) {
  const nextComment = await createFeedComment(post.id, content)
  return applyAppendComment(post, nextComment)
}

export async function deleteCommentWithPost(post: FeedPost, commentId: string) {
  await deleteFeedComment(commentId)
  return applyRemoveComment(post, commentId)
}

export async function deletePostById(feedPostId: string) {
  await deleteFeedPost(feedPostId)
}
