package br.thayllo.labdefisica.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.activity.Home;
import br.thayllo.labdefisica.adapter.ContactAdapter;
import br.thayllo.labdefisica.helper.Base64Custom;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile extends Fragment {

    private Preferences preferences;
    private User currentUser;
    private CollectionReference myContactsReference;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private ListView friendsListView;
    private ArrayAdapter friendsAdapter;
    private ArrayList<User> friendsList;
    private Toolbar profileToolbar;
    private CircleImageView profileCircleImageView;
    private LinearLayout profileLinearLayout;
    private CollectionReference usersFirebaseFirestore = FirebasePreferences.getFirebaseFirestore()
            .collection("users");

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userNameTextView = view.findViewById(R.id.userNameProfileTextView);
        userEmailTextView = view.findViewById(R.id.userEmailProfileTextView);
        friendsListView = view.findViewById(R.id.friendsListView);
        profileToolbar = view.findViewById(R.id.profileToolbar);
        profileCircleImageView = view.findViewById(R.id.profileCircleImageView);
        profileLinearLayout = view.findViewById(R.id.profileLinearLayout);

        // configura o ActionBar
        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).setSupportActionBar(profileToolbar);
        profileToolbar.setTitle("");

        preferences = new Preferences(getActivity());
        currentUser = preferences.getUser();

        userNameTextView.setText(currentUser.getName());
        userEmailTextView.setText(currentUser.getEmail());

        // carrega foto do perfil
        if(currentUser.getPhotoUrl() != null){
            Picasso.get()
                    .load(currentUser.getPhotoUrl())
                    .into(profileCircleImageView);
        } else {
            profileCircleImageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.profile_person));
        }

        myContactsReference = FirebasePreferences.getFirebaseFirestore()
                .collection("users").document(currentUser.getId()).collection("contacts");

        friendsList = new ArrayList<>();
        friendsAdapter = new ContactAdapter( getActivity() , friendsList);
        friendsListView.setAdapter( friendsAdapter );

        myContactsReference
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        User user;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            switch (doc.getType()) {
                                case ADDED:
                                    friendsList.add(doc.getDocument().toObject(User.class));
                                    friendsAdapter.notifyDataSetChanged();
                                    break;
                                case MODIFIED: // ainda não é possivel modificar contatos
                                    break;
                                case REMOVED: //implementado tratamento mas não seu uso ainda
                                    user = doc.getDocument().toObject(User.class);
                                    Iterator<User> a = friendsList.iterator();
                                    while(a.hasNext()){
                                        if(a.next().getId().equals(user.getId()))
                                            a.remove();
                                    }
                                    break;
                            }

                            friendsAdapter.notifyDataSetChanged();
                        }
                        if(friendsList.size() < 1)
                            profileLinearLayout.setVisibility(View.VISIBLE);
                        else
                            profileLinearLayout.setVisibility(View.GONE);
                    }
                });

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popUpProfile(friendsList.get(position));
            }
        });

        //Adicionar evento de clique longo na lista para excluir o amigo selecionado
        friendsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                // referencia para remover do banco
                final DocumentReference friend = myContactsReference.document(friendsList.get(position).getId());

                // AlertDialog para confirmar a exclusão
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.delete)
                        .setIcon( R.drawable.ic_warning)
                        .setMessage(R.string.sure_to_delete_content)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // remove o anexo da lista
                                friend.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(),R.string.delete_error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        })
                        .create()
                        .show();

                return true;
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addFriendButton:
                searchProfileDialog();
                break;
            case R.id.exitAppButton:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.exit_app)
                .setMessage(R.string.sure_to_exit)
                .setCancelable(false)
                .setPositiveButton(R.string.exit,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AuthUI.getInstance()
                                        .signOut(getActivity())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                preferences.limpar();
                                                // manda para Home que deseja fechar o app
                                                Intent intent = new Intent(getActivity(), Home.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra("LOGOUT", true);
                                                startActivity(intent);
                                                getActivity().finish();
                                            }
                                        });
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .create()
                .show();
    }

    private void searchProfileDialog(){

        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20,0,20,0);
        final EditText editText = new EditText(getActivity());
        editText.setLines(1);
        editText.setMaxLines(1);
        container.addView(editText,lp);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.new_contact)
                .setCancelable(false)
                .setMessage(R.string.email_tip)
                .setView(container)
                .setPositiveButton(R.string.search_for,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String emailContato = editText.getText().toString();
                                String idContato = Base64Custom.codificarBase64(emailContato);

                                if( emailContato.isEmpty() ){
                                    Toast.makeText(getActivity(), R.string.type_something, Toast.LENGTH_SHORT).show();
                                } else {
                                    // procura no bd o email passado pelo usuario
                                    DocumentReference friendReference = FirebasePreferences.getFirebaseFirestore()
                                            .collection("users").document(idContato);
                                    friendReference.get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            // se encontrado abre a dialog para add
                                                            User newFriend = document.toObject(User.class);
                                                            popUpProfile(newFriend);
                                                        } else {
                                                            Toast.makeText(getActivity(), R.string.not_found, Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        //Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .create()
                .show();
    }

    private void popUpProfile(final User user){
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.pop_up_profile, null);
        TextView name = alertLayout.findViewById(R.id.popupNameTextView);
        TextView email = alertLayout.findViewById(R.id.popupEmailTextView);
        Button add = alertLayout.findViewById(R.id.popupAddFriendButton);
        final ProgressBar profileProgressBar = alertLayout.findViewById(R.id.popupProgressBar);
        CircleImageView popupCircleImageView = alertLayout.findViewById(R.id.popupCircleImageView);

        // carrega a foto do usuario de houver
        if(user.getPhotoUrl() != null){
            Picasso.get()
                    .load(user.getPhotoUrl())
                    .into(popupCircleImageView);
        }
        // verifica se é o proprio usuario
        if(user.getId().equals(currentUser.getId()))
            add.setVisibility(View.GONE);
        // verifica se ja não é amigo
        for(User u :friendsList){
            if(user.getId().equals(u.getId()))
                add.setVisibility(View.GONE);
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(alertLayout);
        alert.setCancelable(true);
        final AlertDialog dialog = alert.create();
        dialog.show();

        name.setText(user.getName());
        email.setText(user.getEmail());
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add novo usuario a sua lista de contatos
                profileProgressBar.setVisibility(View.VISIBLE);
                DocumentReference myReference = myContactsReference.document(user.getId());
                final DocumentReference friendReference = usersFirebaseFirestore.document(user.getId())
                        .collection("contacts").document(currentUser.getId());

                // add user a minha lista no bd
                myReference.set(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // add meu contato a lista do user
                                    friendReference.set(currentUser)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(), R.string.friend_edded, Toast.LENGTH_SHORT).show();
                                                    profileProgressBar.setVisibility(View.GONE);
                                                    dialog.dismiss();
                                                }
                                            });
                                } else {
                                    //Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    profileProgressBar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        });
    }

}
