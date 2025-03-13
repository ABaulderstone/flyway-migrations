package com.alex.flyway_migrations.user.entities;

import com.alex.flyway_migrations.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    public enum Role {
        ADMIN,
        COACH,
        SUPPORT,
        STUDENT,
        CANDIDATE
    }

    @Column(unique = true)
    private String email;
    @Column()
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private String location;

    @Column
    private String test;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public User() {

    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
