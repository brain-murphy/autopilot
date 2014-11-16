package autopilot.android.brainmurphy.com.autopilot;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import static autopilot.android.brainmurphy.com.autopilot.APSQLiteHelper.ENABLED_CONTACTS_COLUMNS;
import static autopilot.android.brainmurphy.com.autopilot.APSQLiteHelper.TABLE_ENABLED_CONTACTS;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String KEY_QUERY = "queryKey";
    private static final String KEY_SELECTION = "selectionKey";
    public static final String KEY_ALL_SWITCH = "allsqitch";

    private MarkovModel model;

    private ListView contactsListView;

    private CursorAdapter adapter;

    private Loader loader;

    private ArrayList<Long> enabledChildren;

<<<<<<< HEAD
=======

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* This is the code for pulling data from phone. Not needed for this iteration.
        MessageData data = new MessageData();

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            TextMessage txt = new TextMessage(cursor.getString(cursor.getColumnIndex("address")),
                    cursor.getString(cursor.getColumnIndex("body")),
                    cursor.getDouble(cursor.getColumnIndex("date")),
                    cursor.getInt(cursor.getColumnIndex("thread_id")),
                    false);
            data.addTextMessage(txt);
            cursor.moveToNext();
        }

        Log.d("Check 1", "Check 1 Check");
        cursor = getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            TextMessage txt = new TextMessage(cursor.getString(cursor.getColumnIndex("address")),
                    cursor.getString(cursor.getColumnIndex("body")),
                    cursor.getDouble(cursor.getColumnIndex("date")),
                    cursor.getInt(cursor.getColumnIndex("thread_id")),
                    true);
            data.addTextMessage(txt);
            cursor.moveToNext();
        }*/

        Intent intent = new Intent(this, MessageService.class);
        startService(intent);

        contactsListView = (ListView) findViewById(R.id.contactsListView);
        enabledChildren = new ArrayList<Long>();
        Map<String, ?> map = getSharedPreferences(getString(R.string.shared_pref_key), MODE_PRIVATE).getAll();
        map.remove(KEY_ALL_SWITCH);
        for (String key : map.keySet()) {
            enabledChildren.add((Long) map.get(key));
        }
        adapter = new DualCursorAdapter(this,
                R.layout.list_item_row,
                null,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
                new int[]{android.R.id.text1},
                0, enabledChildren);
        contactsListView.setAdapter(adapter);



        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.getCursor().moveToPosition(position);
                String contactId =
                        adapter.getCursor().getString(
                                adapter.getCursor().getColumnIndex(ContactsContract.Contacts._ID));
                //
                //  Get all phone numbers.
                //

                ContentResolver cr = getContentResolver();
                Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                String number = null;
                while (phones.moveToNext()) {
                    number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    switch (type) {
                        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                            // do something with the Home number here...
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                            // do something with the Mobile number here...
                            break;
                        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                            // do something with the Work number here...
                            break;
                    }
                }
                phones.close();
                if (enabledChildren.contains(id)) {
                    enabledChildren.remove(id);
                    getSharedPreferences(getString(R.string.shared_pref_key), MODE_APPEND).edit().remove(number).commit();
                } else {
                    getSharedPreferences(getString(R.string.shared_pref_key), MODE_APPEND).edit().putLong(number, id).commit();
                    enabledChildren.add(id);
                }
                adapter.notifyDataSetChanged();
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = "";
                break;
        }
    }



>>>>>>> 0fb9ef04fdbcb88b47d7b19eb4ce70583ddc70da
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main, menu);
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.contactSearchWidget).getActionView();
            // Assumes current activity is the searchable activity
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Bundle data = null;
                    if (newText.length() > 0) {
                        data = new Bundle();
                        data.putString(KEY_SELECTION, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                                + " LIKE ?");
                        data.putStringArray(KEY_QUERY, new String[]{"%" + newText + "%"});
                    }
                    getLoaderManager().restartLoader(0, data, MainActivity.this);
                    Log.d("onQueryTextChanged", "called");
                    return true;
                }
            });
            searchView.setIconifiedByDefault(true);

        Switch s = (Switch) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.switchForActionBar);
        getSharedPreferences(getString(R.string.shared_pref_key), MODE_PRIVATE).getBoolean(KEY_ALL_SWITCH, false);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences(getString(R.string.shared_pref_key), MODE_PRIVATE)
                        .edit().putBoolean(KEY_ALL_SWITCH, isChecked).commit();
            }
        });
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


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("OnCreateLoader", "Called");
        return new CursorLoader(
                this,
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
                args == null ? null : args.getString(KEY_SELECTION),
                args == null ? null : args.getStringArray(KEY_QUERY),
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
