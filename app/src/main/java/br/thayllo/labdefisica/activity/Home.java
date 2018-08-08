package br.thayllo.labdefisica.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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
import br.thayllo.labdefisica.fragment.Laboratory;
import br.thayllo.labdefisica.fragment.Profile;
import br.thayllo.labdefisica.fragment.ReportList;
import br.thayllo.labdefisica.helper.Base64Custom;
import br.thayllo.labdefisica.model.User;
import br.thayllo.labdefisica.settings.FirebasePreferences;
import br.thayllo.labdefisica.settings.Preferences;

public class Home extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int RC_PERMISSIONS = 10;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    private static final int RC_SIGN_IN = 123;

    private User mainUser;
    private Preferences preferences;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth = FirebasePreferences.getFirebaseAuth();
    private CollectionReference usersFirebaseFirestore = FirebasePreferences.getFirebaseFirestore()
            .collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // verifica se o usuario solicitou logout em ProfileFragment
        if (getIntent().getBooleanExtra("LOGOUT", false)) { finish(); }

        bottomNavigationView = findViewById(R.id.homeBottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_laboratory);
        loadFragment(new Laboratory());

        preferences = new Preferences(Home.this);
        mainUser = preferences.getUser();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // usuario logado
                } else {
                    // sem usuario logado
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.ic_logo)
                                    .setTheme(R.style.FirebaseUI)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.FacebookBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // add listener do usuario
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        // configura permissões
        permissionsSetUp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // remove listener do usuario
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.navigation_profile:
                fragment = new Profile();
                break;
            case R.id.navigation_laboratory:
                fragment = new Laboratory();
                break;
            case R.id.navigation_report:
                fragment = new ReportList();
                break;
        }
        return loadFragment(fragment);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // login com sucesso
                FirebaseUser firebaseUser = FirebasePreferences.getFirebaseAuth().getCurrentUser();
                final FirebaseUserMetadata metadata = FirebasePreferences.getFirebaseAuth().getCurrentUser().getMetadata();

                mainUser.setId(Base64Custom.codificarBase64(firebaseUser.getEmail()));
                mainUser.setName(firebaseUser.getDisplayName());
                mainUser.setEmail(firebaseUser.getEmail());
                if(firebaseUser.getPhotoUrl() != null)
                    mainUser.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
                preferences.saveUser(mainUser);

                DocumentReference documentReference = usersFirebaseFirestore.document(mainUser.getId());

                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    // se o usuario é novo grava no bd suas infos
                    documentReference.set(mainUser)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Home.this, getString(R.string.welcome) + " " + mainUser.getName(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    // usuario retornando
                    Toast.makeText(Home.this, getString(R.string.welcome_back) + " " + mainUser.getName(), Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.signin_canceled, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    public void permissionsSetUp(){

        int permission = ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if ( permission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.confirmation)
                            .setIcon(R.drawable.ic_warning)
                            .setMessage(R.string.permissions_warning)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(Home.this,
                                            PERMISSIONS_STORAGE,
                                            RC_PERMISSIONS);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .create()
                            .show();
                    return;
                } else {
                    ActivityCompat.requestPermissions(Home.this,
                            PERMISSIONS_STORAGE,
                            RC_PERMISSIONS);
                }
            }
            return;
        } else {

        }
    }

}
