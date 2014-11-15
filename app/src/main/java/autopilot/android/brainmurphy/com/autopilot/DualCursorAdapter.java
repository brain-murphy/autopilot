package autopilot.android.brainmurphy.com.autopilot;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;

/**
 * Created by Brian on 11/15/2014.
 */
public class DualCursorAdapter extends SimpleCursorAdapter{

    Cursor enabledCursor;

    public DualCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        //((Switch) view.findViewById(R.id.enabledSwitch)).setChecked(cursor.getInt(cursor.getColumnIndex()));
        return view;
    }
}
