package com.example.myemergencyapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myemergencyapp.classifier.ClassificationResult
import com.example.myemergencyapp.classifier.EmergencyType
import com.example.myemergencyapp.classifier.SemanticClassifier

// ──────────────────────────────────────────────────────────────────────────────
// Main Screen
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var inputText by remember { mutableStateOf("") }
    var currentResult by remember { mutableStateOf<ClassificationResult?>(null) }
    val history = remember { mutableStateListOf<ClassificationResult>() }

    EmergencyAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Emergency Classifier",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Semantic Speech Analysis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Input card ──────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    InputCard(
                        text = inputText,
                        onTextChange = { inputText = it },
                        onClassify = {
                            val result = SemanticClassifier.classify(inputText)
                            currentResult = result
                            if (result.type != EmergencyType.None) {
                                history.add(0, result)
                                if (history.size > 20) history.removeLast()
                            }
                        },
                        onClear = {
                            inputText = ""
                            currentResult = null
                        }
                    )
                }

                // ── Suggestion chips ────────────────────────────────────────
                item {
                    SamplePhrases { phrase -> inputText = phrase }
                }

                // ── Result card ─────────────────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = currentResult != null,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        currentResult?.let { ResultCard(it) }
                    }
                }

                // ── History ─────────────────────────────────────────────────
                if (history.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Recent Detections (${history.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { history.clear() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear history",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(history) { result ->
                        HistoryItem(result)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Input Card
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputCard(
    text: String,
    onTextChange: (String) -> Unit,
    onClassify: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Speech / Text Input",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Type speech text or pick a sample phrase below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        "e.g. \"There is a fire, call the firefighter!\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor     = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor   = MaterialTheme.colorScheme.onBackground,
                    cursorColor          = MaterialTheme.colorScheme.primary,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onClassify,
                    enabled = text.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Classify", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onClear,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor   = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Sample Phrases
// ──────────────────────────────────────────────────────────────────────────────

private val samplePhrases = listOf(
    "I need an ambulance, he is not breathing",
    "There is a huge fire, smoke everywhere!",
    "Someone broke into my house, call the police",
    "Car crash on highway, driver is unconscious",
    "The river is flooding, we are trapped",
    "I don't want to live anymore, please help",
    "Good morning, how are you today?"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SamplePhrases(onSelect: (String) -> Unit) {
    Column {
        Text(
            text = "Quick test phrases",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            samplePhrases.forEach { phrase ->
                SuggestionChip(
                    onClick = { onSelect(phrase) },
                    label = {
                        Text(
                            text = phrase.take(32) + if (phrase.length > 32) "…" else "",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor     = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Result Card
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultCard(result: ClassificationResult) {
    val animatedConfidence by animateFloatAsState(
        targetValue = result.confidence,
        animationSpec = tween(durationMillis = 800),
        label = "confidence"
    )
    val typeColor = result.type.color

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = typeColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Type badge row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.15f))
                        .border(1.5.dp, typeColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = result.type.icon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = result.type.label.uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        ),
                        color = typeColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = result.type.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(14.dp))

            // Confidence bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Confidence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(animatedConfidence * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = typeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { animatedConfidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color            = typeColor,
                trackColor       = typeColor.copy(alpha = 0.15f),
                strokeCap        = StrokeCap.Round
            )

            // Matched keywords
            if (result.matchedKeywords.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Matched Keywords",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    result.matchedKeywords.forEach { kw ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(typeColor.copy(alpha = 0.12f))
                                .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = kw,
                                style = MaterialTheme.typography.labelSmall,
                                color = typeColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// History Item
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryItem(result: ClassificationResult) {
    val typeColor = result.type.color

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, typeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = result.type.icon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.type.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = typeColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (result.matchedKeywords.isNotEmpty()) {
                    Text(
                        text = result.matchedKeywords.take(3).joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = "${(result.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = typeColor.copy(alpha = 0.8f),
                textAlign = TextAlign.End
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0A0E1A)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
