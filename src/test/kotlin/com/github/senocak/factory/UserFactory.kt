package com.github.senocak.factory

import com.github.senocak.TestConstants.USER_EMAIL
import com.github.senocak.TestConstants.USER_NAME
import com.github.senocak.TestConstants.USER_PASSWORD
import com.github.senocak.TestConstants.USER_USERNAME
import com.github.senocak.domain.Role
import com.github.senocak.domain.User
import com.github.senocak.util.RoleName
import java.util.HashSet

object UserFactory {

    /**
     * Creates a new user with the given name, username, email, password and roles.
     * @return the new user
     */
    fun createUser(): User {
        val user = User(USER_NAME, USER_USERNAME, USER_EMAIL, USER_PASSWORD)
        val USER_ROLES: MutableSet<Role> = HashSet<Role>()
        USER_ROLES.add(createRole(RoleName.ROLE_USER))
        USER_ROLES.add(createRole(RoleName.ROLE_ADMIN))
        user.roles = USER_ROLES
        return user
    }

    /**
     * Creates a new role with the given name.
     * @param roleName the name of the role
     * @return the new role
     */
    fun createRole(roleName: RoleName?): Role {
        val role = Role()
        role.name = roleName
        return role
    }
}
