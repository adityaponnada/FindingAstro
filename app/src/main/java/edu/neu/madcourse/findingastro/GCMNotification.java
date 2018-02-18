package edu.neu.madcourse.findingastro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

/**
 * Created by adityaponnada on 22/04/16.
 */
public class GCMNotification {
    private AsyncTask<Void, Void, Void> sendNotificationTask;
    private int status;

    public void sendNotification(final Map<String, String> msgParams,
                                 final List<String> regIds, final Context context) {

        sendNotificationTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... Void) {

                try {
                    post(msgParams, regIds);
                    Log.d("REACHED", "GCM NOTIF IN BACKGROUND");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                if (status != 200) {
                    Toast.makeText(context.getApplicationContext(),
                            "message failed... status: " + status,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context.getApplicationContext(),
                            "message sent... notification coming",
                            Toast.LENGTH_SHORT).show();
                }

                sendNotificationTask = null;
            }
        };
        sendNotificationTask.execute(null, null, null);
    }

    @SuppressLint("NewApi")
    private void post(Map<String, String> params, List<String> regIds)
            throws IOException {

        URL url;
        try {
            url = new URL(findingAstro_communication_constants.BASE_URL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: "
                    + findingAstro_communication_constants.BASE_URL);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet()
                .iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            Log.d("REACHED:", "onPostExecute");
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        // add the regId to the end
        String body = bodyBuilder.toString();
        Log.d(String.valueOf(regIds.size()), regIds.toString());
        Iterator<String> regIdIterator = regIds.iterator();
        while (regIdIterator.hasNext()) {
            body += "&registration_id=" + regIdIterator.next();
        }
        Log.d("HTML info: ", body);
        byte[] bytes = body.getBytes();
        HttpsURLConnection conn = null;

        try {
            Log.e("URL", "> " + url);

            conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });

            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "key="
                    + findingAstro_communication_constants.GCM_API_KEY);
            conn.setRequestMethod("POST");

            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            // handle the response
            status = conn.getResponseCode();
            Log.d("GCM status: ", String.valueOf(status));
            if (status != 200) {
                throw new IOException("GCM Post failed with error code "
                        + status);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
