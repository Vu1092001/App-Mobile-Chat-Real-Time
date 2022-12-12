package com.example.link.Adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.graphics.BitmapKt;

import com.example.link.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.BitSet;
import java.util.List;

public class Adaper_lsImage_GridView_lsNoiDungTinNhan extends BaseAdapter {
    List<Bitmap> lsBitMap;
    LinearLayout cha_image;

    public Adaper_lsImage_GridView_lsNoiDungTinNhan(List<Bitmap> lsBitMap) {
        this.lsBitMap = lsBitMap;
    }

    @Override
    public int getCount() {
        return lsBitMap.size();

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View viewImage;
        if (view == null){
            viewImage = View.inflate(parent.getContext(), R.layout.row_list_image,null);
        }else{
            viewImage = view;
        }
        ImageView imageView = viewImage.findViewById(R.id.image_GridView);
        Bitmap bitmap = lsBitMap.get(position);
        float t = 1;
        if(lsBitMap.size() == 1){
            t = t * 2;
        }
        int h = (int) (bitmap.getHeight() * t);
        int w = (int) (bitmap.getWidth() * t);
        h = pxToDp(h);
        w = pxToDp(w);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, w, h, false));

        return viewImage;
    }
    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
