package com.example.link.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.link.Entity.User;
import com.example.link.R;

import java.util.List;

public class Adapder_lsMess_TrangChinh extends ArrayAdapter<User> {
    private Context context;
    private LayoutInflater inflater;
    private List<User> lsUser;

    public Adapder_lsMess_TrangChinh(@NonNull Context context, List<User> lsUser) {
        super(context,0, lsUser);
        this.context = context;
        this.lsUser = lsUser;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.row_lsmess_trangchinh, parent, false);
        }
        User user = lsUser.get(position);

        TextView txtName = convertView.findViewById(R.id.txtName);
        ImageView imgAvatar = convertView.findViewById(R.id.imgAvata_TTV);

        txtName.setText(user.getName());
        imgAvatar.setImageBitmap(lsUser.get(position).getAvatar());

        return convertView;
    }
}
