/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package us.petrolog.nexus.misc;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import us.petrolog.nexus.R;
import us.petrolog.nexus.rest.model.DeviceDetail;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */
public class DevicesDetailAdapter extends RecyclerView.Adapter<DevicesDetailAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter";

    private List<DeviceDetail> mDataSet;
    private static RecyclerViewClickListener mListener;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView textViewItemName;
        private final TextView textViewItemProblem;
        private final TextView textViewItemGroup;
        private final TextView textViewItemLocation;
        private final TextView textViewItemLastUpdate;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(this);
            textViewItemName = (TextView) v.findViewById(R.id.textViewListItemName);
            textViewItemProblem = (TextView) v.findViewById(R.id.textViewListItemProblem);
            textViewItemGroup = (TextView) v.findViewById(R.id.textViewListItemGroup);
            textViewItemLocation = (TextView) v.findViewById(R.id.textViewListItemLocation);
            textViewItemLastUpdate = (TextView) v.findViewById(R.id.textViewListItemLastUpdate);
        }

        public TextView getTextViewItemName() {
            return textViewItemName;
        }

        public TextView getTextViewItemProblem() {
            return textViewItemProblem;
        }

        public TextView getTextViewItemGroup() {
            return textViewItemGroup;
        }

        public TextView getTextViewItemLocation() {
            return textViewItemLocation;
        }

        public TextView getTextViewItemLastUpdate() {
            return textViewItemLastUpdate;
        }

        @Override
        public void onClick(View v) {
            mListener.recyclerViewListClicked(v, getLayoutPosition());
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet a list with markers
     */
    public DevicesDetailAdapter(List<DeviceDetail> dataSet, RecyclerViewClickListener listener) {
        mListener = listener;
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
//        Logger.d("Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.getTextViewItemName().setText(mDataSet.get(position).getName());
        viewHolder.getTextViewItemProblem().setText(mDataSet.get(position).getState().getRemoteDeviceStatusDescription());
        viewHolder.getTextViewItemGroup().setText(mDataSet.get(position).getGroupName());
        viewHolder.getTextViewItemLocation().setText(mDataSet.get(position).getLocation());
        Calendar date = Utility.getFormattedDate(mDataSet.get(position).getState().getLasUpdateSince());
        long dateInMillis = 0;
        if (date != null) {
            dateInMillis = date.getTimeInMillis();
        }

        Calendar today = Calendar.getInstance();

        String lastupdate = (String) DateUtils.getRelativeTimeSpanString(dateInMillis, today.getTimeInMillis(), DateUtils.FORMAT_ABBREV_RELATIVE);
        viewHolder.getTextViewItemLastUpdate().setText(lastupdate);
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
