package com.example.solify.presentation.components.skeleton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.solify.R
import com.example.solify.presentation.screens.lesson.components.LessonHeader
import com.example.solify.presentation.screens.test.components.TestProgressBar

private val cardShape = RoundedCornerShape(0.dp)
private val sheetTopShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
private val trainingCardShape = RoundedCornerShape(20.dp)

@Composable
fun LessonsListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        LessonsHeaderSkeleton()
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(sheetTopShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Spacer(Modifier.height(33.dp))
            SectionTitleSkeleton()
            Spacer(Modifier.height(8.dp))
            repeat(4) {
                LessonCardSkeleton()
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun TrainingsListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        LessonsHeaderSkeleton()
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(sheetTopShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Spacer(Modifier.height(28.dp))
            SectionTitleSkeleton()
            Spacer(Modifier.height(20.dp))
            repeat(2) {
                TrainingCardSkeleton()
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun LessonDetailSkeleton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LessonHeader(
            title = "",
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SkeletonBox(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth()
                    .height(20.dp),
                shape = RoundedCornerShape(6.dp),
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
        }
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(sheetTopShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Spacer(Modifier.height(34.dp))
            repeat(3) {
                LessonRowCardSkeleton()
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkeletonBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp),
                    shape = RoundedCornerShape(6.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            repeat(2) {
                LessonRowCardSkeleton()
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(29.dp))
        }
    }
}

@Composable
fun TrainersListSkeleton(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LessonDetailSkeleton(onBackClick = onBackClick, modifier = modifier)
}

@Composable
fun ProfileScreenSkeleton(modifier: Modifier = Modifier) {
    val avatarSize = 94
    Column(modifier = modifier.fillMaxSize().padding(top = 16.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 8.dp)
                    .width(18.dp)
                    .height(18.dp),
                shape = RoundedCornerShape(4.dp),
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                SkeletonCircle(
                    size = avatarSize.dp,
                    surface = SkeletonSurface.ON_PRIMARY_CONTAINER
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-(avatarSize / 2)).dp)
                .clip(sheetTopShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            SkeletonBox(
                modifier = Modifier
                    .height(32.dp)
                    .width(32.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height((avatarSize / 2 + 18).dp))
                SkeletonBox(
                    modifier = Modifier
                        .width(180.dp)
                        .height(22.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(16.dp))
                SkeletonBox(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(24.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(12.dp),
                shape = RoundedCornerShape(2.dp),
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(90.dp)
                    .height(14.dp),
                shape = RoundedCornerShape(6.dp),
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(48.dp)
                    .height(18.dp),
                shape = RoundedCornerShape(6.dp),
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
        }
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(sheetTopShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonCircle(size = 94.dp)
            Spacer(Modifier.height(16.dp))
            SkeletonBox(
                modifier = Modifier.width(130.dp).height(18.dp),
                shape = RoundedCornerShape(6.dp)
            )
            Spacer(Modifier.height(32.dp))
            repeat(3) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    surface = SkeletonSurface.ON_CARD
                )
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(24.dp))
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.End)
                    .width(140.dp)
                    .height(16.dp),
                shape = RoundedCornerShape(6.dp)
            )
        }
    }
}

@Composable
fun QuizScreenSkeleton(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    showShuffleSlot: Boolean = false
) {
    Column(modifier = modifier.fillMaxSize()) {
        QuizHeaderSkeleton(onCloseClick = onCloseClick, showHint = showShuffleSlot)
        if (showShuffleSlot) {
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkeletonBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    surface = SkeletonSurface.ON_CARD
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = if (showShuffleSlot) 24.dp else 40.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(22.dp),
                shape = RoundedCornerShape(8.dp),
                surface = SkeletonSurface.ON_CARD
            )
            Spacer(Modifier.height(22.dp))
            SkeletonBox(
                modifier = Modifier
                    .width(280.dp)
                    .height(160.dp),
                shape = RoundedCornerShape(10.dp),
                surface = SkeletonSurface.ON_CARD
            )
            Spacer(Modifier.height(40.dp))
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                repeat(4) {
                    QuizAnswerSkeleton()
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(274.dp)
                    .height(51.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun TheoryScreenSkeleton(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp)
                    .size(12.dp),
                shape = RoundedCornerShape(2.dp),
                surface = SkeletonSurface.ON_CARD
            )
            SkeletonBox(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp)
                    .fillMaxWidth()
                    .height(20.dp),
                shape = RoundedCornerShape(6.dp),
                surface = SkeletonSurface.ON_CARD
            )
        }
        Spacer(Modifier.height(27.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    shape = RoundedCornerShape(4.dp),
                    surface = SkeletonSurface.ON_CARD
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkeletonBox(
                    modifier = Modifier
                        .width(308.dp)
                        .height(180.dp),
                    shape = RoundedCornerShape(20.dp),
                    surface = SkeletonSurface.ON_CARD
                )
            }
            TheoryAudioBlockSkeleton()
        }
    }
}

@Composable
private fun LessonsHeaderSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonCircle(
                size = 48.dp,
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
            Spacer(Modifier.width(19.dp))
            SkeletonBox(
                modifier = Modifier
                    .width(110.dp)
                    .height(18.dp),
                shape = RoundedCornerShape(6.dp),
                surface = SkeletonSurface.ON_PRIMARY_CONTAINER
            )
        }
        SkeletonBox(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            surface = SkeletonSurface.ON_PRIMARY_CONTAINER
        )
    }
}

