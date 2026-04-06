package com.example.solify.domain.utils

import at.favre.lib.crypto.bcrypt.BCrypt

fun verifyPassword(password: String, hash: String): Boolean {
    return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}