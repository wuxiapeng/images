package com.wu.tiktok2.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize // 1. 添加注解
data class VideoModel(
    val videoId: String,
    val videoUrl: String,      // 视频链接
    val title: String,         // 视频标题/文案
    val description: String,   // 描述
    val audioName: String,     // 底部滚动的音乐名
    var isLiked : Boolean = false,  //是否点赞
    var likeCount: Int = 0,      // 点赞数
    var commentCount: Int = 0,    // 评论数
    var recordCount : Int = 0, //收藏数

    val user : User ? = null

): Parcelable // 2. 实现 Parcelable 接口