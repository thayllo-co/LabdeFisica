package br.thayllo.labdefisica.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.adapter.MessageAdapter;
import br.thayllo.labdefisica.model.AttachmentType;
import br.thayllo.labdefisica.model.Message;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

public class ChatUI extends AppCompatActivity {

    private User contatoUser;
    private User currentUser;
    private TextView info;

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btMensagem;
    private DatabaseReference firebase;
    private ListView listView;
    private ArrayList<Message> mensagens;
    private ArrayAdapter<Message> adapter;
    private ValueEventListener valueEventListenerMensagem;

    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ui);

        toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        editMensagem = findViewById(R.id.menssageEditText);
        btMensagem = findViewById(R.id.sendImageButton);
        listView = findViewById(R.id.chatListView);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        preferences = new Preferences(ChatUI.this);
        currentUser = preferences.getUser();

        contatoUser = new User();
        Bundle extra = getIntent().getExtras();
        if( extra != null){
            contatoUser.setName((String) extra.get("nome"));
            contatoUser.setEmail((String) extra.get("email"));
            contatoUser.setId((String) extra.get("id"));
        }

        toolbar.setTitle(contatoUser.getName());

        // Monta listview e adapter
        mensagens = new ArrayList<>();
        adapter = new MessageAdapter(ChatUI.this, mensagens);
        listView.setAdapter( adapter );

        // Recuperar mensagens do Firebase
        firebase = FirebasePreferences.getDatabaseReference()
                .child("messages")
                .child( currentUser.getId() )
                .child( contatoUser.getId() );

        // Cria listener para mensagens
        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Limpar mensagens
                mensagens.clear();

                // Recupera mensagens
                for ( DataSnapshot dados: dataSnapshot.getChildren() ){
                    Message mensagem = dados.getValue( Message.class );
                    mensagens.add( mensagem );
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        firebase.addValueEventListener( valueEventListenerMensagem );

        // Enviar mensagem
        btMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoMensagem = editMensagem.getText().toString();

                if( textoMensagem.isEmpty() ){
                    Toast.makeText(ChatUI.this, "Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
                }else{

                    Message mensagem = new Message();
                    mensagem.setIdUsuario( currentUser.getId() );
                    mensagem.setMensagem( textoMensagem );

                    // salvamos mensagem para o remetente
                    Boolean retornoMensagemRemetente = salvarMensagem(currentUser.getId(), contatoUser.getId() , mensagem );
                    if( !retornoMensagemRemetente ){
                        Toast.makeText(
                                ChatUI.this,
                                "Problema ao salvar mensagem, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();
                    }else {

                        // salvamos mensagem para o destinatario
                        Boolean retornoMensagemDestinatario = salvarMensagem(contatoUser.getId(), currentUser.getId() , mensagem );
                        if( !retornoMensagemDestinatario ){
                            Toast.makeText(
                                    ChatUI.this,
                                    "Problema ao enviar mensagem para o destinatário, tente novamente!",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    }

                    /*// salvamos Conversa para o remetente
                    Conversa conversa = new Conversa();
                    conversa.setIdUsuario( idUsuarioDestinatario );
                    conversa.setNome( nomeUsuarioDestinatario );
                    conversa.setMensagem( textoMensagem );
                    Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, conversa);
                    if( !retornoConversaRemetente ){
                        Toast.makeText(
                                ConversaActivity.this,
                                "Problema ao salvar conversa, tente novamente!",
                                Toast.LENGTH_LONG
                        ).show();
                    }else {
                        // salvamos Conversa para o Destinatario

                        conversa = new Conversa();
                        conversa.setIdUsuario( idUsuarioRemetente );
                        conversa.setNome( nomeUsuarioRemetente );
                        conversa.setMensagem(textoMensagem);

                        Boolean retornoConversaDestinatario = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa );
                        if( !retornoConversaDestinatario ){
                            Toast.makeText(
                                    ConversaActivity.this,
                                    "Problema ao salvar conversa para o destinatário, tente novamente!",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    }*/


                    editMensagem.setText("");
                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagem);
    }

    private boolean salvarMensagem(String idRemetente, String idDestinatario, Message mensagem){
        try {

            firebase = FirebasePreferences.getDatabaseReference().child("messages");

            firebase.child( idRemetente )
                    .child( idDestinatario )
                    .push()
                    .setValue( mensagem );

            return true;

        }catch ( Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /*private boolean salvarConversa(String idRemetente, String idDestinatario, Conversa conversa){
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
            firebase.child( idRemetente )
                    .child( idDestinatario )
                    .setValue( conversa );

            return true;

        }catch ( Exception e){
            e.printStackTrace();
            return false;
        }
    }*/
}
