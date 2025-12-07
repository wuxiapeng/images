package com.wu.tiktok2.ui.home

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
class HomeViewModel @Inject constructor(
    private val repository: VideoRepository
) : ViewModel() {

    private val _videos = MutableLiveData<List<VideoModel>>()
    val videoList: LiveData<List<VideoModel>> = _videos

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val list = repository.fetchVideos()
                _videos.value = list
            } catch (e: Exception) {
                e.printStackTrace() // 处理错误，比如没网
            }
        }
    }
}