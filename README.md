# Ani-es Aplikasi Pencarian & Katalog Anime

> **Responsi Pemrograman Mobile**  
> **Tech Stack:** Kotlin · Jetpack Compose · Material Design 3 · MVVM · Retrofit 2 · Kotlin Coroutines & StateFlow · Coil

---

## 1. Ringkasan & Deskripsi Singkat

**Ani-es** adalah aplikasi Android berbasis **Jetpack Compose** yang mengambil data anime dari REST API secara dinamis. Pengguna dapat menjelajahi katalog anime terpopuler, mencari anime berdasarkan kata kunci dengan mekanisme *debounce*, memfilter hasil pencarian berdasarkan genre, serta membuka halaman detail anime untuk melihat sinopsis, rating, jumlah episode, dan informasi lengkap lainnya.

Aplikasi dirancang dengan arsitektur **MVVM (Model-View-ViewModel)** yang bersih dan menerapkan prinsip **Unidirectional Data Flow (UDF)** serta **State-Driven UI**.

### Fitur Utama
- **Katalog Anime Populer:** Menampilkan daftar anime terpopuler secara default saat aplikasi dibuka.
- **Pencarian Real-Time (Debounced):** Pencarian langsung ke API dengan debounce 500 ms dan pembatalan job lama secara otomatis saat pengguna masih mengetik.
- **Filter Genre Dinamis:** Kategori genre diambil langsung dari API dan ditampilkan dalam baris chip horizontal (`LazyRow`).
- **Halaman Detail Lengkap:** Menampilkan poster besar, judul, rating bintang, jumlah episode, status penayangan, tipe, tahun, daftar genre (chip), serta sinopsis lengkap yang dapat di-scroll.
- **Preserved State Navigation:** Navigasi antara Beranda dan Detail berbagi satu instance ViewModel sehingga kata kunci pencarian dan posisi scroll tidak hilang saat kembali dari halaman detail.
- **Error Handling & Offline Handling:** Tangguh menghadapi gangguan koneksi internet, rate limiting (HTTP 429), dan server error (5xx) dengan pesan berbahasa Indonesia yang ramah serta tombol **Coba Lagi**.
- **Dual-Source API Resilience:** Mendukung *automatic fallback* dari **Tenrai API** ke **Jikan API v4**.

---

## 2. Screenshot & GIF Aplikasi

| Beranda & Populer | Pencarian Judul | Filter Genre |
| :---: | :---: | :---: |
| ![Beranda](docs/screenshots/home.png) | ![Pencarian](docs/screenshots/search.png) | ![Filter Genre](docs/screenshots/genre.png) |

| Halaman Detail | State Loading | State Error & Coba Lagi |
| :---: | :---: | :---: |
| ![Detail](docs/screenshots/detail.png) | ![Loading](docs/screenshots/loading.png) | ![Error](docs/screenshots/error.png) |

> *Catatan Pengumpulan:* Simpan screenshot aplikasi Anda di dalam folder `docs/screenshots/` dengan nama file sesuai tabel di atas (atau ganti tautan dengan gambar screenshot yang Anda unggah).

---

## 3. Arsitektur & Struktur MVVM

Aplikasi ini menerapkan arsitektur **Model-View-ViewModel (MVVM)** dengan pemisahan tanggung jawab (*Separation of Concerns*) dan pola **Unidirectional Data Flow (UDF)**:

### Lapisan Arsitektur:
1. **View (UI Layer - Jetpack Compose):**
   - Bertanggung jawab murni untuk menampilkan antarmuka dan menangkap interaksi pengguna.
   - Tidak pernah memanggil API secara langsung.
   - Membaca data secara reaktif dari ViewModel melalui `StateFlow` menggunakan fungsi `collectAsState()`.
   - Mengirimkan aksi pengguna (seperti mengetik di search bar, memilih chip genre, atau menekan tombol Coba Lagi) ke ViewModel dalam bentuk event/lambda.
   - Mengadopsi pemisahan antara **Stateful Composable** (pengambil state) dan **Stateless Composable** (murni perender UI).

2. **ViewModel (AnimeViewModel):**
   - Berperan sebagai jembatan antara lapisan data dan tampilan, serta mengelola UI State dan siklus hidup data.
   - Menyimpan status internal menggunakan `MutableStateFlow` (bersifat private) dan mengeksposnya ke UI sebagai `StateFlow` read-only untuk menjaga enkapsulasi.
   - Menjalankan logika bisnis seperti penjadwalan *debounce* pencarian selama 500 ms, pembatalan request lama yang belum selesai, dan pemanggilan repository melalui `viewModelScope`.

