package jumanji.sda.com.jumanji

import org.junit.After
import org.junit.Before
import org.junit.Test

class RepositoryTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }


    @Test
    fun saveToDatabese() {

        val repository: Repository = Repository()

        val testUserProfile: UserProfile = UserProfile("Aaron", "aaron@gmai.com", "www.picture.com")

        repository.storeToDatabase(testUserProfile)

    }
}