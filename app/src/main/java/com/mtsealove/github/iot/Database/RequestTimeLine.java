package com.mtsealove.github.iot.Database;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RequestTimeLine {
    String InvoiceNum, Location, WorkDate, WorkTime;
    int WorkId;
    SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat TimeFormat = new SimpleDateFormat("HH:mm");

    public RequestTimeLine(String invoiceNum, String location, int workid) {
        Date date = new Date(System.currentTimeMillis());
        InvoiceNum = invoiceNum;
        Location = location;
        this.WorkId = workid;
        WorkDate = DateFormat.format(date);
        WorkTime = TimeFormat.format(date);
    }

    public String getInvoiceNum() {
        return InvoiceNum;
    }

    public void setInvoiceNum(String invoiceNum) {
        InvoiceNum = invoiceNum;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getWorkDate() {
        return WorkDate;
    }

    public void setWorkDate(String workDate) {
        WorkDate = workDate;
    }

    public String getWorkTime() {
        return WorkTime;
    }

    public void setWorkTime(String workTime) {
        WorkTime = workTime;
    }

    public int getWorkId() {
        return WorkId;
    }

    public void setWorkId(int workId) {
        WorkId = workId;
    }
}
