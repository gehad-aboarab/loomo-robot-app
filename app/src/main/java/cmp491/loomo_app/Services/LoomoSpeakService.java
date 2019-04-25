package cmp491.loomo_app.Services;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.segway.robot.sdk.base.bind.ServiceBinder.BindStateListener;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import cmp491.loomo_app.Helpers.C;
import cmp491.loomo_app.Android.LoomoApplication;

import java.util.Locale;

public class LoomoSpeakService {

    private MediaPlayer player;
    private TextToSpeech tts;

    private static final String TAG = "LoomoSpeakService_Tag";
    private Speaker mSpeaker;
    private Context context;
    private LoomoApplication application;
    private ServiceInteractionListener mListener;

    //constructor
    public LoomoSpeakService(Context context, ServiceInteractionListener mListener) {
        this.context = context;
        this.mListener = mListener;
        init();
        initTts();
    }

    //Instantiate text-to-speech listener
    private void initTts() {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS){

                    int result=tts.setLanguage(Locale.UK);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d(TAG, "This Language is not supported");
                    }
                    setUtteranceListener();
                    if(mListener!=null)
                        mListener.onServiceInteraction(C.CALLBACK_TTS_INIT,null);
                }
            }
        });
    }

    private void setUtteranceListener(){
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_SPEAK_STARTED,new Object[]{utteranceId});
            }
            @Override
            public void onDone(String utteranceId) {
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_SPEAK_DONE,new Object[]{utteranceId});
            }
            @Override
            public void onError(String utteranceId) {
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_SPEAK_ERROR,new Object[]{utteranceId});
            }
        });
    }

    public void setPitch(float pitch){
        tts.setPitch(pitch);
    }
    public void setSpeechRate(float speechRate){
        tts.setSpeechRate(speechRate);
    }
    //Instantiate mSpeaker
    //Bind the service
    public void init() {
        mSpeaker = Speaker.getInstance();
        mSpeaker.bindService(context, new BindStateListener() {
            @Override
            public void onBind() {
                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_BIND, null);
            }

            @Override
            public void onUnbind(String reason) {
                if (mListener != null)
                    mListener.onServiceInteraction(C.CALLBACK_UNBIND, null);
            }
        });
    }

    //Interface to create listeners for an activity using this service
    public interface ServiceInteractionListener {
        void onServiceInteraction(int callbackCode, Object[] params);
    }

    //unbind from service
    public void disconnect() { mSpeaker.unbindService(); }

    //initializes params
    public void reconnect() {
        init();
        initTts();
    }

    //================================SpeakService================================
    // says any sentence
    // delay - amount of time to wait in ms before saying something else
    public void speak(String sentence, String utteranceID) {
        Log.d(TAG, "speak mode");
        setVolume(100);
        tts.speak(sentence,TextToSpeech.QUEUE_ADD,null,utteranceID); //QUEUE_FLUSH maybe?

    }

    // sets the volume of robot's voice
    // value between 1 and 100
    public void setVolume(int volume) {
        Log.d(TAG, "setVolume: " + volume);
        try {
            mSpeaker.setVolume(volume);
        } catch (VoiceException e) {
            Log.d(TAG, "Volume not set: " + e.getMessage());
        }
    }

    //================================MediaPlayer================================
    // Not required for senior project, until further notice

    // Plays an audio clip from local storage
    public void playAudio(String fileName) {
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor = assetManager.openFd(fileName);
            player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.d(TAG, "playVoice " + e.getMessage());
        }
    }
    // Stops the audio clip midway
    public void endAudio() {
        if (player.isPlaying()) {
            player.stop();
            player.reset();
        }
    }
}