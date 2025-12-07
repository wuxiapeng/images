package com.wu.tiktok2.ui.adapter

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.wu.tiktok2.data.model.VideoModel
import com.wu.tiktok2.databinding.ItemVideoBinding
import com.wu.tiktok2.ui.animator.ViewAnimationUtils.playLikeAnimation
import com.wu.tiktok2.ui.animator.ViewAnimationUtils.showHeartAtTouch

class VideoAdapter(
    private var initialList: List<VideoModel>,
    private val onVideoClick: (Int) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    //关键修改：在内部将其转为 MutableList，这样才能增删改
    private var videoList: MutableList<VideoModel> = initialList.toMutableList()

    private val LIKE_COLOR = Color.parseColor("#FF4081") // 点赞红色
    private val UNLIKE_COLOR = Color.WHITE // 未点赞白色

    // 更新数据
    fun updateData(newVideos: List<VideoModel>) {
        this.videoList = newVideos.toMutableList()
        notifyDataSetChanged()
    }

    // 获取特定位置的数据（给 Fragment 用）
    fun getItem(position: Int): VideoModel? {
        return videoList.getOrNull(position)
    }

    // 清理动画（防止内存泄漏）
    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.stopAnimation()
    }

    // ViewHolder
    inner class VideoViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        private var rotationAnimator: ObjectAnimator? = null

        @OptIn(UnstableApi::class)
        fun bind(video: VideoModel, position: Int) {
            // 原有数据绑定
            binding.tvTitle.text = video.title
            binding.tvDesc.text = video.description
            binding.tvAudio.text = video.audioName
            binding.tvAudio.isSelected = true // 跑马灯
            binding.likeCount.text = video.likeCount.toString() // 显示点赞数
            binding.ivCover.visibility = View.VISIBLE
            binding.ivCover.alpha = 1f // 确保透明度是 1，防止复用时是透明的
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // 默认竖屏填满，防止闪烁
            binding.btnFullScreen.visibility = View.GONE

            //加载封面
            Glide.with(binding.root)
                .load(video.videoUrl)
                .centerCrop()
                .into(binding.ivCover)

            // 加载头像
            Glide.with(binding.root)
                .load("https://ui-avatars.com/api/?name=User")
                .circleCrop()
                .into(binding.ivAvatar)

            // 初始化点赞状态（颜色+动画初始化）
            updateLikeUI(video.isLiked)
            // 绑定双击/单击事件
            setupGestureListener(video, position)
            // 绑定爱心图标点击事件
            binding.ivLike.setOnClickListener {
                toggleLike(video, position)
            }

            // 唱片旋转动画
            startRotation()
        }

        /**
         * 切换点赞/取消点赞（核心逻辑）
         */
        private fun toggleLike(video: VideoModel, position: Int) {
            val newLikeState = !video.isLiked

            // 1. 更新数据模型（同步状态和点赞数）
            video.isLiked = newLikeState
            video.likeCount = if (newLikeState) video.likeCount + 1 else video.likeCount - 1
            binding.likeCount.text = video.likeCount.toString() // 实时更新点赞数

            // 2. 更新 UI 和动画
            if (newLikeState) {
                // 点赞：播放动画+变红
                playLikeAnimation(binding.ivLike,binding.lavExplosion)
            } else {
                // 取消点赞：直接变白（无动画）
                updateLikeUI(false)
            }
        }

        /**
         * 更新点赞 UI（仅颜色，无动画）
         */
        private fun updateLikeUI(isLiked: Boolean) {
            binding.ivLike.setColorFilter(if (isLiked) LIKE_COLOR else UNLIKE_COLOR)
        }


        /**
         * 手势监听：双击点赞 + 单击播放/暂停
         */
        @SuppressLint("ClickableViewAccessibility")
        private fun setupGestureListener(video: VideoModel, position: Int) {
            val gestureDetector = GestureDetector(binding.root.context,
                object : GestureDetector.SimpleOnGestureListener() {
                    // 双击：触发点赞
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        toggleLike(video, position)
                        showHeartAtTouch(binding.root,e.x, e.y)//在手指位置显示爱心
                        return true
                    }

                    // 单击：视频播放/暂停（后续添加 ExoPlayer 逻辑）
                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        // 示例：触发播放/暂停回调（可暴露给 Fragment）
                        onVideoClick?.invoke(position)
                        return true
                    }
                })

            // 关键：将手势监听绑定到 itemView（之前遗漏，导致双击无效）
            binding.root.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true // 消费事件，避免冲突
            }
        }

        //唱片旋转
        private fun startRotation() {
            if (rotationAnimator == null) {
                // 假设你的 XML 里唱片 ID 是 ivVinyl
                // 如果没有唱片图，这几行可以先注释
                /*
                rotationAnimator = ObjectAnimator.ofFloat(binding.ivVinyl, "rotation", 0f, 360f).apply {
                    duration = 5000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                }
                */
            }
            if (rotationAnimator?.isRunning == false) {
                rotationAnimator?.start()
            }
        }

        // 停止所有动画（原有逻辑+Lottie）
        fun stopAnimation() {
            rotationAnimator?.cancel()
            binding.lavExplosion.cancelAnimation()
            binding.lavExplosion.visibility = View.GONE
        }
    }

    fun addVideoToTop(video: VideoModel) {
        // 1. 插入到第 0 个位置
        videoList.add(0, video)

        // 2. 通知 RecyclerView 第 0 个位置有新数据插入，触发动画
        notifyItemInserted(0)

        // 可选：如果有 header 或者为了保险，也可以刷新前几个
        // notifyItemRangeChanged(0, videoList.size)
    }


    // Adapter 生命周期方法（修正 onBindViewHolder 传 position）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun getItemCount(): Int = videoList.size

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position) // 传入 position，用于更新数据
    }
}