3. **Repository (AnimeRepository):**
   - Bertindak sebagai *Single Source of Truth* bagi aplikasi.
   - Mengatur strategi pengambilan data, mengeksekusi mekanisme fallback otomatis dari Tenrai API ke Jikan API, dan menangani error teknis.
   - Memetakan respon API mentah menjadi tipe `Result<T>` dengan pesan kesalahan berbahasa Indonesia yang siap dikonsumsi oleh ViewModel.

4. **Model & Remote Data Source (Data Layer):**
   - Terdiri dari antarmuka Retrofit (`AnimeApiService`), factory singleton HTTP (`ApiClient`), serta data class model (`Anime`, `ApiResponses`).
   - Bertanggung jawab melakukan komunikasi HTTP ke REST API eksternal dan melakukan deserialisasi JSON ke objek model Kotlin secara aman (*null-safe*).

### Pembagian Struktur Package

```text
com.pemmob.ani_es
├── HomeActivity.kt             # Single Activity Launcher, menampung NavHost & shared ViewModel
│
├── data/
│   ├── model/                  # Data class & parsing JSON
│   │   ├── Anime.kt            # Model Anime, Genre, AnimeImages, serta null-safe computed properties
│   │   └── ApiResponses.kt     # Wrapper DTO (AnimeListResponse, AnimeDetailResponse, GenreListResponse)
│   │
│   ├── remote/                 # Layer jaringan Retrofit & OkHttp
│   │   ├── AnimeApiService.kt  # Interface Retrofit (@GET anime, @GET anime/{id}, @GET genres/anime)
│   │   └── ApiClient.kt        # Singleton Retrofit builder dengan connect/read timeout 20s (by lazy)
│   │
│   └── repository/             # Single Source of Truth
│       └── AnimeRepository.kt  # Mekanisme fallback Tenrai -> Jikan & pemetaan exception ke pesan ramah
│
├── ui/
│   ├── screen/                 # Halaman UI (Pemisahan Stateful & Stateless Composable)
│   │   ├── HomeScreen.kt       # Stateful HomeScreen + Stateless HomeContent + SearchBar + Grid
│   │   └── DetailScreen.kt     # Stateful DetailScreen + Stateless DetailContent + InfoBadges
│   │
│   ├── viewmodel/              # Pengelola status layar & logika bisnis
│   │   └── AnimeViewModel.kt   # Sealed interface State, StateFlow enkapsulasi, Coroutine debounce 500ms
│   │
│   └── theme/                  # Material Design 3 Design System
│       ├── Color.kt            # Palet warna kustom
│       ├── Theme.kt            # AniesTheme (Mendukung Dark Theme & Light Theme)
│       └── Type.kt             # Tipografi kustom
│
└── util/
    └── AppConstants.kt         # Konstanta Base URL Tenrai, Jikan, Debounce MS, dan Page Size
```

### Alur State Management (Unidirectional Data Flow)
1. **User Action:** Pengguna mengetik judul anime pada `OutlinedTextField` di `HomeScreen`.
2. **Event ke ViewModel:** Event memicu `viewModel.onQueryChange(text)`.
3. **Debounce Coroutine:** Coroutine membatalkan request pencarian lama yang belum selesai, lalu menunggu jeda 500 ms (`AppConstants.DEBOUNCE_MS`).
4. **Panggilan Repository:** Jika tidak ada ketikan baru selama 500 ms, `repository.searchAnime(query)` dipanggil.
5. **Pembaruan State:** ViewModel memperbarui `_searchState.value` menjadi `SearchUiState.Loading` lalu menjadi `SearchUiState.Success(data)` atau `SearchUiState.Error(message)`.
6. **Recomposition UI:** `HomeScreen` yang mengamati `searchState` via `collectAsState()` mendeteksi perubahan state dan melakukan **recomposition** secara otomatis.

---

## 4. Penjelasan Penggunaan API

Ani-es mengonsumsi data anime publik yang kompatibel dengan skema **Jikan REST API v4**.

### Konfigurasi Sumber Data & Fallback
- **Base URL Utama:** `https://api.tenrai.org/v1/` (Tenrai API — mirror berperforma tinggi dengan skema kompatibel Jikan v4).
- **Base URL Fallback:** `https://api.jikan.moe/v4/` (Jikan API v4 resmi).
- **Mekanisme Fallback Otomatis:**  
  Di dalam `AnimeRepository`, semua request dieksekusi melalui helper `executeWithFallback()`. Jika Tenrai API mengalami gangguan jaringan (`IOException`) atau server error (`HttpException 5xx`), aplikasi secara otomatis mengulang request ke Jikan API tanpa crash.

