package com.example.link;

import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class QuenMk extends AppCompatActivity {

    int code = 0;
    boolean checkSDT = true;

    Button btnLuu, btnGuiOPT, btnXacNhanOTP;

    EditText edtSdt, edtMaOTP, edtMKMoi, edtXacNhanMK;

    TextView txtGuiOTP, txtNhapSaiOTP, txtMatKhauMoi, txtTieuDe, txtThongBao;

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
        setContentView(R.layout.activity_quen_mk);

        btnLuu = findViewById(R.id.btnLuu_QMK);
        btnGuiOPT = findViewById(R.id.btnGuiOTP_QMK);
        btnXacNhanOTP = findViewById(R.id.btnXacNhanOTP_QMK2);

        edtSdt = findViewById(R.id.edtSDT_QMK);
        edtMaOTP = findViewById(R.id.edtMaOTP_QMK);
        edtMKMoi = findViewById(R.id.edtMkMoi_QMK);
        edtXacNhanMK = findViewById(R.id.edtXNMKMoi_QMK);

        txtMatKhauMoi = findViewById(R.id.txtTieuDeMKMoi_QMK);
        txtGuiOTP = findViewById(R.id.txtGuiOTP_QMK);
        txtNhapSaiOTP = findViewById(R.id.txtNhapSaiOTP_QMK);
        txtTieuDe = findViewById(R.id.txtTieuDeMKMoi_QMK);
        txtThongBao = findViewById(R.id.txtThongBao_QMK);

        mSocket.connect();
        mSocket.on("KQ", kQcheckSDT);
        mSocket.on("KetQua", KquaDoiMK);
        Gson gson = new Gson();

        btnGuiOPT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkDuLieuNhap()){
                    code = (int) Math.floor(((Math.random() * 899999) + 100000));
                    OTP otp = new OTP(edtSdt.getText().toString(), code);
                    mSocket.emit("sendOTP",gson.toJson(otp));
                }else{
                    txtThongBao.setVisibility(View.VISIBLE);
                    txtThongBao.setText("Sai số điện thoại");
                }
            }
        });
        btnXacNhanOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtMaOTP.getText().toString().equals(String.valueOf(code))){
                    txtGuiOTP.setVisibility(View.INVISIBLE);
                    txtNhapSaiOTP.setVisibility(View.INVISIBLE);
                    txtThongBao.setVisibility(View.INVISIBLE);

                    txtTieuDe.setVisibility(View.VISIBLE);
                    edtMKMoi.setVisibility(View.VISIBLE);
                    edtXacNhanMK.setVisibility(View.VISIBLE);
                    btnLuu.setVisibility(View.VISIBLE);
                }else{
                    txtThongBao.setVisibility(View.INVISIBLE);
                    txtGuiOTP.setVisibility(View.INVISIBLE);
                    txtNhapSaiOTP.setVisibility(View.VISIBLE);
                }
            }
        });
        btnLuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtMKMoi.getText().toString().equals("") || edtXacNhanMK.getText().toString().equals("")){
                    txtThongBao.setVisibility(View.VISIBLE);
                    txtThongBao.setText("Mật khẩu không được để chống");
                }else{
                    if(edtMKMoi.getText().toString().equals(edtXacNhanMK.getText().toString())){
                        User user = new User(edtSdt.getText().toString(),edtMKMoi.getText().toString());
                        mSocket.emit("QuenMK", gson.toJson(user));
                    }else{
                        txtThongBao.setVisibility(View.VISIBLE);
                        txtThongBao.setText("Mật khẩu không trùng khớp");
                    }
                }
            }
        });
    }
    private boolean checkDuLieuNhap(){
        boolean kq = true;
        if (edtSdt.getText().toString().equals("") || edtSdt.length() != 10){
            kq = false;
        }
        return kq;
    }

    private final Emitter.Listener kQcheckSDT = new Emitter.Listener() {
        @Override
        public void call(Object... data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(data[0].toString().equals("false")){
                        checkSDT = false;
                        txtThongBao.setVisibility(View.VISIBLE);
                        txtThongBao.setText("Sai số điện thoại");
                    }else{
                        txtThongBao.setVisibility(View.INVISIBLE);
                        txtGuiOTP.setVisibility(View.VISIBLE);
                    }

                }
            });
        }
    };
    private final Emitter.Listener KquaDoiMK = new Emitter.Listener() {
        @Override
        public void call(Object... data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(data[0].toString().equals("false")){
                        txtThongBao.setVisibility(View.VISIBLE);
                        txtThongBao.setText("Đổi mật khẩu không thành công");
                    }else{
                        Toast.makeText(QuenMk.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QuenMk.this, MainActivity.class);
                        startActivity(intent);
                    }

                }
            });
        }
    };
}