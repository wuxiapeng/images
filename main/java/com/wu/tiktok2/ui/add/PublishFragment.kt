package com.wu.tiktok2.ui.add

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.wu.tiktok2.R
import com.wu.tiktok2.databinding.FragmentPublishBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PublishFragment : Fragment(R.layout.fragment_publish) {

    private var binding: FragmentPublishBinding? = null
    private var videoUri: String? = null
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取传递过来的视频路径
        videoUri = arguments?.getString("video_uri")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPublishBinding.bind(view)

        initPlayer()

        // 返回按钮
        binding?.ivBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // 发布按钮
        binding?.btnPublish?.setOnClickListener {
            publishVideo()
        }
    }

    private fun initPlayer() {
        if (videoUri == null) return

        player = ExoPlayer.Builder(requireContext()).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUri))
            setMediaItem(mediaItem)
            prepare()
            repeatMode = Player.REPEAT_MODE_ONE // 循环播放预览
            playWhenReady = true
        }
        binding?.playerView?.player = player
    }

    private fun publishVideo() {
        val description = binding?.etDescription?.text.toString()

        // 将数据传递给 HomeFragment
        // 注意：这里我们获取 navigation_home 的 BackStackEntry，把数据存进去
        try {
            val homeEntry = findNavController().getBackStackEntry(R.id.navigation_home)
            val bundle = Bundle().apply {
                putString("new_video_uri", videoUri)
                putString("new_video_desc", description)
            }
            homeEntry.savedStateHandle.set("publish_result", bundle)

            Toast.makeText(requireContext(), "发布成功！", Toast.LENGTH_SHORT).show()

            // 直接弹出栈，回到首页
            findNavController().popBackStack(R.id.navigation_home, false)
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果导航栈里没有 home（异常情况），就只是退回
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
        binding = null
    }
}