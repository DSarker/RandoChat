package com.industries.sarker.randochat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ListView mListview;
    private EditText mInputEditText;
    private ImageButton mSendButton;
    private String mUsername;
    private TextView chatWithTextView;
    private String currentChatroomId;
    private Firebase mFirebaseRootRef;
    private Firebase firebaseNumOfUsersRef;
    private FirebaseListAdapter<Message> mAdapter;
    private Firebase firebaseCurrentChatroomRef;
    private Firebase firebaseChatroomsRef;
    private Firebase firebaseMessageRef;
    private Firebase firebaseUser1Ref;
    private Firebase firebaseUser2Ref;
    private ArrayList<Chatroom> chatroomList;
    private boolean createNewRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = getIntent().getExtras();
        mUsername = (String) bundle.get(LoginActivity.USERNAME);
        currentChatroomId = bundle.getString(LoginActivity.CHATROOM_ID);

        mListview = (ListView) findViewById(R.id.listview);
        mInputEditText = (EditText) findViewById(R.id.edittext);
        chatWithTextView = (TextView) findViewById(R.id.chatwith_textview);

        mSendButton = (ImageButton) findViewById(R.id.send_button);

        // Firebase references
        mFirebaseRootRef = new Firebase("https://randochat.firebaseio.com/");
        firebaseChatroomsRef = mFirebaseRootRef.child("chatrooms");
        firebaseCurrentChatroomRef = firebaseChatroomsRef.child(currentChatroomId);
        firebaseMessageRef = firebaseCurrentChatroomRef.child("messages");
        firebaseNumOfUsersRef = firebaseCurrentChatroomRef.child("numOfUsers");
        firebaseUser1Ref = firebaseCurrentChatroomRef.child("user1");
        firebaseUser2Ref = firebaseCurrentChatroomRef.child("user2");

        chatroomList = new ArrayList<>();

        // Remove old messages when user joins for privacy
        firebaseMessageRef.removeValue();
        String time = String.format("%02d:%02d",
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE));
        firebaseMessageRef.push().setValue(new Message("System1920476538", mUsername.substring(0, mUsername.length() - 4) + " has joined.", time));

        // Updates the chatting with textview
        firebaseUser1Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.getValue() == null) && !mUsername.equals(dataSnapshot.getValue())) {
                    if (dataSnapshot.getValue().equals("")) {
                        chatWithTextView.setText("Waiting to chat...");

                    } else {
                        chatWithTextView.setText("Chatting with " + dataSnapshot.getValue()
                                .toString().substring(0, dataSnapshot.getValue().toString().length() - 4));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Updates the chatting with textview
        firebaseUser2Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.getValue() == null) && !mUsername.equals(dataSnapshot.getValue())) {
                    if (dataSnapshot.getValue().equals("")) {
                        chatWithTextView.setText("Waiting to chat...");
                    } else {
                        chatWithTextView.setText("Chatting with " + dataSnapshot.getValue()
                                .toString().substring(0, dataSnapshot.getValue().toString().length() - 4));
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Updates the num of users in the chat
        firebaseNumOfUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue().equals("2")) {
                    for (Chatroom chatroom : chatroomList) {
                        if (chatroom.getKey().equals(currentChatroomId)) {
                            chatroom.setNumOfUsers("2");
                            break;
                        }
                    }
                } else {
                    for (Chatroom chatroom : chatroomList) {
                        if (chatroom.getKey().equals(currentChatroomId)) {
                            chatroom.setNumOfUsers("1");
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Updates the list of chatrooms
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
                for (Chatroom chatroom : chatroomList) {
                    if (chatroom.getKey().equals(dataSnapshot.getKey())) {
                        chatroomList.remove(chatroom);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        // Firebase adapter
        mAdapter = new FirebaseListAdapter<Message>(this, Message.class, R.layout.message_item, firebaseMessageRef) {
            @Override
            protected void populateView(View view, Message message, int i) {
                ImageView imageView1 = (ImageView) view.findViewById(R.id.message_imageview1);
                ImageView imageView2 = (ImageView) view.findViewById(R.id.message_imageview2);
                TextView textView = (TextView) view.findViewById(R.id.message_textview);

                TextView leftArrow = (TextView) view.findViewById(R.id.left_arrow);
                TextView rightArrow = (TextView) view.findViewById(R.id.right_arrow);
                leftArrow.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                rightArrow.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

                textView.setTextColor(Color.WHITE);

                RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.message_relativelayout);

                String author = message.getAuthor();

                // Determine positioning of message depending on sender

                // Current user is sending message
                if (author != null && author.equals(mUsername)) {
                    relativeLayout.setGravity(Gravity.RIGHT);

                    textView.setBackgroundResource(R.drawable.rounded_corners_blue);

                    // Set time in message
                    String resultText = message.getText() + "\n" + message.getTime();
                    SpannableString styledResultText = new SpannableString(resultText);
                    styledResultText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                            message.getText().length() + 1, message.getText().length() + 1 + message.getTime().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(styledResultText);

                    imageView1.setVisibility(View.GONE);
                    leftArrow.setVisibility(View.GONE);

                    rightArrow.setVisibility(View.VISIBLE);
                    imageView2.setVisibility(View.VISIBLE);
                    imageView2.setImageResource(R.drawable.shadow_face);

                    // System is sending message
                } else if (author.equals("System1920476538")) {

                    relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                    textView.setBackgroundResource(R.drawable.rounded_corners_red);

                    // Set time in message
                    String resultText = message.getText() + "\n" + message.getTime();
                    SpannableString styledResultText = new SpannableString(resultText);
                    styledResultText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                            message.getText().length() + 1, message.getText().length() + 1 + message.getTime().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(styledResultText);

                    imageView1.setVisibility(View.GONE);
                    imageView2.setVisibility(View.GONE);
                    leftArrow.setVisibility(View.GONE);
                    rightArrow.setVisibility(View.GONE);

                    // Rando is sending message
                } else {
                    textView.setText(message.getText());

                    relativeLayout.setGravity(Gravity.LEFT);

                    textView.setBackgroundResource(R.drawable.rounded_corners_green);
                    textView.setText(message.getText() + "\n" + message.getTime());

                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.GONE);
                    imageView1.setVisibility(View.VISIBLE);
                    imageView1.setImageResource(R.drawable.question);
                    imageView2.setVisibility(View.GONE);
                }
            }
        };

        mListview.setAdapter(mAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mInputEditText.getText().toString().trim().equals("")) {
                    String time = String.format("%02d:%02d",
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE));

                    firebaseMessageRef.push().setValue(new Message(mUsername, mInputEditText.getText().toString().trim(), time));
                    mInputEditText.setText("");
                }
            }
        });

        mListview.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                Toast.makeText(ChatActivity.this, "Switched rooms", Toast.LENGTH_SHORT).show();
                leaveChatroom();
                checkRoomAvailability();
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
            }
        });
    }

    // Makes the user aware of closing the app using the back button
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            leaveChatroom();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast toast = Toast.makeText(this, "Press BACK twice to exit", Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.WHITE);
        toast.show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    // User left the current chatroom
    private void leaveChatroom() {
        Chatroom currentChatroom = new Chatroom();

        for (Chatroom chatroom : chatroomList) {
            if (chatroom.getKey().equals(currentChatroomId)) {
                currentChatroom = chatroom;
                break;
            }
        }

        if (currentChatroom.getNumOfUsers().equals("2")) {

            currentChatroom.setNumOfUsers("1");
            if (currentChatroom.getUser1().equals(mUsername)) {
                currentChatroom.setUser1("");
                firebaseCurrentChatroomRef.child("/user1").setValue("");
            } else {
                currentChatroom.setUser2("");
                firebaseCurrentChatroomRef.child("/user2").setValue("");
            }

            firebaseNumOfUsersRef.setValue("1");

            String time = String.format("%02d:%02d",
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE));

            firebaseMessageRef.push().setValue(new Message("System1920476538", mUsername.substring(0, mUsername.length() - 4) + " has left.", time));

        } else if (currentChatroom.getNumOfUsers().equals("1")) {
            chatroomList.remove(currentChatroom);
            firebaseCurrentChatroomRef.removeValue();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Check if there are any available rooms. If not, create one
    public void checkRoomAvailability() {
        createNewRoom = true;

        for (Chatroom chatroom : chatroomList) {

            if (chatroom.getNumOfUsers().equals("1") && !chatroom.getKey().equals(currentChatroomId)) {
                chatroom.setNumOfUsers("2");

                firebaseCurrentChatroomRef = firebaseChatroomsRef.child(chatroom.getKey());
                firebaseNumOfUsersRef = firebaseCurrentChatroomRef.child("numOfUsers");
                firebaseNumOfUsersRef.setValue("2");

                if (chatroom.getUser1() == null || chatroom.getUser1().equals("")) {
                    Firebase firebaseRef = firebaseCurrentChatroomRef.child("user1");
                    firebaseRef.setValue(mUsername);

                } else {
                    Firebase firebaseRef = firebaseCurrentChatroomRef.child("user2");
                    firebaseRef.setValue(mUsername);
                }

                startChatRoom(chatroom.getKey());
                createNewRoom = false;
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
                }
            });
        }
    }

    // Start the Chat Activity with the chatroom id and username
    private void startChatRoom(String id) {
        Intent chatIntent = new Intent(ChatActivity.this, ChatActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(LoginActivity.USERNAME, mUsername);
        bundle.putString(LoginActivity.CHATROOM_ID, id);

        chatIntent.putExtras(bundle);
        startActivity(chatIntent);
    }
}