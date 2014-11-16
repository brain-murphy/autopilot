package autopilot.android.brainmurphy.com.autopilot;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by connorrmounts on 11/15/14.
 */
public class MessageService extends IntentService {

    public static String KEY_MESSAGE_DATA = "messdata key";

    private SmsManager smsManager;
    private SmsReceiver smsReceiver;
    private IntentFilter intentFilter;

    private MarkovModel markovModel;

    public MessageService() {
        super("MessageService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        markovModel = MarkovModel.defaultModel(this);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MS Created", "MessageService has been created");
        smsManager = SmsManager.getDefault();
        smsReceiver = new SmsReceiver();

        intentFilter = new IntentFilter();
        intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);

        registerReceiver(smsReceiver, intentFilter);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(smsReceiver);
        } catch (IllegalArgumentException iae) {}
        Log.d("MS Destroyed", "MessageService has been destroyed");
    }

    private class SmsReceiver extends BroadcastReceiver {

        private final String TAG = this.getClass().getSimpleName();

        @Override
        public void onReceive(Context context, final Intent intent) {

            final PendingResult result = goAsync();
            Thread thread = new Thread() {
                public void run() {

                    Bundle myBundle = intent.getExtras();
                    android.telephony.SmsMessage [] messages = null;
                    String strMessage = "";

                    if (myBundle != null)
                    {
                        Object [] pdus = (Object[]) myBundle.get("pdus");
                        messages = new android.telephony.SmsMessage[pdus.length];

                        messages[0] = android.telephony.SmsMessage.createFromPdu((byte[]) pdus[0]);
                        TextMessage message = new TextMessage(messages[0].getMessageBody(),
                                                               messages[0].getOriginatingAddress(),
                                                                false);
                        Log.d("Text received", message.toString2());
                    }
                }
            };
            thread.start();
        }
    }
}