package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Adapter.Adapter_lsNoiDungTinNhan;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.NhomChat;
import com.example.link.Entity.TinNhan;
import com.example.link.Entity.User;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChuyenTiepTinNhan extends AppCompatActivity {
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
    String nguoiGui, nguoiNhan, noiDung, thoiGianGui, loai;
    int slImage;
    byte[] image;
    TextView txtToi;
    ListView lvNguoiDung;
    Button btnXacNhan;
    ImageView btnBack;
    List<User> lsUser = new ArrayList<>();
    List<String> lsNguoiNhan = new ArrayList<>();
    int slBanBe,slNhom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chuyen_tiep_tin_nhan);

        Intent intent = getIntent();
        nguoiGui = intent.getStringExtra("nguoiGui");
        nguoiNhan = intent.getStringExtra("nguoiNhan");
        noiDung = intent.getStringExtra("noiDung");
        thoiGianGui = "";
        loai = intent.getStringExtra("loai");
        slImage = intent.getIntExtra("slImage",0);

        TinNhan tinNhan = new TinNhan(nguoiGui, nguoiNhan, noiDung, thoiGianGui, loai);
        tinNhan.setSlImage(slImage);
        tinNhan.setViTriPhanHoi(-1);

        txtToi = findViewById(R.id.txtToi);
        lvNguoiDung = findViewById(R.id.lvDanhSachBanBe_CT);
        btnXacNhan = findViewById(R.id.btnXacNhan_CT);
        btnBack = findViewById(R.id.btnBack_CT);

        mSocket.connect();

        mSocket.emit("GetLsBanBe", nguoiGui);
        mSocket.on("lsBanBe", kQTimBanBe);
        mSocket.on("slBanbe",getSlBanBe);
        mSocket.on("slNhom",getSlNhom);
        mSocket.on("lsNhom", KqTimNhom);
        lvNguoiDung.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nguoiNhan = lsUser.get(position).getSdt();
                if(nguoiNhan.equals("ChatNhom")){
                    nguoiNhan = "NhomChat" + lsUser.get(position).getName();
                }
                txtToi.setText("Tới : " + nguoiNhan);
                lsNguoiNhan.add(nguoiNhan);
            }
        });
        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loai.contains("chuyenTiep")){
                    loai = loai.replace("chuyenTiep","");
                }
                tinNhan.setLoai("chuyenTiep" + loai);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                tinNhan.setThoiGianGui(currentDateandTime);
                Gson gson = new Gson();
                if(loai.contains("lsImage")){
                    List<String> lsNameImage = intent.getStringArrayListExtra("lsNameImage");
                    for (String i : lsNguoiNhan) {
                        tinNhan.setNguoiNhan(i);
                        for (String j : lsNameImage) {
                            tinNhan.setNoiDung(j);
                            mSocket.emit("SendMess", gson.toJson(tinNhan));
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else if(loai.contains("Image") || loai.contains("Voice")){
                    for (String i : lsNguoiNhan) {
                        tinNhan.setNguoiNhan(i);
                        mSocket.emit("SendMess", gson.toJson(tinNhan));
                    }
                }else{
                    for (String i : lsNguoiNhan) {
                        tinNhan.setNguoiNhan(i);
                        mSocket.emit("SendMess", gson.toJson(tinNhan));
                    }
                }

                finish();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private final Emitter.Listener kQTimBanBe = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    User user = new User(data.optString("sdt"),
                            data.optString("matKhau"),
                            data.optString("name"));
                    String avatar_S = data.optString("avatar");
                    if(!avatar_S.equals("")){
                        byte[] avatar_B = (byte[]) args[1];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar_B, 0, avatar_B.length);
                        user.setAvatar(bitmap);
                    }else{
                        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
                        user.setAvatar(bitmap1);
                    }
                    lsUser.add(user);
                    if(lsUser.size() == slBanBe){
                        Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(ChuyenTiepTinNhan.this, lsUser);
                        lvNguoiDung.setAdapter(adapder_lsMess_trangChinh);
                    }
                }
            });
        }
    };
    private final Emitter.Listener getSlBanBe = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    slBanBe = (int) args[0];
                }
            });
        }
    };
    private final Emitter.Listener KqTimNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    String tenNhom = data.optString("tenNhom");
                    String avatar_s = data.optString("avatar");
                    String nhomTruong = data.optString("nhomTruong");

                    User user = new User("ChatNhom","",tenNhom);
                    if(!avatar_s.equals("")){
                        byte[] avatar_B = (byte[]) args[1];
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar_B, 0, avatar_B.length);
                        user.setAvatar(bitmap);

                    }else{
                        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
                        user.setAvatar(bitmap1);
                    }

                    lsUser.add(user);
                    if(lsUser.size() == slBanBe + slNhom){
                        Log.d("", "run: " + lsUser);
                        Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(ChuyenTiepTinNhan.this, lsUser);
                        lvNguoiDung.setAdapter(adapder_lsMess_trangChinh);
                    }
                }
            });
        }
    };
    private final Emitter.Listener getSlNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    slNhom = (int) args[0];
                }
            });
        }
    };
}