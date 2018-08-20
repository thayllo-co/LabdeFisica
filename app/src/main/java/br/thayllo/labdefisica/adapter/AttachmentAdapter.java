package br.thayllo.labdefisica.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.model.Attachment;
import br.thayllo.labdefisica.model.User;

public class AttachmentAdapter extends ArrayAdapter<Attachment>{

    private ArrayList<Attachment> attachments;
    private Activity context;

    public AttachmentAdapter(Activity c, ArrayList<Attachment> objects) {
        super(c, 0, objects);
        this.attachments = objects;
        this.context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if( convertView == null ){
            convertView = ( (Activity) getContext()).getLayoutInflater().
                    inflate(R.layout.item_attachment, parent, false);
        }

        // recupera elemento para exibição
        final ImageView photoImageView =  convertView.findViewById(R.id.photoImageView);
        final TextView messageTextView =  convertView.findViewById(R.id.messageTextView);
        TextView authorTextView =  convertView.findViewById(R.id.sentByTextView);
        final ProgressBar downloadImageProgressBar = convertView.findViewById(R.id.downloadImageProgressBar);

        Attachment attach = getItem( position );

        boolean isPhoto = attach.getPhotoUrl() != null;
        if (isPhoto) {
            downloadImageProgressBar.setVisibility(View.VISIBLE);
            messageTextView.setVisibility(View.GONE);
            Picasso.get()
                    .load(attach.getPhotoUrl())
                    .into(photoImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            photoImageView.setVisibility(View.VISIBLE);
                            downloadImageProgressBar.setVisibility(View.GONE);
                        }
                        @Override
                        public void onError(Exception e) {
                            photoImageView.setVisibility(View.GONE);
                            downloadImageProgressBar.setVisibility(View.GONE);
                            messageTextView.setVisibility(View.VISIBLE);
                            messageTextView.setText(R.string.upload_error_tip);
                            messageTextView.setTextColor(Color.RED);
                            //Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            downloadImageProgressBar.setVisibility(View.GONE);
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(attach.getText());
        }
        authorTextView.setText(attach.getName());

        return convertView;

    }
}

