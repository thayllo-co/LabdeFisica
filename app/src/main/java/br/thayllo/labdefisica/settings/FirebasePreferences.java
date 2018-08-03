package br.thayllo.labdefisica.settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebasePreferences {

    private static DatabaseReference mFirebaseDatabaseReference;
    private static FirebaseAuth mFirebaseAuth;
    private static StorageReference mFirebaseStorageReference;
    private static FirebaseFirestore mFirebaseFirestore;

    public static DatabaseReference getDatabaseReference(){
        if( mFirebaseDatabaseReference == null )
            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        return mFirebaseDatabaseReference;
    }

    public static FirebaseAuth getFirebaseAuth(){
        if( mFirebaseAuth == null )
            mFirebaseAuth = FirebaseAuth.getInstance();
        return mFirebaseAuth;
    }

    public static StorageReference getFirebaseStorage(){
        if( mFirebaseStorageReference == null )
            mFirebaseStorageReference = FirebaseStorage.getInstance().getReference();
        return mFirebaseStorageReference;
    }

    public static FirebaseFirestore getFirebaseFirestore(){
        if( mFirebaseFirestore == null )
            mFirebaseFirestore = FirebaseFirestore.getInstance();
        return mFirebaseFirestore;
    }

}
