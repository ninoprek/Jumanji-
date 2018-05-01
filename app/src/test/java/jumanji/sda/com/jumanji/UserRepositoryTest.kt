package jumanji.sda.com.jumanji

import org.junit.After
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }


    @Test
    fun saveToDatabese() {

        val userRepository: UserRepository = UserRepository()

        val testUserProfile: UserProfile = UserProfile("Aaron", "aaron@gmai.com", "www.picture.com")

        userRepository.storeToDatabase(testUserProfile)

    }
}