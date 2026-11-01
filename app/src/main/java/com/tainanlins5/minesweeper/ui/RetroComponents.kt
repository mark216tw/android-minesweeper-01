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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tainanlins5.minesweeper.data.RetroStyle

enum class RetroFrame { BEVEL, OUTLINE, DOUBLE, HARDPIXEL }

fun retroFrame(style: RetroStyle): RetroFrame = when (style) {
    RetroStyle.WINDOWS, RetroStyle.WINGREEN, RetroStyle.GAMEBOY -> RetroFrame.BEVEL
    RetroStyle.MACINTOSH, RetroStyle.APPLE2 -> RetroFrame.OUTLINE
    RetroStyle.C64 -> RetroFrame.DOUBLE
    RetroStyle.FAMICOM -> RetroFrame.HARDPIXEL
}

fun DrawScope.drawRetroFrame(
    colors: RetroColors,
    topLeft: Offset,
    width: Float,
    height: Float,
    thickness: Float,
    raised: Boolean,
    embedded: Boolean = false,
) {
    val size = Size(width, height)
    val hairline = 1f
    val line = (thickness * 0.5f).coerceAtLeast(hairline)
    when (retroFrame(colors.style)) {
        RetroFrame.BEVEL -> {
            val hi = if (raised) colors.panelLight else colors.panelDark
            val lo = if (raised) colors.panelDark else colors.panelLight
            drawLine(hi, topLeft, topLeft + Offset(width, 0f), thickness)
            drawLine(hi, topLeft, topLeft + Offset(0f, height), thickness)
            drawLine(lo, topLeft + Offset(0f, height), topLeft + Offset(width, height), thickness)
            drawLine(lo, topLeft + Offset(width, 0f), topLeft + Offset(width, height), thickness)
        }
        RetroFrame.OUTLINE -> {
            drawRect(colors.text, topLeft, size, style = Stroke(if (embedded) hairline else 2f))
            if (embedded) {
                drawLine(colors.panelLight, topLeft + Offset(1f, 1f), topLeft + Offset(width - 1f, 1f), 2f)
                drawLine(colors.panelLight, topLeft + Offset(1f, 1f), topLeft + Offset(1f, height - 1f), 2f)
            }
        }
        RetroFrame.DOUBLE -> {
            val outer = if (raised) colors.text else colors.panelDark
            drawRect(outer, topLeft, size, style = Stroke(thickness))
            val inset = thickness + 2f
            drawRect(
                colors.text,
                topLeft + Offset(inset, inset),
                Size(width - 2 * inset, height - 2 * inset),
                style = Stroke(hairline),
            )
        }
        RetroFrame.HARDPIXEL -> {
            drawRect(colors.text, topLeft, size, style = Stroke(thickness))
            val hi = if (raised) colors.panelLight else colors.panelDark
            val lo = if (raised) colors.panelDark else colors.panelLight
            val inset = thickness / 2 + hairline
            drawLine(hi, topLeft + Offset(inset, inset), topLeft + Offset(width - inset, inset), line)
            drawLine(hi, topLeft + Offset(inset, inset), topLeft + Offset(inset, height - inset), line)
            drawLine(lo, topLeft + Offset(inset, height - inset), topLeft + Offset(width - inset, height - inset), line)
            drawLine(lo, topLeft + Offset(width - inset, inset), topLeft + Offset(width - inset, height - inset), line)
        }
    }
}

fun Modifier.raisedBorder(colors: RetroColors, width: Float = 3f): Modifier = drawBehind {
    drawRetroFrame(colors, Offset.Zero, size.width, size.height, width, raised = true)
}

fun Modifier.sunkenBorder(colors: RetroColors, width: Float = 3f): Modifier = drawBehind {
    drawRetroFrame(colors, Offset.Zero, size.width, size.height, width, raised = false)
}

fun Modifier.headerChrome(colors: RetroColors): Modifier = when (colors.style) {
    RetroStyle.MACINTOSH -> drawBehind {
        drawRect(colors.panel)
        val stripeGap = 4.dp.toPx()
        val stripeWidth = 1.dp.toPx()
        var y = stripeWidth
        while (y < size.height) {
            drawLine(colors.text.copy(alpha = 0.14f), Offset(0f, y), Offset(size.width, y), stripeWidth)
            y += stripeGap
        }
        val bottom = size.height - 1.dp.toPx()
        drawLine(colors.text, Offset(0f, bottom), Offset(size.width, bottom), 2.dp.toPx())
    }
    else -> Modifier
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
            textAlign = TextAlign.Center,
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
            color = colors.displayText,
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
    itemsPerRow: Int = values.size,
) {
    val perRow = itemsPerRow.coerceIn(1, values.size)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        values.chunked(perRow).forEach { rowValues ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowValues.forEach { value ->
                    RetroButton(
                        text = label(value),
                        colors = colors,
                        onClick = { onSelected(value) },
                        selected = selected == value,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(perRow - rowValues.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
