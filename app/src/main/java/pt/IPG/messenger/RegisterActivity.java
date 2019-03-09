package pt.IPG.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


public class RegisterActivity extends AppCompatActivity {

    private UserRegisterTask mAuthTask = null;

    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mGenderView;
    private EditText mBirthDateView;
    private EditText mImageView;
    String imageBase64FromGallery = "";

    CallbackManager callbackManager;
    LoginButton loginButton;
    Boolean isLoggedIn;
    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_register);

        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mFirstNameView = findViewById(R.id.firstName);
        mLastNameView = findViewById(R.id.lastName);
        mGenderView = findViewById(R.id.gender);
        mBirthDateView = findViewById(R.id.birthDate);
        mImageView = findViewById(R.id.image);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        isLoggedIn = accessToken != null && !accessToken.isExpired();

        if (isLoggedIn) {
            disconnectFromFacebook();
        }

        dealWithFacebook();

        Button mEmailSignInButton = findViewById(R.id.register_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

    }

    private void dealWithFacebook() {
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_gender"));

        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                try {
                                    final String password = object.getString("id");
                                    final String gender = object.getString("gender");
                                    final String birthDate = object.getString("birthday");
                                    final String firstName = object.getString("first_name");
                                    final String lastName = object.getString("last_name");
                                    final String email = object.getString("email");

                                    mEmailView.setText(email);
                                    mPasswordView.setText(password);
                                    mFirstNameView.setText(firstName);
                                    mLastNameView.setText(lastName);
                                    mGenderView.setText(gender);
                                    mBirthDateView.setText(birthDate);
                                    final String[] b64 = {""};

                                    imageTask obj = new imageTask(password) {
                                        @Override
                                        protected void onPostExecute(String base64) {
                                            super.onPostExecute(base64);
                                            b64[0] = base64;
                                            mAuthTask = new UserRegisterTask(email, password, firstName, lastName, gender, birthDate, b64[0], RegisterActivity.this);
                                            mAuthTask.execute((Void) null);
                                        }
                                    };

                                    obj.execute();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,first_name,last_name");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.v("LoginActivity", "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Bitmap bm = null;
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (bm != null) {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            }

            byte[] byteArray = baos.toByteArray();
            imageBase64FromGallery = Base64.encodeToString(byteArray, Base64.DEFAULT);

            mImageView.setText("The image was chosen");
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();

    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String gender = mGenderView.getText().toString();
        String birthDate = mBirthDateView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {

            mAuthTask = new UserRegisterTask(email, password, firstName, lastName, gender, birthDate, imageBase64FromGallery, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mFirstName;
        private final String mLastName;
        private final String mGender;
        private final String mBirthDate;
        private final String mImageBase64;
        Activity instance;

        UserRegisterTask(String email, String password, String firstName, String lastName, String gender, String birthDate, String imageBase64, Activity instance) {
            mEmail = email;
            mPassword = password;
            mFirstName = firstName;
            mLastName = lastName;
            mGender = gender;
            mBirthDate = birthDate;
            mImageBase64 = imageBase64;
            this.instance = instance;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            JSONObject request = new JSONObject();
            try {
                request.put("email", mEmail);
                request.put("password", mPassword);
                request.put("firstName", mFirstName);
                request.put("lastName", mLastName);
                request.put("gender", mGender);
                String birth = "";
                for(int i = 0; i < mBirthDate.length(); i++)
                {
                    char c = mBirthDate.charAt(i);
                    if(c != '/'){
                        birth = birth + c;
                    }
                }


                request.put("birthDate", birth);
              //  request.put("picture", mImageBase64);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String result = connectWithServer(instance, request);

            if (!TextUtils.isEmpty(result)) {

                try {
                    JSONObject object = new JSONObject(result);
                    JSONObject userObject = object.getJSONObject("user");
                    String token = object.getString("token");
                    String ID = userObject.getString("_id");

                    SharedPreferences settings = getSharedPreferences("myPrefs", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("token", token);
                    editor.putString("ID", ID);
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                finish();

                Intent registerIntent = new Intent(RegisterActivity.this, MainActivity.class);
                registerIntent.putExtra("nome", mFirstName + " " + mLastName);
             //   registerIntent.putExtra("img", mImageBase64);
                startActivity(registerIntent);

            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }


    public String connectWithServer(Activity ctx, JSONObject request) {

        String result = "";
        try {

            //Connect
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://chat-ipg-04.azurewebsites.net/api/auth/register").openConnection());
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();
            urlConnection.setConnectTimeout(10000);

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(request.toString());
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


        } catch (UnsupportedEncodingException e) {
            return result;
        } catch (IOException e) {
            return result;
        }
        return result;
    }

    public void onClickImage(View view) {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);

    }


    public class imageTask extends AsyncTask<Void, Void, String> {

        String mPassword;

        public imageTask(String password) {
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL imageURL = new URL("https://graph.facebook.com/" + mPassword + "/picture?type=large");
                Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                return imageBase64;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
    }

}

