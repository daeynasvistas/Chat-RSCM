package pt.IPG.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class Tools {

    private static String myLocation;
    static List<Address> addresses;

    public static String getJSONFromUrl(String ConversationID, SharedPreferences settings) {

        String tokenOK = settings.getString("token", ""/*default value*/);
        String result ="";
        try {
            //Connect
            // cache problema .... + "?_=" + System.currentTimeMillis()
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://chat-ipg-04.azurewebsites.net/api/chat/"+ConversationID+ "?_=" + System.currentTimeMillis()).openConnection());
            //   urlConnection.setDoOutput(false);
            urlConnection.setRequestMethod("GET");
            urlConnection.setUseCaches(false);

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
            urlConnection.disconnect();

        } catch (UnsupportedEncodingException e){
            return result;
            //  e.printStackTrace();
        } catch (IOException e) {
            return result;
            // e.printStackTrace();
        }

        return result;

    }

    public static String sendReplyToConversation(String ConversationID, String msg, SharedPreferences settings) {
        String tokenOK = settings.getString("token", ""/*default value*/);

        String result ="";
        try {
            //Connect
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://chat-ipg-04.azurewebsites.net/api/chat/"+ConversationID).openConnection());
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Authorization", tokenOK);

            String params =  "composedMessage="+msg;
            urlConnection.setRequestProperty("Content-Length", Integer.toString(params.getBytes().length));

            urlConnection.connect();
            urlConnection.setConnectTimeout(10000);

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(params);
            writer.close();
            outputStream.close();

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



    /// ---------------------------------- FIM GOOGLE GPS --------------------------

    //Get last known location coordinates
    protected static void getLastLocationNewMethod(final Context applicationContext, final SharedPreferences settings) {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext);
        if (ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                    getAddress(myLat, myLon,applicationContext,settings);


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
    public static void getAddress(double lat, double lng, Context applicationContext, SharedPreferences settings) {
        String currentLocation = "";

        Geocoder geocoder = new Geocoder(applicationContext, Locale.ENGLISH);
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
            //Log.v("IGA", "Address" + add);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //store
       // SharedPreferences settings = getSharedPreferences("myPrefs", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lyLocation", myLocation);
        editor.apply();
    }


    /// ---------------------------------- FIM GOOGLE GPS --------------------------


    @NonNull
    public static String getPictureString(List<File> returnedPhotos) {
        //converter imagem em base64 ---- Aqui
        Bitmap bm = BitmapFactory.decodeFile(returnedPhotos.get(returnedPhotos.size()-1).toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Bitmap converetdImage = getResizedBitmap(bm, 250);
        Bitmap.createScaledBitmap(converetdImage, 250, 250, true);


        converetdImage.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        String encodedImage = "5_";
        encodedImage += Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }


    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}
