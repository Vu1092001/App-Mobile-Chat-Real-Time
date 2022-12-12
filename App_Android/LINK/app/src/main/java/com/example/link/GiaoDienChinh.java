package com.example.link;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.User;
import com.example.link.Entity.*;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GiaoDienChinh extends AppCompatActivity {

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
    TextView txtMyProfile;
    ImageView btnMenu,imgAvata_TTV;
    EditText edtTimBanBe;
    ListView lsTinNhan;
    List<User> lsUser = new ArrayList<>();
    String mySDT;
    String myName;
    int slBanBe;
    int slNhom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giao_dien_chinh);

        Intent intent = getIntent();
        mySDT = intent.getStringExtra("sdt");
        mSocket.connect();
        //lấy danh sách user
        mSocket.emit("GetLsBanBe", mySDT);
        mSocket.on("lsBanBe", kQTimBanBe);
        mSocket.emit("ImOnline", mySDT);
        mSocket.on("VaoNhom", vaoNhom);
        mSocket.emit("getThongTinUser",mySDT);
        mSocket.on("profile",getProfileUser);
        //số lượng bạn bè
        mSocket.on("slBanbe",getSlBanBe);
        //Số lượng nhóm
        mSocket.on("slNhom",getSlNhom);
        //kết quả tìm nhóm
        mSocket.on("lsNhom", KqTimNhom);

        btnMenu = findViewById(R.id.btnMenu);
        lsTinNhan = findViewById(R.id.lsTinNhan);
        imgAvata_TTV = findViewById(R.id.imgAvatar_main);

        imgAvata_TTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(GiaoDienChinh.this, Profile_User.class);
                intent1.putExtra("sdt", mySDT);
                startActivity(intent1);
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(GiaoDienChinh.this, v);
                pm.getMenuInflater().inflate(R.menu.menu, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.toString().equals("Tạo nhóm")){
                            Intent intent = new Intent(GiaoDienChinh.this,TaoNhom.class);
                            intent.putExtra("sdt",mySDT);
                            intent.putExtra("myName",myName);
                            startActivity(intent);
                            finish();
                        }
                        if (item.toString().equals("Đăng xuất")){
                            Intent intent = new Intent(GiaoDienChinh.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
        lsTinNhan.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = lsUser.get(i);
                Intent intent = new Intent(GiaoDienChinh.this,PhongChat.class);
                intent.putExtra("friend_sdt", user.getSdt());
                intent.putExtra("profile_sdt",mySDT);
                intent.putExtra("name", user.getName());
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
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
                        Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(GiaoDienChinh.this, lsUser);
                        lsTinNhan.setAdapter(adapder_lsMess_trangChinh);
                    }
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
                        Adapder_lsMess_TrangChinh adapder_lsMess_trangChinh = new Adapder_lsMess_TrangChinh(GiaoDienChinh.this, lsUser);
                        lsTinNhan.setAdapter(adapder_lsMess_trangChinh);
                    }
                }
            });
        }
    };
    private final Emitter.Listener vaoNhom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lsUser.clear();
                    mSocket.emit("GetLsBanBe", mySDT);
                    Toast.makeText(GiaoDienChinh.this, "bạn đã đc thêm vào nhóm " + args[0], Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
    private final Emitter.Listener getProfileUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    String name = jsonObject.optString("name");
                    String avartar = jsonObject.optString("avatar");
                    myName = name;
                    if(!avartar.equals("")){
                        byte[] avatar = (byte[]) args[1];
                        // edt_name.setText(name);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                        imgAvata_TTV.setImageBitmap(bitmap);
                    }else {
                        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.avatar);
                        imgAvata_TTV.setImageBitmap(bitmap1);
                    }
                }
            });
        }
    };
}

