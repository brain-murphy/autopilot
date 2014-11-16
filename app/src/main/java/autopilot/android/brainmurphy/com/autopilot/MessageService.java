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
import android.provider.Settings.Secure;

/**
 * Created by connorrmounts on 11/15/14.
 */
public class MessageService extends IntentService {


    // Define the connection-string with your values.
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;" +
                    "AccountName=connor;" +
                    "AccountKey=pY9dFj30ulxW0DF06SxdbC+4rezZsSllyVBAytd0qEFlh7gC221cdNa3Yi9COWN9hVd/dc394cIV9VNeeZQzBA==";

    static final String PARTITION = "partition";
    static final String PARTITION_KEY = "PartitionKey";

    private SmsManager smsManager;
    private SmsReceiver smsReceiver;
    private IntentFilter intentFilter;
    private String userNumber;
    private CloudTable messageTable;
    private CloudTable responseTable;


    public MessageService() {
        super("MessageService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread() {
            public void run() {
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


                }
                catch (Exception e)
                {
                    // Output the stack trace.
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {

            public void run() {
                while (true) {
                    try {
                        String partitionFilter = TableQuery.generateFilterCondition(PARTITION_KEY,
                                TableQuery.QueryComparisons.EQUAL,
                                PARTITION);

                        // Specify a partition query, using PARTITION as the partition key filter.
                        TableQuery<Response> partitionQuery = TableQuery.from(Response.class)
                                .where(partitionFilter);

                        // Loop through the results, displaying information about the entity.
                        for (Response r : responseTable.execute(partitionQuery)) {

                            smsManager.sendTextMessage(r.getAddress(),null,
                                                        r.getResponse(),null,null);
                            TableOperation deleteResponse = TableOperation.delete(r);

                            responseTable.execute(deleteResponse);

                            Log.d("Returned", "We have a response.");

                        }
                        break;
                    } catch (Exception e) {
                        Log.d("Query", "No Responses");
                    }
                }
            }
        }.start();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        public void onReceive(final Context context, final Intent intent) {
            new Thread() {
                public void run() {
                    userNumber = Secure.getString(context.getContentResolver(),
                            Secure.ANDROID_ID);

                    Bundle myBundle = intent.getExtras();
                    android.telephony.SmsMessage [] messages = null;

                    if (myBundle != null) {
                        Object [] pdus = (Object[]) myBundle.get("pdus");
                        messages = new android.telephony.SmsMessage[pdus.length];

                        messages[0] = android.telephony.SmsMessage.createFromPdu((byte[]) pdus[0]);
                        Message message = new Message(userNumber);
                        message.setMessage(messages[0].getDisplayMessageBody());
                        message.setRecipient(messages[0].getDisplayOriginatingAddress());

                        // Create an operation to add the new customer to the people table.
                        TableOperation insertMessage = TableOperation.insertOrReplace(message);

                        // Submit the operation to the table service.
                        try {
                            messageTable.execute(insertMessage);
                            Log.d("MessageUp", "Message Uploaded");
                        } catch (StorageException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

    }

    public static class Message extends TableServiceEntity {
        public String message;
        public String address;
        public String recipient;

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String m) {
            this.message = m;
        }

        public String getAddress() {
            return this.address;
        }

        public void setAddress(String a) {
            this.address = a;
        }

        public String getRecipient() {
            return this.recipient;
        }

        public void setRecipient(String r) {
            this.recipient = r;
        }

        public Message() {
            this.partitionKey = PARTITION;
            this.rowKey = "ROW";
        }

        public Message(String row) {
            this.partitionKey = PARTITION;
            this.rowKey = row;
            this.address = row;
        }
    }

    public static class Response extends TableServiceEntity {
        public String response;
        public String address;
        public String recipient;

        public String getResponse() {
            return this.response;
        }

        public void setResponse(String m) {
            this.response = m;
        }

        public String getAddress() {
            return this.address;
        }

        public void setAddress(String a) {
            this.address = a;
        }

        public String getRecipient() {
            return this.recipient;
        }

        public void setRecipient(String r) {
            this.recipient = r;
        }

        public Response() {
            this.partitionKey = PARTITION;
            this.rowKey = "ROW";
        }

        public Response(String row) {
            this.partitionKey = PARTITION;
            this.rowKey = row;
            this.address = row;
        }
    }

    private String[] getEnabledContactsNumbers() {
        Map<String, ?> map = getSharedPreferences(getString(R.string.shared_pref_key),
                MODE_PRIVATE).getAll();
        map.remove(MainActivity.KEY_ALL_SWITCH);
        String[] results = new String[map.size()];
        return map.keySet().toArray(results);
    }

    private boolean getAllEnabled() {
        return getSharedPreferences(getString(R.string.shared_pref_key),
                MODE_PRIVATE).getBoolean(MainActivity.KEY_ALL_SWITCH, false);
    }
}