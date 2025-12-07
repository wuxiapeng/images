package com.wu.tiktok2.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.wu.tiktok2.R
import com.wu.tiktok2.databinding.FragmentProfileBinding
import com.wu.tiktok2.ui.adapter.ProfileVideoAdapter
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile){
    private val viewModel: ProfileViewModel by viewModels()
    private var binding: FragmentProfileBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        initView()
        initData()
    }

    private fun initView() {
        // 1. 设置头像 (这里也可以用 Glide 加载网络头像)
        binding?.ivAvatar?.let { imageView ->
            Glide.with(this)
                .load("https://ui-avatars.com/api/?name=User&background=random")
                .circleCrop()
                .into(imageView)
        }

        // 2. 初始化 RecyclerView (网格布局，3列)
        val adapter = ProfileVideoAdapter(emptyList())
        binding?.rvProfileVideo?.layoutManager = GridLayoutManager(context, 3)
        binding?.rvProfileVideo?.adapter = adapter
    }

    private fun initData() {
        viewModel.myVideos.observe(viewLifecycleOwner) { list ->
            val adapter = binding?.rvProfileVideo?.adapter as? ProfileVideoAdapter
            adapter?.updateData(list)

            // 设置点击事件
            adapter?.onItemClick = { video, position ->
                // 准备数据
                val bundle = Bundle().apply {
                    putInt("position", position)
                    // ArrayList 实现了 Serializable/Parcelable，可以直接传
                    putParcelableArrayList("list", ArrayList(list))
                }

                // 执行跳转
                findNavController()
                    .navigate(R.id.action_profile_to_detail, bundle)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null // 防止内存泄漏
    }

}