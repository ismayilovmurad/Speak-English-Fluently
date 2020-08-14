package com.muradismayilov.speakenglishfluently;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView givenTV, listeningTV, startBTNexplainTV, privacy_policyTV, supportTV;
    Button startBTN, resetBTN;

    private int index;

    ArrayList<String> articles;

    AVLoadingIndicatorView listeningProgress;

    public static final String SHARED_PREFERENCES = "Shared";

    ImageView voiceIV;

    Field[] fields;

    AdView adView;

    InterstitialAd interstitialAd;
    AdRequest adRequest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        givenTV = findViewById(R.id.givenTV);
        listeningTV = findViewById(R.id.listeningTV);
        startBTNexplainTV = findViewById(R.id.startBTNexplainTV);
        startBTN = findViewById(R.id.startBTN);
        resetBTN = findViewById(R.id.resetBTN);
        resetBTN.setOnClickListener(this);
        listeningProgress = findViewById(R.id.listeningProgress);
        listeningProgress.hide();
        voiceIV = findViewById(R.id.voiceIV);
        voiceIV.setOnClickListener(this);
        MobileAds.initialize(this,"ca-app-pub-3531666375863646/9340261292");
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        privacy_policyTV = findViewById(R.id.privacy_policyTV);
        privacy_policyTV.setOnClickListener(this);
        supportTV = findViewById(R.id.supportTV);
        supportTV.setOnClickListener(this);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3531666375863646/3834198451");
        adRequest2 = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest2);

        interstitialAd.setAdListener(new AdListener(){

            @Override
            public void onAdClosed() {
                super.onAdClosed();

                finish();
                MainActivity.this.finish();

                interstitialAd.loadAd(adRequest2);
            }
        });

        fields=R.raw.class.getFields();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        index = sharedPreferences.getInt("index", 0);


        checkPermission();

        final SpeechRecognizer mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                final AlertDialog dialog_result = new AlertDialog.Builder(MainActivity.this).create();
                View view = getLayoutInflater().inflate(R.layout.dialog_result, null);

                TextView conditionTV = view.findViewById(R.id.conditionTV);
                TextView resultTV = view.findViewById(R.id.resultTV);
                Button nextBTN = view.findViewById(R.id.nextBTN);
                Button try_againBTN = view.findViewById(R.id.try_againBTN);
                CardView resultCV = view.findViewById(R.id.resultCV);

                nextBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        index++;
                        SharedPreferences.Editor editor =
                                getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putInt("index", index);
                        editor.commit();

                        dialog_result.dismiss();

                        setText();
                    }
                });

                try_againBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_result.dismiss();
                    }
                });

                // Eliminate the given article
                String givenArticle = givenTV.getText().toString();
                if (givenArticle.contains("?")) {
                    givenArticle = givenArticle.replace("?", "");
                }
                if (givenArticle.contains(",")) {
                    givenArticle = givenArticle.replace(",", "");
                }
                if (givenArticle.contains("\'")) {
                    givenArticle = givenArticle.replace("\'", "'");
                }

                Log.d("GIVEN",givenArticle);

                // Check if the result matches
                if (matches.get(0) != null) {
                    if (matches.get(0).equals(givenArticle)) {
                        nextBTN.setEnabled(true);
                        conditionTV.setText(getResources().getString(R.string.correct));
                    } else {
                        nextBTN.setEnabled(false);
                        nextBTN.setBackgroundResource(R.drawable.disable_button_background);
                        resultCV.setBackgroundColor(getResources().getColor(R.color.colorError));
                        conditionTV.setTextColor(getResources().getColor(R.color.colorError));
                        conditionTV.setText(getResources().getString(R.string.wrong));
                    }

                    resultTV.setText(matches.get(0));
                }

                dialog_result.setView(view);
                dialog_result.getWindow().getAttributes().windowAnimations = R.style.DialogSlide;
                dialog_result.show();

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        startBTN.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        listeningTV.setVisibility(View.INVISIBLE);
                        listeningProgress.hide();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        listeningTV.setVisibility(View.VISIBLE);
                        listeningProgress.show();
                        break;
                }
                return false;
            }
        });

        articles = new ArrayList<>();
        for (String s : getResources().getStringArray(R.array.articles)) {
            articles.add(s);
        }

        setText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resetBTN:
                index = 0;
                SharedPreferences.Editor editor =
                        getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit();
                editor.putInt("index", index);
                editor.commit();
                givenTV.setText(articles.get(index));
            case R.id.voiceIV:
                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(),fields[index].getInt(fields[index]));
                    mediaPlayer.setLooping(false);
                    if(mediaPlayer != null){
                        mediaPlayer.start();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.privacy_policyTV:
                Uri uri = Uri.parse("https://speakenglishfluentlyapp.blogspot.com/p/privacy-policy-martiandeveloper-built.html");
                Intent privacy_policy_intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(privacy_policy_intent);
                break;
            case R.id.supportTV:
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Support");
                builder.setMessage("If you want to support me, just click the ads or review on Google Play :)");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Please accept the Permission :)", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void setText(){
        if (index != 118) {
            givenTV.setText(articles.get(index));
        } else {
            index = 0;
            SharedPreferences.Editor editor =
                    getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE).edit();
            editor.putInt("index", index);
            editor.commit();
            Toast.makeText(this, getResources().getString(R.string.congrats), Toast.LENGTH_SHORT).show();
            givenTV.setText(articles.get(index));
        }
    }

    @Override
    public void onBackPressed() {
        if(interstitialAd.isLoaded()){
            interstitialAd.show();
        }
    }
}