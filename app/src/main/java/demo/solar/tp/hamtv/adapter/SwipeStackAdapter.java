package demo.solar.tp.hamtv.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import demo.solar.tp.hamtv.R;

/**
 * Created by filiperodrigues on 17/05/17.
 */

public class SwipeStackAdapter extends BaseAdapter {

    private List<ItemModel> mData;
    private Context mContext;

    public SwipeStackAdapter(Context context, List<ItemModel> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public ItemModel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.swipe_stack_item, parent, false);

            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.swipe_stack_item_text);
            holder.image = (ImageView) convertView.findViewById(R.id.swipe_stack_item_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText("ID: " + mData.get(position).getId());

        String image = mData.get(position).getImage();
        if(!TextUtils.isEmpty(image)){
            Glide.with(mContext)
                    .load(image)
                    .placeholder(R.drawable.image_ph)
                    .crossFade()
                    .into(holder.image);
        }

        return convertView;
    }

    public void addItem(ItemModel item) {
        mData.add(item);
    }

    public void removeItem(int index) {
        mData.remove(index);
    }

    class ViewHolder {
        ImageView image;
        TextView text;
    }
}
