package com.crazybotstudio.doratv;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AppConfig extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    static {
        System.loadLibrary("api_config");
    }

    public static native String getApiServerUrl();
    public static native String getApiKey();
    public static native String getPurchaseCode();
    public static native String getYouTubeApiKey();

    public static String API_SERVER_URL;
    public static String API_KEY ;
    //copy your terms url from php admin dashboard & paste below
    public static final String TERMS_URL = "http://demo.redtvlive.com/oxoo/v13/terms/";
    public static final String ENVATO_PURCHASE_CODE = getPurchaseCode();
    public static final String YOUTUBE_API_KEY = getYouTubeApiKey();

    //paypal payment status
    public static final boolean PAYPAL_ACCOUNT_LIVE = false;

    // download option for non subscribed user
    public static final boolean ENABLE_DOWNLOAD_TO_ALL = true;

    //enable RTL
    public static boolean ENABLE_RTL = true;

    //youtube video auto play
    public static boolean YOUTUBE_VIDEO_AUTO_PLAY = false;

    //enable external player
    public static final boolean ENABLE_EXTERNAL_PLAYER = false;

    //default theme
    public static boolean DEFAULT_DARK_THEME_ENABLE = true;

    // First, you have to configure firebase to enable facebook, phone and google login
    // facebook authentication
    public static final boolean ENABLE_FACEBOOK_LOGIN = true;

    //Phone authentication
    public static final boolean ENABLE_PHONE_LOGIN = true;

    //Google authentication
    public static final boolean ENABLE_GOOGLE_LOGIN = true;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        documentReference = db.collection("app").document("6qwPPTWwNSXzQcRp2zzK");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        API_SERVER_URL = Objects.requireNonNull(document.get("Api_server_url")).toString();
                        API_KEY = Objects.requireNonNull(document.get("Api_Key")).toString();
                    }
                }
            }
        });

    }
}
