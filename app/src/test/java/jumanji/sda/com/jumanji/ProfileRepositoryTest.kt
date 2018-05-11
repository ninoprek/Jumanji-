package jumanji.sda.com.jumanji

import org.junit.After
import org.junit.Before
import org.junit.Test

class ProfileRepositoryTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }


    @Test
    fun saveToDatabese() {

        val profileRepository: ProfileRepository = ProfileRepository()

        val testUserProfile: UserProfile = UserProfile("Aaron", "aaron@gmai.com", "www.picture.com")

        profileRepository.storeToDatabase(testUserProfile)

    }
}