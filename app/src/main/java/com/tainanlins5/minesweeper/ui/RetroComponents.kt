package com.tainanlins5.minesweeper.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Modifier.raisedBorder(colors: RetroColors, width: Float = 3f): Modifier = drawBehind {
    drawLine(colors.panelLight, Offset(0f, 0f), Offset(size.width, 0f), width)
    drawLine(colors.panelLight, Offset(0f, 0f), Offset(0f, size.height), width)
    drawLine(colors.panelDark, Offset(0f, size.height), Offset(size.width, size.height), width)
    drawLine(colors.panelDark, Offset(size.width, 0f), Offset(size.width, size.height), width)
}

fun Modifier.sunkenBorder(colors: RetroColors, width: Float = 3f): Modifier = drawBehind {
    drawLine(colors.panelDark, Offset(0f, 0f), Offset(size.width, 0f), width)
    drawLine(colors.panelDark, Offset(0f, 0f), Offset(0f, size.height), width)
    drawLine(colors.panelLight, Offset(0f, size.height), Offset(size.width, size.height), width)
    drawLine(colors.panelLight, Offset(size.width, 0f), Offset(size.width, size.height), width)
}

@Composable
fun RetroButton(
    text: String,
    colors: RetroColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val background = if (selected) colors.accent else colors.panel
    val foreground = if (selected) colors.onAccent else colors.text
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .background(background)
            .raisedBorder(colors)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (selected) "✓ $text" else text,
            color = foreground,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun RetroIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    colors: RetroColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val background = if (selected) colors.accent else colors.panel
    val foreground = if (selected) colors.onAccent else colors.text
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .background(background)
            .raisedBorder(colors)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick)
            .padding(11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = foreground,
        )
    }
}

@Composable
fun RetroIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    colors: RetroColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val background = if (selected) colors.accent else colors.panel
    val foreground = if (selected) colors.onAccent else colors.text
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .background(background)
            .raisedBorder(colors)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick)
            .padding(11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = foreground,
        )
    }
}

@Composable
fun SevenSegmentDisplay(value: String, colors: RetroColors, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(colors.well)
            .sunkenBorder(colors)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value,
            color = Color(0xFFFF3333),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
        )
    }
}

@Composable
fun RetroPanel(
    colors: RetroColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.background(colors.panel).raisedBorder(colors).padding(8.dp)) { content() }
}

@Composable
fun SectionTitle(text: String, colors: RetroColors) {
    Text(text, color = colors.text, fontWeight = FontWeight.Black, fontSize = 18.sp)
}

@Composable
fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    colors: RetroColors,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        values.forEach { value ->
            RetroButton(
                text = label(value),
                colors = colors,
                onClick = { onSelected(value) },
                selected = selected == value,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
