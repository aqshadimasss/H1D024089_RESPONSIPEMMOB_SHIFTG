package com.pemmob.ani_es.util

/**
 * Konstanta aplikasi Ani-es.
 * Base URL dipisahkan agar mudah ditukar jika dosen tidak memperbolehkan Tenrai.
 */
object AppConstants {
    // Base URL utama (Tenrai — skema kompatibel Jikan v4)
    const val BASE_URL_TENRAI = "https://api.tenrai.org/v1/"

    // Base URL fallback (Jikan v4 asli)
    const val BASE_URL_JIKAN = "https://api.jikan.moe/v4/"

    // Debounce pencarian: tunggu 500ms setelah pengguna berhenti mengetik
    const val DEBOUNCE_MS = 500L

    // Jumlah item per halaman API
    const val PAGE_SIZE = 24
}
