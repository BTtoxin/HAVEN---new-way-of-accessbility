package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
val spaceGroteskFont = GoogleFont("Space Grotesk")
val SpaceGroteskFontFamily = FontFamily(
    Font(googleFont = spaceGroteskFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = spaceGroteskFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = spaceGroteskFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = spaceGroteskFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = spaceGroteskFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontSize=32.sp, fontWeight=FontWeight.ExtraBold, fontFamily=SpaceGroteskFontFamily, letterSpacing=(-0.5).sp, lineHeight=38.sp),
    displayMedium = TextStyle(fontSize=28.sp, fontWeight=FontWeight.ExtraBold, fontFamily=SpaceGroteskFontFamily, letterSpacing=(-0.3).sp, lineHeight=34.sp),
    titleLarge = TextStyle(fontSize=22.sp, fontWeight=FontWeight.ExtraBold, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.sp, lineHeight=28.sp),
    titleMedium = TextStyle(fontSize=18.sp, fontWeight=FontWeight.Bold, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.sp, lineHeight=24.sp),
    bodyLarge = TextStyle(fontSize=16.sp, fontWeight=FontWeight.SemiBold, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.sp, lineHeight=22.sp),
    bodyMedium = TextStyle(fontSize=14.sp, fontWeight=FontWeight.Normal, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.sp, lineHeight=20.sp),
    bodySmall = TextStyle(fontSize=12.sp, fontWeight=FontWeight.Normal, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.sp, lineHeight=16.sp),
    labelLarge = TextStyle(fontSize=14.sp, fontWeight=FontWeight.Bold, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.2.sp, lineHeight=18.sp),
    labelMedium = TextStyle(fontSize=12.sp, fontWeight=FontWeight.Bold, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.3.sp, lineHeight=16.sp),
    labelSmall = TextStyle(fontSize=11.sp, fontWeight=FontWeight.Bold, fontFamily=SpaceGroteskFontFamily, letterSpacing=0.5.sp, lineHeight=14.sp),
)
