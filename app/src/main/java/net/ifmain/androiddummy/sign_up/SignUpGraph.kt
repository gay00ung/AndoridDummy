package net.ifmain.androiddummy.sign_up

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.*
import androidx.navigation.*
import androidx.navigation.compose.*
import net.ifmain.androiddummy.sign_up.components.*

@Composable
fun SignUpGraph(
    onFinishAll: () -> Unit = {},
) {
    val nav = rememberNavController()
    val vm: SignUpViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = "${SignUpRoute.NAME_BASE}?prefill=${vm.state.collectAsState().value.name}",
    ) {
        composable(
            route = SignUpRoute.Name.route,
            arguments = listOf(
                navArgument("prefill") { nullable = true; defaultValue = null }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "myapp://signup/name?prefill={prefill}" }
            )
        ) { backStackEntry ->
            val prefill = backStackEntry.arguments?.getString("prefill")

            LaunchedEffect(prefill) {
                if (!prefill.isNullOrBlank() && vm.state.value.name.isBlank()) {
                    vm.setName(prefill)
                }
            }

            StepScaffold(
                title = "이름 입력",
                content = {
                    LabeledField(
                        label = "이름",
                        value = vm.state.collectAsState().value.name,
                        onValueChange = vm::setName
                    )
                },
                leftBtn = null,
                rightBtnLabel = "다음",
                onRight = {
                    if (vm.state.value.name.isNotBlank()) {
                        nav.navigate(SignUpRoute.UserId.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // UserId
        composable(SignUpRoute.UserId.route) {
            StepScaffold(
                title = "아이디 입력",
                content = {
                    LabeledField(
                        label = "아이디",
                        value = vm.state.collectAsState().value.userId,
                        onValueChange = vm::setUserId
                    )
                },
                leftBtn = "이전" to { nav.popBackStack() },
                rightBtnLabel = "다음",
                onRight = {
                    if (vm.state.value.userId.isNotBlank()) {
                        nav.navigate(SignUpRoute.Password.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // PASSWORD
        composable(SignUpRoute.Password.route) {
            StepScaffold(
                title = "비밀번호 입력",
                content = {
                    LabeledField(
                        label = "비밀번호",
                        value = vm.state.collectAsState().value.password,
                        onValueChange = vm::setPassword,
                        isPassword = true
                    )
                },
                leftBtn = "이전" to { nav.popBackStack() },
                rightBtnLabel = "완료",
                onRight = {
                    nav.navigate(SignUpRoute.Done.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // DONE
        composable(SignUpRoute.Done.route) {
            StepScaffold(
                title = "가입 완료",
                content = {
                    Text("🎉 가입이 완료되었습니다.")
                },
                leftBtn = "뒤로" to { nav.popBackStack() },
                rightBtnLabel = "홈으로",
                onRight = {
                    vm.clear()
                    onFinishAll()
                }
            )
        }
    }
}
