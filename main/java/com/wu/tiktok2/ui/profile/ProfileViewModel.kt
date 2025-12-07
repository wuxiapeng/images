package com.wu.tiktok2.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wu.tiktok2.data.model.VideoModel
import com.wu.tiktok2.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: VideoRepository
) : ViewModel(){
    private val _myVideos = MutableLiveData<List<VideoModel>>()
    val myVideos : LiveData<List<VideoModel>> = _myVideos

    init {
        loadMyVideos()
    }

    private fun loadMyVideos(){
        viewModelScope.launch {
            try {
                // 这里暂时复用首页的接口，模拟“我的作品”数据
                val list = repository.fetchVideos()
                _myVideos.value = list
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}