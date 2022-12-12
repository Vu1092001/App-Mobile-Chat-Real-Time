package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.NhomChat;
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

public class TaoNhom extends AppCompatActivity {

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
    String mySdt;
    String myName;
    ListView lsNguoiDung; //bạn bè hay ko bạn bè đều có thể tạo nhóm
    TextView txtThanhVien;
    EditText edtTenNhom, edtTimKiem;
    ImageView btnXacNhan, btnBack;
    List<User> lsThanhVien = new ArrayList<>();
    String tenThanhVien = "";
    List<User> lsUser = new ArrayList<>();
    int slBanBe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_nhom);

        Intent intent = getIntent();
        mySdt = intent.getStringExtra("sdt");
        myName = intent.getStringExtra("myName");
        mSocket.connect();
        mSocket.emit("GetLsBanBe", mySdt);
        mSocket.on("lsBanBe", kQTimBanBe);
        mSocket.on("slBanbe",getSlBanBe);
        txtThanhVien = findViewById(R.id.txtThanhVien_TNM);
        edtTenNhom = findViewById(R.id.edtTenNhom_TNM);
        edtTimKiem = findViewById(R.id.edtTimKiem_TNM);
        btnXacNhan = findViewById(R.id.btnXacNhanThemNhom_TNM);
        btnBack = findViewById(R.id.btnBack_TNM);

        lsNguoiDung = findViewById(R.id.lvDanhSachBanBe_CT);
        lsThanhVien.add(new User(mySdt,"",myName));
        lsNguoiDung.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = lsUser.get(position);
                user.setAvatar(null);
                lsThanhVien.add(user);
                tenThanhVien += lsUser.get(position).getName();
                txtThanhVien.setText(tenThanhVien);
                lsUser.remove(position);
                Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(TaoNhom.this, lsUser);
                lsNguoiDung.setAdapter(adapder_lsMess_trangChinh);

                if(lsThanhVien.size() >=3)
                    btnXacNhan.setVisibility(View.VISIBLE);
            }
        });
        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tenNhom = edtTenNhom.getText().toString();
                if(tenNhom.equals("")){
                    tenNhom = myName+", "+lsThanhVien.get(1).getName()+", "+lsThanhVien.get(2).getName();
                }
                NhomChat nhomChat = new NhomChat(tenNhom,mySdt,new ArrayList<>(),lsThanhVien);
                Gson gson = new Gson();
                Log.d("", "onClick: " + nhomChat);
                mSocket.emit("TaoPhongChat", gson.toJson(nhomChat));
                try {
                    Thread.sleep(1000);
                    Intent intent = new Intent(TaoNhom.this,PhongChat.class);
                    intent.putExtra("friend_sdt", "ChatNhom");
                    intent.putExtra("profile_sdt",mySdt);
                    intent.putExtra("name", tenNhom);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaoNhom.this, GiaoDienChinh.class);
                intent.putExtra("sdt",mySdt);
                startActivity(intent);
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
                        Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(TaoNhom.this, lsUser);
                        lsNguoiDung.setAdapter(adapder_lsMess_trangChinh);
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
}