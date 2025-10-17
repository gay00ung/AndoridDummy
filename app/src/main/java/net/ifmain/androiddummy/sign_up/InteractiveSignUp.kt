package net.ifmain.androiddummy.sign_up

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.*
import net.ifmain.androiddummy.sign_up.components.*

@Composable
fun InteractiveSignUp() {
    val vm: SignUpViewModel = viewModel()

    // 순차로 쌓이는 데이터(논리 순서): 이름 → 아이디 → 비번
    val steps =
        remember { mutableStateListOf<Step>() } // Step.Name, Step.Username, Step.Password ...
    val listState = rememberLazyListState()

    // 시작은 이름부터
    LaunchedEffect(Unit) { if (steps.isEmpty()) steps += Step.Name }

    Scaffold(bottomBar = {
        Row(Modifier.fillMaxWidth().padding(bottom = 50.dp), horizontalArrangement = Arrangement.End) {
            Button(onClick = {
                // 규칙: 입력 포맷이 유효하면 다음 step 추가
                val next = when (steps.lastOrNull()) {
                    Step.Name -> Step.UserId
                    Step.UserId -> Step.Password
                    Step.Password -> Step.Done
                    Step.Done, null -> null
                }
                next?.let { steps += it }
            }) { Text(if (steps.contains(Step.Password)) "완료" else "다음") }
        }
    }) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            // 역순 레이아웃: 새 아이템이 "위에" 나타남
            reverseLayout = true,
            state = listState
        ) {
            items(
                items = steps, // 데이터는 [이름, 아이디, 비번] 순차로 쌓이지만,
                key = { it.key } // 각 step 고유 키
            ) { step ->
                Spacer(Modifier.height(12.dp))
                when (step) {
                    Step.Name ->
                        LabeledField(
                            label = "이름",
                            value = vm.state.collectAsState().value.name,
                            onValueChange = vm::setName
                        )

                    Step.UserId -> LabeledField(
                        label = "아이디",
                        value = vm.state.collectAsState().value.userId,
                        onValueChange = vm::setUserId
                    )

                    Step.Password ->
                        LabeledField(
                            label = "비밀번호",
                            value = vm.state.collectAsState().value.password,
                            onValueChange = vm::setPassword,
                            isPassword = true
                        )

                    Step.Done ->
                        DoneBanner()
                }
            }
        }
    }
}

private enum class Step { Name, UserId, Password, Done }

private val Step.key get() = name // 간단 키

@Composable
private fun DoneBanner() {
    Text("🎉 가입이 완료되었습니다.")
}
