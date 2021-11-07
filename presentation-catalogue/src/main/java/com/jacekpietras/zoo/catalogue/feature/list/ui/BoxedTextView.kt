package com.jacekpietras.zoo.catalogue.feature.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.jacekpietras.zoo.catalogue.res.colorPrimary

@Composable
internal fun BoxedTextView(text: String) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color = colorPrimary())
            .padding(vertical = 2.dp, horizontal = 8.dp),
    ) {
        Text(
            color = Color.White,
            text = text,
        )
    }
}

@Preview
@Composable
private fun BoxedTextViewPreview() {
    BoxedTextView("Lorem Ipsum")
}

@Preview
@Composable
private fun ThemedBoxedTextViewPreview() {
    MdcTheme {
        BoxedTextView("Lorem Ipsum")
    }
}
