package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ListView lvMessages;
    private MessagesListAdapter adapter;
    private List<Message> mMessagesList;

    private static final String TAG = "ChatActivity";

    public static boolean toUpdateMessages = true;
    Thread t;
    private final int sleepTimeSeconds = 1;
    private final int sleepTimeMiliSeconds = sleepTimeSeconds* 1000;

    Toast toast;
    TextView tv;
    Typeface tf;

    String groupId = "";
    String groupName = "";

    FloatingActionButton btn_to_send;
    TextView et_to_send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        groupName= getIntent().getExtras().getString("groupName");
        setTitle(groupName);

        lvMessages = (ListView)findViewById(R.id.list_view_messages);
        btn_to_send = (FloatingActionButton)findViewById(R.id.msg_send_button);
        et_to_send = (TextView)findViewById(R.id.input_to_send);



        groupId = getIntent().getExtras().getString("groupId");
        mMessagesList = User.getListOfMessages(groupId);

        //Init adapter with all messages and from now on we will just add messages and not change everything
        adapter = new MessagesListAdapter(getApplicationContext(), mMessagesList);
        lvMessages.setAdapter(adapter);
        lvMessages.setSelection(adapter.getCount() - 1);//scroll to the bottom



        //this line prevent the keyboard from overlaping the listview when shown up
        lvMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        //lvMessages.setStackFromBottom(true);

        //if i want to do somthing on click on message
        /*lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Do something
                //Ex: display msg with Message id get from view.getTag
                Toast.makeText(getApplicationContext(), "Clicked message id(of the adapter list) =" + view.getTag(), Toast.LENGTH_SHORT).show();
            }
        });*/


        btn_to_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked on to_send button");
                String data = et_to_send.getText().toString();
                if(!data.equals("")){
                    et_to_send.setText("");//clear message after send
                    String action = "new_message";
                    int int_type = 1; //public - for group
                    String srcUserId = User.getUserName();
                    String dstId = "-";
                    @SuppressLint("SimpleDateFormat") String sendTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

                    String to_send = action + "%" + int_type + "&" + groupId + "&" + srcUserId + "&" + dstId + "&" + sendTime + "&" + data;
                    ClientTask.queToSend.add(to_send);
                    Log.d(TAG, "data was passed to client task:" + to_send);
                }
                else
                    Log.d(TAG, "user tried to send nothing");
            }
        });

        updateMessages();



    }
    public void updateMessages(){
        t = new Thread() {
            @Override
            public void run() {
                while (toUpdateMessages) {
                    try {
                        Thread.sleep(sleepTimeMiliSeconds);  //1000ms = 1 sec
                        // here you check the value of getActivity() and break up if needed
                        if(getApplicationContext() == null) {
                            Log.d(TAG, "getApplicationContext = null");
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //do somthing every 1 sec

                                List<Message> newMessagesList = new ArrayList<Message>(User.userGroups.get(groupId).messages);
                                Log.d(TAG, "mMessagesList:" + String.valueOf(mMessagesList));//did not make to string to message obj so dont expect...
                                Log.d(TAG, "newMessagesList:" + String.valueOf(newMessagesList));

                                int size1 = newMessagesList.size();
                                int size2 = mMessagesList.size();
                                int minSize = Math.min(size1,size2);
                                boolean areEquals = true;
                                for (int i = 0; i<minSize;i++){
                                    if (!mMessagesList.get(i).equals(newMessagesList.get(i))){
                                        areEquals = false;
                                        break;
                                    }
                                }
                                if(!mMessagesList.equals(newMessagesList)){//!mMessagesList.equals(newMessagesList)){
                                    mMessagesList = newMessagesList;

                                    adapter = new MessagesListAdapter(getApplicationContext(), mMessagesList);

                                    lvMessages.setAdapter(adapter);
                                    lvMessages.setSelection(adapter.getCount() - 1); //scroll to the bottom to see the message
                                    //lvMessages.smoothScrollToPosition(mMessagesList.size()-1);
                                    Log.d(TAG, "messages have changed");
                                }



                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        t.start();
    }




    /*private void displayChatMessage() {

        ListView listOfMessage = (ListView)findViewById(R.id.list_of_message);
        adapter = new FirebaseListAdapter<ChatMessage>(this,ChatMessage.class,R.layout.list_item,FirebaseDatabase.getInstance().getReference())
        {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                //Get references to the views of list_item.xml
                TextView messageText, messageUser, messageTime;
                messageText = (EmojiconTextView) v.findViewById(R.id.message_text);
                messageUser = (TextView) v.findViewById(R.id.message_user);
                messageTime = (TextView) v.findViewById(R.id.message_time);

                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

            }
        };
        listOfMessage.setAdapter(adapter);
    }
    */
}
