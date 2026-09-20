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
        openCode = normalize(code)
    }

    fun close() {
        openCode = null
    }

    /**
     * The code inside whatever was scanned.
     *
     * The website console draws its counter handoff as a link -
     * https://.../turnover/FMITEM1.7.a1b2... - so that any phone's own camera
     * can open the turnover page in a browser. Scanning that same square with
     * this app should not be the one way that fails: the link carries a code
     * this app already knows, so it is lifted out and the native turnover
     * screen opens instead of a browser tab.
     *
     * Anything without a code in it is passed through untouched, and the
     * screen it lands on says what it makes of it.
     */
    internal fun normalize(scanned: String): String {
        val text = scanned.trim()

        return EMBEDDED_CODE.find(text)?.value ?: text
    }

    /** An item turnover code or an order pickup code, wherever it sits. */
    private val EMBEDDED_CODE = Regex("""FM(?:ITEM1|QR1)\.\d+\.[A-Za-z0-9]+""")
}
