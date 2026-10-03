package com.example.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Scanner : Screen("scanner")
    object SelectedImages : Screen("selected_images")
    object ImageEditor : Screen("image_editor")
    object PdfSettings : Screen("pdf_settings")
    object PdfProcessing : Screen("pdf_processing")
    object PdfSuccess : Screen("pdf_success")
    object RecentPdfs : Screen("recent_pdfs")
    object Settings : Screen("settings")
    object DigitalSignature : Screen("digital_signature")
    object About : Screen("about")
    object TextToPdf : Screen("text_to_pdf")
}
