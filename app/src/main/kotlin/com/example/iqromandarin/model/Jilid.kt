package com.example.iqromandarin.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jilid (Book/Level) — 12 levels like Iqro
 */
@Entity(tableName = "jilid")
data class Jilid(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // 1 through 12
    val nomorJilid: Int,

    // Name in Indonesian (e.g. "Konsonan Awal (Initials)")
    val nama: String,

    // Chinese title
    val namaCina: String = "",

    // Description for display
    val deskripsi: String = "",

    // Color accent for UI (hex string like "#4CAF50")
    val warna: String = "#4CAF50",

    // Is this jilid unlocked for the user?
    val isUnlocked: Boolean = false,

    // Is this jilid fully completed?
    val isSelesai: Boolean = false,

    // Total halaman in this jilid
    val totalHalaman: Int = 1,

    // Icon emoji for the jilid
    val icon: String = "📖"
)

/**
 * Halaman (Page) within a Jilid — each page has 5-10 items
 */
@Entity(tableName = "halaman")
data class Halaman(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val jilidId: Int,

    // Page number within jilid (1-based)
    val nomorHalaman: Int,

    // Page title
    val judul: String = "",

    // Is this page unlocked?
    val isUnlocked: Boolean = false,

    // Is this page completed (Lancar)?
    val isSelesai: Boolean = false
)

/**
 * Progres tracks overall user progress
 */
@Entity(tableName = "progres")
data class Progres(
    @PrimaryKey
    val id: Int = 1, // Singleton row

    val jilidAktif: Int = 1,
    val halamanAktif: Int = 1,
    val totalItemDikuasai: Int = 0,
    val totalHalamanSelesai: Int = 0,
    val streakHari: Int = 0,
    val lastStudyDate: Long = 0L
)
