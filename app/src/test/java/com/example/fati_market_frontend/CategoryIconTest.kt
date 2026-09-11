package com.fati_market

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** The home page's category circles pick their icon from the category's name. */
class CategoryIconTest {

    @Test
    fun notebooksAndStationeryLookDifferentFromBooks() {
        assertNotEquals(categoryIcon("Books"), categoryIcon("Notebooks & Stationary"))
    }

    @Test
    fun eitherSpellingOfStationeryIsRecognised() {
        assertEquals(categoryIcon("Stationary"), categoryIcon("Stationery"))
    }

    @Test
    fun anUnknownNameFallsBackToTheGenericIcon() {
        assertEquals(categoryIcon("Miscellaneous"), categoryIcon("Other"))
    }
}
