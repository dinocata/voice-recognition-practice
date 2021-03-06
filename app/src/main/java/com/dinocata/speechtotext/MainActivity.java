package com.dinocata.speechtotext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int MAX_RESULTS = 5;

    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        txtSpeechInput = findViewById(R.id.txtSpeechInput);
        btnSpeak = findViewById(R.id.btnSpeak);

        Log.e("locale", Locale.getDefault().getLanguage());

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // testSVoice();
                promptSpeechInput();
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        5);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        Set<String> categories = getIntent().getCategories();
        if (categories != null) {
            for (String category : categories) {
                Log.e("category", category);
            }
        } else {
            promptSpeechInput();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {


        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS);

        speechRecognizer.setRecognitionListener(this);
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    private void processResult(ArrayList<String> matches) {
        VoiceCommand command = VoiceCommand.getCommandByKeywords(matches);
        if (command != null) {
            switch (command) {
                case START_MEASUREMENT:
                    txtSpeechInput.setText(R.string.result_start_measurement);
                    break;
                case SHOW_LAST_MEASUREMENT:
                    txtSpeechInput.setText(R.string.result_show_last_measurement);
                    break;
                case MEASUREMENT_RD_545:
                    txtSpeechInput.setText(R.string.result_start_rd_545);
                    break;
                case MEASUREMENT_RD_942:
                    txtSpeechInput.setText(R.string.result_start_rd_942);
                    break;
            }

        } else {
            txtSpeechInput.setText(R.string.result_not_recognized);
            promptSpeechInput();
        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        txtSpeechInput.setText(getString(R.string.speech_prompt));
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
        txtSpeechInput.setText("Processing. Please wait ...");
    }

    @Override
    public void onError(int i) {
        promptSpeechInput();
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        processResult(matches);
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    public enum VoiceCommand {
        MEASUREMENT_RD_545(
                new int[] {R.string.rd_545},
                null,
                new int[] {R.string.start, R.string.begin}),
        MEASUREMENT_RD_942(
                new int[] {R.string.rd_942},
                null,
                new int[] {R.string.start, R.string.begin}),
        SHOW_LAST_MEASUREMENT(
                new int[] {R.string.measurement},
                new int[] {R.string.last, R.string.previous},
                new int[] {R.string.show, R.string.display}),
        START_MEASUREMENT(
                new int[] {R.string.measurement, R.string.measuring},
                null,
                new int[] {R.string.start, R.string.begin});

        private int[] keywordNounResources;
        private String[] keywordNouns;

        private int[] keywordAdjectiveResources;
        private String[] keywordAdjectives;

        private int[] keywordVerbResources;
        private String[] keywordVerbs;

        VoiceCommand(@NonNull int[] keywordNounResources, int[] keywordAdjectiveResources, @NonNull int[] keywordVerbResources) {
            this.keywordNounResources = keywordNounResources;
            this.keywordAdjectiveResources = keywordAdjectiveResources;
            this.keywordVerbResources = keywordVerbResources;

            this.keywordNouns = new String[keywordNounResources.length];
            this.keywordVerbs = new String[keywordVerbResources.length];

            if (keywordAdjectiveResources != null) {
                keywordAdjectives = new String[keywordAdjectiveResources.length];
            }
        }

        public void init(Context context) {
            for (int i = 0; i < keywordVerbResources.length; i++) {
                keywordVerbs[i] = context.getString(keywordVerbResources[i]);
            }
            for (int i = 0; i < keywordNounResources.length; i++) {
                keywordNouns[i] = context.getString(keywordNounResources[i]);
            }
            if (keywordAdjectiveResources != null) {
                for (int i = 0; i < keywordAdjectiveResources.length; i++) {
                    keywordAdjectives[i] = context.getString(keywordAdjectiveResources[i]);
                }
            }
        }

        public static void initAll(Context context) {
            for (VoiceCommand voiceCommand : VoiceCommand.values()) {
                voiceCommand.init(context);
            }
        }

        public static boolean containsAnyWord(String word, String ...keywords) {
            if (keywords == null)
                return true;
            for (String k : keywords)
                if (word.contains(k)) return true;
            return false;
        }

        public static VoiceCommand getCommandByKeywords(List<String> keywords) {
            for (String keyword : keywords) {
                for (VoiceCommand voiceCommand : VoiceCommand.values()) {
                    String lowerCase = keyword.toLowerCase();
                    if (containsAnyWord(lowerCase, voiceCommand.keywordVerbs) &&
                            containsAnyWord(lowerCase, voiceCommand.keywordNouns) &&
                            containsAnyWord(lowerCase, voiceCommand.keywordAdjectives)) {
                        return voiceCommand;
                    }
                }
            }
            return null;
        }
    }

}
