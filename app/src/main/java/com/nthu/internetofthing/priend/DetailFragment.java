package com.nthu.internetofthing.priend;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ywuan on 12/06/2016.
 */
public class DetailFragment extends Fragment{
    List<String> Data = new ArrayList<String>();

    RecyclerView recyclerView;
    DateRecyclerViewAdapter dateRecyclerViewAdapter;

    String pet_name;
    String pet_deviceId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity().getIntent().hasExtra(PetListFragment.PetList.PET_LIST_NAME)){
            pet_name = getActivity().getIntent()
                        .getExtras().getString(PetListFragment.PetList.PET_LIST_NAME);
        }
        if(getActivity().getIntent().hasExtra(PetListFragment.PetList.PET_LIST_DEVICEID)){
            pet_deviceId = getActivity().getIntent()
                            .getExtras().getString(PetListFragment.PetList.PET_LIST_DEVICEID);
        }

        updatePetDate();
    }

    public void updatePetDate(){
        UpdateDate updateDate = new UpdateDate();
        updateDate.execute(pet_deviceId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail,container,false);

        TextView textView_petname = (TextView) rootView.findViewById(R.id.textView_petname);
        textView_petname.setText(pet_name);

        Button button_feed = (Button) rootView.findViewById(R.id.button_detail_feed);
        button_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedSwitch feedSwitch = new FeedSwitch();
                feedSwitch.execute(pet_deviceId);
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.detail_date_listview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dateRecyclerViewAdapter = new DateRecyclerViewAdapter(Data);
        recyclerView.setAdapter(dateRecyclerViewAdapter);

        return rootView;
    }

    public class UpdateDate extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = this.getClass().getSimpleName();

        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        final static String BASE_URL = "http://s103062161.web.2y.idv.tw/InternetOfThing_Priend";
        final static String PET_DEVICE_ID = "pet_deviceId";
        final static String ACCESS_TOKEN = "pet_access_token";
        final static String WEATHER_DATE = "weather_date";

        Boolean networkService = true;

        @Override
        protected String doInBackground(String... params) {

            try{
                final String PETLIST_BASE_URL = BASE_URL + "/date.php";

                URL url = new URL(PETLIST_BASE_URL);
                String urlParameters = PET_DEVICE_ID + "=" + params[0];

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

                DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
                dataOutputStream.writeBytes(urlParameters);
                dataOutputStream.flush();
                dataOutputStream.close();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();

                if(inputStream == null){
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line + "\n");
                }
                inputStream.close();

                if(stringBuffer.length() == 0) return null;

                Log.v(LOG_TAG, stringBuffer.toString());

                return stringBuffer.toString();

            }catch (MalformedURLException e){
                Log.e(LOG_TAG,e.getMessage(),e);
            }catch (IOException e){
                Log.e(LOG_TAG,e.getMessage(),e);
            }finally{
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
        protected void onPostExecute(String jsonObject) {
            try{
                if(jsonObject == null || jsonObject.length() == 0){
                    return;
                }

                JSONObject response = new JSONObject(jsonObject);
                JSONArray weather_date = response.getJSONArray(WEATHER_DATE);

                for(int i=weather_date.length()-1; i>=0; i--){
                    Data.add(weather_date.getString(i));
                }

                dateRecyclerViewAdapter.updateData(Data);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public class FeedSwitch extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = this.getClass().getSimpleName();

        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        final static String BASE_URL = "http://s103062161.web.2y.idv.tw/InternetOfThing_Priend";
        final static String PET_DEVICE_ID = "pet_deviceId";
        final static String ACCESS_TOKEN = "pet_access_token";
        final static String WEATHER_DATE = "weather_date";

        Boolean networkService = true;

        @Override
        protected String doInBackground(String... params) {

            try{
                final String PETLIST_BASE_URL = BASE_URL + "/switch.php";

                URL url = new URL(PETLIST_BASE_URL);
                String urlParameters = PET_DEVICE_ID + "=" + params[0];

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

                DataOutputStream dataOutputStream = new DataOutputStream(urlConnection.getOutputStream());
                dataOutputStream.writeBytes(urlParameters);
                dataOutputStream.flush();
                dataOutputStream.close();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();

                if(inputStream == null){
                    return null;
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line + "\n");
                }
                inputStream.close();

                if(stringBuffer.length() == 0) return null;

                Log.v(LOG_TAG, stringBuffer.toString());

                return stringBuffer.toString();

            }catch (MalformedURLException e){
                Log.e(LOG_TAG,e.getMessage(),e);
            }catch (IOException e){
                Log.e(LOG_TAG,e.getMessage(),e);
            }finally{
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
        protected void onPostExecute(String jsonObject) {
            try{
                if(jsonObject == null || jsonObject.length() == 0){
                    return;
                }

                JSONObject response = new JSONObject(jsonObject);
                JSONArray weather_date = response.getJSONArray(WEATHER_DATE);

                Data.clear();
                for(int i=weather_date.length()-1; i>=0; i--){
                    Data.add(weather_date.getString(i));
                }

                dateRecyclerViewAdapter.updateData(Data);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public class DateRecyclerViewAdapter extends RecyclerView.Adapter<DateRecyclerViewAdapter.ViewHolder>{
        final String LOG_TAG = this.getClass().getSimpleName();

        List<String> DataSet = new ArrayList<String>();

        public DateRecyclerViewAdapter(List<String> DataSet){
            this.DataSet = DataSet;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.imageView_date);
                textView = (TextView) itemView.findViewById(R.id.textView_date);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.detail_date_litview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(DataSet.get(position).toString());
            switch (position%6){
                case 0:
                    holder.imageView.setImageResource(R.drawable.bullet_0);
                    break;
                case 1:
                    holder.imageView.setImageResource(R.drawable.bullet_1);
                    break;
                case 2:
                    holder.imageView.setImageResource(R.drawable.bullet_2);
                    break;
                case 3:
                    holder.imageView.setImageResource(R.drawable.bullet_3);
                    break;
                case 4:
                    holder.imageView.setImageResource(R.drawable.bullet_4);
                    break;
                case 5:
                    holder.imageView.setImageResource(R.drawable.bullet_6);
                    break;
                default:
                    holder.imageView.setImageResource(R.drawable.bullet_0);
                    break;
            }
            //Log.v(LOG_TAG, Integer.toString(position%6));
        }

        @Override
        public int getItemCount() {
            return DataSet.size();
        }

        public void updateData(List<String> DataSet){
            this.DataSet = DataSet;
            notifyDataSetChanged();
        }
    }
}
