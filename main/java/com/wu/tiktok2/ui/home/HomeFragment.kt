package com.wu.tiktok2.ui.home

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.wu.tiktok2.R
import com.wu.tiktok2.data.model.User
import com.wu.tiktok2.data.model.VideoModel
import com.wu.tiktok2.databinding.FragmentHomeBinding
import com.wu.tiktok2.ui.adapter.VideoAdapter
import com.wu.tiktok2.ui.add.PublishFragment
import com.wu.tiktok2.utils.VideoCache
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: VideoAdapter
    private var player: ExoPlayer? = null
    private var currentListener: Player.Listener? = null //定义一个变量来持有当前的监听器，方便移除

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setupViewPager()
        observeData()
        PublishFragmentListener()
    }

    private fun setupViewPager() {
        adapter = VideoAdapter(
            mutableListOf(),
            onVideoClick = { position -> togglePlayPause(position) })
        binding.viewPager.adapter = adapter
        // 预加载，保证滑动流畅
        binding.viewPager.offscreenPageLimit = 1


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 滑动停止，播放
                playVideoAt(position)
            }
        })
    }

    private fun togglePlayPause(position: Int) {
        if (player?.isPlaying == true) {
            player?.pause() //暂停
            showPlayIcon(position, true) // 确认显示暂停图标
        } else {
            player?.play()
            showPlayIcon(position, false) // 隐藏暂停图标
        }
    }

    private fun showPlayIcon(position: Int, show: Boolean) {
        val recyclerView = binding.viewPager.getChildAt(0) as? RecyclerView
        val viewHolder =
            recyclerView?.findViewHolderForAdapterPosition(position) as? VideoAdapter.VideoViewHolder
        viewHolder?.binding?.ivPlayStatus?.visibility = if (show) View.VISIBLE else View.GONE

        // 进阶：可以加上缩放动画让图标出现得更自然
    }

    private fun observeData() {
        viewModel.videoList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                adapter.updateData(list)
                // 数据加载完，手动播第一个
                binding.viewPager.post { playVideoAt(0) }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun playVideoAt(position: Int) {
        // 1. 初始化 Player
        if (player == null) {
            val context = requireContext()
            //创建上游数据源工厂 (负责真正的网络请求);DefaultDataSource 既能读网络(Http)，也能读本地文件(File/Asset)
            val upstreamFactory = DefaultDataSource.Factory(context)
            // 2. 创建缓存数据源工厂 (负责先查缓存，没缓存再叫上游去下载)
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(VideoCache.getInstance(context)) // 传入刚才写的单例
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR) // 读缓存出错时忽略，直接走网络
            // 3. 将缓存工厂塞给 ExoPlayer
            player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory))
                .build()
            player = ExoPlayer.Builder(requireContext()).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE // 循环
                playWhenReady = true // 自动播
            }
        }
        // 2. 找到对应的 View
        // 这里的逻辑稍微 tricky：ViewPager2 内部是 RecyclerView
        val recyclerView = binding.viewPager.getChildAt(0) as? RecyclerView
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)

        if (viewHolder is VideoAdapter.VideoViewHolder) {
            val playerView = viewHolder.binding.playerView
            val ivCover = viewHolder.binding.ivCover
            val btnFullScreen = viewHolder.binding.btnFullScreen

            // 3. 绑定 View
            playerView.player = player
            showPlayIcon(position, false) //重置暂停图标状态为消失
            // 4. 绑定数据
            val item = adapter.getItem(position)
            if (item != null) {
                val mediaItem = MediaItem.fromUri(item.videoUrl)

                currentListener?.let { player?.removeListener(it) } //移除上一次的监听器，否则堆积
                currentListener = object : Player.Listener{ //创建新的监听器
                    override fun onRenderedFirstFrame(){//当视频第一帧渲染出来时：隐藏封面
                        ivCover.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction {
                                ivCover.visibility = View.GONE
                                ivCover.alpha = 1f // 重置透明度，供下次复用
                            }
                            .start()
                    }
                    // 处理错误，比如播放失败时把封面显示回来
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        super.onPlayerError(error)
                        ivCover.visibility = View.VISIBLE
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                        if(videoSize.width > videoSize.height){
                            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT //resizeMode改为fit
                            btnFullScreen.visibility = View.VISIBLE //全屏按钮可见
                            ivCover.scaleType = ImageView.ScaleType.FIT_CENTER //跳转封面尺寸
                        }
                        else{
                            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            btnFullScreen.visibility = View.GONE
                            ivCover.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    }
                }

                btnFullScreen.setOnClickListener{
                    val activity = requireActivity()
                    // 判断当前屏幕方向
                    if (activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        // 如果已经是横屏，切回竖屏
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        viewHolder.binding.btnFullScreenText.text = "全屏观看"
                        viewHolder.binding.btnFullScreenImage.setImageResource(R.drawable.ic_fullscreen)
                    } else {
                        // 如果是竖屏，切成横屏
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        viewHolder.binding.btnFullScreenText.text = "退出全屏"
                        viewHolder.binding.btnFullScreen.alpha = 0.5f
                        viewHolder.binding.btnFullScreenImage.setImageResource(R.drawable.ic_fullscreen_exit)
                    }
                }

                player?.addListener(currentListener!!) //添加监听器给 Player

                // 如果是同一个视频就不重置了，防止闪烁（可选优化）
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.play()
            }
        }
    }

    fun PublishFragmentListener() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("publish_result")
            ?.observe(viewLifecycleOwner) { bundle ->
                if (bundle != null) {
                    val uri = bundle.getString("new_video_uri")
                    val desc = bundle.getString("new_video_desc") ?: "无标题"

                    if (uri != null) {
                        addNewVideoToList(uri, desc)
                        // 清除数据避免重复添加
                        findNavController().currentBackStackEntry?.savedStateHandle?.remove<Bundle>(
                            "publish_result"
                        )
                    }
                }
            }
    }

    private fun addNewVideoToList(uri: String, desc: String) {
        val newVideo = VideoModel(
            videoId = System.currentTimeMillis().toString(),
            title = "Android User", // 或者用用户名
            description = desc, // 使用输入的标题
            videoUrl = uri,
            likeCount = 0,
            audioName = "Original Sound",
            isLiked = false,
            user = User(
                "9527",
                "Android User",
                "https://ui-avatars.com/api/?name=User&background=random",
                "专注分享日常 | 喜欢用镜头记录生活",
                false
            )
        )
        // 调用 Adapter 添加数据
        (binding.viewPager.adapter as? VideoAdapter)?.addVideoToTop(newVideo)
        binding.viewPager.setCurrentItem(0, true)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        val isLandscape = newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        // 1. 控制底部导航栏显示/隐藏
        val navView = requireActivity().findViewById<View>(R.id.nav_view)
        navView.visibility = if (isLandscape) View.GONE else View.VISIBLE

        // 2. 控制当前 Item 的 UI 元素 (找到当前 ViewHolder)
        val recyclerView = binding.viewPager.getChildAt(0) as? RecyclerView
        val layoutManager = recyclerView?.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
        val position = layoutManager?.findFirstVisibleItemPosition() ?: 0
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position) as? VideoAdapter.VideoViewHolder

        viewHolder?.let { holder ->
            // 如果是横屏，隐藏右侧互动栏和底部文字；竖屏则显示
            val visibility = if (isLandscape) View.GONE else View.VISIBLE

             holder.binding.rightInteractionLayout.visibility = visibility //隐藏右栏
             holder.binding.bottomTextLayout.visibility = visibility //隐藏文字底栏

            // 按钮本身也可以在横屏时隐藏，或者变成“退出全屏”
            holder.binding.btnFullScreenText.text = "退出全屏"
        }

        // 3. 调整 ViewPager 禁止滑动 (横屏时通常不让上下滑)
        binding.viewPager.isUserInputEnabled = !isLandscape
        android.util.Log.d("TestRotation", "旋转触发了！当前方向: ${newConfig.orientation}")
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }

}