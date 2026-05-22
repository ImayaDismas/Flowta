package com.flowgroup.flowta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flowgroup.flowta.ui.screen.HomePlaceholderScreen
import com.flowgroup.flowta.ui.screen.SplashRoute
import com.flowgroup.flowta.ui.screen.onboarding.AddBusinessScreen
import com.flowgroup.flowta.ui.screen.onboarding.GetStartedScreen
import com.flowgroup.flowta.ui.screen.onboarding.SetPinScreen
import com.flowgroup.flowta.ui.screen.onboarding.SetupCompleteScreen

@Composable
fun FlowtaNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Splash,
    ) {
        composable<Destination.Splash> {
            SplashRoute(
                onToOnboarding = {
                    navController.navigate(Destination.GetStarted) {
                        popUpTo(Destination.Splash) { inclusive = true }
                    }
                },
                onToHome = {
                    navController.navigate(Destination.Home) {
                        popUpTo(Destination.Splash) { inclusive = true }
                    }
                },
            )
        }

        composable<Destination.GetStarted> {
            GetStartedScreen(onContinue = { navController.navigate(Destination.AddBusiness) })
        }

        composable<Destination.AddBusiness> {
            AddBusinessScreen(onNext = { navController.navigate(Destination.SetPin) })
        }

        composable<Destination.SetPin> {
            SetPinScreen(onNext = {
                navController.navigate(Destination.SetupComplete) {
                    popUpTo(Destination.GetStarted) { inclusive = true }
                }
            })
        }

        composable<Destination.SetupComplete> {
            SetupCompleteScreen(onOpenApp = {
                navController.navigate(Destination.Home) {
                    popUpTo(Destination.SetupComplete) { inclusive = true }
                }
            })
        }

        composable<Destination.Home> {
            HomePlaceholderScreen()
        }
    }
}
