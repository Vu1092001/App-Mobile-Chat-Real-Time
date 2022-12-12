package com.example.link.Entity;

public class OTP {
    private String sdt;
    private int code;

    public OTP(String sdt, int code) {
        this.sdt = sdt;
        this.code = code;
    }

    public OTP() {
    }

    @Override
    public String toString() {
        return "OTP{" +
                "sdt='" + sdt + '\'' +
                ", code=" + code +
                '}';
    }
}
