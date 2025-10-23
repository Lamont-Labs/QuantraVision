package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.lamontlabs.quantravision.export.ProofSummaryClipboard

/**
 * ProofSummaryButton
 * Single-tap action to copy proof info.
 */
@Composable
fun ProofSummaryButton(context: Context) {
    var done by remember { mutableStateOf(false) }
    Button(onClick = {
        done = ProofSummaryClipboard.copy(context)
    }) {
        Text(if (done) "Copied" else "Copy Proof Summary")
    }
}
