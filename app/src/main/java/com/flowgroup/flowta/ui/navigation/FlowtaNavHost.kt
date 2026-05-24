package com.flowgroup.flowta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flowgroup.flowta.ui.screen.SplashRoute
import com.flowgroup.flowta.ui.screen.export.ExportScreen
import com.flowgroup.flowta.ui.screen.paywall.PaywallScreen
import com.flowgroup.flowta.ui.screen.deni.AddClientScreen
import com.flowgroup.flowta.ui.screen.deni.ClientDeniDetailScreen
import com.flowgroup.flowta.ui.screen.deni.DeniListScreen
import com.flowgroup.flowta.ui.screen.home.HomeScreen
import com.flowgroup.flowta.ui.screen.reconciliation.ImportStatementScreen
import com.flowgroup.flowta.ui.screen.reconciliation.MatchReviewScreen
import com.flowgroup.flowta.ui.screen.reconciliation.PasteSmsScreen
import com.flowgroup.flowta.ui.screen.reconciliation.ReconciliationHubScreen
import com.flowgroup.flowta.ui.screen.reconciliation.ScanInboxScreen
import com.flowgroup.flowta.ui.screen.reconciliation.ScanReceiptScreen
import com.flowgroup.flowta.ui.screen.onboarding.AddBusinessScreen
import com.flowgroup.flowta.ui.screen.onboarding.GetStartedScreen
import com.flowgroup.flowta.ui.screen.onboarding.SetPinScreen
import com.flowgroup.flowta.ui.screen.onboarding.SetupCompleteScreen
import com.flowgroup.flowta.ui.screen.transaction.EditTransactionScreen
import com.flowgroup.flowta.ui.screen.transaction.RecordTransactionScreen
import com.flowgroup.flowta.ui.screen.transaction.TransactionDetailScreen
import com.flowgroup.flowta.ui.screen.unlock.PinUnlockScreen
import com.flowgroup.flowta.ui.screen.wallet.AddWalletScreen
import com.flowgroup.flowta.ui.screen.wallet.EditWalletScreen
import com.flowgroup.flowta.ui.screen.wallet.WalletDetailScreen

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
                onToSetPin = {
                    navController.navigate(Destination.SetPin) {
                        popUpTo(Destination.Splash) { inclusive = true }
                    }
                },
                onToUnlock = {
                    navController.navigate(Destination.PinUnlock) {
                        popUpTo(Destination.Splash) { inclusive = true }
                    }
                },
                onToPaywall = {
                    navController.navigate(Destination.Paywall) {
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

        composable<Destination.PinUnlock> {
            PinUnlockScreen(onUnlocked = {
                navController.navigate(Destination.Home) {
                    popUpTo(Destination.PinUnlock) { inclusive = true }
                }
            })
        }

        composable<Destination.Home> {
            HomeScreen(
                onAddWallet = { navController.navigate(Destination.AddWallet) },
                onRecordSale = { navController.navigate(Destination.RecordTransaction) },
                onRecordExpense = { navController.navigate(Destination.RecordTransaction) },
                onRecordTransaction = { navController.navigate(Destination.RecordTransaction) },
                onOpenWallet = { walletId ->
                    navController.navigate(Destination.WalletDetail(walletId))
                },
                onOpenTransaction = { transactionId ->
                    navController.navigate(Destination.TransactionDetail(transactionId))
                },
                onOpenDeni = { navController.navigate(Destination.DeniList) },
                onOpenReconciliation = { navController.navigate(Destination.Reconciliation) },
                onOpenExport = { navController.navigate(Destination.Export) },
                onOpenPaywall = { navController.navigate(Destination.Paywall) },
            )
        }

        composable<Destination.AddWallet> {
            AddWalletScreen(
                onClose = { navController.popBackStack() },
                onCreated = { navController.popBackStack() },
            )
        }

        composable<Destination.WalletDetail> {
            WalletDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { walletId ->
                    navController.navigate(Destination.EditWallet(walletId))
                },
                onRecordTransaction = { navController.navigate(Destination.RecordTransaction) },
                onViewAllHistory = { navController.popBackStack() },
                onOpenTransaction = { transactionId ->
                    navController.navigate(Destination.TransactionDetail(transactionId))
                },
            )
        }

        composable<Destination.EditWallet> {
            EditWalletScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onDeleted = {
                    navController.popBackStack(Destination.Home, inclusive = false)
                },
            )
        }

        composable<Destination.RecordTransaction> {
            RecordTransactionScreen(
                onClose = { navController.popBackStack() },
                onRecorded = { navController.popBackStack() },
                onAddWallet = {
                    navController.navigate(Destination.AddWallet) {
                        popUpTo(Destination.RecordTransaction) { inclusive = true }
                    }
                },
            )
        }

        composable<Destination.TransactionDetail> {
            TransactionDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { transactionId ->
                    navController.navigate(Destination.EditTransaction(transactionId))
                },
            )
        }

        composable<Destination.EditTransaction> {
            EditTransactionScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable<Destination.DeniList> {
            DeniListScreen(
                onBack = { navController.popBackStack() },
                onAddClient = { navController.navigate(Destination.AddClient) },
                onOpenClient = { clientId ->
                    navController.navigate(Destination.ClientDeniDetail(clientId))
                },
            )
        }

        composable<Destination.AddClient> {
            AddClientScreen(
                onClose = { navController.popBackStack() },
                onCreated = { navController.popBackStack() },
            )
        }

        composable<Destination.ClientDeniDetail> {
            ClientDeniDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Destination.Reconciliation> {
            ReconciliationHubScreen(
                onBack = { navController.popBackStack() },
                onPaste = { navController.navigate(Destination.PasteSms) },
                onScan = { navController.navigate(Destination.ScanReceipt) },
                onImport = { navController.navigate(Destination.ImportStatement) },
                onInbox = { navController.navigate(Destination.ScanInbox) },
                onOpenPayment = { paymentId ->
                    navController.navigate(Destination.MatchReview(paymentId))
                },
            )
        }

        composable<Destination.PasteSms> {
            PasteSmsScreen(
                onBack = { navController.popBackStack() },
                onParsed = { navController.popBackStack() },
            )
        }

        composable<Destination.MatchReview> {
            MatchReviewScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }

        composable<Destination.ScanReceipt> {
            ScanReceiptScreen(
                onBack = { navController.popBackStack() },
                onParsed = { navController.popBackStack() },
            )
        }

        composable<Destination.ImportStatement> {
            ImportStatementScreen(
                onBack = { navController.popBackStack() },
                onImported = { navController.popBackStack() },
            )
        }

        composable<Destination.ScanInbox> {
            ScanInboxScreen(onBack = { navController.popBackStack() })
        }

        composable<Destination.Export> {
            ExportScreen(onBack = { navController.popBackStack() })
        }

        composable<Destination.Paywall> {
            PaywallScreen(
                onActivated = {
                    navController.navigate(Destination.Splash) {
                        popUpTo(Destination.Paywall) { inclusive = true }
                    }
                },
            )
        }
    }
}
