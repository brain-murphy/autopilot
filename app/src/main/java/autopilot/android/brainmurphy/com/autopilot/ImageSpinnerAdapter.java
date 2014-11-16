package autopilot.android.brainmurphy.com.autopilot;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.List;

/**
 * Created by connorrmounts on 11/16/14.
 */
public class ImageSpinnerAdapter extends ArrayAdapter<String>
{
    private int[] imageIds;
    private String[] pathNames;

    private List<String> personas;

    public ImageSpinnerAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ImageSpinnerAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ImageSpinnerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    public ImageSpinnerAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ImageSpinnerAdapter(Context context, int resource, List<String> objects, List<String> personas) {
        super(context, resource, objects);
        this.personas = personas;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        //Spinner spin = (Spinner)v.findViewById(R.id.spinner1);
        ArrayAdapter<String> personasAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        personasAdapter.addAll(personas);

        //spin.setAdapter(personasAdapter);

        return v;
    }
}
