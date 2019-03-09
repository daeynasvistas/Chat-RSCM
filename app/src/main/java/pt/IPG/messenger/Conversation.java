package pt.IPG.messenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.io.File;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.tajchert.nammu.Nammu;
import pt.IPG.messenger.recylcerchat.ChatData;
import pt.IPG.messenger.recylcerchat.ConversationRecyclerView;


public class Conversation extends BaseActivity  {
    private static final String PHOTOS_KEY = "easy_image_photos_list";

    private RecyclerView mRecyclerView;
    private ConversationRecyclerView mAdapter;
    private EditText text;
    private Button send;
    private ImageButton send_localization;
    private ImageButton send_image;

    // IPG - Alteração -------------- Dinis
    private Encryption encryption;

    String room = "";
    String ID = "";

    SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    // IPG - Alteração -------------- Daey
    private Socket mSocket;
    private String myLocation;
    private ArrayList<File> photos = new ArrayList<>();

    {
        try {
            //mSocket = IO.socket("http://chat-ipg.azurewebsites.net");
            mSocket = IO.socket("http://chat-ipg-04.azurewebsites.net");
        } catch (URISyntaxException e) {}
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Conversation.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    JSONObject dataIO = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = dataIO.getString("username");
                        message = dataIO.getString("message");

                    } catch (JSONException e) {
                        return;
                    }


