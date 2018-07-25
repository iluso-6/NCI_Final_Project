package shay.example.com.dart_master;

/**
 * Created by Shay de Barra on 09,March,2018
 * Email:  x16115864@student.ncirl.ie
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import shay.example.com.dart_master.helpers.ImageUtility;
import shay.example.com.dart_master.models.JourneyObj;

//https://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<JourneyObj>> _listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<JourneyObj>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    public int getTxtCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final JourneyObj ticket = (JourneyObj) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);

        }


        ImageView ticketImage = convertView
                .findViewById(R.id.ticketImage);
        if (ticket == null || ticket.getTicket_image() == null) {
            // do nothing
        } else {
            Bitmap bmp = ImageUtility.convertToBitmap(ticket.getTicket_image());

            ticketImage.setImageBitmap(bmp);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        String text_count = getChildrenCount(groupPosition) + "";
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);


        }

        TextView listHeaderTitle = convertView
                .findViewById(R.id.cardTitle);
        listHeaderTitle.setTypeface(null, Typeface.BOLD);
        listHeaderTitle.setText(headerTitle);

        // number indicating num of children ...getTxtCount
        TextView num = convertView.findViewById(R.id.header_count);
        if (getTxtCount(groupPosition) > 0) {
            num.setVisibility(View.VISIBLE);
        }else {
            num.setVisibility(View.INVISIBLE);// make num==0 INVISIBLE
        }

        num.setText(text_count);


        convertView.setPadding(0, 0, 0, 0);
        parent.setPadding(0, 0, 0, 0);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
