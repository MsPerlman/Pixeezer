package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.size.Size
import com.lostf1sh.pixelplayeross.R

data class DeezerInspiredMixRow(
    val mixId: String,
    val imageUrl: String?,
    val title: String,
    val subtitle: String,
)

@Composable
fun DeezerMixesInspiredSection(
    rows: List<DeezerInspiredMixRow>,
    onRowClick: (DeezerInspiredMixRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) return
    val pages = remember(rows) { rows.chunked(3) }
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.home_mixes_inspired_by_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val pageWidth = this.maxWidth - 64.dp
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pages, key = { page -> page.first().mixId }) { page ->
                    Column(
                        modifier = Modifier.width(pageWidth),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        page.forEach { row ->
                            DeezerInspiredMixCard(row = row, onClick = { onRowClick(row) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeezerInspiredMixCard(row: DeezerInspiredMixRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A1A1A))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmartImage(
            model = row.imageUrl,
            contentDescription = row.title,
            contentScale = ContentScale.Crop,
            targetSize = Size(200, 200),
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
