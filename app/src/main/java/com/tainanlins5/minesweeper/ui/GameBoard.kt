package com.tainanlins5.minesweeper.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tainanlins5.minesweeper.data.RetroStyle
import com.tainanlins5.minesweeper.domain.Cell
import com.tainanlins5.minesweeper.domain.GameStatus
import kotlin.math.floor
import kotlin.math.min

private const val MIN_READABLE_CELL_DP = 20f
private const val MAX_ZOOM_SCALE = 3.5f

@Composable
fun GameBoard(
    width: Int,
    height: Int,
    cells: List<Cell>,
    status: GameStatus,
    colors: RetroColors,
    flagMode: Boolean,
    resetToken: Int,
    showLossDetails: Boolean = true,
    onReveal: (Int) -> Unit,
    onToggleFlag: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellDp = 28.dp
    val density = LocalDensity.current
    BoxWithConstraints(
        modifier = modifier.clipToBounds().background(colors.panelDark),
    ) {
        val boardWidth = cellDp * width
        val boardHeight = cellDp * height
        val fitScale = min(maxWidth.value / boardWidth.value, maxHeight.value / boardHeight.value)
            .coerceIn(0.2f, MAX_ZOOM_SCALE)
        val readableScale = (MIN_READABLE_CELL_DP / cellDp.value).coerceIn(fitScale, MAX_ZOOM_SCALE)
        val initialScale = maxOf(fitScale, readableScale)
        var scale by remember(width, height, maxWidth, maxHeight, resetToken) {
            mutableFloatStateOf(if (resetToken == 0) initialScale else fitScale)
        }
        var translation by remember(width, height, maxWidth, maxHeight, resetToken) {
            mutableStateOf(Offset.Zero)
        }
        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(fitScale, MAX_ZOOM_SCALE)
            translation += panChange
        }

        val currentFlagMode by rememberUpdatedState(flagMode)
        val currentScale by rememberUpdatedState(scale)
        val currentTranslation by rememberUpdatedState(translation)
        val currentOnReveal by rememberUpdatedState(onReveal)
        val currentOnToggleFlag by rememberUpdatedState(onToggleFlag)

        val cellPx = with(density) { cellDp.toPx() }
        val numberPaints = remember(cellPx, colors.numberColors) {
            colors.numberColors.map { color ->
                Paint().apply {
                    this.color = color.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = cellPx * 0.72f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = false
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformState)
                .pointerInput(cellPx, width, height) {
                    detectTapGestures(
                        onTap = { position ->
                            cellIndexAt(
                                position.x,
                                position.y,
                                size.width.toFloat(),
                                size.height.toFloat(),
                                cellPx,
                                width,
                                height,
                                currentScale,
                                currentTranslation.x,
                                currentTranslation.y,
                            )?.let { index ->
                                if (currentFlagMode) currentOnToggleFlag(index) else currentOnReveal(index)
                            }
                        },
                        onLongPress = { position ->
                            cellIndexAt(
                                position.x,
                                position.y,
                                size.width.toFloat(),
                                size.height.toFloat(),
                                cellPx,
                                width,
                                height,
                                currentScale,
                                currentTranslation.x,
                                currentTranslation.y,
                            )?.let { index ->
                                currentOnToggleFlag(index)
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .requiredSize(boardWidth, boardHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = translation.x
                        translationY = translation.y
                    },
            ) {
                val cellSize = size.width / width
                cells.forEachIndexed { index, cell ->
                    val x = (index % width) * cellSize
                    val y = (index / width) * cellSize
                    val displayedCell = cellForDisplay(cell, status, showLossDetails)
                    drawCell(
                        cell = displayedCell,
                        topLeft = Offset(x, y),
                        size = cellSize,
                        colors = colors,
                        numberPaints = numberPaints,
                        showWrongFlag = showLossDetails && status == GameStatus.LOST && cell.isFlagged && !cell.hasMine,
                    )
                }
            }
        }
    }
}

internal fun cellForDisplay(cell: Cell, status: GameStatus, showLossDetails: Boolean): Cell =
    if (status == GameStatus.LOST && !showLossDetails && cell.hasMine && !cell.isExploded) {
        cell.copy(isRevealed = false)
    } else {
        cell
    }

internal fun cellIndexAt(
    tapX: Float,
    tapY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    cellSize: Float,
    columns: Int,
    rows: Int,
    scale: Float,
    translationX: Float,
    translationY: Float,
): Int? {
    if (cellSize <= 0f || scale <= 0f) return null
    val displayedCellSize = cellSize * scale
    val left = (viewportWidth - cellSize * columns * scale) / 2f + translationX
    val top = (viewportHeight - cellSize * rows * scale) / 2f + translationY
    val column = floor((tapX - left) / displayedCellSize).toInt()
    val row = floor((tapY - top) / displayedCellSize).toInt()
    return if (column in 0 until columns && row in 0 until rows) row * columns + column else null
}

private fun DrawScope.drawCell(
    cell: Cell,
    topLeft: Offset,
    size: Float,
    colors: RetroColors,
    numberPaints: List<Paint>,
    showWrongFlag: Boolean,
) {
    if (cell.isRevealed && !showWrongFlag) {
        drawRect(if (cell.isExploded) colors.explodedCell else colors.revealedCell, topLeft, Size(size, size))
        drawRect(colors.cellGrid, topLeft, Size(size, size), style = Stroke(1f))
        when {
            cell.hasMine -> drawMine(
                topLeft = topLeft,
                size = size,
                color = if (cell.isExploded) colors.explodedMineColor else colors.mineColor,
                highlight = if (cell.isExploded) colors.explodedMineHighlight else colors.mineHighlight,
            )
            cell.adjacentMines > 0 -> drawNumber(cell.adjacentMines, topLeft, size, numberPaints[cell.adjacentMines])
        }
    } else {
        drawRect(colors.panel, topLeft, Size(size, size))
        if (colors.style == RetroStyle.MACINTOSH) {
            drawRect(colors.text, topLeft, Size(size, size), style = Stroke(1f))
            drawLine(colors.panelLight, topLeft + Offset(1f, 1f), Offset(topLeft.x + size - 1f, topLeft.y + 1f), 1f)
            drawLine(colors.panelLight, topLeft + Offset(1f, 1f), Offset(topLeft.x + 1f, topLeft.y + size - 1f), 1f)
        } else {
            val bevel = (size * 0.11f).coerceAtLeast(2f)
            drawLine(colors.panelLight, topLeft, Offset(topLeft.x + size, topLeft.y), bevel)
            drawLine(colors.panelLight, topLeft, Offset(topLeft.x, topLeft.y + size), bevel)
            drawLine(colors.panelDark, Offset(topLeft.x, topLeft.y + size), Offset(topLeft.x + size, topLeft.y + size), bevel)
            drawLine(colors.panelDark, Offset(topLeft.x + size, topLeft.y), Offset(topLeft.x + size, topLeft.y + size), bevel)
        }
        if (cell.isFlagged) drawFlag(topLeft, size, colors.flagColor)
        if (showWrongFlag) {
            drawLine(colors.wrongFlagColor, topLeft + Offset(size * 0.2f, size * 0.2f), topLeft + Offset(size * 0.8f, size * 0.8f), size * 0.1f)
            drawLine(colors.wrongFlagColor, topLeft + Offset(size * 0.8f, size * 0.2f), topLeft + Offset(size * 0.2f, size * 0.8f), size * 0.1f)
        }
    }
}

private fun DrawScope.drawNumber(number: Int, topLeft: Offset, size: Float, paint: Paint) {
    drawContext.canvas.nativeCanvas.drawText(
        number.toString(),
        topLeft.x + size / 2,
        topLeft.y + size * 0.76f,
        paint,
    )
}

private fun DrawScope.drawMine(topLeft: Offset, size: Float, color: Color, highlight: Color) {
    val center = topLeft + Offset(size / 2, size / 2)
    drawCircle(color, size * 0.23f, center)
    for (step in 0 until 4) {
        val diagonal = step % 2 == 1
        val dx = if (diagonal) size * 0.28f else if (step == 0) size * 0.36f else 0f
        val dy = if (diagonal) size * 0.28f else if (step == 0) 0f else size * 0.36f
        drawLine(color, center - Offset(dx, dy), center + Offset(dx, dy), size * 0.08f)
        if (diagonal) drawLine(color, center - Offset(dx, -dy), center + Offset(dx, -dy), size * 0.08f)
    }
    drawCircle(highlight, size * 0.045f, center - Offset(size * 0.08f, size * 0.08f))
}

private fun DrawScope.drawFlag(topLeft: Offset, size: Float, color: Color) {
    val poleX = topLeft.x + size * 0.48f
    drawLine(color, Offset(poleX, topLeft.y + size * 0.2f), Offset(poleX, topLeft.y + size * 0.75f), size * 0.08f)
    val flag = Path().apply {
        moveTo(poleX, topLeft.y + size * 0.2f)
        lineTo(topLeft.x + size * 0.82f, topLeft.y + size * 0.36f)
        lineTo(poleX, topLeft.y + size * 0.5f)
        close()
    }
    drawPath(flag, color)
    drawLine(color, Offset(topLeft.x + size * 0.28f, topLeft.y + size * 0.78f), Offset(topLeft.x + size * 0.7f, topLeft.y + size * 0.78f), size * 0.09f)
}

private fun Color.toArgb(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(),
    (red * 255).toInt(),
    (green * 255).toInt(),
    (blue * 255).toInt(),
)
