package com.mtsealove.github.iot.Database;

public class StatusMap {
    public static String GetStatus(int status) {
        switch (status) {
            case 1:
                return "집하처리";
            case 2:
                return "화물 상차";
            case 3:
                return "화물 하차";
            case 4:
                return "배송 출발";
            case 5:
                return "배송 완료";
            default:
                return "배송 준비중";
        }
    }
}
