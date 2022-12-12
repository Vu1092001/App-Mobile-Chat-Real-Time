package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.link.Entity.IPAddress;
import com.example.link.Entity.User;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;


import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Button btnDangNhap, btnDangKy, btnQuenMK;
    private EditText edtTaiKhoan, edtMatKhau;
    private TextView txtThongBao;

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

    private String tk = "0355034827";
    private String mk = "nam";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtThongBao = findViewById(R.id.txtThongBao);

        btnDangNhap = findViewById(R.id.btnDangNhap);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnQuenMK = findViewById(R.id.btnQuenMK);

        edtTaiKhoan = findViewById(R.id.edtTaiKhoan);
        edtMatKhau = findViewById(R.id.edtMatKhau);

        edtTaiKhoan.setText(tk);
        edtMatKhau.setText(mk);

        mSocket.connect();

        mSocket.on("ketQua",kQuaDN);

        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkThongTinDN()){
                    Gson gson = new Gson();
                    User user = new User(edtTaiKhoan.getText().toString(), edtMatKhau.getText().toString());
                    mSocket.emit("DangNhap", gson.toJson(user));
                    tk = edtMatKhau.getText().toString();
                    mk = edtMatKhau.getText().toString();
                }else{
                    txtThongBao.setVisibility(View.VISIBLE);
                    txtThongBao.setText("Sai tài khoản hoặc mật khẩu");
                }
            }
        });
        btnDangKy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Registed.class);
                startActivity(intent);
            }
        });
        btnQuenMK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,QuenMk.class);
                startActivity(intent);
            }
        });
    }
    private final Emitter.Listener kQuaDN = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String kq = null;
                    try {
                        kq = data.getString("noiDung");
                        if(kq.equals("true")){
                          Intent intent = new Intent(MainActivity.this, GiaoDienChinh.class);
                          intent.putExtra("sdt",edtTaiKhoan.getText().toString());
                          startActivity(intent);
                        }else{
                            txtThongBao.setVisibility(View.VISIBLE);
                            txtThongBao.setText("Sai tài khoản hoặc mật khẩu");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private boolean checkThongTinDN(){
        boolean kq = true;
        if(edtTaiKhoan.getText().toString().equals("") || edtMatKhau.getText().toString().equals("") || edtTaiKhoan.length() != 10 ){
            kq = false;
        }
        return kq;
    }

}