package edu.neu.madcourse.findingastro;

import android.content.Context;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

/**
 * Created by adityaponnada on 21/04/16.
 */
public class RemoteClient_findingastro {
    private static final String MyPREFERENCES = "MyPrefs" ;
    private static final String FIREBASE_DB = "https://findingastro.firebaseio.com/";
    private static final String TAG = "RemoteClient";
    private static boolean isDataChanged = false;
    private Context mContext;
    private HashMap<String, String> fireBaseData = new HashMap<String, String>();

    private HashMap<String, String> returnMap = new HashMap<>();

    public RemoteClient_findingastro(Context mContext)
    {
        this.mContext = mContext;
        Firebase.setAndroidContext(mContext);
    }

    public void saveValue(String key, String value) {
        Firebase ref = new Firebase(FIREBASE_DB);
        Firebase usersRef = ref.child(key);
        usersRef.setValue(value, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.d(TAG, "Data could not be saved. " + firebaseError.getMessage());
                } else {
                    Log.d(TAG, "Data saved successfully.");
                }
            }
        });
    }

    public boolean isDataFetched()
    {
        return isDataChanged;
    }

    public String getValue(String key)
    {
        return fireBaseData.get(key);
    }

    public void fetchValue(String key) {

        Log.d(TAG, "Get Value for Key - " + key);
        Firebase ref = new Firebase(FIREBASE_DB + key);
        Query queryRef = ref.orderByKey();
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // snapshot contains the key and value
                isDataChanged = true;
                if(snapshot.getValue() != null)
                {
                    Log.d(TAG, "Data Received" + snapshot.getValue().toString());

                    // Adding the data to the HashMap
                    fireBaseData.put(snapshot.getKey(), snapshot.getValue().toString());

                }
                else {
                    Log.d(TAG, "Data Not Received");
                    fireBaseData.put(snapshot.getKey(), null);

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, firebaseError.getMessage());
                Log.e(TAG, firebaseError.getDetails());
            }
        });
    }

    public HashMap<String, String> getAll(){
        // fireBaseData.keySet();
        // Get a reference to our posts
        Firebase ref = new Firebase(FIREBASE_DB);
        // Attach an listener to read the data at our posts reference

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("TOTAL ", snapshot.getChildrenCount() + " payers");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String name = postSnapshot.getKey();
                    String id = (String) postSnapshot.getValue(); //check here
                    Log.d("KEY-VALUE-1", name + " " + id);
                    returnMap.put(name, id);
                    //postSnapshot.getKey();
                    // Log.d(post.getUserName(), " - " + post.getUserRegId());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("The read failed: ", firebaseError.getMessage());
            }
        });
        return returnMap;
    }

}
