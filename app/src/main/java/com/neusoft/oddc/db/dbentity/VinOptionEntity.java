package com.neusoft.oddc.db.dbentity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class VinOptionEntity {

    @Id(autoincrement = true)
    private Long id;
    private String key_user;
    private int vinOption = 0;

    public VinOptionEntity() {
    }

    @Generated(hash = 1449768754)
    public VinOptionEntity(Long id, String key_user, int vinOption) {
        this.id = id;
        this.key_user = key_user;
        this.vinOption = vinOption;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey_user()
    {
        return key_user;
    }

    public void setKey_user(String key_user)
    {
        this.key_user = key_user;
    }

    public int getVinOption()
    {
        return vinOption;
    }

    public void setVinOption(int vinOption)
    {
        this.vinOption = vinOption;
    }
}
