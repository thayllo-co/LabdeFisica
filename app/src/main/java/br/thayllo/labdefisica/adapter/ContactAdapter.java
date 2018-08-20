package br.thayllo.labdefisica.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.model.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends ArrayAdapter<User> {

    private ArrayList<User> contatos;
    private Activity context;

    public ContactAdapter(Activity c, ArrayList<User> objects) {
        super(c, 0, objects);
        this.contatos = objects;
        this.context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        if( contatos != null ){
            view = context.getLayoutInflater().inflate( R.layout.item_contato , parent, false);

            // recupera elemento para exibição
            TextView nomeContato = view.findViewById(R.id.contactNameTextView);
            TextView emailContato = view.findViewById(R.id.contactEmailTextView);
            final CircleImageView profilePicCircleImageView = view.findViewById(R.id.profilePicCircleImageView);
            final ProgressBar progressBarProfilePic = view.findViewById(R.id.progressBarProfilePic);

            User contato = contatos.get(position);
            nomeContato.setText(contato.getName());
            emailContato.setText(contato.getEmail());
            if (contato.getPhotoUrl() != null) {
                progressBarProfilePic.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(contato.getPhotoUrl())
                        .into(profilePicCircleImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBarProfilePic.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                progressBarProfilePic.setVisibility(View.GONE);
                                profilePicCircleImageView.setImageResource(R.drawable.profile_person);
                            }
                        });
            }
        }

        return view;
    }
}
