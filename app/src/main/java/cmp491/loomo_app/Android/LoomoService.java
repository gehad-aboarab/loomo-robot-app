package cmp491.loomo_app.Android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LoomoService extends Service {
    private LoomoMain loomoActivity;

    public LoomoService() { }

    @Override
    public void onCreate() {
        loomoActivity = new LoomoMain(getApplication());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        loomoActivity.onStart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        loomoActivity.onStop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
