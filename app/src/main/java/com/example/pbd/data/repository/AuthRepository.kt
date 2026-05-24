package com.example.pbd.data.repository

import com.example.pbd.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getUserProfile(userId: String): Result<User>
    fun getCurrentUserId(): String?
    fun logout()
    suspend fun updateUserProfile(user: User): Result<Unit>
}

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User is null after login")
            
            // Fetch their full profile from Firestore
            val userProfileResult = getUserProfile(userId)
            if (userProfileResult.isSuccess) {
                userProfileResult
            } else {
                throw Exception("Failed to fetch user profile")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            // 1. Create the user in Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User is null after registration")
            
            // 2. Create the User mapping for Firestore
            val newUser = User(
                id = userId,
                name = name,
                email = email,
                baseCurrency = "LKR",
                totalBalanceLKR = 0.0
            )

            // 3. Save to Firestore "users" collection
            usersCollection.document(userId).set(newUser).await()

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("User is null after Google sign-in")
            val userId = firebaseUser.uid
            val userDocument = usersCollection.document(userId).get().await()

            val user = if (userDocument.exists()) {
                val existingUser = userDocument.toObject(User::class.java) ?: User(id = userId)
                existingUser.copy(
                    id = userId,
                    name = existingUser.name.ifBlank { firebaseUser.displayName.orEmpty() },
                    email = existingUser.email.ifBlank { firebaseUser.email.orEmpty() }
                )
            } else {
                User(
                    id = userId,
                    name = firebaseUser.displayName ?: "Google User",
                    email = firebaseUser.email.orEmpty(),
                    baseCurrency = "LKR",
                    totalBalanceLKR = 0.0
                )
            }

            usersCollection.document(userId).set(user, SetOptions.merge()).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java) 
                ?: throw Exception("User data not found in Firestore")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun logout() {
        auth.signOut()
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
