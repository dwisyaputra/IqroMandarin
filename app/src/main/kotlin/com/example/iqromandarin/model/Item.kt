package com.example.iqromandarin.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents one learnable item (pinyin, hanzi, kata, kalimat, etc.)
 */
@Parcelize
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Pinyin dengan tone marks (misal: "bā", "nǐ hǎo")
    val pinyin: String,

    // Hanzi characters (nullable — not used in early jilid)
    val hanzi: String? = null,

    // Indonesian-friendly pronunciation approximation
    // contoh: "pa (seperti 'papa' pelan)"
    val indoPron: String = "",

    // Indonesian translation/meaning
    val arti: String = "",

    // Category (INITIAL, FINAL, HANZI, KATA, KALIMAT, CERITA, CUSTOM)
    val kategori: String = "INITIAL",

    // Contoh penggunaan
    val contoh: String? = null,

    // Reference to halaman (page) this item belongs to
    val halamanId: Int = 1,

    // Reference to jilid this item belongs to
    val jilidId: Int = 1,

    // Raw audio resource name (tanpa R.raw. prefix, misal "audio_ba")
    // null = use TTS
    val audioResName: String? = null,

    // Stroke order data for Hanzi (JSON string, optional)
    val strokeOrder: String? = null,

    // User added item
    val isCustom: Boolean = false,

    // Mastery state
    val isKuasai: Boolean = false,

    // SRS Leitner box (1=hari ini, 2=3 hari, 3=7 hari, 4=14 hari, 5=lama)
    val srsBox: Int = 0,

    // Next review date (timestamp millis)
    val nextReviewAt: Long = 0L,

    // How many times reviewed
    val reviewCount: Int = 0,

    // How many times got wrong
    val wrongCount: Int = 0
) : Parcelable
