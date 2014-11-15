package autopilot.android.brainmurphy.com.autopilot;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO determine whether we need to update our data set and do so//
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();
        int c = 0;
        while (!cursor.isAfterLast()) {
            StringBuilder builder = new StringBuilder();
           // for(int idx=0;idx<cursor.getColumnCount();idx++)
            //{
               // builder.append(cursor.getColumnName(idx));
                builder.append("in: ");
                builder.append(cursor.getString(cursor.getColumnIndex("body")));
            //}
            Log.d(c + "", builder.toString());
            if (c++ > 100000) {
                break;
            }
            cursor.moveToNext();
        }


        cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
        cursor.moveToFirst();
        c = 0;
        while (!cursor.isAfterLast()) {
            StringBuilder builder = new StringBuilder();
            // for(int idx=0;idx<cursor.getColumnCount();idx++)
            //{
            // builder.append(cursor.getColumnName(idx));
            builder.append("sent: ");
            builder.append(cursor.getString(cursor.getColumnIndex("body")));
            //}
            Log.d(c + "", builder.toString());
            if (c++ > 100000) {
                break;
            }
            cursor.moveToNext();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
