package com.aichat.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtil {

    private const val MAX_SIZE = 1024
    private const val JPEG_QUALITY = 80

    /**
     * 将图片 URI 转换为 base64 data URI 字符串。
     * 自动压缩大图以控制请求体积。
     */
    fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("无法读取图片")

        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val bitmap = if (original.width > MAX_SIZE || original.height > MAX_SIZE) {
            val ratio = minOf(MAX_SIZE.toFloat() / original.width, MAX_SIZE.toFloat() / original.height)
            val newW = (original.width * ratio).toInt()
            val newH = (original.height * ratio).toInt()
            val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)
            if (scaled != original) original.recycle()
            scaled
        } else {
            original
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        val bytes = outputStream.toByteArray()
        outputStream.close()

        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }
}
