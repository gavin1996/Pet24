package com.nthu.internetofthing.priend;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nthu.internetofthing.priend.data.AccountHelper;

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

public class PetListFragment extends Fragment {
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    android.support.design.widget.FloatingActionButton addPetListButton;

    List<PetListData> Data = new ArrayList<PetListData>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updatePetList();
    }

    private void updatePetList(){
        AccountHelper accountHelper = new AccountHelper(getContext());

        PetList petList = new PetList();
        petList.execute(accountHelper.getAccessToken());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_pet_list, container, false);

        recyclerView = (RecyclerView) rootview.findViewById(R.id.petlistview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(getContext())
        );
        recyclerViewAdapter = new RecyclerViewAdapter(Data, getContext());
        recyclerView.setAdapter(recyclerViewAdapter);

        addPetListButton = (android.support.design.widget.FloatingActionButton) rootview.findViewById(R.id.petlist_action_add);
        addPetListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext(), R.style.AppTheme_Dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_add_pet_device);

                Window window = dialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);

                final EditText editView_PetName = (EditText) dialog.findViewById(R.id.editTex_petname);
                final EditText editView_DeviceId = (EditText) dialog.findViewById(R.id.editText_deviceId);
                Button add_pet_submit = (Button) dialog.findViewById(R.id.button_pet_dialog_add);
                Button add_pet_cancel = (Button) dialog.findViewById(R.id.button_pet_dialog_cancel);

                add_pet_submit.setOnClickListener(new View.OnClickListener() {
                    private final String LOG_TAG = this.getClass().getSimpleName();

                    @Override
                    public void onClick(View v) {
                        Log.v(LOG_TAG, "add_pet_submit Clicked!");
                        AddPetList addPetList = new AddPetList();
                        addPetList.execute(editView_PetName.getText().toString(),
                                            editView_DeviceId.getText().toString(),
                                            new AccountHelper(getContext()).getAccessToken());
                        recyclerViewAdapter.updateData(Data);
                        dialog.dismiss();
                    }
                });

                add_pet_cancel.setOnClickListener(new View.OnClickListener() {
                    private final String LOG_TAG = this.getClass().getSimpleName();

                    @Override
                    public void onClick(View v) {
                        Log.v(LOG_TAG, "add_pet_cancel Clicked!");
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        return rootview;
    }

    public class PetListData {
        private String pet_name;
        private String pet_deviceId;

        public PetListData(String pet_name, String pet_deviceId){
            this.pet_name = pet_name;
            this.pet_deviceId = pet_deviceId;
        }

        public void set_name(String pet_name){
            this.pet_name = pet_name;
        }

        public void set_deviceId(String pet_deviceId){
            this.pet_deviceId = pet_deviceId;
        }

        public String get_name(){
            return pet_name;
        }

        public String get_deviceId(){
            return pet_deviceId;
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private final String LOG_TAG = this.getClass().getSimpleName();
        List<PetListData> DataSet;
        Context context;

        public RecyclerViewAdapter(List<PetListData> DataSet, Context context) {
            this.DataSet = DataSet;
            this.context = context;
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView mTextView_name;
            TextView mTextView_deviceId;
            Context context;
            List<PetListData> DataSet;

            public ViewHolder(View itemView, Context context, List<PetListData> DataSet) {
                super(itemView);
                itemView.setOnClickListener(this);
                this.context = context;
                this.DataSet = DataSet;
                mTextView_name = (TextView) itemView.findViewById(R.id.petlist_petname);
                mTextView_deviceId = (TextView) itemView.findViewById(R.id.petlist_deviceId);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                PetListData petListData = this.DataSet.get(position);
                Intent intent = new Intent(this.context, PetlistDetail.class);
                intent.putExtra(PetList.PET_LIST_NAME, petListData.get_name());
                intent.putExtra(PetList.PET_LIST_DEVICEID, petListData.get_deviceId());
                this.context.startActivity(intent);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
            holder.mTextView_name.setText(DataSet.get(position).get_name());
            holder.mTextView_deviceId.setText(DataSet.get(position).get_deviceId());
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_pet,parent,false);
            return new ViewHolder(view, context, DataSet);
        }

        @Override
        public int getItemCount() {
            return DataSet.size();
        }

        public void updateData(List<PetListData> DataSet){
            this.DataSet = DataSet;
            notifyDataSetChanged();
        }
    }

    public class PetList extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = this.getClass().getSimpleName();

        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        final static String PET_LIST = "petlist";
        final static String PET_LIST_ID = "petlist_id";
        final static String PET_LIST_NAME = "petlist_name";
        final static String PET_LIST_DEVICEID = "petlist_deviceId";

        final static String BASE_URL = "http://s103062161.web.2y.idv.tw/InternetOfThing_Priend";
        final static String ACCESS_TOKEN = "access_token";

        Boolean networkService = true;

        @Override
        protected String doInBackground(String... params) {

            try{
                final String PETLIST_BASE_URL = BASE_URL + "/petlist.php";

                URL url = new URL(PETLIST_BASE_URL);
                String urlParameters = ACCESS_TOKEN + "=" + params[0];

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
                    Log.e(LOG_TAG, "Unable to get return json.");
                    if(!networkService)
                        Toast.makeText(getContext(),"Unable to connect to internet. Please check for network service."
                                , Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject response = new JSONObject(jsonObject);
                if(response == null || response.getString(PET_LIST).equals("empty")) {
                    return;
                }

                JSONArray petlistArray = response.getJSONArray(PET_LIST);

                Data.clear();
                for(int i=0; i<petlistArray.length(); i++){
                    JSONObject petObject = petlistArray.getJSONObject(i);
                    PetListData tmpPetListData = new PetListData(
                            petObject.getString(PET_LIST_NAME),
                            petObject.getString(PET_LIST_DEVICEID));
                    Data.add(tmpPetListData);
                }
                recyclerViewAdapter.updateData(Data);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public class AddPetList extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = this.getClass().getSimpleName();

        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;

        final static String PET_LIST = "petlist";
        final static String PET_NAME = "pet_name";
        final static String PET_DEVICEID = "pet_deviceId";

        final static String BASE_URL = "http://s103062161.web.2y.idv.tw/InternetOfThing_Priend";
        final static String ACCESS_TOKEN = "pet_access_token";

        final static String STATE = "state";
        final static String SUCCEED = "new_pet_added";
        final static String UNSUCCEED = "failed_mkdir";

        Boolean networkService = true;

        @Override
        protected String doInBackground(String... params) {

            try{
                final String PETLIST_BASE_URL = BASE_URL + "/add_petlist.php";

                URL url = new URL(PETLIST_BASE_URL);
                String urlParameters = PET_NAME + "=" + params[0] + "&" +
                                       PET_DEVICEID + "=" + params[1] + "&" +
                                        ACCESS_TOKEN + "=" + params[2];

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
                    Log.e(LOG_TAG, "Unable to get return json.");
                    if(!networkService)
                        Toast.makeText(getContext(),"Unable to connect to internet. Please check for network service."
                                , Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject response = new JSONObject(jsonObject);
                JSONObject pet = response.getJSONObject(PET_LIST);
                String state = pet.getString(STATE);

                if(state.equals(SUCCEED)){
                    updatePetList();
                    recyclerView.smoothScrollToPosition(Data.size()+1);
                }else if(state.equals(UNSUCCEED)){
                    Toast.makeText(getContext(), "Failed to make directory.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Failed to add new pet.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }
}
