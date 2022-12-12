package com.example.link.Entity;

import android.graphics.Bitmap;

import java.util.List;

public class NhomChat {
    private String tenNhom;
    private String nhomTruong;
    private List<TinNhan> lsTinNhan;
    private List<User> lsUser;
    private Bitmap avatarNhom;

    public NhomChat() {
    }

    public NhomChat(String tenNhom, String nhomTruong, List<TinNhan> lsTinNhan, List<User> lsUser) {
        this.tenNhom = tenNhom;
        this.nhomTruong = nhomTruong;
        this.lsTinNhan = lsTinNhan;
        this.lsUser = lsUser;
    }

    public Bitmap getAvatarNhom() {
        return avatarNhom;
    }

    public void setAvatarNhom(Bitmap avatarNhom) {
        this.avatarNhom = avatarNhom;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getNhomTruong() {
        return nhomTruong;
    }

    public void setNhomTruong(String nhomTruong) {
        this.nhomTruong = nhomTruong;
    }

    public List<TinNhan> getLsTinNhan() {
        return lsTinNhan;
    }

    public void setLsTinNhan(List<TinNhan> lsTinNhan) {
        this.lsTinNhan = lsTinNhan;
    }

    public List<User> getLsUser() {
        return lsUser;
    }

    public void setLsUser(List<User> lsUser) {
        this.lsUser = lsUser;
    }

    @Override
    public String toString() {
        return "NhomChat{" +
                "tenNhom='" + tenNhom + '\'' +
                ", nhomTruong='" + nhomTruong + '\'' +
                ", lsTinNhan=" + lsTinNhan +
                ", lsUser=" + lsUser +
                '}';
    }
}
