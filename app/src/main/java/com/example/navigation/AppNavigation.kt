package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.DigitalSignatureScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageEditorScreen
import com.example.ui.screens.PdfProcessingScreen
import com.example.ui.screens.PdfSettingsScreen
import com.example.ui.screens.PdfSuccessScreen
import com.example.ui.screens.RecentPdfsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SelectedImagesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TextToPdfScreen
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: AppViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToScanner = {
                    navController.navigate(Screen.Scanner.route)
                },
                onNavigateToGallery = {
                    navController.navigate(Screen.SelectedImages.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToRecentPdfs = {
                    navController.navigate(Screen.RecentPdfs.route)
                },
                onNavigateToTextToPdf = {
                    navController.navigate(Screen.TextToPdf.route)
                }
            )
        }

        // 3. Scanner Screen
        composable(Screen.Scanner.route) {
            ScannerScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToSelectedImages = {
                    navController.navigate(Screen.SelectedImages.route)
                }
            )
        }

        // 4. Selected Images Screen
        composable(Screen.SelectedImages.route) {
            SelectedImagesScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onAddMoreClick = {
                    navController.navigate(Screen.Scanner.route)
                },
                onEditImageClick = { _ ->
                    navController.navigate(Screen.ImageEditor.route)
                },
                onNavigateToPdfSettings = {
                    navController.navigate(Screen.PdfSettings.route)
                }
            )
        }

        // 5. Image Editor Screen
        composable(Screen.ImageEditor.route) {
            ImageEditorScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToPdfSettings = {
                    navController.navigate(Screen.PdfSettings.route)
                }
            )
        }

        // 6. PDF Settings Screen
        composable(Screen.PdfSettings.route) {
            PdfSettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToProcessing = {
                    navController.navigate(Screen.PdfProcessing.route)
                }
            )
        }

        // 7. PDF Processing Screen
        composable(Screen.PdfProcessing.route) {
            PdfProcessingScreen(
                viewModel = viewModel,
                onProcessingFinished = {
                    navController.navigate(Screen.PdfSuccess.route) {
                        popUpTo(Screen.PdfSettings.route) { inclusive = true }
                    }
                },
                onBackToSettings = {
                    navController.popBackStack()
                }
            )
        }

        // 8. PDF Success Screen
        composable(Screen.PdfSuccess.route) {
            PdfSuccessScreen(
                viewModel = viewModel,
                onCreateAnother = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        // 9. Recent PDFs Screen
        composable(Screen.RecentPdfs.route) {
            RecentPdfsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToCreate = {
                    navController.navigate(Screen.Scanner.route)
                }
            )
        }

        // 10. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToSignature = {
                    navController.navigate(Screen.DigitalSignature.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        // 11. Digital Signature Screen
        composable(Screen.DigitalSignature.route) {
            DigitalSignatureScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 12. About Screen
        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // 13. Text to PDF Screen
        composable(Screen.TextToPdf.route) {
            TextToPdfScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onPdfCreatedSuccess = {
                    navController.navigate(Screen.PdfSuccess.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
    }
}
