package com.example.erlan.firebasekloopoperator;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private Switch aSwitch;
    private Button buttonSignOut;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;
    private String branch = "users";

    public static final String ANONYMOUS = "anonymous";
    private String mUsername;
    private String userNameForFirebase;
    List<Character> errorList;

    FirebaseDatabase database;
    DatabaseReference myRef;
    private ChildEventListener listener;

    ImageView img;

//    private ListView mMessageListView;
//    private OperatorAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init(){
        //mUsername = ANONYMOUS;
        buttonSignOut = (Button) findViewById(R.id.button2);
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
            }
        });

        img = (ImageView) findViewById(R.id.imageView2);

        userNameForFirebase = ANONYMOUS;
        errorList = new ArrayList<>();
        errorList.add('.');
        errorList.add('#');
        errorList.add('$');
        errorList.add('[');
        errorList.add(']');

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
                    if (mFirebaseUser.getEmail().equals("admin@kloop.kg")){
                        Toast.makeText(MainActivity.this, "admin", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                        startActivity(intent);
                    }
                    Toast.makeText(MainActivity.this, "Your are logged in!", Toast.LENGTH_LONG).show();
                }
            }
        };


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(branch);

        aSwitch = (Switch) findViewById(R.id.switch1);
        aSwitch.setOnCheckedChangeListener(this);


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
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user.getEmail().equals(mUsername)){
                        Toast.makeText(MainActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                        if (user.getStatus()==0){
                            img.clearAnimation();
                            img.setBackgroundColor(Color.GREEN);
                        } else if (user.getStatus() == 1){
                            img.clearAnimation();
                            img.setBackgroundColor(Color.RED);
                        } else if (user.getStatus() == 2){
                            //img.setBackgroundColor(Color.CYAN);
                            img.setBackgroundColor(Color.RED);
                            final Animation animation = new AlphaAnimation(1, 0);
                            animation.setDuration(1000);
                            animation.setInterpolator(new LinearInterpolator());
                            animation.setRepeatCount(Animation.INFINITE);
                            animation.setRepeatMode(Animation.REVERSE);
                            img.startAnimation(animation);

                        }
                    }
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


        char[] ech = mUsername.toCharArray();
        StringBuilder username = new StringBuilder();
        for (char i : ech) {
            if (!errorList.contains(i)) username.append(i);
        }
        userNameForFirebase = username.toString();
    }

    private void onSignOutCleanUp() {
        detachDatabaseListener();
        //mUsername = ANONYMOUS;
        userNameForFirebase =ANONYMOUS;
        // mMessageAdapter.clear();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            //Toast.makeText(MainActivity.this, "Ready", Toast.LENGTH_SHORT).show();
            Users user = new Users(mUsername, 0);
            myRef.child(userNameForFirebase).setValue(user);

        }
        else {
            myRef.child(userNameForFirebase).setValue(null);

           // Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
        }
    }
}
