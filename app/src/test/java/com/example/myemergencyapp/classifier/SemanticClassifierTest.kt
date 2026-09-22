package com.example.myemergencyapp.classifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SemanticClassifier].
 * Run with: ./gradlew test
 */
class SemanticClassifierTest {

    @Test
    fun `medical keywords trigger Medical type`() {
        val result = SemanticClassifier.classify("Call an ambulance, he is not breathing")
        assertEquals(EmergencyType.Medical, result.type)
        assertTrue(result.confidence > 0f)
    }

    @Test
    fun `fire keywords trigger Fire type`() {
        val result = SemanticClassifier.classify("There is a fire, smoke everywhere!")
        assertEquals(EmergencyType.Fire, result.type)
    }

    @Test
    fun `police keywords trigger Police type`() {
        val result = SemanticClassifier.classify("Robbery! The thief stole my bag, police help!")
        assertEquals(EmergencyType.Police, result.type)
    }

    @Test
    fun `accident keywords trigger Accident type`() {
        val result = SemanticClassifier.classify("Car crash on the highway, driver unconscious")
        assertEquals(EmergencyType.Accident, result.type)
    }

    @Test
    fun `flood keywords trigger NaturalDisaster type`() {
        val result = SemanticClassifier.classify("The flood is rising, we are trapped!")
        assertEquals(EmergencyType.NaturalDisaster, result.type)
    }

    @Test
    fun `mental health keywords trigger MentalHealth type`() {
        val result = SemanticClassifier.classify("I don't want to live anymore, I am going to kill myself")
        assertEquals(EmergencyType.MentalHealth, result.type)
    }

    @Test
    fun `empty string returns None`() {
        val result = SemanticClassifier.classify("")
        assertEquals(EmergencyType.None, result.type)
        assertEquals(0f, result.confidence)
        assertTrue(result.matchedKeywords.isEmpty())
    }

    @Test
    fun `neutral sentence returns None`() {
        val result = SemanticClassifier.classify("Good morning, how are you today?")
        assertEquals(EmergencyType.None, result.type)
    }

    @Test
    fun `matched keywords are present in result`() {
        val result = SemanticClassifier.classify("I need an ambulance fast, someone is bleeding")
        assertTrue(result.matchedKeywords.isNotEmpty())
        assertTrue(result.matchedKeywords.any { it.contains("ambulance") || it.contains("bleeding") })
    }

    @Test
    fun `confidence is between 0 and 1`() {
        val inputs = listOf(
            "fire smoke flame",
            "hospital ambulance doctor",
            "random text with no emergency"
        )
        inputs.forEach { text ->
            val result = SemanticClassifier.classify(text)
            assertTrue("Confidence out of range for: $text", result.confidence in 0f..1f)
        }
    }
}
