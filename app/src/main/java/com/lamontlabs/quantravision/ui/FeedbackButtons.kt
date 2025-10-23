package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.feedback.FalsePositiveLog

/**
 * FeedbackButtons
 * Embedded in detection details to let user report/suppress false positives.
 */
@Composable
fun FeedbackButtons(context: Context, imagePath: String, patternId: String) {
    var reported by remember { mutableStateOf(FalsePositiveLog.isSuppressed(context, imagePath, patternId)) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        if (!reported) {
            Button(onClick = {
                FalsePositiveLog.record(context, imagePath, patternId)
                reported = true
            }) { Text("Report False Positive") }
        } else {
            OutlinedButton(enabled = false, onClick = {}) { Text("Reported") }
        }
    }
}
