package pt.IPG.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.IPG.messenger.R;

import pt.IPG.messenger.recyclerview.Chat;
import pt.IPG.messenger.recyclerview.ChatAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Dytstudio.
 */


public class FragmentHome extends Fragment implements ChatAdapter.ViewHolder.ClickListener {
    private RecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private TextView tv_selection;
    private List<String> venueList;

    String name[] = {};
    String lastChat[] = {};
    List<JSONObject> list = new ArrayList<JSONObject>();
    ArrayList<String> conversation = new ArrayList<String>();
    String myLocation;

    List<Chat> data = new ArrayList<>();
    SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    public FragmentHome(){
        setHasOptionsMenu(true);
    }
    public void onCreate(Bundle a){
        super.onCreate(a);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null, false);

        //retrieve
        conversation = getArguments().getStringArrayList("Contactos");
        myLocation = getArguments().getString("Localization");

        getActivity().supportInvalidateOptionsMenu();
        ((MainActivity)getActivity()).changeTitle(R.id.toolbar, "Messages");

        tv_selection = (TextView) view.findViewById(R.id.tv_selection);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new ChatAdapter(getContext(),setData(),this);
        mRecyclerView.setAdapter (mAdapter);



        return view;
    }


    public List<Chat> setData(){
        // RECEBER conversas vers 0.1

        //String name[]= {"Rita  Gomes" }; //, "Vicente  Pimentel", "Melissa  Andrade", "Filomena  Nascimento", "Carlos  Sá", "Cristiana  Valente", "Nádia  Lima", "Kevin  Cerqueira", "Manuel  Gonçalves", "Benedita  Fontes" };
        //String lastchat[]= {"Última conversa" }; //, "Última conversa", "Última conversa", "Última conversa", "Última conversa", "Última conversa", "Última conversa", "Última conversa", "Última conversa", "Última conversa" };

        @DrawableRes int img[]= {R.drawable.user1 , R.drawable.user1, R.drawable.user1, R.drawable.user1, R.drawable.user1 , R.drawable.user1 , R.drawable.user1, R.drawable.user1, R.drawable.user1, R.drawable.user1 };
        boolean online[] = {true, false, true, false, true, true, true, false, false, true};

        for (int i = 0; i< conversation.size(); i+= 2){
            Chat chat = new Chat();
            //chat.setmTime("5:04pm");
            chat.setmTime(conversation.get(i+1));
            chat.setName(" beta test");
            chat.setImage(img[i]);
            chat.setOnline(online[i]);
            chat.setLastChat(conversation.get(i));
            data.add(chat);
        }
        return data;

    }




        @Override
    public void onItemClicked (int position) {
        // passar informação sobre conversa
            SharedPreferences settings = getActivity().getSharedPreferences("myPrefs", 0);
            String ID = settings.getString("ID", ""/*default value*/);

            Intent intent = new Intent(getActivity(), Conversation.class);
            String room = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tv_last_chat)).getText().toString();

            intent.putExtra("roomName", room);  // pass your values and retrieve them in the other Activity using keyName
            intent.putExtra("ID", ID);  // pass your values and retrieve them in the other Activity using keyName
            intent.putExtra("Localization", myLocation);  // pass your values and retrieve them in the other Activity using keyName

        startActivity(intent);
     }

    @Override
    public boolean onItemLongClicked (int position) {
        toggleSelection(position);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection (position);
        if (mAdapter.getSelectedItemCount()>0){
            tv_selection.setVisibility(View.VISIBLE);
        }else
            tv_selection.setVisibility(View.GONE);


        getActivity().runOnUiThread(new Runnable() {
            public void run()
            {
                tv_selection.setText("Delete ("+mAdapter.getSelectedItemCount()+")");
            }
        });

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_edit, menu);
    }
}
