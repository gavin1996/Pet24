package com.nthu.internetofthing.priend;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nthu.internetofthing.priend.data.AccountHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


class LoginFragment extends Fragment{
    private EditText UserId;
    private EditText UserPassword;
    private Button SignInButton;

    public LoginFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login,container,false);

        UserId = (EditText) rootView.findViewById(R.id.login_id);
        UserPassword = (EditText) rootView.findViewById(R.id.login_password);
        SignInButton = (Button) rootView.findViewById(R.id.login_signin_btn);

        SignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginAuthorization authorization = new LoginAuthorization();
                authorization.execute(UserId.getText().toString(),
                        UserPassword.getText().toString());
            }
        });


        return rootView;
    }

    public class LoginAuthorization extends AsyncTask<String,Void,String>{
        final String LOG_TAG = LoginAuthorization.class.getSimpleName();

        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        final String WEB_SERVER = "web_server";
        final String SECURE = "secure";
        final static String ACCESS_TOKEN = "access-token";

        final String AUTHORIZED = "AUTHORIZED";

        final static String BASE_URL = "http://s103062161.web.2y.idv.tw/InternetOfThing_Priend";
        final static String LOGIN_ID = "user_name";
        final static String LOGIN_PASSWORD = "user_pass";

        Boolean networkService = true;

        @Override
        protected String doInBackground(String... params) {
            //Http Url Connection and get the result from web server and pass it to onPostExecute
            try{
                final String LOGIN_BASE_URL = BASE_URL + "/login.php";

                URL url = new URL(LOGIN_BASE_URL);
                String urlParameters = LOGIN_ID + "=" + params[0] + "&" + LOGIN_PASSWORD + "=" +
                        params[1];

                Log.v(LOG_TAG, url.toString() + urlParameters);

                ConnectivityManager connectivityManager = (ConnectivityManager) getContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null || !networkInfo.isConnected()){
                    networkService = false;
                    return null;
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                DataOutputStream dStream = new DataOutputStream(urlConnection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if(inputStream == null) return null;

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line + "\n");
                }
                inputStream.close();

                if(stringBuffer.length() == 0) return null;

                Log.v(LOG_TAG, stringBuffer.toString());

                return stringBuffer.toString();

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG,e.getMessage(),e);
            } catch (IOException e) {
                Log.e(LOG_TAG,e.getMessage(),e);
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(bufferedReader != null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing buffer.", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String b) {
            //String are JSONObject need to decode and get the result
            try{
                if(b == null || b.length() == 0) {
                    Log.e(LOG_TAG, "Unable to get return json.");
                    if(!networkService)
                        Toast.makeText(getContext(),"Unable to connect to internet. Please check for network service."
                                , Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject response = new JSONObject(b);
                JSONObject web_server = response.getJSONObject(WEB_SERVER);
                String result = web_server.getString(SECURE);

                if(result.equals(AUTHORIZED)){
                    //Start new Activity
                    String access_token = web_server.getString(ACCESS_TOKEN);
                    Log.v(LOG_TAG, access_token);

                    AccountHelper accountHelper = new AccountHelper(getContext());
                    accountHelper.deleteData();
                    if(accountHelper.insertData(UserId.getText().toString(), access_token))
                        Log.v(LOG_TAG, "inserted to database");
                    else
                        Log.e(LOG_TAG, "failed to insert to database");

                    Intent intent = new Intent(getActivity(), PetListActivity.class)
                            .putExtra(ACCESS_TOKEN, access_token);
                    startActivity(intent);
                }else{
                    if(networkService)
                        Toast.makeText(getContext(),"Wrong Username or Password.",
                            Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(),"Unable to connect to internet. Please check for network service."
                        , Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG,e.getMessage(),e);
            }
        }
    }
}
