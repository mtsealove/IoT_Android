package com.mtsealove.github.iot.Database;

import java.io.Serializable;
import java.util.ArrayList;


public class AItemList implements Serializable {
    String Result;
    ArrayList<AItem> data;
    int Status;

    public AItemList(String result, ArrayList<AItem> data, int status) {
        Result = result;
        this.data = data;
        Status = status;
    }

    public AItemList() {
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public ArrayList<AItem> getData() {
        return data;
    }

    public void setData(ArrayList<AItem> data) {
        this.data = data;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @Override
    public String toString() {
        return "AItemList{" +
                "Result='" + Result + '\'' +
                ", data=" + data +
                ", Status=" + Status +
                '}';
    }
}
