package cmp491.loomo_app.Services;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.audiodata.RawDataListener;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

import cmp491.loomo_app.Helpers.C;

public class LoomoRecognitionService {

    private static final String TAG = "SeniorSucks_Recog";
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private Recognizer mRecognizer;
    private WakeupListener mWakeupListener; // "OK Loomo"
    private RecognitionListener mRecognitionListener; // Actual command
    private Context context;
    private ServiceInteractionListener mListener;

    public interface ServiceInteractionListener{
        void onServiceInteraction(int callbackCode, Object[] params);
    }

    public LoomoRecognitionService(Context context, ServiceInteractionListener mListener) {
        this.context = context;
        this.mListener = mListener;
        init();
    }

    public void init() {
        mRecognizer = Recognizer.getInstance();
        mRecognizer.bindService(context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "onBind: ");
                setWakeupListener();
                setRecognitionListener();
                dismissRecognition();
                startJourneyRecognition();
                startWakeupAndRecognition();
                if(mListener !=null)
                    mListener.onServiceInteraction(C.CALLBACK_BIND, null);
            }
            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "onUnbind: "+reason);
                if(mListener !=null)
                    mListener.onServiceInteraction(C.CALLBACK_UNBIND, null);
            }
        });
    }

    public void disconnect() {
        stopRecognition();
        mRecognizer.unbindService();
    }
    public void reconnect() { init(); }

    public void startWakeup(){
        try {
            mRecognizer.startWakeupMode(mWakeupListener);
            Log.d(TAG,"Wakeup");
        } catch (VoiceException e) { Log.d(TAG,e.getMessage()); }
    }
    // Loomo automatically goes into recognition mode without having to wakeup
    // When app first starts
    public void startRecognitionAndWakeup(){
        try {
            mRecognizer.startRecognitionAndWakeup(mRecognitionListener,mWakeupListener);
            Log.d(TAG,"Recognizing then Wakeup");
        } catch (VoiceException e) { Log.d(TAG,e.getMessage()); }
    }
    //Loomo waits for keyword to wake up, before starting to recognize
    public void startWakeupAndRecognition(){
        try {
            mRecognizer.startWakeupAndRecognition(mWakeupListener,mRecognitionListener);
            Log.d(TAG,"Wakeup then Recognizing");
        } catch (VoiceException e) { Log.d(TAG,e.getMessage()); }
    }
    public void stopRecognition(){
        try {
            mRecognizer.stopRecognition();
        } catch (VoiceException e) { Log.d(TAG,e.getMessage()); }
    }
    public void startBeamForm(){
        try {
            mRecognizer.startBeamFormingListen(new RawDataListener() {
                @Override
                public void onRawData(byte[] data, int dataLength) {
                    createFile(data, "raw.pcm");
                }
            });
        } catch (VoiceException e) { Log.d(TAG,e.getMessage()); }
    }

    public void stopBeamForm(){
        try {
            mRecognizer.stopBeamFormingListen();
        } catch (VoiceException e) { Log.d(TAG,e.getMessage()); }
    }

    private void createFile(byte[] buffer, String fileName) {
        RandomAccessFile randomFile = null;
        try {
            randomFile = new RandomAccessFile(FILE_PATH + fileName, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.write(buffer);
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            if (randomFile != null) {
                try {
                    randomFile.close();
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    private void setWakeupListener(){
        mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {
                Log.d(TAG, "onStandby");
            }
            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {
                Log.d(TAG, "wakeup word:" + wakeupResult.getResult() + ", angle " + wakeupResult.getAngle());
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_RECOGNITION_RESULT,new Object[]{wakeupResult.getResult()});
            }

            @Override
            public void onWakeupError(String s) {
                Log.d(TAG, "onWakeupError: "+s);
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_WAKEUP_ERROR,new Object[]{s});
            }
        };
    }
    private void setRecognitionListener(){
        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.d(TAG, "onRecognitionStart");
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_RECOGNITION_STARTED,null);
            }
            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                Log.d(TAG, "recognition phase: " + recognitionResult.getRecognitionResult() +
                        ", confidence:" + recognitionResult.getConfidence());
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_RECOGNITION_RESULT,new Object[]{recognitionResult.getRecognitionResult(),recognitionResult.getConfidence()});
                return false;
            }
            @Override
            public boolean onRecognitionError(String s) {
                Log.d(TAG, "onRecognitionError: " + s);
                if(mListener!=null)
                    mListener.onServiceInteraction(C.CALLBACK_RECOGNITION_ERROR,new Object[]{s});
                return false;
            }
        };
    }
    private void dismissRecognition(){
        Slot dismissSlot = new Slot("dismiss");
        List<Slot> controlSlotList = new LinkedList<>();
        // You must say any of these words
        dismissSlot.addWordsArray(new String[]{"dismiss","bye","go away","be gone"});
        dismissSlot.setOptional(false);
        ((LinkedList<Slot>) controlSlotList).addFirst(dismissSlot);
        GrammarConstraint dismissConstraint = new GrammarConstraint("dismissConstraint", controlSlotList);
        try {
            mRecognizer.addGrammarConstraint(dismissConstraint);
        }catch(Exception e){
            Log.d(TAG, "dismissRecognitionException: "+e.getMessage());
        }
    }
    private void startJourneyRecognition(){
        Slot startJourneySlot = new Slot("start");
        List<Slot> controlSlotList2 = new LinkedList<>();
        // You must say any of these words
        startJourneySlot.addWordsArray(new String[]{"start journey","move"});
        startJourneySlot.setOptional(false);
        // These words are optional
        // Eg: 'move' or 'start journey'
        ((LinkedList<Slot>) controlSlotList2).addFirst(startJourneySlot);
        GrammarConstraint startJourneyConstraint = new GrammarConstraint("startJourneyConstraint", controlSlotList2);
        try {
            mRecognizer.addGrammarConstraint(startJourneyConstraint);
        }catch(Exception e){
            Log.d(TAG, "startJourneyRecognitionException: "+e.getMessage());
        }
    }
}