package autopilot.android.brainmurphy.com.autopilot;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.SearchManager;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

import static autopilot.android.brainmurphy.com.autopilot.APSQLiteHelper.ENABLED_CONTACTS_COLUMNS;
import static autopilot.android.brainmurphy.com.autopilot.APSQLiteHelper.TABLE_ENABLED_CONTACTS;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String KEY_QUERY = "queryKey";
    private static final String KEY_SELECTION = "selectionKey";

    private MarkovModel model;

    private ListView contactsListView;

    private CursorAdapter adapter;

    private Loader loader;

    private ArrayList<Long> enabledChildren;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // This code should be called when autopilot is in use
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
        }

        Intent intent = new Intent(this, MessageService.class);
<<<<<<< HEAD
        //intent.putExtra(MessageService.KEY_MESSAGE_DATA, data);
       // startService(intent);




        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
=======
        //TODO startService(intent);
>>>>>>> bd9127f813a060b8d08d715ab9fb51e185341b9d

        contactsListView = (ListView) findViewById(R.id.contactsListView);
        enabledChildren = new ArrayList<Long>();
        Map<String, ?> map = getSharedPreferences("asdfasdf", MODE_PRIVATE).getAll();
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
                if (enabledChildren.contains(id)) {
                    enabledChildren.remove(id);
                    getSharedPreferences("asdfasdf", MODE_APPEND).edit().remove(Long.toString(id)).commit();
                } else {
                    getSharedPreferences("asdfasdf", MODE_APPEND).edit().putLong(Long.toString(id), id).commit();
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
