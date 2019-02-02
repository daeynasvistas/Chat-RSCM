package pt.ipg.chatui.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pt.ipg.chatui.ChatView;
import pt.ipg.chatui.models.ChatMessage;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

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
                    chatView.addMessage(new ChatMessage(message,
                            System.currentTimeMillis(), ChatMessage.Type.RECEIVED, username));

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // IPG - Alteração -------------- Daey
        mSocket.on("new message", onNewMessage);
        mSocket.emit("add user", "ANDROID");
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
                        System.currentTimeMillis(), ChatMessage.Type.SENT, "Daey");

                mSocket.emit("new message", chatMessage.getMessage());
                // IPG - Alteração -------------- Daey
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
