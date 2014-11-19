package autopilot.android.brainmurphy.com.autopilot;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import android.provider.Settings.Secure;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

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

    private Timer timer;

    public MessageService() {
        super("MessageService");
    }

    private boolean isStarted;

    public class MessageServiceBinder extends Binder {
        public boolean getStarted() {
            return isStarted;
        }

        public void notifySelectedContactsChanged() {
            //TODO
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MessageServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                    // Retrieve storage account from connection-string.
//                CloudStorageAccount storageAccount =
//                        null;
//                try {
//                    storageAccount = CloudStorageAccount.parse(storageConnectionString);
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                } catch (InvalidKeyException e) {
//                    e.printStackTrace();
//                }
//
//                // Create the table clients.
//                CloudTableClient tableClient = storageAccount.createCloudTableClient();
//
//                // Create the tables if they doesn't exist.
//                try {
//                    messageTable = new CloudTable("messages",tableClient);
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                } catch (StorageException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    messageTable.createIfNotExists();
//                } catch (StorageException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    responseTable = new CloudTable("responses", tableClient);
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                } catch (StorageException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    responseTable.createIfNotExists();
//                } catch (StorageException e) {
//                    e.printStackTrace();
//                }
//
//                timer = new Timer();
//                timer.scheduleAtFixedRate(new TimerTask() {
//                    @Override
//                    public void run() {
//
//                        if (smsManager != null) {
//                            String partitionFilter = TableQuery.generateFilterCondition(PARTITION_KEY,
//                                    TableQuery.QueryComparisons.EQUAL,
//                                    PARTITION);
//
//                            // Specify a partition query, using PARTITION as the partition key filter.
//                            TableQuery<Response> partitionQuery = TableQuery.from(Response.class)
//                                    .where(partitionFilter);
//
//                            ArrayList<Response> list = new ArrayList<Response>();
//
//                            // Loop through the results, displaying information about the entity.
//                            for (Response r : responseTable.execute(partitionQuery)) {
//                                try {
//                                    if (r.getAddress().equals(userNumber)) {
//                                        smsManager.sendTextMessage(r.getRecipient(), null,
//                                                r.getResponse(), null, null);
//
//
//                                        list.add(r);
//                                    }
//
//                                } catch (NullPointerException e) {
//                                    Log.d("NPE", "NullPointer");
//                                }
//                            }
//
//                            for (Response r : list) {
//                                TableOperation deleteResponse = TableOperation.delete(r);
//
//                                try {
//                                    responseTable.execute(deleteResponse);
//                                } catch (StorageException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//                }, 0, 100);
//            }
//        }).start();
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
        if (timer != null) {
            timer.cancel();
        }
        unregisterReceiver(smsReceiver);
    }

    private class SmsReceiver extends BroadcastReceiver {
        public void onReceive(final Context context, final Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    userNumber = Secure.getString(context.getContentResolver(),
                            Secure.ANDROID_ID);
                    Bundle myBundle = intent.getExtras();
                    android.telephony.SmsMessage [] messages = null;

                    if (myBundle != null) {
                        Object [] pdus = (Object[]) myBundle.get("pdus");
                        messages = new android.telephony.SmsMessage[pdus.length];

                        messages[0] = android.telephony.SmsMessage.createFromPdu((byte[]) pdus[0]);
                        //Message message = new Message(userNumber);
                        String messageIn = messages[0].getDisplayMessageBody();
                        String sender = messages[0].getDisplayOriginatingAddress();

                        String[] enabledContacts = getEnabledContactsNumbers();
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpHost host = new HttpHost("autopilotapi.taptools.net");
                        HttpRequest request = null;
                        try {
                            request = new HttpGet("/api/message?text="
                                    + URLEncoder.encode(messageIn, "Utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        try {
                            HttpResponse response = httpClient.execute(host, request);
                            String str = "";
                            Scanner scn = new Scanner(response.getEntity().getContent());
                            while (scn.hasNextLine()) {
                                str += scn.nextLine();
                            }

                            JSONObject json = new JSONObject(str);
                            Log.d("json", json.toString());
                            smsManager.sendTextMessage(sender, null,
                                    json.getString("Response"), null, null);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

//                        } else if (enabledContacts.length > 0) {
//                            for (String s : enabledContacts) {
//                                if (s.equals(message.getRecipient())) {
//                                    try {
//                                        messageTable.execute(insertMessage);
//                                        Log.d("MessageUp", "Message Uploaded");
//                                    } catch (StorageException e) {
//                                        e.printStackTrace();
//                                    }
////                                    break;
//                                }
//                            }
//                        }


                        // Create an operation to add the new customer to the people table.


                        // Submit the operation to the table service.

                    }
                }
            }).start();
        }

    }

//    public static class Message extends TableServiceEntity {
//        public String message;
//        public String address;
//        public String recipient;
//        public String modelType;
//
//        public String getMessage() {
//            return this.message;
//        }
//
//        public void setMessage(String m) {
//            this.message = m;
//        }
//
//        public String getAddress() {
//            return this.address;
//        }
//
//        public void setAddress(String a) {
//            this.address = a;
//        }
//
//        public String getRecipient() {
//            return this.recipient;
//        }
//
//        public void setRecipient(String r) {
//            this.recipient = r;
//        }
//
//        public String getModelType() { return this.recipient; }
//
//        public void setModelType(String r) { this.modelType = r; }

//        public Message() {
//            this.partitionKey = PARTITION;
//            this.rowKey = "ROW";
//        }
//
//        public Message(String row) {
//            this.partitionKey = PARTITION;
//            this.rowKey = row;
//            this.address = row;
//        }
//    }

//    public static class Response extends TableServiceEntity {
//        public String response;
//        public String address;
//        public String recipient;
//        public String modelType;
//
//        public String getResponse() {
//            return this.response;
//        }
//
//        public void setResponse(String m) {
//            this.response = m;
//        }
//
//        public String getAddress() {
//            return this.address;
//        }
//
//        public void setAddress(String a) {
//            this.address = a;
//        }
//
//        public String getRecipient() {
//            return this.recipient;
//        }
//
//        public void setRecipient(String r) {
//            this.recipient = r;
//        }
//
//        public String getModelType() { return this.recipient; }
//
//        public void setModelType(String r) { this.modelType = r; }
//
////        public Response() {
////            this.partitionKey = PARTITION;
////            this.rowKey = "ROW";
////        }
////
////        public Response(String row) {
////            this.partitionKey = PARTITION;
////            this.rowKey = row;
////            this.address = row;
////        }
//    }
//
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