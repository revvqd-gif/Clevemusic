package com.cleve.music.ui.common.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign

/**
 * Phosphor icon font glyphs (regular weight, `Phosphor.ttf`).
 * Codepoints taken from phosphor-icons/web 2.1.1 `src/regular/style.css`.
 */
enum class Ph(val code: String) {
    House("\uE2C2"),
    Search("\uE30C"),
    MagnifyingGlass("\uE30C"),
    MusicNotes("\uE340"),
    MusicNote("\uE33C"),
    Play("\uE3D0"),
    Pause("\uE39E"),
    SkipBack("\uE5A4"),
    SkipForward("\uE5A6"),
    Shuffle("\uE422"),
    Repeat("\uE3F6"),
    RepeatOnce("\uE3F8"),
    Heart("\uE2A8"),
    HeartStraight("\uE2AA"),
    Queue("\uE6AC"),
    ListDashes("\uE2F4"),
    ListBullets("\uE2F2"),
    VinylRecord("\uECAC"),
    Trash("\uE4A6"),
    Plus("\uE3D4"),
    CaretLeft("\uE138"),
    CaretDown("\uE136"),
    X("\uE4F6"),
    DotsThree("\uE1FE"),
    DotsThreeVertical("\uE208"),
    GearSix("\uE272"),
    Clock("\uE19A"),
    ClockCounterClockwise("\uE1A0"),
    DownloadSimple("\uE20C"),
    ShareNetwork("\uE408"),
    ArrowLeft("\uE058"),
    ArrowClockwise("\uE036"),
    ArrowClockwiseAlt("\uE094"),
    SpeakerHigh("\uE44A"),
    SpeakerSlash("\uE45A"),
    Waveform("\uE802"),
    Disc("\uE564"),
    Playlist("\uE6AA"),
    Radio("\uE77E"),
    Headphones("\uE2A6"),
    Star("\uE46A"),
    Spinner("\uE66C"),
}

/**
 * Renders a Phosphor glyph. Uses Text so the icon scales with the font and
 * renders identically at any density.
 */
@Composable
fun PhIcon(
    icon: Ph,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = LocalContentColor.current,
    fontSize: TextUnit = size.value.sp,
) {
    Text(
        text = icon.code,
        modifier = modifier.size(size),
        color = tint,
        style = TextStyle(
            fontFamily = PhosphorFont,
            fontSize = fontSize,
            lineHeight = fontSize,
            textAlign = TextAlign.Center,
        ),
    )
}

@Composable
fun PhIconTinted(
    icon: Ph,
    size: Dp = 22.dp,
    tint: Color,
    modifier: Modifier = Modifier,
) = PhIcon(icon = icon, modifier = modifier, size = size, tint = tint)
