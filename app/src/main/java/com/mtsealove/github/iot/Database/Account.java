package com.mtsealove.github.iot.Database;

import java.io.Serializable;

//기사 정보 클래스
public class Account implements Serializable {
    String ID, DriverName, DriverPhone, CourierID, Status;

    public Account(String ID, String driverName, String driverPhone, String courierID, String status) {
        this.ID = ID;
        DriverName = driverName;
        DriverPhone = driverPhone;
        CourierID = courierID;
        Status = status;
    }

    public Account() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDriverName() {
        return DriverName;
    }

    public void setDriverName(String driverName) {
        DriverName = driverName;
    }

    public String getDriverPhone() {
        return DriverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        DriverPhone = driverPhone;
    }

    public String getCourierID() {
        return CourierID;
    }

    public void setCourierID(String courierID) {
        CourierID = courierID;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "ID='" + ID + '\'' +
                ", DriverName='" + DriverName + '\'' +
                ", DriverPhone='" + DriverPhone + '\'' +
                ", CourierID='" + CourierID + '\'' +
                ", Status='" + Status + '\'' +
                '}';
    }
}
