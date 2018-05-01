package jumanji.sda.com.jumanji

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test

class UserProfileRepositoryTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }


    @Test
    fun saveToDatabese() {

        val userProfileRepository: UserProfileRepository = UserProfileRepository()

        val testUserProfile: UserProfile = UserProfile("Aaron", "aaron@gmai.com", "www.picture.com")

        userProfileRepository.storeToDatabase(testUserProfile)

    }
}