package net.ifmain.androiddummy.age

import android.graphics.*
import java.io.*

interface AgeGenderEngine : Closeable {
    suspend fun classify(bitmap: Bitmap): AgeGenderResult
}
