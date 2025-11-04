package com.canopas.lib.showcase.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Creates a [IntroShowcaseState] that is remembered across compositions.
 *
 * Changes to the provided values for [initialIndex] will **not** result in the state being
 * recreated or changed in any way if it has already
 * been created.
 *
 * @param initialIndex the initial value for [IntroShowcaseState.currentTargetIndex]
 */
@Composable
fun rememberIntroShowcaseState(
    initialIndex: Int = 0,
): IntroShowcaseState {
    return remember {
        IntroShowcaseState(
            initialIndex = initialIndex,
        )
    }
}

/**
 * Modifier that marks Compose UI element as a target for [IntroShowCaseTarget]
 */
internal fun Modifier.introShowcaseTarget(
    state: IntroShowcaseState,
    index: Int,
    style: ShowcaseStyle = ShowcaseStyle.Default,
    content: @Composable BoxScope.() -> Unit,
): Modifier = onGloballyPositioned { coordinates ->
    if (!coordinates.isAttached) return@onGloballyPositioned
    
    val rootRect = coordinates.boundsInRoot()
    val winRect = coordinates.boundsInWindow()

    // Only update state when the rect really changed to avoid churn
    val prev = state.targets[index]
    if (prev == null || prev.rectInRoot != rootRect || prev.rectInWindow != winRect) {
        state.targets[index] = TargetInfo(
            index = index,
            rectInRoot = rootRect,
            rectInWindow = winRect,
            style = style,
            content = content,
            revision = System.nanoTime()
        )
    }
}

/**
 * State class for managing the state of the IntroShowcase. Tracks the current target index and
 * associated targets.
 */
class IntroShowcaseState internal constructor(
    initialIndex: Int,
) {

    internal var targets = mutableStateMapOf<Int, TargetInfo>()

    var currentTargetIndex by mutableIntStateOf(initialIndex)
        internal set

    val currentTarget: TargetInfo?
        get() = targets[currentTargetIndex]

    /**
     * Resets the state to its initial values, effectively restarting the showcase.
     */
    fun reset() {
        currentTargetIndex = 0
    }
}

/**
 * New lightweight model: store rects in both spaces to support both overlay modes.
 */
data class TargetInfo(
    val index: Int,
    val rectInRoot: Rect,
    val rectInWindow: Rect,
    val style: ShowcaseStyle,
    val content: @Composable BoxScope.() -> Unit,
    val revision: Long = System.nanoTime()
)
