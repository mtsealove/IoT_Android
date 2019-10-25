package com.mtsealove.github.iot.Database;

public class RequestAitemStatus {
    String invoice;
    int status;

    public RequestAitemStatus(String invoice, int status) {
        this.invoice = invoice;
        this.status = status;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
