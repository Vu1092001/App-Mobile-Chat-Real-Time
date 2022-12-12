package com.example.link.Entity;

import android.graphics.Bitmap;

import java.time.LocalDateTime;
import java.util.List;

public class TinNhan {
    private String nguoiGui;
    private String nguoiNhan;
    private String noiDung;
    private String thoiGianGui;
    private String loai;
    private List<Bitmap> hinhAnh;
    private List<String> nameImage;
    private int viTriPhanHoi;
    private int slImage;
    private int stt;
    private byte [] voice;

    public TinNhan() {
    }

    public TinNhan(String nguoiGui, String nguoiNhan, String noiDung, String thoiGianGui, String loai) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.thoiGianGui = thoiGianGui;
        this.loai = loai;
    }

    public TinNhan(String nguoiGui, String nguoiNhan, String noiDung, String loai, List<Bitmap> hinhAnh) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.loai = loai;
        this.hinhAnh = hinhAnh;
    }

    public TinNhan(String nguoiGui, String nguoiNhan) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
    }

    public TinNhan(String nguoiGui, String nguoiNhan, String noiDung, String loai) {
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.loai = loai;
    }

    public byte[] getVoice() {
        return voice;
    }

    public void setVoice(byte[] voice) {
        this.voice = voice;
    }

    public int getViTriPhanHoi() {
        return viTriPhanHoi;
    }

    public void setViTriPhanHoi(int viTriPhanHoi) {
        this.viTriPhanHoi = viTriPhanHoi;
    }

    public List<String> getNameImage() {
        return nameImage;
    }

    public void setNameImage(List<String> nameImage) {
        this.nameImage = nameImage;
    }

    public int getSlImage() {
        return slImage;
    }

    public void setSlImage(int slImage) {
        this.slImage = slImage;
    }

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public List<Bitmap> getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(List<Bitmap> hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getNguoiGui() {
        return nguoiGui;
    }

    public void setNguoiGui(String nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public String getNguoiNhan() {
        return nguoiNhan;
    }

    public void setNguoiNhan(String nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(String thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    @Override
    public String toString() {
        return "TinNhan{" +
                "nguoiGui='" + nguoiGui + '\'' +
                ", nguoiNhan='" + nguoiNhan + '\'' +
                ", noiDung='" + noiDung + '\'' +
                ", thoiGianGui='" + thoiGianGui + '\'' +
                ", loai='" + loai + '\'' +
                ", hinhAnh=" + hinhAnh +
                ", nameImage=" + nameImage +
                ", slImage=" + slImage +
                ", stt=" + stt +
                '}';
    }
}
