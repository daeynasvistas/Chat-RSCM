package pt.ipg.chatui.sample;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import pt.ipg.chatui.ChatView;
import pt.ipg.chatui.models.ChatMessage;
import pt.ipg.chatui.models.ChatMessage.Type;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public ArrayList<String> messages = new ArrayList<String>();
    // IPG - Alteração -------------- Daey
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://chat-ipg.azurewebsites.net");
        } catch (URISyntaxException e) {}
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    ChatView chatView = (ChatView) findViewById(R.id.chat_view);
                    // add the message to view
                 //   chatView.addMessage(new ChatMessage("Message received", System.currentTimeMillis(), ChatMessage.Type.RECEIVED));

                    // IPG - Alteração -- 05/02/19 -- JDinis
                    ChatMessage msg = new ChatMessage(message,
                            System.currentTimeMillis(), Type.RECEIVED, username);
                    chatView.addMessage(msg);

                    // IPG - Alteração -- 05/02/19 -- JDinis
                    try {
                        data.put("time",msg.getTimestamp());
                        data.put("type", msg.getType().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    messages.add(data.toString());
                    // IPG - Alteração -- 05/02/19 -- JDinis
                }

            });
        }


    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }
    // IPG - Alteração -------------- Daey


    // IPG - Alteração -- 05/02/19 -- JDinis
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ChatView chatView = (ChatView) findViewById(R.id.chat_view);
        outState.putStringArrayList("Messages",messages);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ChatView chatView = (ChatView) findViewById(R.id.chat_view);
        if(savedInstanceState!=null && savedInstanceState.containsKey("Messages")) {
            for (String json :
                    savedInstanceState.getStringArrayList("Messages")) {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    chatView.addMessage(new ChatMessage(jsonObject.getString("message"), jsonObject.getLong("time"), Type.valueOf(jsonObject.getString("type")), jsonObject.getString("username")));
                    messages.add(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // IPG - Alteração -- 05/02/19 -- JDinis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String reqString = Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

        // IPG - Alteração -------------- Daey
        mSocket.on("new message", onNewMessage);
        mSocket.emit("add user", reqString);
        mSocket.connect();
        // IPG - Alteração -------------- Daey


        ChatView chatView = (ChatView) findViewById(R.id.chat_view);

    //    chatView.addMessage(new ChatMessage("Message received", System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
    //    chatView.addMessage(new ChatMessage("A message with a sender name",
     //           System.currentTimeMillis(), ChatMessage.Type.RECEIVED, "Ryan Java"));


        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                // IPG - Alteração -------------- Daey
                ChatMessage msg = new ChatMessage(chatMessage.getMessage(),
                        System.currentTimeMillis(), Type.SENT, "Daey");

                mSocket.emit("new message", chatMessage.getMessage());
                // IPG - Alteração -------------- Daey

                // IPG - Alteração -- 05/02/19 -- JDinis
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username",msg.getSender());
                    jsonObject.put("message",msg.getMessage());
                    jsonObject.put("time",msg.getTimestamp());
                    jsonObject.put("type", msg.getType().toString());
                    messages.add(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // IPG - Alteração -- 05/02/19 -- JDinis
                return true;
            }
        });

        chatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {

            }

            @Override
            public void userStoppedTyping() {

            }
        });
    }
}
