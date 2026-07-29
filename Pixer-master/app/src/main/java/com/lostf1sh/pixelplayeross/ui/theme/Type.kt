package com.lostf1sh.pixelplayeross.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lostf1sh.pixelplayeross.R


// Title typography uses the bundled variable font so it never depends on a
// downloadable-font provider at runtime.
@OptIn(ExperimentalTextApi::class)
val MontserratFamily = FontFamily(
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Black.weight))
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.ExtraBold.weight))
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Bold.weight))
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.SemiBold.weight))
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Medium.weight))
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Normal.weight))
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.Light.weight))
    ),
)

val ExpTitleTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 60.sp,
        textGeometricTransform = TextGeometricTransform(scaleX = 1.5f),
        letterSpacing = (-0.02).em,
        lineHeight = 0.95.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    displayMedium = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 50.sp,
        //textGeometricTransform = TextGeometricTransform(scaleX = 1f),
        letterSpacing = (-0.02).em,
        lineHeight = 0.95.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        textGeometricTransform = TextGeometricTransform(scaleX = 1.3f),
        letterSpacing = (-0.02).em,
        lineHeight = 0.95.em,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
)

// Bundled variable font with rounded axis for a soft title style.
private const val RoundedSansFlexRond = 100f

@OptIn(ExperimentalTextApi::class)
val RoundedSans = FontFamily(
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Light.weight),
            FontVariation.Setting("ROND", RoundedSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            FontVariation.Setting("ROND", RoundedSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
            FontVariation.Setting("ROND", RoundedSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.SemiBold.weight),
            FontVariation.Setting("ROND", RoundedSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
            FontVariation.Setting("ROND", RoundedSansFlexRond)
        )
    ),
)

// Typography - Use friendly and modern fonts.
// Consider adding custom fonts in res/font for a more unique look.
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RoundedSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Deezer skin typography — same bundled variable font as RoundedSans, "ROND" axis dialed
// toward 0 instead of 100 for sharper, less bubbly letterforms closer to Deezer's bold headers.
private const val DeezerSansFlexRond = 0f

@OptIn(ExperimentalTextApi::class)
val DeezerSansFamily = FontFamily(
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Light.weight),
            FontVariation.Setting("ROND", DeezerSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            FontVariation.Setting("ROND", DeezerSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
            FontVariation.Setting("ROND", DeezerSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.SemiBold.weight),
            FontVariation.Setting("ROND", DeezerSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
            FontVariation.Setting("ROND", DeezerSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Black,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Black.weight),
            FontVariation.Setting("ROND", DeezerSansFlexRond)
        )
    ),
)

// Deezer's UI runs noticeably tighter than Pixer's expressive type scale — measuring the same
// section header against a reference screenshot put Pixer ~25% larger. Sizes below are the
// Pixer scale shrunk by ~0.8 (with line heights following) rather than inherited as-is.
val DeezerTypography = Typography(
    displayLarge = Typography.displayLarge.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Black, fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-0.5).sp),
    displayMedium = Typography.displayMedium.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.ExtraBold, fontSize = 29.sp, lineHeight = 35.sp),
    displaySmall = Typography.displaySmall.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineLarge = Typography.headlineLarge.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineMedium = Typography.headlineMedium.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = Typography.headlineSmall.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 25.sp),
    titleLarge = Typography.titleLarge.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp),
    titleMedium = Typography.titleMedium.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall = Typography.titleSmall.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge = Typography.bodyLarge.copy(fontFamily = DeezerSansFamily, fontSize = 14.sp, lineHeight = 20.sp),
    bodyMedium = Typography.bodyMedium.copy(fontFamily = DeezerSansFamily, fontSize = 12.sp, lineHeight = 17.sp),
    bodySmall = Typography.bodySmall.copy(fontFamily = DeezerSansFamily, fontSize = 11.sp, lineHeight = 15.sp),
    labelLarge = Typography.labelLarge.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp),
    labelMedium = Typography.labelMedium.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 15.sp),
    labelSmall = Typography.labelSmall.copy(fontFamily = DeezerSansFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp)
)
