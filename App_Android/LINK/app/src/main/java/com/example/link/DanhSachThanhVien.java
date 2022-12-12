package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.User;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DanhSachThanhVien extends AppCompatActivity {
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
    ImageView btnBack;
    ListView lvThanhVien;
    List<User> lsTv = new ArrayList<>();
    String mySDT, tenNhom, nhomTruong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_thanh_vien);

        Intent intent = getIntent();
        mySDT = intent.getStringExtra("sdt");
        tenNhom = intent.getStringExtra("tenNhom");
        nhomTruong = intent.getStringExtra("nhomTruong");
        mSocket.connect();
        //lấy danh sách user
        mSocket.emit("joinNhom", tenNhom + "");
        mSocket.on("ThongTinNhom",thongTinNhom);

        btnBack = findViewById(R.id.btnBack_DSTV);
        lvThanhVien = findViewById(R.id.lvDSTV);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DanhSachThanhVien.this,PhongChat.class);
                intent.putExtra("friend_sdt", "ChatNhom");
                intent.putExtra("profile_sdt",mySDT);
                intent.putExtra("name", tenNhom);
                startActivity(intent);
            }
        });
    }

    private final Emitter.Listener thongTinNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray data = (JSONArray) args[1];
                    int n = data.length();
                    for (int i = 0; i < n; i++){
                        User user = null;
                        try {
                            String name = data.getJSONObject(i).optString("name");
                            if(data.getJSONObject(i).optString("sdt").equals(nhomTruong)){
                                name += " (Trưởng Nhóm)";
                            }
                            user = new User(data.getJSONObject(i).optString("sdt"),
                                    data.getJSONObject(i).optString("matKhau"),
                                    name);
                            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
                            user.setAvatar(bitmap1);
                            lsTv.add(user);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mSocket.emit("GetLsBanBe", mySDT);
                    Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(DanhSachThanhVien.this, lsTv);
                    lvThanhVien.setAdapter(adapder_lsMess_trangChinh);
                }
            });
        }
    };
}