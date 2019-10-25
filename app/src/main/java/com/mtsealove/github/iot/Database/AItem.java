package com.mtsealove.github.iot.Database;

public class AItem {
    String  InvoiceNum, CompanyID, StAddress, DstAddress, Status, Location, CenterPhone, Driver, ItemName, Action;

    public AItem(String invoiceNum, String companyID, String stAddress, String dstAddress, String status, String location, String centerPhone, String driver, String itemName, String action) {
        InvoiceNum = invoiceNum;
        CompanyID = companyID;
        StAddress = stAddress;
        DstAddress = dstAddress;
        Status = status;
        Location = location;
        CenterPhone = centerPhone;
        Driver = driver;
        ItemName = itemName;
        Action = action;
    }

    public AItem() {
    }

    public String getInvoiceNum() {
        return InvoiceNum;
    }

    public void setInvoiceNum(String invoiceNum) {
        InvoiceNum = invoiceNum;
    }

    public String getCompanyID() {
        return CompanyID;
    }

    public void setCompanyID(String companyID) {
        CompanyID = companyID;
    }

    public String getStAddress() {
        return StAddress;
    }

    public void setStAddress(String stAddress) {
        StAddress = stAddress;
    }

    public String getDstAddress() {
        return DstAddress;
    }

    public void setDstAddress(String dstAddress) {
        DstAddress = dstAddress;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getCenterPhone() {
        return CenterPhone;
    }

    public void setCenterPhone(String centerPhone) {
        CenterPhone = centerPhone;
    }

    public String getDriver() {
        return Driver;
    }

    public void setDriver(String driver) {
        Driver = driver;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    @Override
    public String toString() {
        return "AItem{" +
                "InvoiceNum='" + InvoiceNum + '\'' +
                ", CompanyID='" + CompanyID + '\'' +
                ", StAddress='" + StAddress + '\'' +
                ", DstAddress='" + DstAddress + '\'' +
                ", Status='" + Status + '\'' +
                ", Location='" + Location + '\'' +
                ", CenterPhone='" + CenterPhone + '\'' +
                ", Driver='" + Driver + '\'' +
                ", ItemName='" + ItemName + '\'' +
                ", Action='" + Action + '\'' +
                '}';
    }
}
