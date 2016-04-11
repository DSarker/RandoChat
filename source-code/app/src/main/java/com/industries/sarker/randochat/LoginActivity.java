package com.industries.sarker.randochat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    public static final String USERNAME = "username";
    public static final String CHATROOM_ID = "chatroomId";

    private Button mStartChatButton;
    private EditText mUserNameEditText;

    private Firebase mFirebaseRootRef;
    private Firebase firebaseChatroomsRef;
    private ArrayList<Chatroom> chatroomList;
    private boolean createNewRoom;

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mFirebaseRootRef = new Firebase("https://randochat.firebaseio.com/");

            firebaseChatroomsRef = mFirebaseRootRef.child("chatrooms");

            chatroomList = new ArrayList<>();


            mUserNameEditText = (EditText) findViewById(R.id.username_edittext);
            mStartChatButton = (Button) findViewById(R.id.start_chat_button);

            // Get a list of chatrooms
            firebaseChatroomsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Chatroom chatroom = dataSnapshot.getValue(Chatroom.class);
                    chatroom.setKey(dataSnapshot.getKey());
                    chatroomList.add(chatroom);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    for (Chatroom chatroom : chatroomList) {
                        if (chatroom.getKey().equals(dataSnapshot.getKey())) {
                            Chatroom updatedRoom = dataSnapshot.getValue(Chatroom.class);
                            chatroom.setNumOfUsers(updatedRoom.getNumOfUsers());
                            break;
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    chatroomList.remove(dataSnapshot.getValue(Chatroom.class));
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

            mStartChatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mUserNameEditText.getText().toString().trim().equals("")) {
                        Random random = new Random();

                        // Attach a random 4 digit number for unique names
                        mUsername = mUserNameEditText.getText().toString().trim() + String.valueOf(random.nextInt(9999 - 1000 + 1) + 1000);
                        checkRoomAvailability();
                    } else {
                        mUserNameEditText.setError("Please enter a username");
                    }
                }
            });

        } else { // Connection failed
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Internet connection required. Please restart the application.");
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    // Check for an available room. If not, create one
    public void checkRoomAvailability() {
        createNewRoom = true;

        for (Chatroom chatroom : chatroomList) {

            if (chatroom.getNumOfUsers().equals("1")) {
                chatroom.setNumOfUsers("2");

                Firebase firebaseCurrentChatroomRef = firebaseChatroomsRef.child(chatroom.getKey());
                Firebase firebaseUpdateNumOfUsersRef = firebaseCurrentChatroomRef.child("numOfUsers");
                firebaseUpdateNumOfUsersRef.setValue("2");

                if (chatroom.getUser1() == null || chatroom.getUser1().equals("")) {
                    Firebase firebaseRef = firebaseCurrentChatroomRef.child("user1");
                    firebaseRef.setValue(mUsername);

                } else {
                    Firebase firebaseRef = firebaseCurrentChatroomRef.child("user2");
                    firebaseRef.setValue(mUsername);
                }

                startChatRoom(chatroom.getKey());
                createNewRoom = false;
                finish();
                break;
            }
        }

        if (createNewRoom) {
            Chatroom chatroom = new Chatroom(new HashMap<String, Message>(), "1", mUsername, "");
            firebaseChatroomsRef.push().setValue(chatroom, new Firebase.CompletionListener() {

                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    firebase.getKey();
                    startChatRoom(firebase.getKey());
                    finish();
                }
            });
        }
    }

    // Start the Chat Activity with the chatroom id and username
    private void startChatRoom(String id) {
        Intent chatIntent = new Intent(LoginActivity.this, ChatActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(USERNAME, mUsername);
        bundle.putString(CHATROOM_ID, id);

        chatIntent.putExtras(bundle);
        startActivity(chatIntent);
    }
}