package com.alpine12.unsplashimagesearch.data

import com.alpine12.unsplashimagesearch.api.UnsplashApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnsplashRepository @Inject constructor(private val unsplashApi: UnsplashApi) {



}