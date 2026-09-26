package com.example.locationlogger.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.locationlogger.R
import com.example.locationlogger.ui.theme.LocationLoggerTheme

@Composable
fun PermissionCard(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.14f), contentColor = accentColor) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                TextButton(onClick = onAction, modifier = Modifier.align(Alignment.End)) {
                    Text(actionLabel, color = accentColor)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PermissionCardPreview() {
    LocationLoggerTheme {
        PermissionCard(
            title = "Location is turned off",
            body = "Turn on location so new entries can be recorded.",
            actionLabel = "Turn on",
            onAction = {},
            icon = R.drawable.ic_notification
        )
    }
}
