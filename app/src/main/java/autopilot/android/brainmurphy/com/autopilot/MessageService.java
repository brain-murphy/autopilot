package autopilot.android.brainmurphy.com.autopilot;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;

/**
 * Created by connorrmounts on 11/15/14.
 */
public class MessageService extends IntentService {

    public static String KEY_MESSAGE_DATA = "messdata key";
    // Define the connection-string with your values.
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
                    "AccountName=your_storage_account;" +
                    "AccountKey=your_storage_account_key";

    private SmsManager smsManager;
    private SmsReceiver smsReceiver;
    private IntentFilter intentFilter;
    private CloudTable messageTable;
    private CloudTable responseTable;


    public MessageService() {
        super("MessageService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("MS Created", "MessageService has been created");
        try
        {
            // Retrieve storage account from connection-string.
            CloudStorageAccount storageAccount =
                    CloudStorageAccount.parse(storageConnectionString);

            // Create the table clients.
            CloudTableClient tableClient = storageAccount.createCloudTableClient();

            // Create the tables if they doesn't exist.
            messageTable = new CloudTable("messages",tableClient);
            messageTable.createIfNotExists();

            responseTable = new CloudTable("responses", tableClient);
            responseTable.createIfNotExists();

            final String ROW_KEY = "row";
        }
        catch (Exception e)
        {
            // Output the stack trace.
            e.printStackTrace();
        }

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

        @Override
        public void onReceive(Context context, final Intent intent) {

             Bundle myBundle = intent.getExtras();
             android.telephony.SmsMessage [] messages = null;
             String strMessage = "";

             TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
             String phoneNumber = tMgr.getLine1Number();

             if (myBundle != null)
             {
             Object [] pdus = (Object[]) myBundle.get("pdus");
             messages = new android.telephony.SmsMessage[pdus.length];

             messages[0] = android.telephony.SmsMessage.createFromPdu((byte[]) pdus[0]);
             Message message = new Message(messages[0].getMessageBody(),
                                             phoneNumber, messages[0].getOriginatingAddress());
             }
        }

        private class Message extends TableServiceEntity {

            public Message(String inMessage, String inAddress, String inRecipient) {
                message = inMessage;
                address = inAddress;
                recipient = inRecipient;
            }

            public String message;
            public String address;
            public String recipient;
        }

        private class Response extends TableServiceEntity {

            public Response (String inMessage, String inAddress, String inRecipient) {
                message = inMessage;
                address = inAddress;
                recipient = inRecipient;
            }

            public String message;
            public String address;
            public String recipient;

        }


    }

    private String[] getEnabledContactsNumbers() {

        Map<String, ?> map = getSharedPreferences(getString(R.string.shared_pref_key),
                MODE_PRIVATE).getAll();
        String[] results = new String[map.size()];
        return map.keySet().toArray(results);
    }
}