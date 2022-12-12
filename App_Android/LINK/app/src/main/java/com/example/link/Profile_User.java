package com.example.link;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.link.Adapter.Adapder_lsMess_TrangChinh;
import com.example.link.Entity.IPAddress;
import com.example.link.Entity.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Profile_User extends AppCompatActivity {
    IPAddress ip = new IPAddress();
    String IP = ip.getIp();
    private Socket mSocket;
    ImageView btnChuphinh,imgAvatar,imgBack;
    EditText edt_sdt,edt_name;
    Button btnUpdate;
    String sdt,name;

    private final  int CHUP_HINH = 123;
    private final  int CHON_HINH = 321;
    {
        try {
            //InetAddress ip4 = InetAddress.getLocalHost();
            mSocket = IO.socket("http://"+IP+":3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        Intent intent = getIntent();
        sdt = intent.getStringExtra("sdt");
        edt_sdt = findViewById(R.id.edt_sdt_profile);
        edt_name = findViewById(R.id.edt_name_profile);
        mSocket.connect();
        mSocket.emit("getThongTinUser",sdt);
        mSocket.on("profile",getProfileUser);
        edt_sdt.setText(sdt);
        imgAvatar = findViewById(R.id.imgAvata_TTV);
        imgBack = findViewById(R.id.back_profile);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile_User.this,GiaoDienChinh.class);
                intent.putExtra("sdt",sdt);
                finish();
                startActivity(intent);

            }
        });
        btnUpdate = findViewById(R.id.update_profile);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = edt_name.getText().toString();
                mSocket.emit("updateProfile",name,sdt);
                imgBack.callOnClick();
            }
        });
        btnChuphinh = findViewById(R.id.btnChupHinhAvatar);
        btnChuphinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if(ContextCompat.checkSelfPermission(Profile_User.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Profile_User.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
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
                        edt_name.setText(name);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                        imgAvatar.setImageBitmap(bitmap);
                    }else {
                        edt_name.setText(name);
                    }
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Uri uriImage = data.getData();
            InputStream is = null;
            is = getContentResolver().openInputStream(uriImage);
            Bitmap bm = BitmapFactory.decodeStream(is);
            byte[] bt = getByteArrFromBitmap(bm);
            mSocket.emit("saveImgProfile",bt,sdt);
            imgAvatar.setImageBitmap(bm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private byte[] getByteArrFromBitmap(Bitmap bm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}