package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.link.Entity.IPAddress;
import com.example.link.Entity.OTP;
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

public class Registed extends AppCompatActivity {
    int code = 0;

    Button btnXacNhan, btnGuiOTP;

    EditText edtSDT, edtMk, edtHoTen, edtMaOPT;

    TextView txtNhapSaiOTP, txtGuiOPT, txtThongBao;

    IPAddress ip = new IPAddress();
    String IP = ip.getIp();

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://"+IP+":3000");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registed);

        btnXacNhan = findViewById(R.id.btnDKLuu);
        btnGuiOTP = findViewById(R.id.btnGuiOTP);

        edtHoTen = findViewById(R.id.edtDKHoTen);
        edtSDT = findViewById(R.id.edtDKSDT);
        edtMk = findViewById(R.id.edtDKMK);
        edtMaOPT = findViewById(R.id.edtMaOTP);

        txtGuiOPT = findViewById(R.id.txtGuiOTP);
        txtNhapSaiOTP = findViewById(R.id.txtNhapSaiOTP);
        txtThongBao = findViewById(R.id.txtThongBao_DK);

        mSocket.connect();

        mSocket.on("ketQua",kQuaDK);


        Gson gson = new Gson();

        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtMaOPT.getText().toString().equals(code + "")){
                    mSocket.emit("DangKy", gson.toJson(new User(edtSDT.getText().toString(),
                            edtMk.getText().toString(),
                            edtHoTen.getText().toString())));
                }else{
                    txtNhapSaiOTP.setVisibility(View.VISIBLE);
                    txtGuiOPT.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnGuiOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkDuLieuNhap()){
                    code = (int) Math.floor(((Math.random() * 899999) + 100000));
                    OTP otp = new OTP(edtSDT.getText().toString(), code);
                    mSocket.emit("sendOTP",gson.toJson(otp));
                    txtThongBao.setVisibility(View.INVISIBLE);
                    txtNhapSaiOTP.setVisibility(View.INVISIBLE);
                    txtGuiOPT.setVisibility(View.VISIBLE);
                }else{
                    txtThongBao.setVisibility(View.VISIBLE);
                    txtThongBao.setText("Kiển tra thông tin nhập");
                }
            }
        });
    }
    private boolean checkDuLieuNhap(){
        boolean kq = true;
        if (edtSDT.getText().toString().equals("") ||
        edtMk.getText().toString().equals("") ||
        edtHoTen.getText().toString().equals("") ||
        edtSDT.length() != 10){
            kq = false;
        }
        return kq;
    }
    private final Emitter.Listener kQuaDK = new Emitter.Listener() {
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
                            Intent intent = new Intent(Registed.this, GiaoDienChinh.class);
                            intent.putExtra("sdt",edtSDT.getText().toString());
                            startActivity(intent);
                        }else{
                            txtThongBao.setVisibility(View.VISIBLE);
                            txtThongBao.setText("Đăng ký không thành công");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}