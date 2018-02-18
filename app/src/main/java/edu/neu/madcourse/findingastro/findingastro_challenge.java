package edu.neu.madcourse.findingastro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class findingastro_challenge extends AppCompatActivity {

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    public String value = "";

    //Get the opponent list from FireBase
    HashMap<String, String> opponentsHashMap;
    //Radio buttons to display opponents
    ArrayList<String> opponentsKeysInRadioButtons = new ArrayList<String>();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final String TAG = "GCM Sample Demo";

    EditText playerName; //TODO: 1
    GoogleCloudMessaging gcm;
    RadioGroup playerList;
    SharedPreferences prefs;
    Context context;
    public static String regid;
    public static String regid_opponent;
    //Get playerName and Display challengers


    RemoteClient_findingastro remote;
    public String playerNameSaved;

    final Handler handler = new Handler();

    Timer timer;
    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findingastro_challenge);

        playerName = (EditText) findViewById(R.id.regid);
        Button register_button = (Button) findViewById(R.id.register_button);
        Button notify_button = (Button) findViewById(R.id.notify_button);
        Button goHome_button = (Button) findViewById(R.id.gohome_button);
        playerList = (RadioGroup) findViewById(R.id.playerlist);
        Button refresh_button = (Button)findViewById(R.id.refresh_button);
        Button unregister_button = (Button)findViewById(R.id.unregister_button);

        gcm = GoogleCloudMessaging.getInstance(this);

        context = getApplicationContext();
        Firebase.setAndroidContext(this);
        Firebase myFirebaseRef = new Firebase("https://findingastro.firebaseio.com/");

        remote = new RemoteClient_findingastro(this);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPlayServices()) {
                    regid = getRegistrationId(context);
                    if (TextUtils.isEmpty(regid)) {
                        registerInBackground();
                    }
                }
            }
        });

        goHome_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), findingastro_main.class);
                startActivity(intent);
            }
        });

        notify_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("Click to start Astro Challenge");
                Intent intent = new Intent(getApplicationContext(), skyview_activity.class);
                startActivity(intent);
            }
        });

        refresh_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getAllOpponents();
                Log.d("CLICKED", "REFRESH");
            }
        });

        unregister_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                unregister();
            }
        });
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
                Integer.MIN_VALUE);
        Log.i(TAG, String.valueOf(registeredVersion));
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(findingastro_challenge.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    //setRegisterValues();
                    regid = gcm.register(findingAstro_communication_constants.GCM_SENDER_ID);

                    // implementation to store and keep track of registered devices here


                    msg = "Device registered, registration ID=" + regid;
                    sendRegistrationIdToBackend();
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                // mDisplay.append(msg + "\n");
                //remote.saveValue(playerNameSaved, msg);
                playerNameSaved = playerName.getText().toString();
                Log.d("PLAYER", playerNameSaved);

                remote.saveValue(playerNameSaved, regid);
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        Log.d("REACHED", "PLAY SERVICES CHECK");
        return true;
    }

    private void unregister() {
        Log.d(findingAstro_communication_constants.TAG, "UNREGISTER USERID: " + regid);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    msg = "Sent unregistration";
                    //setUnregisterValues();
                    gcm.unregister();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                removeRegistrationId(getApplicationContext());
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                // ((TextView) findViewById(R.id.communication_display))
                //  .setText(regid);
            }
        }.execute();
    }

    private void removeRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(findingAstro_communication_constants.TAG, "Removig regId on app version "
                + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.commit();
        regid = null;
    }

    //@SuppressLint("NewApi")
    public void sendMessage(final String message) {
        if (regid == null || regid.equals("")) {
            Toast.makeText(this, "You must register first", Toast.LENGTH_LONG)
                    .show();
            Log.d("REACHED", "SEND MESSAGE REGISTER FIRST");
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                List<String> regIds = new ArrayList<String>();
                String reg_device = regid_opponent;
                Map<String, String> msgParams;
                msgParams = new HashMap<>();
                msgParams.put("data.message", message);
                GCMNotification gcmNotification = new GCMNotification();
                regIds.clear();
                regIds.add(reg_device);
                Log.d("REACHED", "SEND MESSAGE GCM");
                gcmNotification.sendNotification(msgParams, regIds, findingastro_challenge.this);
                Log.d("REACHED", "SENT MESSAGE GCM");
                //msg = "sending information..."; //TODO: this msg might have to be changed
                return message;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }.execute(null, null, null);
    }

    public String startTimer(String key) {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        String value = initializeTimerTask(key);
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        // The values can be adjusted depending on the performance
        timer.schedule(timerTask, 5000, 1000);

        return value;
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public String initializeTimerTask(final String key) {

        timerTask = new TimerTask() {
            public void run() {
                Log.d(TAG, "isDataFetched >>>>" + remote.isDataFetched());
                if (remote.isDataFetched()) {
                    handler.post(new Runnable() {

                        public void run() {
                            Log.d(TAG, "Value >>>>" + remote.getValue(key));
                            value = (remote.getValue(key));
                        }
                    });

                    stoptimertask();
                }

            }
        };

        return value;
    }

    public void getAllOpponents() {
        opponentsHashMap = remote.getAll();
        int radioButtonID = 0;
        for (Map.Entry entry : opponentsHashMap.entrySet()) {
            if (!opponentsKeysInRadioButtons.contains(entry.getKey().toString())) {
                opponentsKeysInRadioButtons.add(entry.getKey().toString());
                RadioButton radioButton = new RadioButton(this);
                radioButton.setId(radioButtonID);
                radioButton.setText(entry.getKey().toString());
                final String userName = entry.getKey().toString();
                final String ID = entry.getValue().toString();
                radioButtonID++;
                playerList.addView(radioButton);
                Log.d("RADIOBUTTONS", "ADDED");
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "Player Name: " + userName + " ID: " + ID, Toast.LENGTH_LONG).show();
                        sendMessage(userName + " Challenged");
                        //regid_opponent = startTimer(userName);
                        regid_opponent = ID;
                    }
                });
            }
        }
    }
}
