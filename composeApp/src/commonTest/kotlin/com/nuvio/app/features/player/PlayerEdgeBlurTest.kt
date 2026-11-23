package com.nuvio.app.features.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerEdgeBlurTest {
    @Test
    fun edgeIsOpaqueAndInnerBoundaryIsClear() {
        assertEquals(1f, playerEdgeBlurStrength(0f))
        assertEquals(0f, playerEdgeBlurStrength(0.92f))
        assertEquals(0f, playerEdgeBlurStrength(1f))
        assertEquals(1f, playerEdgeBlurStrength(-1f))
        assertEquals(0f, playerEdgeBlurStrength(2f))
    }

    @Test
    fun strengthFadesContinuouslyWithoutStepsOrOvershoot() {
        var previous = 1f
        for (index in 0..1000) {
            val strength = playerEdgeBlurStrength(index / 1000f)
            assertTrue(strength in 0f..1f)
            assertTrue(strength <= previous)
            assertTrue(previous - strength < 0.002f)
            previous = strength
        }
        assertTrue(playerEdgeBlurStrength(0.919f) < 0.00001f)
    }

    @Test
    fun progressiveEasingHasMatchingSoftEnds() {
        assertEquals(0f, PlayerEdgeBlurEasing.transform(0f))
        assertEquals(0.5f, PlayerEdgeBlurEasing.transform(0.5f))
        assertEquals(1f, PlayerEdgeBlurEasing.transform(1f))
        assertTrue(PlayerEdgeBlurEasing.transform(0.001f) < 0.00001f)
        assertTrue(1f - PlayerEdgeBlurEasing.transform(0.999f) < 0.00001f)
    }
}
