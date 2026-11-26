package com.bridgeai.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bridgeai.android.BridgeAiApp
import com.bridgeai.android.ui.screens.*

sealed class Route(val path: String) {
    data object Home      : Route("home")
    data object Chat      : Route("chat")
    data object Config    : Route("config")
    data object Providers : Route("providers")
    data object Status    : Route("status")
    data object About     : Route("about")
}

@Composable
fun Navigation(app: BridgeAiApp) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Home.path) {
        composable(Route.Home.path)      { HomeScreen(onNavigate = { nav.navigate(it.path) }) }
        composable(Route.Chat.path)      { ChatScreen(app = app, onBack = { nav.popBackStack() }) }
        composable(Route.Config.path)    { ConfigScreen(app = app, onBack = { nav.popBackStack() }) }
        composable(Route.Providers.path) { ProvidersScreen(onBack = { nav.popBackStack() }) }
        composable(Route.Status.path)    { StatusScreen(app = app, onBack = { nav.popBackStack() }) }
        composable(Route.About.path)     { AboutScreen(onBack = { nav.popBackStack() }) }
    }
}
