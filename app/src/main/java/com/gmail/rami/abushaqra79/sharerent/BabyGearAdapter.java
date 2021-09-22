package com.gmail.rami.abushaqra79.sharerent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class BabyGearAdapter extends ArrayAdapter<BabyGear> {

    public static final int GET_FROM_GALLERY = 200;
    private final Context context;
    private TextView uploadPhoto;
    private ImageView image;

    public BabyGearAdapter(@NonNull Context context, int resource, @NonNull List<BabyGear> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.baby_gear_details,
                    parent,false);
        }

        BabyGear currentBabyGear = getItem(position);

        TextView babyGearType = convertView.findViewById(R.id.product_type);
        babyGearType.setText(currentBabyGear.getBabyGearType());

        TextView babyGearDescription = convertView.findViewById(R.id.product_description);
        babyGearDescription.setText(currentBabyGear.getBabyGearDescription());

        TextView rentPrice = convertView.findViewById(R.id.rent_price);
        rentPrice.setText(currentBabyGear.getRentPrice());

        image = convertView.findViewById(R.id.product_photo);

        uploadPhoto = convertView.findViewById(R.id.upload_image);
        uploadPhoto.setText("Upload photo");

        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                ((Activity) context).startActivityForResult(intent, GET_FROM_GALLERY);
            }
        });

        return convertView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                image.setImageURI(selectedImage);
                image.setVisibility(View.VISIBLE);
                uploadPhoto.setVisibility(View.GONE);
            }
        }
    }
}
