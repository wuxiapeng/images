package com.wu.tiktok2.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wu.tiktok2.R
import com.wu.tiktok2.data.model.VideoModel
import com.wu.tiktok2.databinding.FragmentVideoDetailBinding
import com.wu.tiktok2.ui.adapter.VideoAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoDetailFragment : Fragment(R.layout.fragment_video_detail) {

    private var binding: FragmentVideoDetailBinding? = null

    // 接收的数据
    private var startPosition = 0
    private var videoList = ArrayList<VideoModel>()

    // 播放器实例
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            startPosition = it.getInt("position", 0)
            it.getParcelableArrayList<VideoModel>("list")?.let { list ->
                videoList = list
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVideoDetailBinding.bind(view)

        // 1. 隐藏底部导航栏
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE

        initViewPager()

        binding?.ivBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initViewPager() {
        val adapter = VideoAdapter(
            videoList,
            onVideoClick = { position-> togglePlayPause(position) })
        binding?.viewPagerDetail?.adapter = adapter

        // 关闭预加载，确保只播放当前的
        binding?.viewPagerDetail?.offscreenPageLimit = 1

        // 跳转到指定位置 (false = 无滚动动画，直接闪现)
        binding?.viewPagerDetail?.setCurrentItem(startPosition, false)

        // 关键：因为 setCurrentItem 是异步的，我们需要用 post 确保布局完成后才开始播放
        binding?.viewPagerDetail?.post {
            playVideoAt(startPosition)
        }

        // 监听页面切换
        binding?.viewPagerDetail?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 滑动切换时播放
                playVideoAt(position)
            }
        })
    }

    private fun togglePlayPause(position : Int){
        if(player?.isPlaying == true){
            player?.pause() //暂停
            showPlayIcon(position, true) // 确认显示暂停图标
        }
        else{
            player?.play()
            showPlayIcon(position, false) // 隐藏暂停图标
        }
    }
    private fun showPlayIcon(position : Int,show : Boolean){
        val recyclerView = binding?.viewPagerDetail?.getChildAt(0) as? RecyclerView
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position) as? VideoAdapter.VideoViewHolder
        viewHolder?.binding?.ivPlayStatus ?.visibility = if (show) View.VISIBLE else View.GONE

        // 进阶：可以加上缩放动画让图标出现得更自然
    }

    /**
     * 核心播放逻辑 (复用自 HomeFragment)
     */
    private fun playVideoAt(position: Int) {
        // 1. 初始化 Player (单例模式，如果为空才创建)
        if (player == null) {
            player = ExoPlayer.Builder(requireContext()).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE // 循环播放
                playWhenReady = true // 准备好就播
            }
        }

        // 2. 找到 ViewPager2 当前显示的 ViewHolder
        // ViewPager2 内部其实是一个 RecyclerView
        val recyclerView = binding?.viewPagerDetail?.getChildAt(0) as? RecyclerView
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)

        // 3. 如果 ViewHolder 是我们定义的类型，开始绑定
        if (viewHolder is VideoAdapter.VideoViewHolder) {
            val playerView = viewHolder.binding.playerView

            // 绑定 Player 到 View
            playerView.player = player

            // 4. 设置媒体源
            val item = videoList.getOrNull(position)
            if (item != null) {
                val mediaItem = MediaItem.fromUri(item.videoUrl)

                // 只有当播放地址变化时才重新加载 (防止闪烁)
                // 这里简单处理：每次都重新加载，确保状态正确
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.play()
            }
        }
    }

    // --- 生命周期管理 ---

    override fun onPause() {
        super.onPause()
        // 页面不可见时暂停
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        // 页面回来时继续播放
        player?.play()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 释放播放器资源
        player?.release()
        player = null

        // 恢复底部导航栏显示
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        binding = null
    }
}