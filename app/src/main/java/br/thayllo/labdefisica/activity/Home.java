package br.thayllo.labdefisica.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.Arrays;
import java.util.List;

import br.thayllo.labdefisica.R;
import br.thayllo.labdefisica.fragment.LaboratoryFragment;
import br.thayllo.labdefisica.fragment.ProfileFragment;
import br.thayllo.labdefisica.fragment.ReportFragment;
import br.thayllo.labdefisica.helper.Base64Custom;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

public class Home extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;

    // Choose authentication providers
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build());

    private FirebaseAuth mFirebaseAuth;
    private Preferences preferences;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private User mainUser;
    private CollectionReference usersFirebaseFirestore = FirebasePreferences.getFirebaseFirestore()
            .collection("users");
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.homeBottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_laboratory);
        loadFragment(new LaboratoryFragment());

        preferences = new Preferences(Home.this);
        mFirebaseAuth = FirebasePreferences.getFirebaseAuth();
        mainUser = preferences.getUser();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                if( firebaseUser != null){
                    //user is signed in;

                } else {
                    //user is signed out
                    Toast.makeText(Home.this, "DESCONECTADO", Toast.LENGTH_SHORT).show();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.ic_logo)
                                    .setTheme(R.style.LoginTheme)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }

    private boolean loadFragment(Fragment fragment){
        if( fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_profile:
                fragment = new ProfileFragment();
                break;
            case R.id.navigation_report:
                fragment = new ReportFragment();
                break;
            case R.id.navigation_laboratory:
                fragment = new LaboratoryFragment();
                break;
        }
        return loadFragment(fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                FirebaseUser firebaseUser = FirebasePreferences.getFirebaseAuth().getCurrentUser();
                final FirebaseUserMetadata metadata = FirebasePreferences.getFirebaseAuth().getCurrentUser().getMetadata();

                mainUser.setId(Base64Custom.codificarBase64(firebaseUser.getEmail()));
                mainUser.setName(firebaseUser.getDisplayName());
                mainUser.setEmail(firebaseUser.getEmail());
                preferences.saveUser( mainUser );

                DocumentReference documentReference = usersFirebaseFirestore.document(mainUser.getId());

                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    // The user is new, show them a fancy intro screen!
                    documentReference.set(mainUser)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Home.this, "NOVO USUARIO" , Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    // This is an existing user, show them a welcome back screen.
                    Toast.makeText(Home.this, "USUARIO VOLTANDO" , Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if( mAuthStateListener != null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

}
