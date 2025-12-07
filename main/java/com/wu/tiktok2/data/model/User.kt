package com.wu.tiktok2.data.model

import java.io.Serializable

data class User(
    val uId : String,
    val nickName : String,
    val avatarUrl : String,
    val bio : String = "",
    val isFollowing : Boolean = false
) : Serializable //实现对象序列化的接口 是为了以后如果在 Activity/Fragment 之间传参方便