                    if(!username.equals(ID)){
                        //problema com broadcast to self
                        List<ChatData> data = new ArrayList<ChatData>();
                        ChatData item = new ChatData();
                        SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String currentDateTimeString = newFormat.getDateTimeInstance().format(new Date());

                        item.setTime(currentDateTimeString);
                        // IPG - Alteração -------------- Dinis
                        try {
                            //item.setText(message);
                            message = encryption.Decrypt(message);
                            // DINIS .. não funciona aqui quando recebo do servidor
                            item.setText(new String(message));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        // luis image
                        if (message.startsWith("5_")){
                              message.substring(2);
                              item.setType("1");
                        }else item.setType("1");



                        data.add(item);
                        mAdapter.addItem(data);
                        try {
                            mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() -1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        text.setText("");

                    }
                }
            });
        }
    };




    //---------------------------- Imagens .. envio -.------- v 0.1
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PHOTOS_KEY, photos);
    }

    private void checkGalleryAppAvailability() {
        if (!EasyImage.canDeviceHandleGallery(this)) {
            //Device has no app that handles gallery intent
           // galleryButton.setVisibility(View.GONE);  remover icon da camera que não tens permições
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {


            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(Conversation.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }



    private void onPhotosReturned(List<File> returnedPhotos) {
        photos.addAll(returnedPhotos);
     // fotos aqui
        List<ChatData> data = new ArrayList<ChatData>();
        ChatData item = new ChatData();

        //    String msg= myLocation;
        Date currentTime = Calendar.getInstance().getTime();

        item.setTime(newFormat.format(currentTime));

        String encodedImage = Tools.getPictureString(returnedPhotos);


        // já há string ----
        String msg = encodedImage;

        item.setType("2");
        item.setText(msg);
        data.add(item);
        mAdapter.addItem(data);

        // IPG - Alteração -------------- Dinis
        try {
            msg = encryption.Encrypt(msg, Encryption.MessageType.Encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // background para fazer cenas na base de dados mongop
            final String finalMsg = msg;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //TODO your background code
                    // mongoDB save stuff
                    // IPG - Alteração -------------- Dinis
                    SharedPreferences settings = getApplication().getSharedPreferences("myPrefs", 0);
                    Tools.sendReplyToConversation(room, finalMsg, settings);
                }
            });

            // IPG - Alteração -------------- Dinis
            mSocket.emit("new message", room, msg, ID);
            //mSocket.emit("new message", room, text.getText() , ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //mSocket.emit("refresh messages", text.getText().toString());

        try {
            mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        text.setText("");



        //   imagesAdapter.notifyDataSetChanged();
     //   recyclerView.scrollToPosition(photos.size() - 1);
    }


    //-------------------------------------------------------------------






    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("refresh messages", onNewMessage);
    }
    // IPG - Alteração -------------- Daey



    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            photos = (ArrayList<File>) savedInstanceState.getSerializable(PHOTOS_KEY);
        }

        mAdapter = new ConversationRecyclerView(this,setData());
        room = getIntent().getExtras().getString("roomName",null);
        ID = getIntent().getExtras().getString("ID",null);
        myLocation = getIntent().getExtras().getString("Localization",null);

        setContentView(R.layout.activity_conversation);
        setupToolbarWithUpNav(R.id.toolbar, "Alterar para API getuser" , R.drawable.ic_action_back);

        encryption = new Encryption(room);


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

// initiate progress bar and start button
        final ProgressBar simpleProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        simpleProgressBar.setVisibility(View.VISIBLE);
        // receber conversa do mongodb
        AsyncTask.execute(new Runnable() {
            List<ChatData> data = new ArrayList<ChatData>();

            @Override
            public void run() {
                //TODO your background code
                //retrieve
                SharedPreferences settings = getApplication().getSharedPreferences("myPrefs", 0);
                String result =  Tools.getJSONFromUrl(room,settings);

                try {
                    JSONObject jsonRoot  = new JSONObject(result);
                    JSONArray jsonData = jsonRoot.getJSONArray("conversation");


                    for (int i = 0; i < jsonData.length(); i++) {
                        ChatData item = new ChatData();
                        String time = jsonData.getJSONObject(i).getString("createdAt");
                        //2019-02-19T12:24:06.557Z
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                       // SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = format.parse(time.replaceAll("Z$", "+0000"));


                        item.setTime( newFormat.format(date));

                        // problemas de enviar para todos no nodeJS
                        String author = jsonData.getJSONObject(i).getJSONObject("author").getString("_id");
                        String body = "";
                        try {
                            body = encryption.Decrypt(jsonData.getJSONObject(i).getString("body"));
                            item.setText(body);

                                    if (!author.equals(ID)) {
                                        item.setType("1");
                                      /*  if (item.getText().startsWith("5_")) {
                                            item.setText(body.substring(2));
                                        }
*/
                                    }else{
                                        item.setType("2");
                                        /*
                                        if (item.getText().startsWith("5_")) {
                                            item.setText(body.substring(2));
                                         }
                                         */

                        }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        // IPG - Alteração -------------- Dinis


                        data.add(item);
                    }

                } catch (JSONException e) {
                    //   System.out.println(e.getMessage());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
// update do UI deve ser feito pelo UI
                
                Collections.reverse(data);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        mAdapter.addItem(data);
                        mRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    // todo BUG "Invalid target position"
                                    mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                                    simpleProgressBar.setVisibility(View.INVISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);
                        // Stuff that updates the UI

                    }
                });





            }

        });

        //--fim receber conversas

        // IPG - Alteração -------------- Daey
        mSocket.emit("enter conversation", room);
        mSocket.on("refresh messages", onNewMessage);
        ///mSocket.emit("new message", "Hello !!!!!");


        mSocket.connect();
        // IPG - Alteração -------------- Daey





        text = (EditText) findViewById(R.id.et_message);

        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 500);
            }
        });


        //----Luis enviar imagem
        findViewById(R.id.bt_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCameraForImage(Conversation.this, 0);
            }
        });


        findViewById(R.id.bt_attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Some devices such as Samsungs which have their own gallery app require write permission. Testing is advised! */
                EasyImage.openGallery(Conversation.this, 0);
            }
        });




        // --- Daey enviar localização
        send_localization = (ImageButton) findViewById(R.id.bt_attachment_localization);
        send_localization.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!text.getText().equals("")){
                    List<ChatData> data = new ArrayList<ChatData>();
                    ChatData item = new ChatData();
                    String msg= myLocation;

                    item.setType("2");
                    item.setText(msg);
                    data.add(item);
                    mAdapter.addItem(data);

                    // IPG - Alteração -------------- Dinis
                    try {
                        msg = encryption.Encrypt(msg, Encryption.MessageType.Encrypted);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        // background para fazer cenas na base de dados mongop
                        final String finalMsg = msg;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                //TODO your background code
                                // mongoDB save stuff
                                // IPG - Alteração -------------- Dinis
                                SharedPreferences settings = getApplication().getSharedPreferences("myPrefs", 0);
                                Tools.sendReplyToConversation(room, finalMsg, settings);
                            }
                        });

                        // IPG - Alteração -------------- Dinis
                        mSocket.emit("new message", room,msg, ID);
                        //mSocket.emit("new message", room, text.getText() , ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //mSocket.emit("refresh messages", text.getText().toString());

                    try {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    text.setText("");
                }
            }
        });



         /*
        // --- Luis enviar imagem
        send_image = (ImageButton) findViewById(R.id.bt_attachment);
        send_image.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
               if (!text.getText().equals("")){
                    List<ChatData> data = new ArrayList<ChatData>();
                    ChatData item = new ChatData();

                     //    String msg= myLocation;
                    Date currentTime = Calendar.getInstance().getTime();

                    item.setTime(newFormat.format(currentTime));
                    String msg = "5_http://i.imgur.com/DvpvklR.png";

                    // ImageView ivBasicImage = (ImageView) findViewById(R.id.image_view);
                   // Picasso.with(getApplication()).load(msg).into(ivBasicImage);

                    item.setType("3");
                    item.setText(msg);
                    data.add(item);
                    mAdapter.addItem(data);

                    // IPG - Alteração -------------- Dinis
                    try {
                        msg = encryption.Encrypt(msg, Encryption.MessageType.Encrypted);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        // background para fazer cenas na base de dados mongop
                        final String finalMsg = msg;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                //TODO your background code
                                // mongoDB save stuff
                                // IPG - Alteração -------------- Dinis
                                SharedPreferences settings = getApplication().getSharedPreferences("myPrefs", 0);
                                Tools.sendReplyToConversation(room, finalMsg, settings);
                            }
                        });

                        // IPG - Alteração -------------- Dinis
                        mSocket.emit("new message", room,msg, ID);
                        //mSocket.emit("new message", room, text.getText() , ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //mSocket.emit("refresh messages", text.getText().toString());

                    try {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    text.setText("");
                }
            }

        });

        */


        send = (Button) findViewById(R.id.bt_send);
        send.setOnClickListener(new View.OnClickListener() {
            // IPG - Alteração -------------- Dinis
            String msg="";

            @Override
            public void onClick(View view) {
                if (!text.getText().equals("")){
                    List<ChatData> data = new ArrayList<ChatData>();
                    ChatData item = new ChatData();
                    Date currentTime = Calendar.getInstance().getTime();

                    item.setTime(newFormat.format(currentTime));
                    item.setType("2");
                    if ( text.getText().toString().startsWith("5_")) {
                        item.setText( text.getText().toString().substring(2));
                    }else  item.setText( text.getText().toString());

                    data.add(item);
                    mAdapter.addItem(data);

                    // IPG - Alteração -------------- Dinis
                    try {
                        msg = encryption.Encrypt(text.getText().toString(), Encryption.MessageType.Encrypted);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        // background para fazer cenas na base de dados mongop
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                //TODO your background code
                                // mongoDB save stuff
                                SharedPreferences settings = getApplication().getSharedPreferences("myPrefs", 0);
                                // IPG - Alteração -------------- Dinis
                                Tools.sendReplyToConversation(room, msg, settings);
                            }
                        });

                        // IPG - Alteração -------------- Dinis
                        mSocket.emit("new message", room,msg, ID);
                        //mSocket.emit("new message", room, text.getText() , ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //mSocket.emit("refresh messages", text.getText().toString());

                    try {
                        mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() -1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    text.setText("");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
       // Toast.makeText(this,"Fechar Socket!!",Toast.LENGTH_LONG).show();
        // fechar socket!!!
       // mSocket.disconnect();
      //  mSocket.emit("leave-room", room);
      //  mSocket.off("new message", onNewMessage);
        finish();
        return;
    }


    public List<ChatData> setData(){
        List<ChatData> data = new ArrayList<>();
        return data;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_userphoto, menu);
        return true;
    }








}
