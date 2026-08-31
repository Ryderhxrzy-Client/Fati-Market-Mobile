package com.fati_market

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The counter screen, openable from anywhere.
 *
 * Scanning a QR is one way to reach the walk-in screens; pressing Complete on
 * an order, or Mark acquired on an offer, is another - and it must land on
 * the same page, because that page is where the handover gets photographed.
 * Those buttons live deep inside the chat and the transactions list, several
 * layers below the dashboard that hosts the screen, so instead of threading a
 * callback through every one of them they publish the code here and the
 * dashboard opens it.
 *
 * The code is the order's own signed QR, handed over by the server, so
 * arriving from a button and arriving from the camera are indistinguishable
 * by the time the screen loads.
 */
object AdminCounter {

    /** The code the dashboard should be showing, or null for none. */
    var openCode by mutableStateOf<String?>(null)
        private set

    fun open(code: String) {
        openCode = code
    }

    fun close() {
        openCode = null
    }
}
