package com.neusoft.oddc.db.dbentity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class UserProfileEntity {

    @Id(autoincrement = true)
    private Long id;

    private String key_user;
    private String username;
    private String firstname;
    private String lastname;
    private String phoneno;
    private String email;
    private String driverlicenseno;

    public UserProfileEntity(String key_user, String username, String firstname, String lastname, String phoneno, String email, String driverlicenseno) {
        this.id = null;
        this.key_user = key_user;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneno = phoneno;
        this.email = email;
        this.driverlicenseno = driverlicenseno;
    }

    @Generated(hash = 1844059019)
    public UserProfileEntity(Long id, String key_user, String username, String firstname, String lastname, String phoneno, String email,
                             String driverlicenseno) {
        this.id = id;
        this.key_user = key_user;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phoneno = phoneno;
        this.email = email;
        this.driverlicenseno = driverlicenseno;
    }

    @Generated(hash = 1557014447)
    public UserProfileEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey_user() {
        return this.key_user;
    }

    public void setKey_user(String key_user) {
        this.key_user = key_user;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhoneno() {
        return this.phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDriverlicenseno() {
        return this.driverlicenseno;
    }

    public void setDriverlicenseno(String driverlicenseno) {
        this.driverlicenseno = driverlicenseno;
    }

}
