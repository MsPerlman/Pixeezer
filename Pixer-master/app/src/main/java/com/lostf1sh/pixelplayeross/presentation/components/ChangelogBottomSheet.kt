package com.lostf1sh.pixelplayeross.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.network.GitHubRelease
import com.lostf1sh.pixelplayeross.presentation.components.subcomps.SineWaveLine
import com.lostf1sh.pixelplayeross.presentation.viewmodel.MainViewModel
import com.lostf1sh.pixelplayeross.ui.theme.ExpTitleTypography
import com.lostf1sh.pixelplayeross.ui.theme.RoundedSans
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ChangelogBottomSheet(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val changelogUrl = "https://github.com/Minuga-RC/Pixer"
    val changelogs by mainViewModel.changelogs.collectAsStateWithLifecycle()

    val fabCornerRadius = 16.dp

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.presentation_batch_g_changelog_title),
                fontFamily = RoundedSans,
                style = ExpTitleTypography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            SineWaveLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .height(32.dp)
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp),
                animate = true,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                alpha = 0.95f,
                strokeWidth = 4.dp,
                amplitude = 4.dp,
                waves = 7.6f,
                phase = 0f
            )

            if (changelogs == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (changelogs!!.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.changelog_load_error))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(changelogs!!) { release ->
                        ChangelogReleaseItem(release = release)
                    }
                }
            }
        }

        MediumExtendedFloatingActionButton(
            onClick = { openUrl(context, changelogUrl) },
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusBR = fabCornerRadius,
                smoothnessAsPercentBR = 60,
                cornerRadiusBL = fabCornerRadius,
                smoothnessAsPercentBL = 60,
                cornerRadiusTR = fabCornerRadius,
                smoothnessAsPercentTR = 60,
                cornerRadiusTL = fabCornerRadius,
                smoothnessAsPercentTL = 60
            ),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.github),
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.presentation_batch_g_changelog_view_github)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(30.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                )
        ) {

        }
    }
}

@Composable
fun ChangelogReleaseItem(release: GitHubRelease) {
    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val date = release.publishedAt?.let { parser.parse(it) }
        if (date != null) {
            SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VersionBadge(versionNumber = release.tagName)
            Spacer(modifier = Modifier.weight(1f))
            if (formattedDate.isNotEmpty()) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = release.name ?: stringResource(R.string.changelog_release_fallback_name_format, release.tagName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Text(
                    text = release.body ?: stringResource(R.string.changelog_no_release_notes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun VersionBadge(
    versionNumber: String
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
    ) {
        Text(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
            text = versionNumber,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun openUrl(context: Context, url: String) {
    val uri = try { url.toUri() } catch (_: Throwable) { url.toUri() }
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}
