package at.burgr.distancewarner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;

import at.burgr.distancewarner.data.Warning;

public class ListViewAdapter extends ArrayAdapter<Warning> {

    private int resourceLayout;
    private Context mContext;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    public ListViewAdapter(Context context, int resource, List<Warning> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        Warning p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.tvWarningTime);
            TextView tt2 = (TextView) v.findViewById(R.id.tvWarningDistance);

            if (tt1 != null) {
                tt1.setText(dateFormat.format(p.timestamp));
            }

            if (tt2 != null) {
                tt2.setText(Integer.toString(p.distance));
            }
        }

        return v;
    }
}
