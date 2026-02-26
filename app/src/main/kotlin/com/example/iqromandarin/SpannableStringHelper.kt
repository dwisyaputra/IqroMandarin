package com.example.iqromandarin

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat

/**
 * Helper for coloring tone marks in Pinyin text.
 * Tone 1 = Red, Tone 2 = Green, Tone 3 = Blue, Tone 4 = Purple, Neutral = Gray
 */
object SpannableStringHelper {

    fun colorTones(pinyin: String, context: Context): SpannableString {
        val spannable = SpannableString(pinyin)

        val tone1 = ContextCompat.getColor(context, R.color.red_tone)
        val tone2 = ContextCompat.getColor(context, R.color.green_primary)
        val tone3 = ContextCompat.getColor(context, R.color.blue_tone)
        val tone4 = ContextCompat.getColor(context, R.color.purple_tone)

        pinyin.forEachIndexed { i, c ->
            val color = when (c) {
                'ā', 'ē', 'ī', 'ō', 'ū', 'Ā', 'Ē', 'Ī', 'Ō', 'Ū' -> tone1
                'á', 'é', 'í', 'ó', 'ú', 'ǘ' -> tone2
                'ǎ', 'ě', 'ǐ', 'ǒ', 'ǔ', 'ǚ' -> tone3
                'à', 'è', 'ì', 'ò', 'ù', 'ǜ' -> tone4
                else -> null
            }
            color?.let {
                spannable.setSpan(ForegroundColorSpan(it), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return spannable
    }
}
