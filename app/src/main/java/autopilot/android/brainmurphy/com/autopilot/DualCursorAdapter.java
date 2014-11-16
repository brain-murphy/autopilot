package autopilot.android.brainmurphy.com.autopilot;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Brian on 11/15/2014.
 */
public class DualCursorAdapter extends SimpleCursorAdapter{

    Cursor enabledCursor;

    public DualCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags,
                            Cursor enabledCursor) {
        super(context, layout, c, from, to, flags);
        this.enabledCursor = enabledCursor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);


        //enabledCursor.moveToPosition(position);
        //TODO set enabled and such//
        return view;
    }
}
