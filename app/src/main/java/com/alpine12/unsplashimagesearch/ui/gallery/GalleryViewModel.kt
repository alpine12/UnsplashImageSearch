package com.alpine12.unsplashimagesearch.ui.gallery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.alpine12.unsplashimagesearch.data.UnsplashRepository

class GalleryViewModel @ViewModelInject constructor(private val repository: UnsplashRepository) :
    ViewModel() {
}