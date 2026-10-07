package com.pemmob.ani_es.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pemmob.ani_es.data.model.Anime
import com.pemmob.ani_es.data.model.Genre
import com.pemmob.ani_es.data.repository.AnimeRepository
import com.pemmob.ani_es.util.AppConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Representasi status UI untuk halaman pencarian/beranda (Home).
 */
sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val animes: List<Anime>) : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}

/**
 * Representasi status UI untuk halaman detail anime.
 */
sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val anime: Anime) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

/**
 * ViewModel pengelola state dan logika bisnis aplikasi Ani-es.
 * Menggunakan Unidirectional Data Flow (UDF):
 * Event dari Composable -> ViewModel memproses logika -> Memperbarui StateFlow -> Composable recompose.
 */
class AnimeViewModel(
    private val repository: AnimeRepository = AnimeRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    private var currentDetailId: Int? = null
    private var searchJob: Job? = null

    init {
        loadGenres()
        loadInitialAnimes()
    }

    /**
     * Mengambil daftar genre anime dari API untuk ditampilkan pada baris chip filter.
     */
    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().onSuccess { genreList ->
                _genres.value = genreList
            }
        }
    }

    /**
     * Memuat daftar anime populer saat pertama kali aplikasi dibuka (FR-7).
     */
    private fun loadInitialAnimes() {
        executeSearch(query = "", genreId = null)
    }

    /**
     * Dipanggil setiap kali pengguna mengetik di search bar.
     * Menerapkan debounce 500 ms dan membatalkan job lama sebelum memulai yang baru.
     */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(AppConstants.DEBOUNCE_MS)
            executeSearch(query = newQuery, genreId = _selectedGenreId.value)
        }
    }

    /**
     * Dipanggil saat chip genre diklik.
     * Jika memilih genre yang sama, filter dibatalkan (null).
     * Filter genre dijalankan langsung tanpa debounce.
     */
    fun onGenreSelected(genreId: Int?) {
        val newGenreId = if (_selectedGenreId.value == genreId) null else genreId
        _selectedGenreId.value = newGenreId

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch(query = _query.value, genreId = newGenreId)
        }
    }

    /**
     * Menjalankan ulang pencarian beranda dengan query dan filter genre saat ini (tombol Coba Lagi).
     */
    fun retrySearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch(query = _query.value, genreId = _selectedGenreId.value)
        }
    }

    /**
     * Membersihkan teks search bar dan memuat ulang data sesuai genre yang aktif.
     */
    fun clearQuery() {
        onQueryChange("")
    }

    /**
     * Logika internal eksekusi pencarian ke Repository dan pengubahan SearchUiState.
     */
    private fun executeSearch(query: String, genreId: Int?) {
        _searchState.value = SearchUiState.Loading

        viewModelScope.launch {
            val result = repository.searchAnime(
                query = query,
                genreId = genreId
            )

            result.onSuccess { animeList ->
                _searchState.value = if (animeList.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    SearchUiState.Success(animeList)
                }
            }.onFailure { throwable ->
                _searchState.value = SearchUiState.Error(
                    message = throwable.message ?: "Terjadi kesalahan saat memuat anime."
                )
            }
        }
    }

    /**
     * Memuat detail anime berdasarkan mal_id.
     * State selalu direset ke Loading agar konten anime sebelumnya tidak sempat berkedip.
     */
    fun loadDetail(malId: Int) {
        currentDetailId = malId
        _detailState.value = DetailUiState.Loading

        viewModelScope.launch {
            val result = repository.getAnimeDetail(malId)
            result.onSuccess { anime ->
                _detailState.value = DetailUiState.Success(anime)
            }.onFailure { throwable ->
                _detailState.value = DetailUiState.Error(
                    message = throwable.message ?: "Gagal memuat detail anime."
                )
            }
        }
    }

    /**
     * Menjalankan ulang pemuatan detail anime jika terjadi error (tombol Coba Lagi di Detail).
     */
    fun retryDetail() {
        currentDetailId?.let { loadDetail(it) }
    }
}
