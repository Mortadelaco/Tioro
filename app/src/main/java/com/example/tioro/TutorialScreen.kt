package com.example.tioro

import io.github.mortadelaco.tioro.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

data class TutorialPageData(
    val icon: ImageVector,
    val titleRes: Int,
    val bodyRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    navController: NavController,
    viewModel: InquiViewModel
) {
    val pages = listOf(
        TutorialPageData(
            icon = Icons.Default.Schedule,
            titleRes = R.string.tutorial_title_1,
            bodyRes = R.string.tutorial_body_1
        ),
        TutorialPageData(
            icon = Icons.Default.FlashOn,
            titleRes = R.string.tutorial_title_2,
            bodyRes = R.string.tutorial_body_2
        ),
        TutorialPageData(
            icon = Icons.Default.Whatshot,
            titleRes = R.string.tutorial_title_3,
            bodyRes = R.string.tutorial_body_3
        ),
        TutorialPageData(
            icon = Icons.Default.History,
            titleRes = R.string.tutorial_title_4,
            bodyRes = R.string.tutorial_body_4
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    val onFinishTutorial = {
        viewModel.saveTutorialCompleted(true)
        navController.navigate("home") {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = InquiDarkBackground,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    TextButton(onClick = { onFinishTutorial() }) {
                        Text(
                            text = stringResource(R.string.tutorial_skip),
                            color = InquiTextMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = InquiDarkBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageData = pages[page]
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = InquiSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(InquiButtonInner),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = pageData.icon,
                                contentDescription = null,
                                tint = InquiGold,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = stringResource(pageData.titleRes),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(pageData.bodyRes),
                            color = InquiTextMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Indicadores de página
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) InquiGold else Color(0xFF333333))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón inferior
            val isLastPage = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinishTutorial()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InquiGold)
            ) {
                Text(
                    text = if (isLastPage) stringResource(R.string.tutorial_start) else stringResource(R.string.tutorial_next),
                    color = InquiDarkBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
