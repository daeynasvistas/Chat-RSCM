package pt.IPG.messenger;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pt.IPG.messenger.recyclerview.Chat;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView chats;
    NavigationView navigationView, navigationViewBottom;
    DrawerLayout drawer;

    List<JSONObject> list = new ArrayList<JSONObject>();
    List<Address> addresses;
    ArrayList<String> conversation = new ArrayList<String>();

    List<Chat> data = new ArrayList<>();
    SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String myLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLocation();
        getLastLocationNewMethod();

        setupToolbar(R.id.toolbar, "Messages");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code

                //retrieve
                SharedPreferences settings = MainActivity.this.getSharedPreferences("myPrefs", 0);
                String auth_token_string = settings.getString("token", ""/*default value*/);
                String token = auth_token_string;

                JSONObject request = new JSONObject();
                String result =  getJSONFromUrl();

                try {
                    JSONObject jsonRoot  = new JSONObject(result);
                    JSONArray jsonData = jsonRoot.getJSONArray("conversations");
                    JSONArray array = new JSONArray(jsonData.toString());

                    for (int i = 0; i < jsonData.length(); i++) {
                        list.add(array.getJSONArray(i).getJSONObject(0));
                        try {
                            String conver = String.valueOf(array.getJSONArray(i).getJSONObject(0).getString("conversationId"));
                            String updateDate = String.valueOf(array.getJSONArray(i).getJSONObject(0).getString("updatedAt"));

                            // conversation.add(String.valueOf(array.getJSONArray(i).getJSONObject(0).getString("conversationId")));
                            conversation.add(conver);
                            //2019-03-02T16:25:43.693Z
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                            // SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            Date date =  format.parse(updateDate.replaceAll("Z$", "+0000"));
                            String dateString = newFormat.format(date);

                             conversation.add(dateString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // bundle
                    Bundle b = new Bundle();
                    b.putStringArrayList("Contactos", conversation);
                    b.putString("Localization",myLocation);

                    // enviar lista de contactos
                    FragmentTransaction ft;
                    FragmentHome fragmentHome = new FragmentHome();
                    fragmentHome.setArguments(b);
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.frameLayout, fragmentHome).commit();
                } catch (JSONException e) {
                    //   System.out.println(e.getMessage());
                }
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationViewBottom = findViewById(R.id.nav_view_bottom);
        navigationViewBottom.setNavigationItemSelectedListener(this);

        chats =(TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_chats));
        initializeCountDrawer();
    }


    private void initLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            // faqzer cenas depois de aceitar
        }
    }

    /// ---------------------------------- FIM GOOGLE GPS --------------------------

    //Get last known location coordinates
    private void getLastLocationNewMethod() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //session.saveCurrentLocation("Everywhere");
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // GPS location can be null if GPS is switched off
                if (location != null) {
                    double myLat = location.getLatitude();
                    double myLon = location.getLongitude();

                    getAddress(myLat, myLon);


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("MapDemoActivity", "Error trying to get last GPS location");
                e.printStackTrace();

            }
        });
    }

    //get location name from coordinates
    public void getAddress(double lat, double lng) {
        String currentLocation = "";

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
        try {

            addresses = geocoder.getFromLocation(lat, lng, 1);

            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            add = add + "\n" + obj.getCountryName();
          //  add = add + "\n" + obj.getCountryCode();
          //  add = add + "\n" + obj.getAdminArea();
          //  add = add + "\n" + obj.getPostalCode();
          //  add = add + "\n" + obj.getSubAdminArea();
          //  add = add + "\n" + obj.getLocality();
          //  add = add + "\n" + obj.getSubThoroughfare();
            add = add + "\n" + "Lat:"+lat;
            add = add + "\n" + "Lng:"+lng;

            myLocation = add;
            Log.v("IGA", "Address" + add);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /// ---------------------------------- FIM GOOGLE GPS --------------------------



    public String getJSONFromUrl() {
        SharedPreferences settings = MainActivity.this.getSharedPreferences("myPrefs", 0);
        String tokenOK = settings.getString("token", ""/*default value*/);

        //String tokenOK = "JWT eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJfaWQiOiI1YzY2OWU4YWU0M2UzZDNlMjQ0ZjRhZTciLCJmaXJzdE5hbWUiOiJEYW5pZWwiLCJsYXN0TmFtZSI6Ik1lbmRlcyIsImVtYWlsIjoiZGFuaWVsQGVwdC5wdCIsInJvbGUiOiJNZW1iZXIiLCJpYXQiOjE1NTA0OTQ3NDAsImV4cCI6MTU1MTA5OTU0MH0.pNmjguEXsaHDBIp1Hwt5BuzF74iSlFqsqMZCrendwxk";
        String result ="";
        try {
            //Connect
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("https://chat-ipg-04.azurewebsites.net/api/chat").openConnection());
            //   urlConnection.setDoOutput(false);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Authorization", tokenOK);

            urlConnection.connect();
            urlConnection.setConnectTimeout(10000);

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            bufferedReader.close();
            result = sb.toString();
        } catch (UnsupportedEncodingException e){
            return result;
            //  e.printStackTrace();
        } catch (IOException e) {
            return result;
            // e.printStackTrace();
        }
        return result;
    }


    private void initializeCountDrawer(){
        chats.setGravity(Gravity.CENTER);
        chats.setTypeface(null, Typeface.BOLD);
        chats.setTextColor(getResources().getColor(R.color.colorAccent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            chats.setTextAppearance(R.style.LightNav);
            chats.setTextColor(getResources().getColor(R.color.colorAccent));
        }
        chats.setText("99+");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentTransaction ft;
        int id = item.getItemId();

        if (id == R.id.nav_contacts) {
            FragmentContacts fragmentContacts = new FragmentContacts();
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, fragmentContacts).addToBackStack(null).commit();
        } else if (id == R.id.nav_chats) {
            FragmentHome fragmentHome = new FragmentHome();
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frameLayout, fragmentHome).commit();
        } else if (id == R.id.nav_trash) {
        } else if (id == R.id.nav_settings) {
        } else if (id == R.id.nav_logout) { }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

}
