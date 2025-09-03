package net.ifmain.androiddummy.produceState_test

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 *
 * @author gayoung.
 * @since 2025. 9. 3.
 *
 * 비교 목적: A(mutableStateOf + LaunchedEffect 2개) vs B(produceState + LaunchedEffect 1개)
 * - B가 더 간결한 이유: 값 생산을 produceState 한 덩어리로 선언(키 기반 재시작 포함).
 * - 두 모드 모두 동작은 동일하도록 키/초기값/증가 로직을 맞춤.
 */
enum class AutoScrollMode { MutableCombo, Produce }

@Composable
fun AutoScroller(
    items: List<Any>,
    isAutoScrolling: Boolean,
    listState: LazyListState,
    mode: AutoScrollMode = AutoScrollMode.Produce, // 기본값: 간결한 Produce 모드
    intervalMs: Long = 200L
) {
    val startIndex = listState.firstVisibleItemIndex

    /** =========================
     * A) mutableStateOf + LaunchedEffect 2개
     * =========================
     * - 별도의 상태(targetIndexA) + 값 생산 루프 + 스크롤 사이드이펙트, 총 3요소 필요.
     */
    var targetIndexA by remember(
        isAutoScrolling,
        items.size,
        mode
    ) { mutableIntStateOf(startIndex) }

    /** 값 생산 루프 (키 변경 시 재시작) */
    LaunchedEffect(isAutoScrolling, items.size, mode) {
        if (mode != AutoScrollMode.MutableCombo) return@LaunchedEffect
        if (!isAutoScrolling || items.isEmpty()) return@LaunchedEffect
        while (isActive) {
            delay(intervalMs)
            targetIndexA = if (targetIndexA < items.lastIndex) targetIndexA + 1 else 0
        }
    }

    /** 사이드이펙트 (값 변화에 반응해 스크롤) */
    LaunchedEffect(targetIndexA, isAutoScrolling, mode) {
        if (mode != AutoScrollMode.MutableCombo) return@LaunchedEffect
        if (isAutoScrolling && items.isNotEmpty()) {
            listState.animateScrollToItem(targetIndexA)
        }
    }

    /** =========================
     * B) produceState + LaunchedEffect 1개
     * =========================
     * - 별도의 remember 상태 불필요.
     * - 값 생산과 키 기반 재시작을 한 블록으로 표현 → 코드가 짧고 의도가 명확.
     */
    val targetIndexB by produceState(
        initialValue = startIndex,
        key1 = isAutoScrolling,
        key2 = items.size,
        key3 = mode
    ) {
        if (mode != AutoScrollMode.Produce) return@produceState
        if (!isAutoScrolling || items.isEmpty()) return@produceState
        while (isActive) {
            delay(intervalMs)
            value = if (value < items.lastIndex) value + 1 else 0
        }
    }

    /** 사이드이펙트 (값 변화에 반응해 스크롤) */
    LaunchedEffect(targetIndexB, isAutoScrolling, mode) {
        if (mode != AutoScrollMode.Produce) return@LaunchedEffect
        if (isAutoScrolling && items.isNotEmpty()) {
            listState.animateScrollToItem(targetIndexB)
        }
    }
}

/**
요약
- A: 상태 remember + 값 생산 LaunchedEffect + 소비 LaunchedEffect → 구성요소 3개.
- B: 값 생산을 produceState 한 블록으로 대체 + 소비 LaunchedEffect → 구성요소 2개(더 간결).
- 둘 다 key(isAutoScrolling, items.size, mode), 초기값(startIndex), 증가 로직 동일 → 체감 동작 동일.
- 외부 콜백/리스너를 어댑트할 땐 B에서 awaitDispose { ... }로 정리까지 한곳에 모을 수 있어 유지보수 유리.
 */