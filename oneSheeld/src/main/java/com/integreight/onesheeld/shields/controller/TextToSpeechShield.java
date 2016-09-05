package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.utils.Log;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechShield extends ControllerParent<TextToSpeechShield>
        implements TextToSpeech.OnInitListener {
    private TTsEventHandler eventHandler;
    private TextToSpeech myTTS;
    private float ttsPitch = 1.0f;

    public TextToSpeechShield() {
        super();
    }

    public TextToSpeechShield(Activity activity, String tag) {
        super(activity, tag, true);
    }

    @Override
    public ControllerParent<TextToSpeechShield> init(String tag) {
        myTTS = new TextToSpeech(getApplication(), TextToSpeechShield.this);
        return super.init(tag, true);
    }

    public void setEventHandler(final TTsEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public boolean isSpeaking() {
        return myTTS != null && myTTS.isSpeaking();
    }

    @Override
    public void refresh() {
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.TEXT_TO_SPEECH_SHIELD.getId()) {
            if (frame.getFunctionId() == 0x01) {
                speech(frame.getArgumentAsString(0));
                if (eventHandler != null) {
                    eventHandler.onSpeek(frame.getArgumentAsString(0));
                }
            }
        }
    }

    @Override
    public void reset() {
        if (myTTS != null) {
            myTTS.stop();
            myTTS.shutdown();
            myTTS = null;
        }
    }


    public interface TTsEventHandler {
        public void onSpeek(String txt);

        public void onStop();

        public void onError(String error, int errorCode);
    }

    @SuppressWarnings("unused")
    private void talkToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Extra options
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text example"); // Text
        // message
        // in
        // the
        // recognition
        // dialog

        try {
            // startActivityForResult(intent, RESULT_TALK_CODE);

        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(activity.getApplicationContext(),
                    R.string.text_to_speech_your_device_doesnt_support_text_to_speech,
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    public void speech(String speechText) {
        if (myTTS != null) {
//            Toast.makeText(activity,speechText,Toast.LENGTH_SHORT).show();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                    activity.getPackageName());
            myTTS.speak(speechText, TextToSpeech.QUEUE_FLUSH, //
                    map);
        }

    }

    @Override
    public void onInit(int status) {

        switch (status) {
            case TextToSpeech.SUCCESS:
                notifyHardwareOfShieldSelection();
                configureTTSLocale();
                break;

            case TextToSpeech.ERROR:
                Toast.makeText(activity, R.string.text_to_speech_text_to_speech_failed, Toast.LENGTH_SHORT)
                        .show();
                Log.e("[ERROR] doc.saulmm.text2speech.MainActivity.onInit ",
                        "TTS Failed");
                break;
        }
    }

    private void configureTTSLocale() {
        if (myTTS != null) {
            if (Build.VERSION.SDK_INT >= 21 && myTTS.getDefaultVoice() != null && (myTTS.isLanguageAvailable(myTTS.getDefaultVoice().getLocale()) == TextToSpeech.LANG_AVAILABLE || myTTS.isLanguageAvailable(myTTS.getDefaultVoice().getLocale()) == TextToSpeech.LANG_COUNTRY_AVAILABLE || myTTS.isLanguageAvailable(myTTS.getDefaultVoice().getLocale()) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE)) {
                myTTS.setLanguage(myTTS.getDefaultVoice().getLocale());
            } else if (Build.VERSION.SDK_INT >= 18 && myTTS.getDefaultLanguage() != null && (myTTS.isLanguageAvailable(myTTS.getDefaultLanguage()) == TextToSpeech.LANG_AVAILABLE || myTTS.isLanguageAvailable(myTTS.getDefaultLanguage()) == TextToSpeech.LANG_COUNTRY_AVAILABLE || myTTS.isLanguageAvailable(myTTS.getDefaultLanguage()) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE)) {
                myTTS.setLanguage(myTTS.getDefaultLanguage());
            } else if (myTTS.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
            if (Build.VERSION.SDK_INT >= 15) {
                int listenerResult = myTTS
                        .setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onDone(String utteranceId) {
                                if (eventHandler != null) {
                                    eventHandler.onStop();
                                }
                            }

                            @Override
                            public void onError(String utteranceId) {
                                if (eventHandler != null) {
                                    eventHandler.onError(activity.getString(R.string.text_to_speech_speech_error), 0);
                                }
                            }

                            @Override
                            public void onStart(String utteranceId) {
                            }
                        });
                if (listenerResult != TextToSpeech.SUCCESS) {
                    Toast.makeText(getActivity(), R.string.text_to_speech_failed_utterance_progress,
                            Toast.LENGTH_SHORT).show();
                    Log.e("TAG", "failed to add utterance progress listener");
                }
            } else {
                @SuppressWarnings("deprecation")
                int listenerResult = myTTS
                        .setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String utteranceId) {
                                if (eventHandler != null)
                                    eventHandler.onStop();
                            }
                        });
                if (listenerResult != TextToSpeech.SUCCESS) {
                    Toast.makeText(getActivity(),
                            R.string.text_to_speech_failed_utterance_completion, Toast.LENGTH_SHORT)
                            .show();
                    Log.e("TAG", "failed to add utterance completed listener");
                }
            }
        } else
            myTTS = new TextToSpeech(getApplication(), TextToSpeechShield.this);

    }

    public float getTtsPitch() {
        return ttsPitch;
    }

    public void setTtsPitch(float ttsPitch) {
        if (myTTS.setPitch(.1f) == TextToSpeech.SUCCESS)
            this.ttsPitch = ttsPitch;
    }
}
