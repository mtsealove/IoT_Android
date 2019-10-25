package com.mtsealove.github.iot.Database;

public class RequestAddress {
    String driver_id, address;

    public RequestAddress(String driver_id, String address) {
        this.driver_id = driver_id;
        this.address = address;
    }

    public RequestAddress() {
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
