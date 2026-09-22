package com.example.myemergencyapp.classifier

/**
 * SemanticClassifier
 *
 * Classifies a piece of text (representing transcribed speech) into an [EmergencyType].
 *
 * Design notes
 * ─────────────
 * • Uses a weighted keyword-matching algorithm with per-category keyword banks.
 * • Each keyword bank can be extended at any time without changing the core logic.
 * • Returns a [ClassificationResult] containing the winning [EmergencyType],
 *   a 0-1 confidence score, and the matched keywords (useful for UI and debugging).
 * • Designed to be a drop-in host for an on-device ML model later (TFLite / ONNX).
 */
object SemanticClassifier {

    // ──────────────────────────────────────────────────────────────────────────
    // Keyword banks  (add more as needed)
    // ──────────────────────────────────────────────────────────────────────────

    private val medicalKeywords = setOf(
        // English
        "ambulance", "hospital", "doctor", "nurse", "bleeding", "blood",
        "heart attack", "cardiac", "stroke", "chest pain", "unconscious", "faint",
        "fainted", "breathe", "breathing", "asthma", "inhaler", "seizure",
        "epilepsy", "wound", "injury", "injured", "hurt", "pain", "medicine",
        "overdose", "poison", "poisoning", "allergic", "anaphylaxis", "emt",
        "paramedic", "first aid", "bandage", "fracture", "broken bone", "fractures",
        "pulse", "defibrillator", "cpr", "resuscitate", "emergency room", "er",
        "icu", "surgery", "accident", "fell", "fall", "fallen",
        // Hindi transliteration
        "dawakhana", "chot", "dard", "behoshi", "ambulans", "doctor bulao",
        // Tamil transliteration
        "maruttuvam", "maruttuvamana", "ambulance vangu", "doctor vaanga"
    )

    private val fireKeywords = setOf(
        // English
        "fire", "flame", "flames", "burning", "burn", "smoke", "smoky",
        "blaze", "wildfire", "explosion", "explode", "gas leak", "arson",
        "firefighter", "fire truck", "fire engine", "extinguisher", "sprinkler",
        "evacuat", "evacuate", "evacuation", "inferno", "ember", "ash",
        "charred", "scorched", "hot", "ignite", "ignition",
        // Hindi transliteration
        "aag", "jalana", "dhuaan",
        // Tamil transliteration
        "thee", "pukai", "eriyuthu"
    )

    private val policeKeywords = setOf(
        // English
        "police", "cop", "officer", "robbery", "robber", "thief", "theft",
        "steal", "stolen", "murder", "kill", "weapon", "gun", "knife",
        "hostage", "kidnap", "abduct", "assault", "attack", "threat",
        "intruder", "break in", "break-in", "burglar", "burglary",
        "harassment", "stalking", "stalker", "crime", "criminal", "gang",
        "terror", "terrorist", "bomb", "explosion", "shooter", "shooting",
        "rape", "violence", "domestic violence",
        // Hindi transliteration
        "police bulao", "chor", "dakaiti", "murder",
        // Tamil transliteration
        "police varanga", "thirudan", "kolai"
    )

    private val accidentKeywords = setOf(
        // English
        "accident", "crash", "collision", "car crash", "hit", "ran over",
        "knocked", "vehicle", "car", "truck", "motorcycle", "bike", "road",
        "highway", "skid", "overturned", "rollover", "hit-and-run",
        "traffic", "pedestrian", "crosswalk",
        // Hindi transliteration
        "durghatna", "gaadi", "takkar",
        // Tamil transliteration
        "viluntu", "vandi", "thadippu"
    )

    private val naturalDisasterKeywords = setOf(
        // English
        "flood", "flooding", "flooded", "earthquake", "quake", "tremor",
        "tsunami", "cyclone", "hurricane", "tornado", "storm", "landslide",
        "avalanche", "drought", "lightning", "thunder", "heatwave",
        "submerged", "trapped", "rescue",
        // Hindi transliteration
        "baadhh", "bhuukamp", "toofan",
        // Tamil transliteration
        "vellam", "nilanadukkam", "puyal"
    )

    private val mentalHealthKeywords = setOf(
        // English
        "suicide", "suicidal", "kill myself", "end my life", "self harm",
        "self-harm", "cut myself", "overdose", "depression", "depressed",
        "hopeless", "hopelessness", "worthless", "no reason to live",
        "don't want to live", "mental breakdown", "panic attack", "anxiety",
        "crisis", "help me", "nobody cares", "alone", "scared",
        // Hindi transliteration
        "marna chahta", "jeena nahi", "khud ko hurt",
        // Tamil transliteration
        "ennai vittu po", "thani", "bayam"
    )

    // ──────────────────────────────────────────────────────────────────────────
    // Classify
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Main entry point.
     *
     * @param text  Raw transcribed speech (or manually typed text).
     * @return [ClassificationResult] with the best match and supporting metadata.
     */
    fun classify(text: String): ClassificationResult {
        val normalized = text.lowercase().trim()
        if (normalized.isEmpty()) return ClassificationResult(EmergencyType.None, 0f, emptyList())

        // Gather scores for each category
        val scores = mapOf(
            EmergencyType.Medical        to scoreText(normalized, medicalKeywords),
            EmergencyType.Fire           to scoreText(normalized, fireKeywords),
            EmergencyType.Police         to scoreText(normalized, policeKeywords),
            EmergencyType.Accident       to scoreText(normalized, accidentKeywords),
            EmergencyType.NaturalDisaster to scoreText(normalized, naturalDisasterKeywords),
            EmergencyType.MentalHealth   to scoreText(normalized, mentalHealthKeywords)
        )

        val best = scores.maxByOrNull { it.value.score }!!

        // If no keyword matched at all → None
        if (best.value.score == 0f) {
            return ClassificationResult(EmergencyType.None, 0f, emptyList())
        }

        // Compute confidence as fraction of total matched weight
        val totalWeight = scores.values.sumOf { it.score.toDouble() }.toFloat()
        val confidence = (best.value.score / totalWeight).coerceIn(0f, 1f)

        return ClassificationResult(
            type           = best.key,
            confidence     = confidence,
            matchedKeywords = best.value.matched
        )
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun scoreText(normalized: String, keywords: Set<String>): KeywordScore {
        var totalScore = 0f
        val matched = mutableListOf<String>()

        for (kw in keywords) {
            if (normalized.contains(kw)) {
                // Longer, more specific phrases score higher
                val weight = if (kw.contains(' ')) 2f else 1f
                totalScore += weight
                matched += kw
            }
        }
        return KeywordScore(totalScore, matched)
    }

    private data class KeywordScore(val score: Float, val matched: List<String>)
}

/**
 * Result returned by [SemanticClassifier.classify].
 *
 * @property type            The best-matching emergency category.
 * @property confidence      0.0 (low) → 1.0 (high) relative confidence score.
 * @property matchedKeywords All keywords that were found in the input text.
 */
data class ClassificationResult(
    val type: EmergencyType,
    val confidence: Float,
    val matchedKeywords: List<String>
)
