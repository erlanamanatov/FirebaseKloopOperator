package com.example.erlan.firebasekloopoperator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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

public class AdminActivity extends AppCompatActivity {

    Button buttonSignOut;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private ChildEventListener listener;
    private String branch = "users";

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;

    private ListView operatorsListView;
    //private OperatorAdapter mMessageAdapter;
    OperatorAdapter operatorAdapter;
    List<Character> errorList;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        init();

    }

    private void init()

    {

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
                    // onSignOutCleanUp();
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
                    // onSignInInit(mFirebaseUser);
                    if (mFirebaseUser.getEmail().equals("admin@kloop.kg")) {
                        Toast.makeText(AdminActivity.this, "admin", Toast.LENGTH_SHORT).show();

                    }
                    Toast.makeText(AdminActivity.this, "Your are logged in!", Toast.LENGTH_LONG).show();
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        buttonSignOut = (Button) findViewById(R.id.button5);
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAuth.signOut();
            }
        });


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(branch);
        attachDatabaseListener();
        // textView = (TextView) findViewById(R.id.textView);
        // textView.append("\n");

        operatorsListView = (ListView) findViewById(R.id.listview);


        List<Users> operatorList= new ArrayList<>();
        operatorAdapter = new OperatorAdapter(this, R.layout.item_operator, operatorList);
        operatorsListView.setAdapter(operatorAdapter);
        //showList();
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
                    Users user = dataSnapshot.getValue(Users.class);
                    operatorAdapter.add(user);
                    //textView.append(user.getEmail()+", status: "+Integer.toString(user.getStatus())+"\n");
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
                   // textView.append("Child removed\n");

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

    private void showList() {
        List<Users> operators = new ArrayList<>();
        registerForContextMenu(operatorsListView);
        //OperatorAdapter operatorAdapter = new OperatorAdapter(AdminActivity.this, operators);
        operatorAdapter = new OperatorAdapter(AdminActivity.this,R.layout.item_operator, operators);
        operatorsListView.setAdapter(operatorAdapter);
    }

    class OperatorAdapter extends ArrayAdapter<Users> {
        public OperatorAdapter(Context context, int resource, List<Users> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_operator, parent, false);
            }

            TextView username = (TextView) convertView.findViewById(R.id.userName);
            Button buttonChangeStatus = (Button) convertView.findViewById(R.id.button3);

            username.setText(getItem(position).getEmail());

            buttonChangeStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Users user = getItem(position);
                    String mUsername = user.getEmail();
                    char[] ech = mUsername.toCharArray();
                    StringBuilder username = new StringBuilder();
                    for (char i : ech) {
                        if (!errorList.contains(i)) username.append(i);
                    }
                    String userNameForFirebase = username.toString();

                    int status = user.getStatus();
                    if (status == 2) status = 0;
                    else status += 1;
                    user.setStatus(status);

                    myRef.child(userNameForFirebase).setValue(user);
                }
            });

            return convertView;
        }

    }
}