@Composable
private fun SectionTitleSkeleton() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SkeletonBox(
            modifier = Modifier
                .width(140.dp)
                .height(20.dp),
            shape = RoundedCornerShape(6.dp)
        )
    }
}

@Composable
private fun LessonCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(cardShape)
            .background(com.example.solify.presentation.ui.theme.Grey100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(start = 12.dp, end = 25.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(size = 50.dp, surface = SkeletonSurface.ON_CARD)
            Spacer(Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(16.dp),
                    shape = RoundedCornerShape(4.dp),
                    surface = SkeletonSurface.ON_CARD
                )
                Spacer(Modifier.height(4.dp))
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                    surface = SkeletonSurface.ON_CARD
                )
            }
            Spacer(Modifier.width(8.dp))
            SkeletonCircle(size = 40.dp, surface = SkeletonSurface.ON_CARD)
        }
    }
}

@Composable
private fun LessonRowCardSkeleton() = LessonCardSkeleton()

@Composable
private fun TrainingCardSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(200.dp)
            .clip(trainingCardShape)
            .background(com.example.solify.presentation.ui.theme.Grey100)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(131.dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                surface = SkeletonSurface.ON_CARD
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 19.dp, end = 16.dp, top = 12.dp, bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(18.dp),
                        shape = RoundedCornerShape(4.dp),
                        surface = SkeletonSurface.ON_CARD
                    )
                    Spacer(Modifier.height(5.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp),
                        surface = SkeletonSurface.ON_CARD
                    )
                    Spacer(Modifier.height(4.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp),
                        shape = RoundedCornerShape(4.dp),
                        surface = SkeletonSurface.ON_CARD
                    )
                }
                Spacer(Modifier.width(8.dp))
                SkeletonCircle(size = 40.dp, surface = SkeletonSurface.ON_CARD)
            }
        }
    }
}

@Composable
private fun QuizHeaderSkeleton(
    onCloseClick: () -> Unit,
    showHint: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = if (showHint) Arrangement.SpaceBetween else Arrangement.Start
        ) {
            Icon(
                painter = painterResource(R.drawable.cross),
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onCloseClick),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            if (showHint) {
                SkeletonBox(
                    modifier = Modifier
                        .width(19.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(4.dp),
                    surface = SkeletonSurface.ON_CARD
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(200.dp)
                    .height(26.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(11.dp))
            TestProgressBar(progress = 0f)
        }
    }
}

@Composable
private fun QuizAnswerSkeleton() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        SkeletonBox(
            modifier = Modifier
                .widthIn(min = 221.dp, max = 280.dp)
                .height(38.dp),
            shape = RoundedCornerShape(15.dp),
            surface = SkeletonSurface.ON_CARD
        )
    }
}

@Composable
private fun TheoryAudioBlockSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(com.example.solify.presentation.ui.theme.White300)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonCircle(size = 40.dp, surface = SkeletonSurface.ON_CARD)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .width(72.dp)
                    .height(16.dp),
                shape = RoundedCornerShape(4.dp),
                surface = SkeletonSurface.ON_CARD
            )
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                surface = SkeletonSurface.ON_CARD
            )
        }
        SkeletonBox(
            modifier = Modifier
                .width(17.dp)
                .height(17.dp),
            shape = RoundedCornerShape(4.dp),
            surface = SkeletonSurface.ON_CARD
        )
    }
}
