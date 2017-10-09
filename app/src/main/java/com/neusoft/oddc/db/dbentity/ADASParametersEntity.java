package com.neusoft.oddc.db.dbentity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class ADASParametersEntity {

    @Id(autoincrement = true)
    private Long id;

    private String key_user;
    private String gps_frequency;
    private int ld_sensor_sensitivity;
    private int fwd_col_sensitivity;
    private String accelerometer_sensitivity;
    private int vehicle_length;
    private int vehicle_width;
    private int vehicle_height;
    private String camera_fov;
    private String wheelbase;
    private String curb_weight;
    private int camera_ht_from_ground;
    private int camera_offset_from_center;
    private int camera_dist_from_front;
    @Generated(hash = 279479782)
    public ADASParametersEntity(Long id, String key_user, String gps_frequency,
            int ld_sensor_sensitivity, int fwd_col_sensitivity,
            String accelerometer_sensitivity, int vehicle_length, int vehicle_width,
            int vehicle_height, String camera_fov, String wheelbase,
            String curb_weight, int camera_ht_from_ground,
            int camera_offset_from_center, int camera_dist_from_front) {
        this.id = id;
        this.key_user = key_user;
        this.gps_frequency = gps_frequency;
        this.ld_sensor_sensitivity = ld_sensor_sensitivity;
        this.fwd_col_sensitivity = fwd_col_sensitivity;
        this.accelerometer_sensitivity = accelerometer_sensitivity;
        this.vehicle_length = vehicle_length;
        this.vehicle_width = vehicle_width;
        this.vehicle_height = vehicle_height;
        this.camera_fov = camera_fov;
        this.wheelbase = wheelbase;
        this.curb_weight = curb_weight;
        this.camera_ht_from_ground = camera_ht_from_ground;
        this.camera_offset_from_center = camera_offset_from_center;
        this.camera_dist_from_front = camera_dist_from_front;
    }
    @Generated(hash = 1619007450)
    public ADASParametersEntity() {
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
    public String getGps_frequency() {
        return this.gps_frequency;
    }
    public void setGps_frequency(String gps_frequency) {
        this.gps_frequency = gps_frequency;
    }
    public int getLd_sensor_sensitivity() {
        return this.ld_sensor_sensitivity;
    }
    public void setLd_sensor_sensitivity(int ld_sensor_sensitivity) {
        this.ld_sensor_sensitivity = ld_sensor_sensitivity;
    }
    public int getFwd_col_sensitivity() {
        return this.fwd_col_sensitivity;
    }
    public void setFwd_col_sensitivity(int fwd_col_sensitivity) {
        this.fwd_col_sensitivity = fwd_col_sensitivity;
    }
    public String getAccelerometer_sensitivity() {
        return this.accelerometer_sensitivity;
    }
    public void setAccelerometer_sensitivity(String accelerometer_sensitivity) {
        this.accelerometer_sensitivity = accelerometer_sensitivity;
    }
    public int getVehicle_length() {
        return this.vehicle_length;
    }
    public void setVehicle_length(int vehicle_length) {
        this.vehicle_length = vehicle_length;
    }
    public int getVehicle_width() {
        return this.vehicle_width;
    }
    public void setVehicle_width(int vehicle_width) {
        this.vehicle_width = vehicle_width;
    }
    public int getVehicle_height() {
        return this.vehicle_height;
    }
    public void setVehicle_height(int vehicle_height) {
        this.vehicle_height = vehicle_height;
    }
    public String getCamera_fov() {
        return this.camera_fov;
    }
    public void setCamera_fov(String camera_fov) {
        this.camera_fov = camera_fov;
    }
    public String getWheelbase() {
        return this.wheelbase;
    }
    public void setWheelbase(String wheelbase) {
        this.wheelbase = wheelbase;
    }
    public String getCurb_weight() {
        return this.curb_weight;
    }
    public void setCurb_weight(String curb_weight) {
        this.curb_weight = curb_weight;
    }
    public int getCamera_ht_from_ground() {
        return this.camera_ht_from_ground;
    }
    public void setCamera_ht_from_ground(int camera_ht_from_ground) {
        this.camera_ht_from_ground = camera_ht_from_ground;
    }
    public int getCamera_offset_from_center() {
        return this.camera_offset_from_center;
    }
    public void setCamera_offset_from_center(int camera_offset_from_center) {
        this.camera_offset_from_center = camera_offset_from_center;
    }
    public int getCamera_dist_from_front() {
        return this.camera_dist_from_front;
    }
    public void setCamera_dist_from_front(int camera_dist_from_front) {
        this.camera_dist_from_front = camera_dist_from_front;
    }



}
