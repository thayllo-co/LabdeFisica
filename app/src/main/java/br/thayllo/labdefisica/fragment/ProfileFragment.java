package br.thayllo.labdefisica.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.activity.AttachmentPicker;
import br.thayllo.labdefisica.adapter.ContactAdapter;
import br.thayllo.labdefisica.helper.Base64Custom;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private Preferences preferences;
    private User currentUser;
    private CollectionReference myContactsReference;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private ListView friendsListView;
    private ArrayAdapter friendsAdapter;
    private ArrayList<User> friendsList;
    private Toolbar profileToolbar;
    private TextView userIdProfileTextView;
    private CollectionReference usersFirebaseFirestore = FirebasePreferences.getFirebaseFirestore()
            .collection("users");

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        setHasOptionsMenu(true);

        userNameTextView = view.findViewById(R.id.userNameProfileTextView);
        userEmailTextView = view.findViewById(R.id.userEmailProfileTextView);
        userIdProfileTextView = view.findViewById(R.id.userIdProfileTextView);
        friendsListView = view.findViewById(R.id.friendsListView);
        profileToolbar = view.findViewById(R.id.profileToolbar);

        ((AppCompatActivity)getActivity()).setSupportActionBar(profileToolbar);
        profileToolbar.setTitle("");

        preferences = new Preferences(getActivity());
        currentUser = preferences.getUser();

        userNameTextView.setText(currentUser.getName());
        userEmailTextView.setText(currentUser.getEmail());
        userIdProfileTextView.setText(currentUser.getId());

        myContactsReference = FirebasePreferences.getFirebaseFirestore()
                .collection("users").document(currentUser.getId()).collection("contacts");

        friendsList = new ArrayList<>();
        friendsAdapter = new ContactAdapter( getActivity() , friendsList);
        friendsListView.setAdapter( friendsAdapter );

        myContactsReference
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                friendsList.add(doc.getDocument().toObject(User.class));
                            }else if(doc.getType() == DocumentChange.Type.REMOVED){
                                //reportsAdapter.notifyDataSetChanged();
                            }
                            friendsAdapter.notifyDataSetChanged();
                        }
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
                searchProfile();
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
                                                // user is now signed out
                                                Toast.makeText(getActivity(), "Desconectado", Toast.LENGTH_SHORT).show();
                                                preferences.limpar();
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

    private void searchProfile(){
        final EditText editText = new EditText(getActivity());
        editText.setHint("Digite o e-mail");
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.new_contact)
                .setCancelable(false)
                .setView(editText)
                .setPositiveButton(R.string.look_for,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String emailContato = editText.getText().toString();
                                String idContato = Base64Custom.codificarBase64(emailContato);

                                if( emailContato.isEmpty() ){
                                    Toast.makeText(getActivity(), "Preencha o e-mail", Toast.LENGTH_LONG).show();
                                }else {
                                    DocumentReference friendReference = FirebasePreferences.getFirebaseFirestore()
                                            .collection("users").document(idContato);

                                    friendReference.get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            User newFriend = document.toObject(User.class);
                                                            Toast.makeText(getActivity(), "ENCONTRADO", Toast.LENGTH_SHORT).show();
                                                            popUpProfile(newFriend);
                                                        } else {
                                                            Toast.makeText(getActivity(), "NÃO ENCONTRADO", Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
        TextView name = alertLayout.findViewById(R.id.nameTextView);
        TextView email = alertLayout.findViewById(R.id.emailTextView);
        TextView id = alertLayout.findViewById(R.id.idTextView);
        Button add = alertLayout.findViewById(R.id.addFriendButton);
        final ProgressBar profileProgressBar = alertLayout.findViewById(R.id.profileProgressBar);

        name.setText(user.getName());
        email.setText(user.getEmail());
        id.setText(user.getId());
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                profileProgressBar.setVisibility(View.VISIBLE);
                DocumentReference myReference = myContactsReference.document(user.getId());
                final DocumentReference friendReference = usersFirebaseFirestore.document(user.getId())
                        .collection("contacts").document(currentUser.getId());

                myReference.set(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendReference.set(currentUser)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(), "AMIGO ADCIONADO", Toast.LENGTH_SHORT).show();
                                                    profileProgressBar.setVisibility(View.GONE);
                                                }
                                            });
                                } else {
                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    profileProgressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        if(user.getId() == currentUser.getId())
            add.setVisibility(View.GONE);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(alertLayout);
        alert.setCancelable(true);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

}
