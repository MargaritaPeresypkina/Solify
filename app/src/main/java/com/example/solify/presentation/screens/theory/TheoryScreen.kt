package com.example.solify.presentation.screens.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.solify.presentation.screens.theory.components.TheoryAudioBlock
import com.example.solify.presentation.screens.theory.components.TheoryHeader
import com.example.solify.presentation.screens.theory.components.TheoryImageBlock
import com.example.solify.presentation.screens.theory.components.TheoryTextBlock

@Composable
fun TheoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TheoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.onNavigateAway() }
    }

    Scaffold { innerPadding ->
        MaterialTheme.colorScheme.background
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TheoryHeader(
                    title = uiState.title,
                    onCloseClick = onNavigateBack
                )
                Spacer(Modifier.height(27.dp))
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding()),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = uiState.contentItems,
                                key = { index, item -> "${item.type}_${item.payload}_$index" }
                            ) { _, item ->
                                when (item.type) {
                                    TheoryContentType.TEXT -> TheoryTextBlock(text = item.payload)
                                    TheoryContentType.IMAGE -> TheoryImageBlock(imageUrl = item.payload)
                                    TheoryContentType.AUDIO -> TheoryAudioBlock(
                                        audioUrl = item.payload,
                                        audioState = uiState.audioState,
                                        onPlayToggle = { viewModel.onAudioToggle(item.payload) },
                                        onMuteToggle = viewModel::onAudioMuteToggle
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
