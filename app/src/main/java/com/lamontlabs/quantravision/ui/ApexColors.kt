/*
 * Copyright (c) 2025 Lamont Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lamontlabs.quantravision.ui

import androidx.compose.ui.graphics.Color

/**
 * ApexColors
 * 
 * Master Spec v2.0 Color System
 * 
 * Canonical Apex palette colors for consistent branding.
 * These colors are used throughout the app for overlays, HUD, and UI elements.
 */
object ApexColors {
    val apexCyan = Color(0xFF00E0FF)
    val apexBlue = Color(0xFF005CFF)
    val apexRed = Color(0xFFFF3B3B)
    val apexGreen = Color(0xFF00FF8C)
    val apexYellow = Color(0xFFFFDB4D)
    val textPrimary = Color(0xFFD7E6FA)
    val backgroundDeep = Color(0xFF030A14)
    
    val apexCyanInt = 0xFF00E0FF.toInt()
    val apexBlueInt = 0xFF005CFF.toInt()
    val apexRedInt = 0xFFFF3B3B.toInt()
    val apexGreenInt = 0xFF00FF8C.toInt()
    val apexYellowInt = 0xFFFFDB4D.toInt()
    val textPrimaryInt = 0xFFD7E6FA.toInt()
    val backgroundDeepInt = 0xFF030A14.toInt()
    
    val passSolid = apexCyan
    val waitAmber = apexYellow
    val failRed = apexRed
    val suppressedViolet = Color(0xFF8A2BE2)
    val omegaGray = Color(0xFF4A4A4A)
    
    val overlayPass = apexCyanInt
    val overlayWait = 0xFFFFDB4D.toInt()
    val overlayFail = apexRedInt
    val overlaySuppressed = 0xFF8A2BE2.toInt()
    val overlayOmega = 0xFF4A4A4A.toInt()
}

/**
 * ApexBranding
 * 
 * Master Spec v2.0 Branding Constants
 */
object ApexBranding {
    const val POWERED_BY = "Powered by QuantraCore Apex™ logic"
    const val BUILT_BY = "Built by Lamont Labs"
    const val LEGAL_FOOTER = "Educational use only. Not financial advice."
    const val APP_NAME = "QuantraVision Apex"
    const val VERSION_TAG = "v2.0"
    
    const val TRADEMARK_FULL = "QuantraCore Apex™"
    const val TRADEMARK_SHORT = "Apex™"
    const val COMPANY = "Lamont Labs"
    
    val brandingFooter: String
        get() = "$POWERED_BY | $BUILT_BY"
}
