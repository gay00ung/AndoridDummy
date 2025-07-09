package net.ifmain.androiddummy.biometric

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * AndroidDummy
 * Class : FingerprintAuthTheme.
 * Created by gayoung.
 * Created On 2025-07-09.
 * Description:
 */
@Composable
fun FingerprintAuthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}