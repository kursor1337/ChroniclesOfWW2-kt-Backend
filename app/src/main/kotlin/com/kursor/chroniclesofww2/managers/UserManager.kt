package com.kursor.chroniclesofww2.managers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kursor.chroniclesofww2.Variables.JWT_SECRET
import com.kursor.chroniclesofww2.entities.User
import com.kursor.chroniclesofww2.features.*
import com.kursor.chroniclesofww2.features.LoginErrorMessages.INCORRECT_PASSWORD
import com.kursor.chroniclesofww2.features.LoginErrorMessages.NO_SUCH_USER
import com.kursor.chroniclesofww2.features.RegisterErrorMessages.USER_ALREADY_REGISTERED
import com.kursor.chroniclesofww2.repositories.UserRepository
import org.mindrot.jbcrypt.BCrypt

class UserManager(val userRepository: UserRepository) {

    suspend fun getAllUsers(): List<User> {
        return userRepository.getAllUsers()
    }

    suspend fun getUserByLogin(login: String): User? = userRepository.getUserByLogin(login)

    suspend fun loginUser(loginReceiveDTO: LoginReceiveDTO): LoginResponseDTO {
        val user = userRepository.getUserByLogin(loginReceiveDTO.login)
            ?: return LoginResponseDTO(
                token = null,
                errorMessage = NO_SUCH_USER
            )

        if (!BCrypt.checkpw(loginReceiveDTO.password, user.passwordHash)) {
            return LoginResponseDTO(
                token = null,
                errorMessage = INCORRECT_PASSWORD
            )
        }
        val token = JWT.create()
            .withClaim("login", user.login)
            .sign(Algorithm.HMAC256(System.getenv("JWT_SECRET")))

        return LoginResponseDTO(token = token)
    }

    suspend fun updateUserInfo(updateUserInfoReceiveDTO: UpdateUserInfoReceiveDTO): Boolean {
        val token = updateUserInfoReceiveDTO.token
        val newUserInfo = updateUserInfoReceiveDTO.updatedUserInfo
        val login = JWT.decode(token).getClaim("login").asString()
        val user = userRepository.getUserByLogin(login) ?: return false
        userRepository.updateUser(User(login, newUserInfo.username, user.passwordHash))
        return true
    }

    suspend fun changePasswordForUser(changePasswordReceiveDTO: ChangePasswordReceiveDTO): Boolean {
        val token = changePasswordReceiveDTO.token
        val newPassword = changePasswordReceiveDTO.newPassword
        val login = JWT.decode(token).getClaim("login").asString()
        val user = userRepository.getUserByLogin(login) ?: return false
        userRepository.updateUser(User(login, user.username, BCrypt.hashpw(newPassword, BCrypt.gensalt())))
        return true
    }

    suspend fun registerUser(registerReceiveDTO: RegisterReceiveDTO): RegisterResponseDTO {
        if (userRepository.getUserByLogin(registerReceiveDTO.login) != null) return RegisterResponseDTO(
            token = null,
            errorMessage = USER_ALREADY_REGISTERED
        )
        val passwordHash = BCrypt.hashpw(registerReceiveDTO.password, BCrypt.gensalt())
        val user = User(registerReceiveDTO.login, registerReceiveDTO.username, passwordHash)
        userRepository.saveUser(user)
        val token = JWT.create()
            .withClaim("login", user.login)
            .sign(Algorithm.HMAC256(JWT_SECRET))
        return RegisterResponseDTO(token = token)
    }
}