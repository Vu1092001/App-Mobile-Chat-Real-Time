package com.example.link.Entity;

import android.graphics.Bitmap;

import java.util.List;

public class User {
    private String sdt;
    private String matKhau;
    private String name;
    private List<NhomChat> lsNhomChat;
    private Bitmap avatar;

    public User() {
    }

    public User(String sdt, String matKhau, String name) {
        this.sdt = sdt;
        this.matKhau = matKhau;
        this.name = name;
    }

    public User(String sdt, String matKhau) {
        this.sdt = sdt;
        this.matKhau = matKhau;
    }

    public User(String sdt) {
        this.sdt = sdt;
    }

    public List<NhomChat> getLsNhomChat() {
        return lsNhomChat;
    }

    public void setLsNhomChat(List<NhomChat> lsNhomChat) {
        this.lsNhomChat = lsNhomChat;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public User(String sdt, String matKhau, String name, Bitmap avatar) {
        this.sdt = sdt;
        this.matKhau = matKhau;
        this.name = name;
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "sdt='" + sdt + '\'' +
                ", matKhau='" + matKhau + '\'' +
                ", name='" + name + '\'' +
                ", lsNhomChat=" + lsNhomChat +
                ", avatar=" + avatar +
                '}';
    }
}