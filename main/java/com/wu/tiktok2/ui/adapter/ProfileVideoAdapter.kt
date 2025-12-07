package com.wu.tiktok2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wu.tiktok2.data.model.VideoModel
import com.wu.tiktok2.databinding.ItemProfileVideoGridBinding

class ProfileVideoAdapter(
    private var videoList : List<VideoModel>
) : RecyclerView.Adapter<ProfileVideoAdapter.ProfileViewHolder>(){

    var onItemClick : ((VideoModel, Int) -> Unit) ? = null //1.定义点击回调 (参数是 点击的视频, 点击的位置)

    inner class ProfileViewHolder(val binding: ItemProfileVideoGridBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(video : VideoModel){
            binding.tvPlayCount.text = "▷ ${video.likeCount}"
            // 2. 加载封面
            // Glide 非常强大，直接把视频 URL 传给它，它会自动截取第一帧作为封面！
            Glide.with(binding.root)
                .load(video.videoUrl)
                .centerCrop()
                .into(binding.ivCover)
        }
    }

    fun updateData(newList: List<VideoModel>) {
        videoList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileVideoGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProfileViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(videoList[position])
        //2.设置监听
        holder.itemView.setOnClickListener{
            onItemClick?.invoke(videoList[position],position)
        }
    }
}