package com.mtsealove.github.iot.Database;

public class RequestDriverStatus {
    String driver_id;
    int status;

    public RequestDriverStatus(String driver_id, int status) {
        this.driver_id = driver_id;
        this.status = status;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
