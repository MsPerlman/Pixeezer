package com.lostf1sh.pixelplayeross.presentation.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.preferences.PinnedHomeItem
import com.lostf1sh.pixelplayeross.data.preferences.PinnedItemType
import com.lostf1sh.pixelplayeross.presentation.viewmodel.LibraryViewModel
import com.lostf1sh.pixelplayeross.ui.theme.AppSkin
import com.lostf1sh.pixelplayeross.ui.theme.LocalAppSkin

/**
 * Pin/unpin a playlist, album or artist to the Deezer home grid.
 *
 * Renders nothing outside the Deezer skin, since the pinned grid only exists there. Uses the
 * activity-scoped LibraryViewModel so it shares pin state with the home screen.
 */
@Composable
fun PinToHomeIconButton(
    type: PinnedItemType,
    id: String,
    label: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (LocalAppSkin.current != AppSkin.DEEZER) return
    val activity = LocalContext.current as? ComponentActivity ?: return
    val libraryViewModel: LibraryViewModel = hiltViewModel(activity)
    val pinnedItems by libraryViewModel.pinnedHomeItems.collectAsStateWithLifecycle()
    val isPinned = pinnedItems.any { it.type == type && it.id == id }

    FilledIconButton(
        modifier = modifier,
        onClick = {
            libraryViewModel.togglePin(
                PinnedHomeItem(type = type, id = id, label = label, imageUrl = imageUrl)
            )
        },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_push_pin_24),
            contentDescription = if (isPinned) "Unpin from Home" else "Pin to Home",
            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
