package com.example.myemergencyapp.classifier

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class representing every possible emergency classification.
 * Add new categories here to extend the classifier.
 */
sealed class EmergencyType(
    val label: String,
    val description: String,
    val color: Color,
    val icon: ImageVector,
    val priority: Int   // 1 = highest
) {
    /** 🔴 Life-threatening medical situation */
    object Medical : EmergencyType(
        label       = "Medical Emergency",
        description = "Signs of injury, illness, or a need for immediate medical help detected.",
        color       = Color(0xFFD32F2F),
        icon        = Icons.Default.MedicalServices,
        priority    = 1
    )

    /** 🔥 Fire or burn-related hazard */
    object Fire : EmergencyType(
        label       = "Fire Emergency",
        description = "Fire, smoke, or burn-related hazard detected.",
        color       = Color(0xFFE64A19),
        icon        = Icons.Default.LocalFireDepartment,
        priority    = 1
    )

    /** 🚓 Crime or security threat */
    object Police : EmergencyType(
        label       = "Security / Police",
        description = "Crime, assault, or security threat detected.",
        color       = Color(0xFF1565C0),
        icon        = Icons.Default.LocalPolice,
        priority    = 1
    )

    /** 🚗 Road accident or vehicular emergency */
    object Accident : EmergencyType(
        label       = "Road Accident",
        description = "Vehicle collision or road accident scenario detected.",
        color       = Color(0xFFF57F17),
        icon        = Icons.Default.DirectionsCar,
        priority    = 2
    )

    /** 🌊 Natural disaster (flood, earthquake, etc.) */
    object NaturalDisaster : EmergencyType(
        label       = "Natural Disaster",
        description = "Flood, earthquake, cyclone, or other natural disaster detected.",
        color       = Color(0xFF00796B),
        icon        = Icons.Default.Flood,
        priority    = 2
    )

    /** 🧠 Mental health crisis */
    object MentalHealth : EmergencyType(
        label       = "Mental Health Crisis",
        description = "Expressions of severe distress, self-harm, or suicidal ideation detected.",
        color       = Color(0xFF6A1B9A),
        icon        = Icons.Default.Psychology,
        priority    = 1
    )

    /** ⚠️ Generic danger but not clearly categorized */
    object GeneralDanger : EmergencyType(
        label       = "General Danger",
        description = "Possible danger detected but category is unclear.",
        color       = Color(0xFF757575),
        icon        = Icons.Default.Warning,
        priority    = 3
    )

    /** ✅ No emergency detected */
    object None : EmergencyType(
        label       = "No Emergency",
        description = "No emergency keywords detected in the speech.",
        color       = Color(0xFF388E3C),
        icon        = Icons.Default.QuestionMark,
        priority    = 99
    )

    override fun toString(): String = label
}
