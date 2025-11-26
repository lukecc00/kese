package com.example.module.videoview.ai.chat

import android.graphics.Bitmap

data class ChatMessageUi(
    val fromUser: Boolean,
    val text: String? = null,
    val image: Bitmap? = null,
    val pending: Boolean = false,
)

