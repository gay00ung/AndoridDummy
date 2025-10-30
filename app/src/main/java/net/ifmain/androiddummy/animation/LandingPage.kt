package net.ifmain.androiddummy.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import net.ifmain.androiddummy.R

/**
 * Landing Page Composable
 * 주요 효과 :
 * 1) 패럴랙스 헤더: 배경 이미지는 느리게 이동, 전경 텍스트는 고정
 * 2) 섹션 블록: 뷰포트 진입 시 페이드 인 및 슬라이드 인 애니메이션
 * 3) 상단 진행 바: 스크롤 비율에 따른 진행 표시
 *
 */
@Composable
fun LandingPage() {
    val headerHeight = 280.dp
    val scroll = rememberScrollState()

    val viewportHeightPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    Box(Modifier.fillMaxSize()) {
        // 1) 패럴랙스 헤더 (배경)
        ParallaxHeader(
            headerHeight = headerHeight,
            scrollY = scroll.value.toFloat()
        )

        // 2) 스크롤 가능한 컨텐츠
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .clipToBounds()
        ) {
            Spacer(Modifier.height(headerHeight))

            // 섹션들 — 각 섹션은 자신의 top(절대 위치)을 기록하고, 뷰포트 진입 시 애니메이션
            SectionBlock(
                title = "Fast Onboarding",
                body = "간결한 애니메이션으로 첫인상을 강화합니다.\n스크롤 진입 시 자연스러운 페이드/슬라이드로 시선을 유도합니다.",
                index = 0,
                viewportHeightPx = viewportHeightPx,
                scrollPx = { scroll.value.toFloat() }
            )
            SectionBlock(
                title = "Parallax Visuals",
                body = "헤더/배경은 느리게, 전경은 빠르게 — 깊이감과 몰입감을 주는 패럴랙스.",
                index = 1,
                viewportHeightPx = viewportHeightPx,
                scrollPx = { scroll.value.toFloat() }
            )
            SectionBlock(
                title = "Progressive Disclosure",
                body = "중요한 정보부터 순차적으로 등장시켜 사용자의 인지 부하를 줄입니다.",
                index = 2,
                viewportHeightPx = viewportHeightPx,
                scrollPx = { scroll.value.toFloat() }
            )
            SectionBlock(
                title = "Physics-based Motion",
                body = "스프링/감쇠 등을 활용해 손맛 나는 인터랙션을 구현합니다.",
                index = 3,
                viewportHeightPx = viewportHeightPx,
                scrollPx = { scroll.value.toFloat() }
            )
            SectionBlock(
                title = "냥냥",
                body = "냥!냥! 귀여운 고양이 사진으로 마무리합니다. 스크롤을 끝까지 내려주셔서 감사합니다!",
                index = 4,
                viewportHeightPx = viewportHeightPx,
                scrollPx = { scroll.value.toFloat() }
            )

            Footer()
        }

        // 3) 상단 진행 바 (스크롤 비율)
        val progress by remember {
            derivedStateOf {
                val max = scroll.maxValue.takeIf { it > 0 } ?: 1
                (scroll.value / max.toFloat()).coerceIn(0f, 1f)
            }
        }
        TopProgressBar(progress = progress)
    }
}

@Composable
fun ParallaxHeader(headerHeight: Dp, scrollY: Float) {
    val translation = scrollY * 0.5f

    Box(
        Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .zIndex(0f)
            .clipToBounds()
    ) {
        // 배경 이미지 (원하는 리소스로 교체)
        Image(
            painter = painterResource(id = R.drawable.kitty),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { translationY = translation },
        )

        // 헤더 타이틀(전경)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Landing Page Animations",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun SectionBlock(
    title: String,
    body: String,
    index: Int,
    viewportHeightPx: Float,
    scrollPx: () -> Float,
) {
    var topInParentPx by remember { mutableFloatStateOf(Float.POSITIVE_INFINITY) }
    val triggerLinePx = viewportHeightPx * 0.88f

    // 현재 스크롤 위치 + 트리거 라인이 섹션의 top을 지나면 보이도록
    val visible by remember {
        derivedStateOf {
            val threshold = scrollPx() + triggerLinePx
            threshold > topInParentPx
        }
    }

    val delay = index * 90

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            // 항상 레이아웃되어 위치를 측정
            .onGloballyPositioned { coords ->
                topInParentPx = coords.positionInParent().y
            }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(durationMillis = 520, delayMillis = delay)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 520, delayMillis = delay)
                ) { full -> full / 4 },
            exit = fadeOut(tween(280)) + slideOutVertically { full -> full / 6 }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C1E))
                    .padding(20.dp)
            ) {
                Text(
                    title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEEEEEE)
                )
                Spacer(Modifier.height(8.dp))
                Text(body, fontSize = 15.sp, color = Color(0xFFBDBDBD), lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun TopProgressBar(progress: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .zIndex(2f)
            .background(Color.Transparent)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Color(0xFF6CC6FF))
        )
    }
}

@Composable
fun Footer() {
    Spacer(Modifier.height(32.dp))
    Text(
        text = "Thanks for scrolling!",
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        color = Color(0xFF9E9E9E),
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(120.dp))
}
