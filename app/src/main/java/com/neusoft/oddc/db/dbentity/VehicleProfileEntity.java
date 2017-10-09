package com.neusoft.oddc.db.dbentity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class VehicleProfileEntity {

    @Id(autoincrement = true)
    private Long id;

    private String key_user;
    private String year;
    private String brand;
    private String model;
    private String color;
    private String mileage;
    private String vin;

    public VehicleProfileEntity(String key_user, String year, String brand, String model, String color, String mileage, String vin) {
        this.id = null;
        this.key_user = key_user;
        this.year = year;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.mileage = mileage;
        this.vin = vin;
    }

    @Generated(hash = 1226256535)
    public VehicleProfileEntity(Long id, String key_user, String year, String brand, String model, String color, String mileage,
                                String vin) {
        this.id = id;
        this.key_user = key_user;
        this.year = year;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.mileage = mileage;
        this.vin = vin;
    }

    @Generated(hash = 1280611590)
    public VehicleProfileEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey_user() {
        return key_user;
    }

    public void setKey_user(String key_user) {
        this.key_user = key_user;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }
}
