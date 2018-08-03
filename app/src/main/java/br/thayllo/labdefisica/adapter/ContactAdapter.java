package br.thayllo.labdefisica.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.model.User;

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
            TextView nomeContato = (TextView) view.findViewById(R.id.contactNameTextView);
            TextView emailContato = (TextView) view.findViewById(R.id.contactEmailTextView);

            User contato = contatos.get( position );
            nomeContato.setText( contato.getName() );
            emailContato.setText( contato.getEmail() );
        }

        return view;
    }
}
