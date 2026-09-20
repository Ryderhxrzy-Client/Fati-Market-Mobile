package com.fati_market

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * What a line of the activity feed carries.
 *
 * The feed used to hand over a display name and nothing else, so the screen
 * drew the same icon on every row. These hold the parsing to keeping the
 * person: their id, their face, the other party, and the facts behind the
 * sentence.
 */
class ActivityEntryTest {

    private fun handover(): JSONObject = JSONObject(
        """
        {
          "action": "purchase",
          "user": "Ofelia Store",
          "user_id": 1,
          "user_photo": "https://cdn.test/ofelia.jpg",
          "user_role": "admin",
          "user_email": "ofelia@fatima.edu.ph",
          "description": "Handed \"Living in the IT Era\" over to Sheryl Cris Carigma",
          "resource_type": "order",
          "resource_id": 15,
          "timestamp": "2026-09-16 18:11:00",
          "subject": {
            "user_id": 2,
            "name": "Sheryl Cris Carigma",
            "photo": "https://cdn.test/sheryl.jpg",
            "role": "student",
            "email": "sheryl@student.fatima.edu.ph"
          },
          "details": [
            {"label": "Buyer", "value": "Sheryl Cris Carigma"},
            {"label": "Amount due", "value": "PHP 250.00"}
          ]
        }
        """.trimIndent()
    )

    @Test
    fun `a handover keeps the admin who did it`() {
        val entry = parseActivity(handover())

        assertEquals("purchase", entry.action)
        assertEquals(1, entry.actor.userId)
        assertEquals("Ofelia Store", entry.actor.name)
        assertEquals("https://cdn.test/ofelia.jpg", entry.actor.photo)
        assertEquals("admin", entry.actor.role)
        assertEquals("O", entry.actor.initial)
    }

    @Test
    fun `and the person it was handed to`() {
        val entry = parseActivity(handover())

        assertEquals(2, entry.subject?.userId)
        assertEquals("Sheryl Cris Carigma", entry.subject?.name)
        assertEquals("https://cdn.test/sheryl.jpg", entry.subject?.photo)
    }

    @Test
    fun `the facts behind the sentence come through in order`() {
        val entry = parseActivity(handover())

        assertEquals(2, entry.details.size)
        assertEquals("Buyer", entry.details[0].label)
        assertEquals("PHP 250.00", entry.details[1].value)
    }

    @Test
    fun `a line with no photo falls back to an initial rather than breaking`() {
        val entry = parseActivity(
            JSONObject(
                """
                {
                  "action": "create",
                  "user": "Juan Dela Cruz",
                  "user_id": 4,
                  "user_photo": null,
                  "user_role": "student",
                  "user_email": null,
                  "description": "Registered a student account",
                  "resource_type": "user",
                  "resource_id": 4,
                  "timestamp": "2026-09-16 09:00:00",
                  "subject": null,
                  "details": []
                }
                """.trimIndent()
            )
        )

        assertEquals("", entry.actor.photo)
        assertEquals("", entry.actor.email)
        assertEquals("J", entry.actor.initial)
        assertNull(entry.subject)
        assertEquals(emptyList<ActivityDetail>(), entry.details)
    }

    @Test
    fun `an older server that sends only the original fields still parses`() {
        val entry = parseActivity(
            JSONObject(
                """
                {
                  "action": "update",
                  "user": "Ofelia Store",
                  "description": "Published \"Calculator\"",
                  "resource_type": "item",
                  "resource_id": 7,
                  "timestamp": "2026-09-16 10:00:00"
                }
                """.trimIndent()
            )
        )

        assertEquals("Ofelia Store", entry.user)
        assertEquals(0, entry.actor.userId)
        assertEquals("", entry.actor.photo)
        assertNull(entry.subject)
        assertEquals(0, entry.details.size)
    }
}
