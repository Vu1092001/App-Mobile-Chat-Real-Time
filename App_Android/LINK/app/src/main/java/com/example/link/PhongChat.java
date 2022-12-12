package com.example.link;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.link.Adapter.Adapter_lsNoiDungTinNhan;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.TinNhan;
import com.google.gson.Gson;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PhongChat extends AppCompatActivity {
    private final  int CHUP_HINH = 123;
    private final  int CHON_HINH = 321;
    private static final int PERMISSION_CODE = 1001;
    IPAddress ip = new IPAddress();
    String IP = ip.getIp();
    private Socket mSocket;
    {
        try {
            //InetAddress ip4 = InetAddress.getLocalHost();
            mSocket = IO.socket("http://"+IP+":3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    LinearLayout lnPhanHoi, lnGhiAm ,lnChat;
    ImageView btnGui, btnChonHinh, btnChupHinh, btnEmoji, btnBack, btnMenu, btnCancel, btnGhiAm, btnGuiVoice, btnXoaVoice, imgFr;
    ListView lsTinNhan_lV;
    TextView txtChaoMung, txtTenBanBe ,txtPhanHoiToi, txtNoiDungPhanHoi;
    EditText edtNoiDung;
    List<TinNhan> lsTinNhan_arr = new ArrayList<TinNhan>();
    List<Bitmap> lsImage_arr = new ArrayList<>();
    TinNhan tinNhanTam = new TinNhan();
    String mySDT;
    String frind_sdt;
    String nameFriend;
    String loaiTinNhanPhanHoi;
    int viTriTinNhanPhanHoi;
    int soLuongTinNhan;
    int slImageLoad;
    int slImageDem = 0;
    String nhomTruong;
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    byte [] voice;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phong_chat);
        Intent intent = getIntent();
        frind_sdt = intent.getStringExtra("friend_sdt"); // là ChatNhom nếu chat nhóm
        mySDT = intent.getStringExtra("profile_sdt");
        nameFriend = intent.getStringExtra("name"); // là tên nhóm nếu chat nhóm
        nhomTruong = intent.getStringExtra("nhomTruong");


        mSocket.connect();
        //Get Avartar ban be

        //Go tin nhắn
        mSocket.on("GoTin", Gotin);
        //Online
        mSocket.emit("ImOnline", mySDT);
        //Join vào nhóm để chát real time
        if(frind_sdt.equals("ChatNhom")){
            mSocket.emit("joinNhom", nameFriend + "");
            Gson gson = new Gson();
            TinNhan tinNhan = new TinNhan(mySDT,nameFriend);
            mSocket.emit("loadMessGroup", gson.toJson(tinNhan));
        }else{
            //gửi yêu cầu lầy toàn bộ danh sách tin nhắn đơn
            mSocket.emit("getThongTinUser",frind_sdt);
            mSocket.on("profile",getProfileUser);
            Gson gson = new Gson();
            TinNhan tinNhan = new TinNhan(mySDT,frind_sdt);
            mSocket.emit("loadMess", gson.toJson(tinNhan));
        }
        //Nhận thông tin nhóm
        mSocket.on("ThongTinNhom",thongTinNhom);
        //nhận số lượng tin nhắn
        mSocket.on("soLuongTinNhan", soLuonTin);
        //Nhận về danh sách tin nhắn
        mSocket.on("lsMess", LoadLsMess);
        //Lắng nghe khi có người nhắn tin đến
        mSocket.on("getMess",getMess);
        //kết quả rời xóa nhóm
        mSocket.on("ketQua",ketQua);

        btnBack = findViewById(R.id.btnBackChat);
        btnEmoji = findViewById(R.id.btn_emoji);
        btnChonHinh = findViewById(R.id.btnChonHinh);
        btnChupHinh = findViewById(R.id.btnChupHinh);
        btnMenu = findViewById(R.id.btnMenu_Chat);
        btnGui = findViewById(R.id.btnGuiTinNhan);
        lsTinNhan_lV = findViewById(R.id.lsNoiDungTinNhan);
        txtTenBanBe = findViewById(R.id.txtTenBanBe);
        edtNoiDung = findViewById(R.id.edtNhapTinNhan);
        lnPhanHoi = findViewById(R.id.lnPhanHoi);
        txtPhanHoiToi = findViewById(R.id.txtPhanHoi_PhongChat);
        txtNoiDungPhanHoi = findViewById(R.id.txtNoiDungPhanHoi_PhongChat);
        btnCancel = findViewById(R.id.btnCancel_PhongChat);
        btnGhiAm = findViewById(R.id.btnGhiAm);
        btnXoaVoice = findViewById(R.id.btnXoaVoice);
        btnGuiVoice = findViewById(R.id.btnGuiVoice);
        lnGhiAm = findViewById(R.id.lnGhiAm);
        lnChat = findViewById(R.id.lnChat);
        imgFr = findViewById(R.id.imgAvatar_BanBe);
        lnGhiAm.setVisibility(View.GONE);
        lnPhanHoi.setVisibility(View.GONE);

        txtTenBanBe.setText(intent.getStringExtra("name"));


        EmojiPopup popup = EmojiPopup.Builder.fromRootView(
                findViewById(R.id.root_view)
        ).build(edtNoiDung);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhongChat.this, GiaoDienChinh.class);
                intent.putExtra("sdt",mySDT);
                startActivity(intent);
            }
        });
        btnEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.toggle();
            }
        });
        btnGui.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(!edtNoiDung.getText().toString().equals("")){
                    if(frind_sdt.equals("ChatNhom")){
                        frind_sdt = "NhomChat" + nameFriend;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,edtNoiDung.getText().toString(), currentDateandTime,"text");
                    tinNhan.setViTriPhanHoi(-1);
                    edtNoiDung.getText().clear();
                    if(lnPhanHoi.getVisibility() == View.VISIBLE){
                        tinNhan.setViTriPhanHoi(viTriTinNhanPhanHoi);
                    }
                    Gson gson = new Gson();
                    mSocket.emit("SendMess", gson.toJson(tinNhan));
                    if(lnPhanHoi.getVisibility() == View.VISIBLE){
                        btnCancel.callOnClick();
                    }
                    lsTinNhan_arr.add(tinNhan);
                    Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                    lsTinNhan_lV.setAdapter(ls);
                    lsTinNhan_lV.setSelection(lsTinNhan_arr.size()-1);
                }
            }
        });
        btnChonHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if(ContextCompat.checkSelfPermission(PhongChat.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(PhongChat.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                            ,101);
                    return;
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), CHON_HINH);
                }else{
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, CHON_HINH);
                }
            }
        });
        btnChupHinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChupHinh();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtNoiDungPhanHoi.setText("");
                txtNoiDungPhanHoi.setText("");
                lnPhanHoi.setVisibility(View.INVISIBLE);
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(PhongChat.this, v);
                if(frind_sdt.equals("ChatNhom") || frind_sdt.equals("NhomChat" + nameFriend)){
                    pm.getMenuInflater().inflate(R.menu.menu_nhom_chat, pm.getMenu());
                }else{
                    pm.getMenuInflater().inflate(R.menu.menu_chat_don, pm.getMenu());
                }
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.toString()){
                            case "QL thành viên" : {
                                if(nhomTruong.equals(mySDT)){
                                    Intent intent = new Intent(PhongChat.this,QuanLyThanhVien.class);
                                    intent.putExtra("sdt",mySDT);
                                    intent.putExtra("tenNhom",nameFriend);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(PhongChat.this, "Chỉ nhóm trưởng", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case  "Đổi nhóm trưởng" : {
                                if(nhomTruong.equals(mySDT)){
                                    Intent intent = new Intent(PhongChat.this,UyQuyenNhomTruong.class);
                                    intent.putExtra("sdt",mySDT);
                                    intent.putExtra("tenNhom",nameFriend);
                                    intent.putExtra("nhomTruong",nhomTruong);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(PhongChat.this, "Chỉ nhóm trưởng", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case "Xem thành viên nhóm": {
                                Intent intent = new Intent(PhongChat.this,DanhSachThanhVien.class);
                                intent.putExtra("sdt",mySDT);
                                intent.putExtra("tenNhom",nameFriend);
                                intent.putExtra("nhomTruong",nhomTruong);
                                startActivity(intent);
                                break;
                            }
                            case "Rời nhóm": {
                                if(nhomTruong.equals(mySDT)){
                                    Toast.makeText(PhongChat.this, "Nhóm trưởng không thể rời nhóm", Toast.LENGTH_SHORT).show();
                                }else{
                                    mSocket.emit("XoaThanhVien",mySDT,nameFriend);//2nd name of group
                                    try {
                                        Thread.sleep(1000);
                                        Intent intent = new Intent(PhongChat.this, GiaoDienChinh.class);
                                        intent.putExtra("sdt",mySDT);
                                        startActivity(intent);
                                        finish();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }
                                break;
                            }
                            case "Giải tán nhóm" : {
                                if(nhomTruong.equals(mySDT)){
                                    mSocket.emit("GiaiTanNhom",nameFriend);
                                }else{
                                    Toast.makeText(PhongChat.this, "Chỉ nhóm trưởng", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                            case "Xóa đoạn chát" : {
                                mSocket.emit("XoaDoanChat",mySDT,frind_sdt);//2nd is phone number of nguoiNhan
                            }
                        }
                        return  false;
                    }
                });
                pm.show();
            }
        });
        edtNoiDung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnGhiAm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lnGhiAm.setVisibility(View.VISIBLE);
                lnChat.setVisibility(View.GONE);
                if (ActivityCompat.checkSelfPermission(PhongChat.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PhongChat.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
                } else if(ActivityCompat.checkSelfPermission(PhongChat.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(PhongChat.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else
                    start(v);
            }
        });
        btnGuiVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);
                try {
                    if(frind_sdt.equals("ChatNhom")){
                        frind_sdt = "NhomChat" + nameFriend;
                    }
                    btnXoaVoice.callOnClick();
                    Thread.sleep(1000);
                    String part = outputFile = Environment.getExternalStorageDirectory().
                            getAbsolutePath() + "/voiceMp3.3gpp";
                    byte [] voice = FileLocal_To_Byte(part);
                    Gson gson = new Gson();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,"",currentDateandTime,"Voice");
                    tinNhan.setSlImage(1);
                    tinNhan.setViTriPhanHoi(-1);
                    tinNhan.setVoice(voice);
                    if(lnPhanHoi.getVisibility() == View.VISIBLE){
                        tinNhan.setViTriPhanHoi(viTriTinNhanPhanHoi);
                    }
                    lsTinNhan_arr.add(tinNhan);
                    Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                    lsTinNhan_lV.setAdapter(ls);
                    lsTinNhan_lV.setSelection(lsTinNhan_arr.size()-1);
                    mSocket.emit("SendMess", gson.toJson(tinNhan), voice);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        btnXoaVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);
                lnGhiAm.setVisibility(View.GONE);
                lnChat.setVisibility(View.VISIBLE);
            }
        });
        lsTinNhan_lV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PopupMenu popupMenu = new PopupMenu(PhongChat.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_tin_nhan, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.toString()){
                            case "Gỡ tin nhắn" : {
                                if(frind_sdt.equals("ChatNhom")){
                                    frind_sdt = "NhomChat" + nameFriend;
                                }
                                mSocket.emit("GoTinNhan",mySDT,frind_sdt, position);
                                TinNhan tinNhan = lsTinNhan_arr.get(position);
                                tinNhan.setLoai("TinNhanGo");
                                lsTinNhan_arr.set(position,tinNhan);
                                Adapter_lsNoiDungTinNhan adapter = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                                lsTinNhan_lV.setAdapter(adapter);
                                lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                                break;
                            }
                            case "Chuyển tiếp" : {
                                Intent intent = new Intent(PhongChat.this, ChuyenTiepTinNhan.class);
                                TinNhan tinNhan = lsTinNhan_arr.get(position);
                                tinNhan.setNguoiGui(mySDT);

                                intent.putExtra("nguoiGui",tinNhan.getNguoiGui());
                                intent.putExtra("nguoiNhan", tinNhan.getNguoiNhan());
                                intent.putExtra("noiDung", tinNhan.getNoiDung());
                                intent.putExtra("loai", tinNhan.getLoai());
                                intent.putExtra("slImage", tinNhan.getSlImage());
                                if(tinNhan.getLoai().equals("lsImage") || tinNhan.getLoai().equals("chuyenTieplsImage")){
                                    intent.putStringArrayListExtra("lsNameImage", (ArrayList<String>) tinNhan.getNameImage());
                                }
                                startActivity(intent);
                                break;
                            }
                            case  "Phản hồi" : {
                                if(frind_sdt.equals("ChatNhom")){
                                    frind_sdt = "NhomChat" + nameFriend;
                                }
                                TinNhan tinNhan = lsTinNhan_arr.get(position);
                                viTriTinNhanPhanHoi = position;

                                lnPhanHoi.setVisibility(View.VISIBLE);
                                if(tinNhan.getNguoiGui().equals(mySDT)){
                                    txtPhanHoiToi.setText("Đang trả lời chính mình");
                                }else{
                                    txtPhanHoiToi.setText("Đang trả lời" + tinNhan.getNguoiGui());
                                }
                                txtNoiDungPhanHoi.setText(tinNhan.getNoiDung());
                                break;
                            }
                        }
                        return false;
                    }
                });
                return false;
            }
        });
        lsTinNhan_lV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(lsTinNhan_arr.get(position).getLoai().contains("Voice")){
                    playMp3(lsTinNhan_arr.get(position).getVoice());
                }
                int index = lsTinNhan_arr.get(position).getViTriPhanHoi();
                if(index != -1){
                    lsTinNhan_lV.setSelection(index);
                }
            }
        });
    }
    private final Emitter.Listener soLuonTin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    soLuongTinNhan = (int) args[0];
                    slImageLoad = (int) args[1];
                    lsImage_arr.clear();
                    lsTinNhan_arr.clear();
                }
            });
        }
    };
    private final Emitter.Listener getMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    String nguoiGui = jsonObject.optString("nguoiGui");
                    String nguoiNhan = jsonObject.optString("nguoiNhan");
                    String noiDung = jsonObject.optString("noiDung");
                    String loai = jsonObject.optString("loai");
                    int slImage = jsonObject.optInt("slImage");
                    int viTriPhanHoi = jsonObject.optInt("viTriPhanHoi");
                    String thoiGianGui = jsonObject.optString("thoiGianGui");

                    TinNhan tinNhan = new TinNhan(nguoiGui,nguoiNhan,noiDung,thoiGianGui,loai);
                    tinNhan.setSlImage(slImage);
                    tinNhan.setViTriPhanHoi(viTriPhanHoi);

                    if(tinNhan.getLoai().equals("Image") || tinNhan.getLoai().equals("chuyenTiepImage")){
                        byte[] imageUser = (byte[]) args[1];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageUser,0,imageUser.length);
                        List<Bitmap> ls = new ArrayList<>();
                        ls.add(bitmap);
                        tinNhan.setHinhAnh(ls);

                        lsTinNhan_arr.add(tinNhan);
                        Adapter_lsNoiDungTinNhan adapter = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                        lsTinNhan_lV.setAdapter(adapter);
                        lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                    }else if(tinNhan.getLoai().equals("lsImage")){
                        List<Bitmap> lsBitMap;
                        List<String> lsImageName;
                        if(tinNhanTam.getHinhAnh() != null){
                            lsBitMap = tinNhanTam.getHinhAnh();
                            lsImageName = tinNhanTam.getNameImage();
                        }else{
                            lsBitMap = new ArrayList<>();
                            lsImageName = new ArrayList<>();
                        }
                        tinNhanTam = tinNhan;
                        byte[] imageUser = (byte[]) args[1];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageUser,0,imageUser.length);
                        lsBitMap.add(bitmap);
                        lsImageName.add(noiDung);
                        tinNhanTam.setHinhAnh(lsBitMap);
                        tinNhanTam.setNameImage(lsImageName);
                        if(tinNhanTam.getHinhAnh().size() == slImage){
                            lsTinNhan_arr.add(tinNhanTam);
                            Adapter_lsNoiDungTinNhan adapter = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                            lsTinNhan_lV.setAdapter(adapter);
                            lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                            tinNhanTam = null;
                        }
                    }else if(tinNhan.getLoai().contains("Voice")){
                        byte[] voice = (byte[]) args[1];
                        tinNhan.setVoice(voice);

                        lsTinNhan_arr.add(tinNhan);
                        Adapter_lsNoiDungTinNhan adapter = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                        lsTinNhan_lV.setAdapter(adapter);
                        lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                    }
                    else{
                        lsTinNhan_arr.add(tinNhan);
                        Adapter_lsNoiDungTinNhan adapter = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                        lsTinNhan_lV.setAdapter(adapter);
                        lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                    }
                }
            });
        }
    };
    private final Emitter.Listener LoadLsMess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    String nguoiGui = jsonObject.optString("nguoiGui");
                    String nguoiNhan = jsonObject.optString("nguoiNhan");
                    String noiDung = jsonObject.optString("noiDung");
                    String loai = jsonObject.optString("loai");
                    int slImage = jsonObject.optInt("slImage");
                    int viTriPhanHoi = jsonObject.optInt("viTriPhanHoi");
                    String thoiGianGui = jsonObject.optString("thoiGianGui");

                    int index = (int) args[1];

                    TinNhan tinNhan = new TinNhan(nguoiGui,nguoiNhan,noiDung,thoiGianGui,loai);
                    tinNhan.setStt(index);
                    tinNhan.setSlImage(slImage);
                    tinNhan.setViTriPhanHoi(viTriPhanHoi);

                    if(tinNhan.getLoai().contains("lsImage")){
                        slImageDem ++;
                        byte [] imageByte = (byte[]) args[2];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                        List<Bitmap> lsBitmap_lsHinhAnh = new ArrayList<>();
                        List<String> lsNameImage = new ArrayList<>();

                        lsBitmap_lsHinhAnh.add(bitmap);
                        lsNameImage.add(noiDung);

                        tinNhan.setHinhAnh(lsBitmap_lsHinhAnh);
                        tinNhan.setNameImage(lsNameImage);
                        lsTinNhan_arr.add(tinNhan);

                        for (int i = lsTinNhan_arr.size() - 2; i >= 0; i--) {
                            if(tinNhan.getThoiGianGui().equals(lsTinNhan_arr.get(i).getThoiGianGui()) || tinNhan.getStt() == lsTinNhan_arr.get(i).getStt()){
                                lsBitmap_lsHinhAnh = lsTinNhan_arr.get(i).getHinhAnh();
                                lsNameImage = lsTinNhan_arr.get(i).getNameImage();
                                lsNameImage.add(noiDung);
                                lsBitmap_lsHinhAnh.add(bitmap);
                                tinNhan.setNameImage(lsNameImage);
                                tinNhan.setHinhAnh(lsBitmap_lsHinhAnh);
                                lsTinNhan_arr.set(i,tinNhan);
                                lsTinNhan_arr.remove(lsTinNhan_arr.size()-1);
                                break;
                            }
                        }
                    }else if(tinNhan.getLoai().contains("Image")){
                        byte [] imageByte = (byte[]) args[2];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                        List<Bitmap> lsBitmap_lsHinhAnh = new ArrayList<>();
                        lsBitmap_lsHinhAnh.add(bitmap);
                        tinNhan.setHinhAnh(lsBitmap_lsHinhAnh);
                        lsTinNhan_arr.add(tinNhan);
                    }else if(tinNhan.getLoai().contains("Voice")){
                        byte[] voice = (byte[]) args[2];
                        tinNhan.setVoice(voice);

                        lsTinNhan_arr.add(tinNhan);
                    }
                    else {
                        lsTinNhan_arr.add(tinNhan);
                    }

                    if(slImageDem == slImageLoad && lsTinNhan_arr.size() == soLuongTinNhan){
                        sapXepTinNhan();
                        Adapter_lsNoiDungTinNhan adapter = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr, mySDT);
                        lsTinNhan_lV.setAdapter(adapter);
                        lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                    }
                }
            });
        }
    };
    private final Emitter.Listener thongTinNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    nhomTruong = data.optString("nhomTruong");
                }
            });
        }
    };
    private final Emitter.Listener ketQua = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String kq = (String) args[0];
                    if(kq.equals("oke")){
                        Intent intent = new Intent(PhongChat.this, GiaoDienChinh.class);
                        intent.putExtra("sdt",mySDT);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    };
    private final Emitter.Listener Gotin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(frind_sdt.equals("ChatNhom")){
                mSocket.emit("joinNhom", nameFriend + "");
                Gson gson = new Gson();
                TinNhan tinNhan = new TinNhan(mySDT,nameFriend);
                mSocket.emit("loadMessGroup", gson.toJson(tinNhan));
            }else{
                //gửi yêu cầu lầy toàn bộ danh sách tin nhắn đơn
                Gson gson = new Gson();
                TinNhan tinNhan = new TinNhan(mySDT,frind_sdt);
                mSocket.emit("loadMess", gson.toJson(tinNhan));
            }
        }
    };
    private void sapXepTinNhan(){
        Collections.sort(lsTinNhan_arr, new Comparator<TinNhan>() {
            @Override
            public int compare(TinNhan o1, TinNhan o2) {
                return o1.getStt() - o2.getStt();
            }
        });
    }
    private void ChupHinh(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CHUP_HINH);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(frind_sdt.equals("ChatNhom")){
            frind_sdt = "NhomChat" + nameFriend;
        }//nhiều hình
        if(requestCode == CHON_HINH && resultCode == RESULT_OK){
            if(data.getClipData() != null){
                int x = data.getClipData().getItemCount();
                List<Bitmap> lsBitMap = new ArrayList<>();
                Gson gson = new Gson(); SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,"",currentDateandTime,"lsImage");
                tinNhan.setViTriPhanHoi(-1);
                if(lnPhanHoi.getVisibility() == View.VISIBLE){
                    tinNhan.setViTriPhanHoi(viTriTinNhanPhanHoi);
                }
                tinNhan.setSlImage(x);
                for (int i=0;i<x;i++){
                    try {
                        Uri uriImage = data.getClipData().getItemAt(i).getUri();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriImage);
                        byte[] bt = getByteArrFromBitmap(bitmap);
                        lsBitMap.add(bitmap);
                        mSocket.emit("SendMess", gson.toJson(tinNhan),bt);
                        Thread.sleep(1000);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                tinNhan.setHinhAnh(lsBitMap);
                lsTinNhan_arr.add(tinNhan);
                Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                lsTinNhan_lV.setAdapter(ls);
                lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
            }else{//nếu chụp 1 hình
                try {
                Uri uriImage = data.getData();
                InputStream is = getContentResolver().openInputStream(uriImage);
                Bitmap bm = BitmapFactory.decodeStream(is);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                TinNhan tinNhan = new TinNhan(mySDT,frind_sdt,"",currentDateandTime,"Image");
                tinNhan.setSlImage(1);
                tinNhan.setViTriPhanHoi(-1);
                if(lnPhanHoi.getVisibility() == View.VISIBLE){
                    tinNhan.setViTriPhanHoi(viTriTinNhanPhanHoi);
                }
                byte[] bt = getByteArrFromBitmap(bm);
                Gson gson = new Gson();
                mSocket.emit("SendMess", gson.toJson(tinNhan), bt);
                List<Bitmap> lsBitMap = new ArrayList<>();
                lsBitMap.add(bm);
                tinNhan.setHinhAnh(lsBitMap);
                lsTinNhan_arr.add(tinNhan);
                Adapter_lsNoiDungTinNhan ls = new Adapter_lsNoiDungTinNhan(lsTinNhan_arr,mySDT);
                lsTinNhan_lV.setAdapter(ls);
                lsTinNhan_lV.setSelection(lsTinNhan_arr.size() -1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public byte[] getByteArrFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public void start(View view){
        try {
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voiceMp3.3gpp";
            myRecorder = new MediaRecorder();
//            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            myRecorder.setOutputFile(outputFile);
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Start recording...",
                Toast.LENGTH_SHORT).show();
    }

    public void stop(View view){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;
            Toast.makeText(getApplicationContext(), "Stop recording...",
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            // resetting mediaplayer instance to evade problems
            mediaPlayer.reset();

            // In case you run into issues with threading consider new instance like:
            // MediaPlayer mediaPlayer = new MediaPlayer();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());
            Toast.makeText(getApplicationContext(), "Start play the recording...",Toast.LENGTH_SHORT).show();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

    public byte[] FileLocal_To_Byte(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }
    private final Emitter.Listener getProfileUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    String name = jsonObject.optString("name");
                    String avartar = jsonObject.optString("avatar");
                    if(!avartar.equals("")){
                        byte[] avatar = (byte[]) args[1];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                        imgFr.setImageBitmap(bitmap);
                    }else {
                        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
                        imgFr.setImageBitmap(bitmap1);
                    }
                }
            });
        }
    };
}