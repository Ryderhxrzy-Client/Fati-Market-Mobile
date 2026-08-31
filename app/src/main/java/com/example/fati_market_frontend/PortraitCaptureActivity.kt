package com.fati_market

import com.journeyapps.barcodescanner.CaptureActivity

/**
 * The QR scanner, upright.
 *
 * zxing-android-embedded declares its own CaptureActivity as landscape in the
 * library manifest, so launching the scanner flipped the whole phone sideways
 * at the counter. This subclass exists purely so our manifest can pin the
 * scanner to portrait - the orientation comes from the manifest entry, and
 * ScanOptions points here instead of at the library's activity.
 */
class PortraitCaptureActivity : CaptureActivity()
