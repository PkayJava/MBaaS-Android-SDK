package com.angkorteam.mbaas.sdk.android.library;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.angkorteam.mbaas.sdk.android.library.response.oauth2.OAuth2AuthorizeResponse;
import com.angkorteam.mbaas.sdk.android.library.response.oauth2.OAuth2RefreshResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.WeakHashMap;

import retrofit2.Call;

/**
 * Created by socheat on 4/13/16.
 */
public class MBaaSIntentService extends IntentService {

    private static final String TAG = MBaaSIntentService.class.getName();

    public static final WeakHashMap<Integer, Call> REVOKED = new WeakHashMap<>();

    public static final String SERVICE = "service";
    public static final String SERVICE_ACCESS_TOKEN = "accessToken";
    public static final String SERVICE_REFRESH_TOKEN = "refreshToken";
    public static final String SERVICE_GCM_TOKEN = "gcmToken";

    public static final String GCM_TOKEN = "gcmToken";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String AUTHENTICATED = "authenticated";

    public static final String OAUTH2_RESULT = "result";

    public static final String OAUTH2_STATE = "state";
    public static final String OAUTH2_CODE = "code";
    public static final String OAUTH2_CLIENT_ID = "client_id";
    public static final String OAUTH2_CLIENT_SECRET = "client_secret";
    public static final String OAUTH2_GRANT_TYPE = "grant_type";
    public static final String OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    public static final String OAUTH2_REDIRECT_URI = "redirect_uri";

    //    public static final String TOKEN = "token";
    public static final String SENDER_ID = "senderId";

    public static final String RECEIVER = "receiver";

    private static final String[] TOPICS = {"global"};

    public MBaaSIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("MBaaS", "Service " + intent.getStringExtra(MBaaSIntentService.SERVICE));
        MBaaS mbaas = MBaaS.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (SERVICE_ACCESS_TOKEN.equals(intent.getStringExtra(MBaaSIntentService.SERVICE))) {
            String oauth2Code = intent.getStringExtra(MBaaSIntentService.OAUTH2_CODE);
            String oauth2State = intent.getStringExtra(MBaaSIntentService.OAUTH2_STATE);
            MBaaSClient client = mbaas.getClient();
            Call<OAuth2AuthorizeResponse> responseCall = client.oauth2Authorize(oauth2State, oauth2Code);
            try {
                int eventId = intent.getIntExtra(HttpBroadcastReceiver.EVENT_ID, -1);
                String activity = intent.getStringExtra(HttpBroadcastReceiver.EVENT_ACTIVITY);
                Class<Activity> clazz = (Class<Activity>) Class.forName(activity);
                try {
                    retrofit2.Response<OAuth2AuthorizeResponse> response = responseCall.execute();
                    OAuth2AuthorizeResponse responseBody = response.body();
                    sharedPreferences.edit().putString(MBaaSIntentService.ACCESS_TOKEN, responseBody.getAccessToken()).apply();
                    sharedPreferences.edit().putString(MBaaSIntentService.REFRESH_TOKEN, responseBody.getRefreshToken()).apply();
                    sharedPreferences.edit().putBoolean(MBaaSIntentService.AUTHENTICATED, true).apply();
                    if (!client.hasCommunication()) {
                        client.initCommunication();
                    }
                    if (REVOKED.get(eventId) != null) {
                        try {
                            retrofit2.Response res = REVOKED.get(eventId).execute();
                            Intent intentActivity = new Intent(this, clazz);
                            intentActivity.putExtra(HttpBroadcastReceiver.EVENT, HttpBroadcastReceiver.EVENT_RESPONSE);
                            intentActivity.putExtra(HttpBroadcastReceiver.EVENT_ID, eventId);
                            intentActivity.putExtra(HttpBroadcastReceiver.EVENT_JSON, new Gson().toJson(res.body()));
                            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentActivity);
                        } catch (IOException e) {
                            Intent intentActivity = new Intent(this, clazz);
                            intentActivity.putExtra(HttpBroadcastReceiver.EVENT, HttpBroadcastReceiver.EVENT_FAILURE);
                            intentActivity.putExtra(HttpBroadcastReceiver.EVENT_ID, eventId);
                            intentActivity.putExtra(HttpBroadcastReceiver.EVENT_MESSAGE, e.getMessage());
                            intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentActivity);
                        }
                    } else {
                        Intent intentActivity = new Intent(this, clazz);
                        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentActivity);
                    }
                } catch (IOException e) {
                    Intent intentActivity = new Intent(this, clazz);
                    intentActivity.putExtra(HttpBroadcastReceiver.EVENT, HttpBroadcastReceiver.EVENT_UNAUTHORIZED);
                    intentActivity.putExtra(HttpBroadcastReceiver.EVENT_ID, eventId);
                    intentActivity.putExtra(HttpBroadcastReceiver.EVENT_MESSAGE, e.getMessage());
                    intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentActivity);
                }
            } catch (ClassNotFoundException e) {
            }
        } else if (SERVICE_REFRESH_TOKEN.equals(intent.getStringExtra(MBaaSIntentService.SERVICE))) {
            MBaaSClient client = mbaas.getClient();
            String refreshToken = sharedPreferences.getString(MBaaSIntentService.REFRESH_TOKEN, "");
            Call<OAuth2RefreshResponse> responseCall = client.oauth2Refresh(refreshToken);
            retrofit2.Response<OAuth2RefreshResponse> response = null;
            try {
                response = responseCall.execute();
            } catch (IOException e) {
            }
            if (response != null) {
                OAuth2RefreshResponse responseBody = response.body();
                sharedPreferences.edit().putString(MBaaSIntentService.ACCESS_TOKEN, responseBody.getAccessToken()).apply();
            }
        } else if (SERVICE_GCM_TOKEN.equals(intent.getStringExtra(MBaaSIntentService.SERVICE))) {
            try {
                MBaaSUtils.requestGcm(sharedPreferences, this, mbaas.getConfiguration());
            } catch (Throwable e) {
                sharedPreferences.edit().putString(MBaaSIntentService.ACCESS_TOKEN, "").apply();
            }
        }
        if (intent.hasExtra(MBaaSIntentService.RECEIVER) && intent.getStringExtra(MBaaSIntentService.RECEIVER) != null && !"".equals(intent.getStringExtra(MBaaSIntentService.RECEIVER))) {
            String receiver = intent.getStringExtra(MBaaSIntentService.RECEIVER);
            Intent receiverIntent = new Intent(receiver);
            LocalBroadcastManager.getInstance(this).sendBroadcast(receiverIntent);
        }
    }
}
