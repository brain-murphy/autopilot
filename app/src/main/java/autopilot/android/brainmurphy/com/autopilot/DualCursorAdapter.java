package autopilot.android.brainmurphy.com.autopilot;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

import static autopilot.android.brainmurphy.com.autopilot.APSQLiteHelper.*;

/**
 * Created by Brian on 11/15/2014.
 */
public class DualCursorAdapter extends SimpleCursorAdapter{

    private ArrayList<Long> enabledChildren;
    private Context context;

    public DualCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags,
                            ArrayList<Long> enabledCursor) {
        super(context, layout, c, from, to, flags);
        this.enabledChildren = enabledCursor;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        getCursor().moveToPosition(position);
        if (enabledChildren.contains(getCursor().getLong(getCursor().getColumnIndex(ContactsContract.Contacts._ID)))) {
            view.setBackgroundColor(context.getResources().getColor(R.color.active));
        } else {
            view.setBackgroundColor(context.getResources().getColor(R.color.inactive));
        }
        
        return view;
    }
}
