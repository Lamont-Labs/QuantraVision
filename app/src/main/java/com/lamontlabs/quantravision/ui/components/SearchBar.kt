package com.lamontlabs.quantravision.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

/**
 * SearchBar
 * Small reusable search input for screens.
 */
@Composable
fun SearchBar(
    state: MutableState<TextFieldValue>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        modifier = modifier,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        placeholder = { Text(placeholder) },
        singleLine = true
    )
}
