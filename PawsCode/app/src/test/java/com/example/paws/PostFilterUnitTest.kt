package com.example.paws

import com.example.paws.ui.screens.home.PuppyPost
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PostFilterUnitTest {

    private lateinit var samplePosts: List<PuppyPost>

    @Before
    fun setUp() {
        samplePosts = listOf(
            PuppyPost(id = "1", name = "Milo", type = "Dog", gender = "male", age = "6 months", userType = "Private User"),
            PuppyPost(id = "2", name = "Bella", type = "Cat", gender = "female", age = "1 years", userType = "Animal Shelter"),
            PuppyPost(id = "3", name = "Charlie", type = "Dog", gender = "male", age = "1 years", userType = "Animal Shelter"),
            PuppyPost(id = "4", name = "Titi", type = "Bird", gender = "female", age = "6 months", userType = "Private User")
        )
    }

    @Test
    fun filterByAnimalType_filtersDogsCorrectly() {
        val filtered = samplePosts.filter { it.type == "Dog" }
        assertEquals(2, filtered.size)
        assertEquals("Milo", filtered[0].name)
        assertEquals("Charlie", filtered[1].name)
    }

    @Test
    fun filterByGender_filtersFemaleCorrectly() {
        val filtered = samplePosts.filter { it.gender == "female" }
        assertEquals(2, filtered.size)
        assertEquals("Bella", filtered[0].name)
        assertEquals("Titi", filtered[1].name)
    }

    @Test
    fun filterByUserType_filtersAnimalShelterCorrectly() {
        val filtered = samplePosts.filter { it.userType == "Animal Shelter" }
        assertEquals(2, filtered.size)
        assertEquals("Bella", filtered[0].name)
        assertEquals("Charlie", filtered[1].name)
    }

    @Test
    fun filterByMultipleCriteria_combinesCorrectly() {
        val filtered = samplePosts.filter { it.type == "Dog" && it.userType == "Animal Shelter" }
        assertEquals(1, filtered.size)
        assertEquals("Charlie", filtered[0].name)
    }
}
