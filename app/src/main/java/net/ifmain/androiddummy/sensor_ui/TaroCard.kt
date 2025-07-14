package net.ifmain.androiddummy.sensor_ui

data class TaroCard(
    val id: Int,
    val isFlipped: Boolean = false,
    val flipProgress: Float = 0f, // 0f = 앞면, 1f = 뒤면
    val frontImage: String,
    val backPattern: String = "🔮"
)

data class CardDeckState(
    val cards: List<TaroCard>,
    val currentTopCardIndex: Int = 0,
    val flipSpeed: Float = 0f
)
