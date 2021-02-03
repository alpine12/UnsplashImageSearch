package com.alpine12.unsplashimagesearch.api

import com.alpine12.unsplashimagesearch.data.UnsplashPhoto

data class UnsplashResponse(
    val reuslts: List<UnsplashPhoto>
)