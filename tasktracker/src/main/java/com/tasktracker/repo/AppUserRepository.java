package com.tasktracker.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasktracker.model.AppUser;

//in this there is no password checking just the username is provided from the db the find my username is used in userdetailservixce 
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);// it loads users for login which is then used in next stepm of
                                                      // secutity config

    boolean existsByUsername(String username);// check if the username aready taken

    boolean existsByEmail(String email);// prevent duplicate email registrations

    Optional<AppUser> findByEmail(String email);
}
// 1. Extending JpaRepository<AppUser, Long>
// AppUser → your Entity class (represents a table in the database).

// Long → the type of the primary key (id field).

// By extending JpaRepository, you automatically get CRUD methods like:

// save(AppUser user) → insert or update user.

// findById(Long id) → fetch by primary key.

// findAll() → get all users.

// deleteById(Long id) → delete a user.