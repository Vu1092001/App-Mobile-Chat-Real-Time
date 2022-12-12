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

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.User;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class QuanLyThanhVien extends AppCompatActivity {
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

    ListView lvUser, lvTvNhom;
    Button btnXacNhan;
    ImageView btnBack;
    List<User> lsUser = new ArrayList<>();
    List<User> lsTv = new ArrayList<>();
    int slBanBe;
    String mySDT, tenNhom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_thanh_vien);

        Intent intent = getIntent();
        mySDT = intent.getStringExtra("sdt");
        tenNhom = intent.getStringExtra("tenNhom");

        mSocket.connect();
        //lấy danh sách user
        mSocket.on("lsBanBe", kQTimBanBe);
        mSocket.on("slBanbe",getSlBanBe);
        mSocket.emit("joinNhom", tenNhom + "");
        mSocket.on("ThongTinNhom",thongTinNhom);

        lvUser = findViewById(R.id.lvUser_QLTV);
        lvTvNhom = findViewById(R.id.lvTvNhom_QLTV);
        btnXacNhan = findViewById(R.id.btnXacNhan_QLTV);
        btnBack = findViewById(R.id.btnBack_QLTV);
        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuanLyThanhVien.this,PhongChat.class);
                intent.putExtra("friend_sdt", "ChatNhom");
                intent.putExtra("profile_sdt",mySDT);
                intent.putExtra("name", tenNhom);
                startActivity(intent);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuanLyThanhVien.this,PhongChat.class);
                intent.putExtra("friend_sdt", "ChatNhom");
                intent.putExtra("profile_sdt",mySDT);
                intent.putExtra("name", tenNhom);
                finish();
                startActivity(intent);
            }
        });
        lvTvNhom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = lsTv.get(position);
                mSocket.emit("XoaThanhVien",user.getSdt(),tenNhom);
                lsUser.add(lsTv.get(position));
                lsTv.remove(position);
                Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(QuanLyThanhVien.this, lsTv);
                lvTvNhom.setAdapter(adapder_lsMess_trangChinh);
                Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh2 = new Adapder_lsMess_TrangChinh(QuanLyThanhVien.this, lsUser);
                lvUser.setAdapter(adapder_lsMess_trangChinh2);
            }
        });
        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Gson gson = new Gson();
                User user = lsUser.get(position);
                user.setAvatar(null);
                mSocket.emit("ThemThanhVien",gson.toJson(user),tenNhom);
                lsTv.add(lsUser.get(position));
                lsUser.remove(position);
                Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(QuanLyThanhVien.this, lsTv);
                lvTvNhom.setAdapter(adapder_lsMess_trangChinh);
                Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh2 = new Adapder_lsMess_TrangChinh(QuanLyThanhVien.this, lsUser);
                lvUser.setAdapter(adapder_lsMess_trangChinh2);
            }
        });
    }
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
                        Log.d("1", "run: " + lsUser);
                        for(int i = 0; i<lsTv.size(); i++){
                            for(int j=0;j<lsUser.size();j++){
                                if (lsUser.get(j).getSdt().equals(lsTv.get(i).getSdt())){
                                    lsUser.remove(j);
                                    break;
                                }
                            }
                        }
                        Log.d("2", "run: " + lsUser);
                        Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(QuanLyThanhVien.this, lsUser);
                        lvUser.setAdapter(adapder_lsMess_trangChinh);
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
                    JSONArray data = (JSONArray) args[1];
                    int n = data.length();
                    for (int i = 0; i < n; i++){
                        User user = null;
                        try {
                            user = new User(data.getJSONObject(i).optString("sdt"),
                                    data.getJSONObject(i).optString("matKhau"),
                                    data.getJSONObject(i).optString("name"));
                                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
                                user.setAvatar(bitmap1);
                            if(!user.getSdt().equals(mySDT)){
                                lsTv.add(user);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mSocket.emit("GetLsBanBe", mySDT);
                    Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(QuanLyThanhVien.this, lsTv);
                    lvTvNhom.setAdapter(adapder_lsMess_trangChinh);
                }
            });
        }
    };
}