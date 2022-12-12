package com.example.link.Adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.link.Entity.TinNhan;
import com.example.link.PhongChat;
import com.example.link.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Adapter_lsNoiDungTinNhan extends BaseAdapter {
    List<TinNhan> lsTinNhan;
    String nguoiGui;
    ExpandableHeightGridView lsImage, lsImagePhanHoi;
    LinearLayout linearLayout, lnPhanHoi;
    TextView txtTinNhanPhanHoi, txtPhanHoiToi, txtPlayVoice, txtVoicePhanHoi;
    ListView lv;
    int viTriPhanHoi;
    int index;
    int statusVoice = 0;
    int voiceIndex;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public Adapter_lsNoiDungTinNhan(List<TinNhan> lsTinNhan, String nguoiGui) {
        this.lsTinNhan = lsTinNhan;
        this.nguoiGui = nguoiGui;
    }

    public int getViTriPhanHoi() {
        return viTriPhanHoi;
    }

    @Override
    public int getCount() {
        return lsTinNhan.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View viewTinNhan;
        if (view == null){
            viewTinNhan = View.inflate(viewGroup.getContext(),R.layout.row_tinnhan,null);
        }else{
            viewTinNhan = view;
        }
        TextView txtChuyenTiep = viewTinNhan.findViewById(R.id.txtChuyenTiep);
        TextView txtTinNhan = viewTinNhan.findViewById(R.id.txtNoiDung);
        linearLayout = viewTinNhan.findViewById(R.id.linearMess);
        lnPhanHoi = viewTinNhan.findViewById(R.id.lnPhanHoi);
        lsImage = viewTinNhan.findViewById(R.id.lsImage);
        lsImagePhanHoi = viewTinNhan.findViewById(R.id.lsImage_PhanHoi);
        txtTinNhanPhanHoi = viewTinNhan.findViewById(R.id.txtPhanHoi);
        txtPhanHoiToi = viewTinNhan.findViewById(R.id.txtPhanHoiCua);
        lv = viewTinNhan.findViewById(R.id.lsNoiDungTinNhan);
        txtPlayVoice = viewTinNhan.findViewById(R.id.txtVoice);
        txtVoicePhanHoi = viewTinNhan.findViewById(R.id.txtVoicePhanHoi);

        txtChuyenTiep.setVisibility(View.GONE);
        txtTinNhan.setVisibility(View.GONE);
        lsImage.setVisibility(View.GONE);
        txtTinNhanPhanHoi.setVisibility(View.GONE);
        lnPhanHoi.setVisibility(View.GONE);
        txtPlayVoice.setVisibility(View.GONE);
        txtVoicePhanHoi.setVisibility(View.GONE);

        //Chuyển tiếp
        if(lsTinNhan.get(i).getLoai().contains("chuyenTiep")){
            txtChuyenTiep.setVisibility(View.VISIBLE);
        }
        //Phản hồi tin nhắn
        index = lsTinNhan.get(i).getViTriPhanHoi();
        if(index != -1){
            txtPhanHoiToi.setVisibility(View.VISIBLE);
            if(lsTinNhan.get(index).getNguoiGui().equals(nguoiGui)){
                txtPhanHoiToi.setText("<-phản hồi chính mình");
            }else
                txtPhanHoiToi.setText("<-phản hồi " + lsTinNhan.get(index).getNguoiGui());
           if(lsTinNhan.get(index).getLoai().contains("lsImage")){
               lnPhanHoi.setVisibility(View.VISIBLE);
               List<Bitmap> lsBitmap = lsTinNhan.get(index).getHinhAnh();
               int col = 3;
               if(lsBitmap.size() < col)
                   col = lsBitmap.size();
               lsImagePhanHoi.setNumColumns(col);
               lsImagePhanHoi.setPadding(50,50,50,50);
               lsImagePhanHoi.setAdapter(new Adaper_lsImage_GridView_lsNoiDungTinNhan(lsBitmap));
               lsImagePhanHoi.setExpanded(true);
           }else if (lsTinNhan.get(index).getLoai().contains("Image")){
               lnPhanHoi.setVisibility(View.VISIBLE);
               List<Bitmap> lsBitmap = lsTinNhan.get(index).getHinhAnh();
               Log.d("", "getView: " + lsBitmap);
               int col = 3;
               if(lsBitmap.size() < col)
                   col = lsBitmap.size();
               lsImagePhanHoi.setNumColumns(col);
               lsImagePhanHoi.setPadding(50,50,50,50);
               lsImagePhanHoi.setAdapter(new Adaper_lsImage_GridView_lsNoiDungTinNhan(lsBitmap));
               lsImagePhanHoi.setExpanded(true);
           }else if(lsTinNhan.get(index).getLoai().contains("Voice")){
               txtVoicePhanHoi.setVisibility(View.VISIBLE);
           }
           else{
               txtTinNhanPhanHoi.setVisibility(View.VISIBLE);
               index = lsTinNhan.get(i).getViTriPhanHoi();
               txtTinNhanPhanHoi.setText(lsTinNhan.get(index).getNoiDung());
           }
        }
        //hiển thị tin nhắn
        if(lsTinNhan.get(i) != null){
            if (lsTinNhan.get(i).getNguoiGui().equals(nguoiGui)){
                linearLayout.setGravity(Gravity.RIGHT);
                txtChuyenTiep.setText("->Bạn đã chuyển tiếp tin nhắn");
                if(lsTinNhan.get(i).getLoai().equals("TinNhanGo")){
                    txtTinNhan.setVisibility(View.VISIBLE);
                    txtTinNhan.setText("Đoạn tin đã bị gỡ");
                    txtTinNhan.setTextColor(Color.WHITE);
                }else if(lsTinNhan.get(i).getLoai().contains("Image")){
                    lsImage.setVisibility(View.VISIBLE);
                    List<Bitmap> lsBitmap = lsTinNhan.get(i).getHinhAnh();
                    int col = 3;
                    if(lsBitmap.size() < col)
                        col = lsBitmap.size();
                    lsImage.setNumColumns(col);
                    lsImage.setAdapter(new Adaper_lsImage_GridView_lsNoiDungTinNhan(lsBitmap));
                    lsImage.setExpanded(true);
                }else if(lsTinNhan.get(i).getLoai().contains("Voice")){
                    txtPlayVoice.setVisibility(View.VISIBLE);
                }else{
                    txtTinNhan.setVisibility(View.VISIBLE);
                    txtTinNhan.setText(lsTinNhan.get(i).getNoiDung());
                }
            }else{
                linearLayout.setGravity(Gravity.LEFT);
                txtChuyenTiep.setText("->Tin Nhắn chuyển tiếp");
                if(lsTinNhan.get(i).getLoai().equals("TinNhanGo")){
                    txtTinNhan.setVisibility(View.VISIBLE);
                    txtTinNhan.setText("Đoạn tin đã bị gỡ");
                    txtTinNhan.setTextColor(Color.WHITE);
                }else if(lsTinNhan.get(i).getLoai().contains("Image")){
                    lsImage.setVisibility(View.VISIBLE);
                    List<Bitmap> lsBitmap = lsTinNhan.get(i).getHinhAnh();
                    int col = 3;
                    if(lsBitmap.size() < col)
                        col = lsBitmap.size();
                    lsImage.setNumColumns(col);
                    lsImage.setAdapter(new Adaper_lsImage_GridView_lsNoiDungTinNhan(lsBitmap));
                    lsImage.setExpanded(true);
                }else if(lsTinNhan.get(i).getLoai().contains("Voice")){
                    txtPlayVoice.setVisibility(View.VISIBLE);
                }else{
                    txtTinNhan.setVisibility(View.VISIBLE);
                    txtTinNhan.setText(lsTinNhan.get(i).getNoiDung());
                }
            }
            lsImage.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    return false;
                }
            });
            txtTinNhan.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            txtPlayVoice.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    statusVoice = 0;
                    Toast.makeText(viewTinNhan.getContext(), "Stop playing", Toast.LENGTH_SHORT).show();
                }
            });
            txtPlayVoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(statusVoice == 0){
                        Toast.makeText(viewTinNhan.getContext(), "Start playing", Toast.LENGTH_SHORT).show();
                        playMp3(lsTinNhan.get(i).getVoice());
                        statusVoice = 1;
                        voiceIndex = i;
                    }else {
                        Toast.makeText(viewTinNhan.getContext(), "Stop playing", Toast.LENGTH_SHORT).show();
                        if(i == voiceIndex){
                            mediaPlayer.stop();
                            statusVoice = 0;
                        }else{
                            Toast.makeText(viewTinNhan.getContext(), "Start playing", Toast.LENGTH_SHORT).show();
                            mediaPlayer.stop();
                            playMp3(lsTinNhan.get(i).getVoice());
                            statusVoice = 1;
                            voiceIndex = i;
                        }
                    }
                }
            });
        }
        return viewTinNhan;
    }

    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3");
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
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
}
