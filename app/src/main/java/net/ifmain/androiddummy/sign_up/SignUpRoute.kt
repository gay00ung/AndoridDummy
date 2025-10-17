package net.ifmain.androiddummy.sign_up

sealed class SignUpRoute(val route: String) {
    data object Name: SignUpRoute("name?prefill={prefill}")
    data object UserId: SignUpRoute("userid")
    data object Password: SignUpRoute("password")
    data object Done: SignUpRoute("done")

    companion object {
        const val NAME_BASE = "name"
    }
}
