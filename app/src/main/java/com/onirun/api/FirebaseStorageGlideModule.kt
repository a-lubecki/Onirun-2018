package com.onirun.api

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import com.onirun.BuildConfig
import java.io.InputStream


/**
 * Created by Aurelien Lubecki
 * on 29/04/2018.
 */
@GlideModule
class FirebaseStorageGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {

        if (BuildConfig.IS_LOGGER_ENABLED) {
            builder.setLogLevel(Log.VERBOSE)
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {

        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference::class.java, InputStream::class.java, FirebaseImageLoader.Factory())
    }

}