package com.pemmob.ani_es.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pemmob.ani_es.data.model.Anime
import com.pemmob.ani_es.data.model.Genre
import com.pemmob.ani_es.ui.theme.AniesTheme
import com.pemmob.ani_es.ui.theme.StarGold
import com.pemmob.ani_es.ui.viewmodel.AnimeViewModel
import com.pemmob.ani_es.ui.viewmodel.SearchUiState

/**
 * Composable Stateful:
 * Menghubungkan ViewModel dengan UI, mengumpulkan StateFlow,
 * dan meneruskan event pengguna ke ViewModel.
 */
@Composable
fun HomeScreen(
    viewModel: AnimeViewModel,
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchState by viewModel.searchState.collectAsState()
    val query by viewModel.query.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()

    HomeContent(
        searchState = searchState,
        query = query,
        genres = genres,
        selectedGenreId = selectedGenreId,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onGenreSelected = viewModel::onGenreSelected,
        onRetry = viewModel::retrySearch,
        onAnimeClick = onAnimeClick,
        modifier = modifier
    )
}

/**
 * Composable Stateless:
 * Murni menerima data dan callback event, memudahkan pengujian dan Preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    searchState: SearchUiState,
    query: String,
    genres: List<Genre>,
    selectedGenreId: Int?,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onGenreSelected: (Int?) -> Unit,
    onRetry: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ani-es",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBarSection(
                query = query,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            GenreFilterSection(
                genres = genres,
                selectedGenreId = selectedGenreId,
                onGenreSelected = onGenreSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (searchState) {
                    is SearchUiState.Loading -> {
                        LoadingStateView(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is SearchUiState.Empty -> {
                        EmptyStateView(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is SearchUiState.Error -> {
                        ErrorStateView(
                            message = searchState.message,
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is SearchUiState.Success -> {
                        AnimeGridView(
                            animes = searchState.animes,
                            onAnimeClick = onAnimeClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Komponen Input Pencarian Judul Anime dengan tombol pembersih (Clear).
 */
@Composable
fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Cari judul anime...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ikon Cari",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus teks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Baris Horizontal Filter Genre (chip "Semua" + genre dari API).
 */
@Composable
fun GenreFilterSection(
    genres: List<Genre>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val isAllSelected = selectedGenreId == null
            FilterChip(
                selected = isAllSelected,
                onClick = { onGenreSelected(null) },
                label = { Text("Semua") },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        items(
            items = genres,
            key = { it.malId }
        ) { genre ->
            val isSelected = selectedGenreId == genre.malId
            FilterChip(
                selected = isSelected,
                onClick = { onGenreSelected(genre.malId) },
                label = { Text(genre.name) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Tampilan Grid 2 Kolom untuk menampilkan daftar kartu anime.
 */
@Composable
fun AnimeGridView(
    animes: List<Anime>,
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = animes,
            key = { it.malId }
        ) { anime ->
            AnimeCard(
                anime = anime,
                onClick = { onAnimeClick(anime.malId) }
            )
        }
    }
}

/**
 * Kartu per item anime: Poster (2:3), Judul, Badge Tipe, dan Rating bintang.
 */
@Composable
fun AnimeCard(
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = anime.posterUrl,
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = anime.displayType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = StarGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = anime.displayScore,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Tampilan status Loading.
 */
@Composable
fun LoadingStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Mencari anime...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tampilan status hasil kosong (Empty).
 */
@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = "Tidak ditemukan",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Anime tidak ditemukan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Coba cari dengan kata kunci lain atau pilih genre berbeda.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tampilan status Error dengan tombol Coba Lagi.
 */
@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gagal Memuat Data",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Coba Lagi")
        }
    }
}

private val dummyAnimes = listOf(
    Anime(
        malId = 1,
        title = "Cowboy Bebop",
        type = "TV",
        score = 8.75,
        episodes = 26
    ),
    Anime(
        malId = 20,
        title = "Naruto",
        type = "TV",
        score = 8.03,
        episodes = 220
    )
)

private val dummyGenres = listOf(
    Genre(malId = 1, name = "Action"),
    Genre(malId = 2, name = "Adventure"),
    Genre(malId = 10, name = "Fantasy")
)

@Preview(name = "Home - Success Light", showBackground = true)
@Composable
fun HomeSuccessPreviewLight() {
    AniesTheme(darkTheme = false) {
        HomeContent(
            searchState = SearchUiState.Success(dummyAnimes),
            query = "",
            genres = dummyGenres,
            selectedGenreId = null,
            onQueryChange = {},
            onClearQuery = {},
            onGenreSelected = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}

@Preview(name = "Home - Success Dark", showBackground = true)
@Composable
fun HomeSuccessPreviewDark() {
    AniesTheme(darkTheme = true) {
        HomeContent(
            searchState = SearchUiState.Success(dummyAnimes),
            query = "",
            genres = dummyGenres,
            selectedGenreId = null,
            onQueryChange = {},
            onClearQuery = {},
            onGenreSelected = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}

@Preview(name = "Home - Loading", showBackground = true)
@Composable
fun HomeLoadingPreview() {
    AniesTheme {
        HomeContent(
            searchState = SearchUiState.Loading,
            query = "naruto",
            genres = dummyGenres,
            selectedGenreId = null,
            onQueryChange = {},
            onClearQuery = {},
            onGenreSelected = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}

@Preview(name = "Home - Empty", showBackground = true)
@Composable
fun HomeEmptyPreview() {
    AniesTheme {
        HomeContent(
            searchState = SearchUiState.Empty,
            query = "zzzxxqq",
            genres = dummyGenres,
            selectedGenreId = null,
            onQueryChange = {},
            onClearQuery = {},
            onGenreSelected = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}

@Preview(name = "Home - Error", showBackground = true)
@Composable
fun HomeErrorPreview() {
    AniesTheme {
        HomeContent(
            searchState = SearchUiState.Error("Tidak ada koneksi internet. Periksa jaringanmu."),
            query = "",
            genres = dummyGenres,
            selectedGenreId = null,
            onQueryChange = {},
            onClearQuery = {},
            onGenreSelected = {},
            onRetry = {},
            onAnimeClick = {}
        )
    }
}
