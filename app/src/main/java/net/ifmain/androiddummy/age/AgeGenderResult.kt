package net.ifmain.androiddummy.age

import android.graphics.*

data class AgeGenderResult(
    val faceBitmap: Bitmap,
    val age: Int,
    val gender: String,
    val ageRaw: Float,
    val genderScores: FloatArray,
    val ageInferenceMs: Long,
    val genderInferenceMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AgeGenderResult

        if (age != other.age) return false
        if (ageRaw != other.ageRaw) return false
        if (ageInferenceMs != other.ageInferenceMs) return false
        if (genderInferenceMs != other.genderInferenceMs) return false
        if (faceBitmap != other.faceBitmap) return false
        if (gender != other.gender) return false
        if (!genderScores.contentEquals(other.genderScores)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = age
        result = 31 * result + ageRaw.hashCode()
        result = 31 * result + ageInferenceMs.hashCode()
        result = 31 * result + genderInferenceMs.hashCode()
        result = 31 * result + faceBitmap.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + genderScores.contentHashCode()
        return result
    }
}
