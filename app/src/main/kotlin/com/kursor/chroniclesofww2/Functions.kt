package com.kursor.chroniclesofww2

import com.kursor.chroniclesofww2.entities.User
import com.kursor.chroniclesofww2.features.UserInfo

fun User.userInfo(): UserInfo = UserInfo(username)