### Daftar Endpoint yang Digunakan

| Kebutuhan | HTTP Method & Path | Parameter Query | Fungsi |
| :--- | :--- | :--- | :--- |
| **Daftar / Cari Anime** | `GET anime` | `q` (string), `genres` (id genre), `page` (int), `limit` (int, default 24), `sfw=true`, `order_by` (misal: "members"), `sort` ("desc") | Mengambil daftar anime populer, mencari berdasarkan judul, dan memfilter berdasarkan genre. |
| **Detail Anime** | `GET anime/{id}` | `id` (MyAnimeList ID / `mal_id`) | Mengambil detail spesifik anime (sinopsis, status, episode, dll). |
| **Daftar Genre** | `GET genres/anime` | `filter=genres` | Mengambil daftar genre utama untuk ditampilkan pada filter chip. |

### Contoh Respons JSON (Ringkasan Skema Jikan v4)
```json
{
  "pagination": {
    "current_page": 1,
    "has_next_page": true
  },
  "data": [
    {
      "mal_id": 1,
      "title": "Cowboy Bebop",
      "type": "TV",
      "episodes": 26,
      "status": "Finished Airing",
      "score": 8.75,
      "synopsis": "Di tahun 2071...",
      "year": 1998,
      "images": {
        "jpg": {
          "image_url": "https://cdn.myanimelist.net/images/anime/4/19644.jpg",
          "large_image_url": "https://cdn.myanimelist.net/images/anime/4/19644l.jpg"
        }
      },
      "genres": [
        { "mal_id": 1, "name": "Action" },
        { "mal_id": 24, "name": "Sci-Fi" }
      ]
    }
  ]
}
```

### Pemetaan Error & Ketahanan Jaringan
Untuk memastikan pengalaman pengguna yang ramah, exception teknis dipetakan ke bahasa Indonesia:

| Kondisi Teknis | Pesan yang Ditampilkan ke Pengguna |
| :--- | :--- |
| `IOException` (Offline / Timeout) | *"Tidak ada koneksi internet. Periksa jaringanmu."* |
| `HTTP 404` (Not Found) | *"Data anime tidak ditemukan."* |
| `HTTP 429` (Rate Limited) | *"Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi."* |
| `HTTP 5xx` (Server Error) | *"Server sedang bermasalah. Coba lagi nanti."* |

---

## 5. Cara Menjalankan Proyek

### Prasyarat:
1. **Android Studio:** Ladybug, Koala, atau Jellyfish (disarankan versi terbaru).
2. **JDK:** Versi 17 atau yang lebih baru.
3. **Android SDK:**
   - `compileSdk`: 36
   - `minSdk`: 24 (Mendukung Android 7.0 Nougat ke atas)
   - `targetSdk`: 36

### Langkah Instalasi:
1. Clone repositori ini:
   ```bash
   git clone <URL_REPOSITORY_ANDA>
   ```
2. Buka Android Studio, pilih **Open** lalu arahkan ke folder proyek.
3. Tunggu hingga proses **Gradle Sync** selesai.
4. Hubungkan perangkat fisik via USB Debugging atau jalankan **Android Virtual Device (AVD)** emulator.
5. Tekan tombol **Run 'app'** (`Shift + F10`) di Android Studio.

---

## 6. Atribusi Sumber Data
- Data anime dan metadata disediakan oleh **[MyAnimeList](https://myanimelist.net/)**.
- REST API disediakan oleh **[Tenrai API](https://api.tenrai.org/)** dan **[Jikan API](https://jikan.moe/)**.

## 7. DOKUMENTASI


https://github.com/user-attachments/assets/085feeb6-fccd-49d7-90ec-f6afeb1e2d04



https://github.com/user-attachments/assets/77e7f6d7-362c-442c-8ec0-b3c014e6a5bf

<img width="720" height="1640" alt="Screenshot_20261008_000418" src="https://github.com/user-attachments/assets/635e13ac-256f-452c-94d5-7ca992eef0cb" />
<img width="720" height="1640" alt="Screenshot_20261008_000439" src="https://github.com/user-attachments/assets/39079276-9aec-4d31-8a79-e57c1f8d102b" />



