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

package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.ui.ApexBranding
import com.lamontlabs.quantravision.ui.ApexColors

/**
 * BrandingFooter
 * 
 * Master Spec v2.0 Branding Component
 * 
 * Displays the required branding footer:
 * - "Powered by QuantraCore Apex™ logic"
 * - "Built by Lamont Labs"
 * - "Educational use only. Not financial advice."
 */
@Composable
fun BrandingFooter(
    modifier: Modifier = Modifier,
    showLegal: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = ApexBranding.POWERED_BY,
            style = MaterialTheme.typography.labelSmall,
            color = ApexColors.apexCyan,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = ApexBranding.BUILT_BY,
            style = MaterialTheme.typography.labelSmall,
            color = ApexColors.textPrimary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        if (showLegal) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = ApexBranding.LEGAL_FOOTER,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = ApexColors.textPrimary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Compact branding line for use in overlays or constrained spaces.
 */
@Composable
fun BrandingLine(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = ApexBranding.brandingFooter,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = ApexColors.textPrimary.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

/**
 * Legal disclaimer component for screens requiring explicit legal notice.
 */
@Composable
fun LegalDisclaimer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = ApexBranding.LEGAL_FOOTER,
            style = MaterialTheme.typography.bodySmall,
            color = ApexColors.apexYellow,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "This application provides educational chart analysis only. " +
                   "It does not provide financial advice, trading signals, or recommendations. " +
                   "Always consult a qualified financial advisor before making investment decisions.",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = ApexColors.textPrimary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
