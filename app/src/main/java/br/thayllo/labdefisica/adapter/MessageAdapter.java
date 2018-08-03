package br.thayllo.labdefisica.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.model.Message;
import br.thayllo.labdefisica.settings.Preferences;

public class MessageAdapter extends ArrayAdapter<Message> {

    private Context context;
    private ArrayList<Message> mensagens;

    public MessageAdapter(Context c, ArrayList<Message> objects) {
        super(c, 0, objects);
        this.context = c;
        this.mensagens = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = null;

        // Verifica se a lista está preenchida
        if( mensagens != null ){

            // Recupera dados do usuario remetente
            Preferences preferences = new Preferences(context);
            String idUsuarioRementente = preferences.getUser().getId();

            // Inicializa objeto para montagem do layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            // Recupera mensagem
            Message mensagem = mensagens.get( position );

            // Monta view a partir do xml
            if( idUsuarioRementente.equals( mensagem.getIdUsuario() )  ){
                view = inflater.inflate(R.layout.item_mensagem_direita, parent, false);
            }else {
                view = inflater.inflate(R.layout.item_mensagem_esquerda, parent, false);
            }

            // Recupera elemento para exibição
            TextView textoMensagem = (TextView) view.findViewById(R.id.menssageTextView);
            textoMensagem.setText( mensagem.getMensagem() );

        }

        return view;

    }
}
