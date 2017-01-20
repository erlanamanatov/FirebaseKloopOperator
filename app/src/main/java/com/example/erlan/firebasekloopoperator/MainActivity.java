package com.example.erlan.firebasekloopoperator;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;
    private String branch = "users";

    public static final String ANONYMOUS = "anonymous";
    private String mUsername;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private ChildEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init(){
        mUsername = ANONYMOUS;

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
                //User is signed out
                if (mFirebaseUser == null) {
                    onSignOutCleanUp();
                    //Starts sign-in flow
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                                            //new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN); //RC_SIGN_IN - request code
                    //User is signed in
                } else {
                    onSignInInit(mFirebaseUser);
                    Toast.makeText(MainActivity.this, "Your are logged in!", Toast.LENGTH_LONG).show();
                }
            }
        };


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(branch);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        //Possibly redundant code
        detachDatabaseListener();
        // mMessageAdapter.clear();
    }

    private void detachDatabaseListener() {
        if (listener != null) {
            myRef.removeEventListener(listener);
            listener = null;
        }

    }

    private void attachDatabaseListener() {
        if (listener == null) {
            // Listens to changes in the database
            listener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    // Get data from database and deserialize
//                    Messages m = dataSnapshot.getValue(Messages.class);
//                    String text = m.getName() + ": " + m.getText();
//                    chat.append(text + "\n");
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            //Connects listener to specific reference in the database
            myRef.addChildEventListener(listener);
        }
    }

    private void onSignInInit(FirebaseUser user) {
        attachDatabaseListener();
         mUsername = user.getEmail();
    }

    private void onSignOutCleanUp() {
        detachDatabaseListener();
        mUsername = ANONYMOUS;
        // mMessageAdapter.clear();
    }
}
