package br.thayllo.labdefisica.settings;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebasePreferences {

    private static FirebaseAuth mFirebaseAuth;
    private static StorageReference mFirebaseStorageReference;
    private static FirebaseFirestore mFirebaseFirestore;


    public static FirebaseAuth getFirebaseAuth(){
        if( mFirebaseAuth == null )
            mFirebaseAuth = FirebaseAuth.getInstance();
        return mFirebaseAuth;
    }

    public static FirebaseFirestore getFirebaseFirestore(){
        if( mFirebaseFirestore == null )
            mFirebaseFirestore = FirebaseFirestore.getInstance();
        return mFirebaseFirestore;
    }

    public static StorageReference getFirebaseStorage(){
        if( mFirebaseStorageReference == null )
            mFirebaseStorageReference = FirebaseStorage.getInstance().getReference();
        return mFirebaseStorageReference;
    }

}
