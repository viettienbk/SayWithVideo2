package datn.bkdn.com.saywithvideo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.Sound;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class ListMySoundAdapter extends RealmBaseAdapter<Sound> {

    public OnItemClicked mItemClicked;

    public ListMySoundAdapter(Context context, RealmResults<Sound> sounds) {
        super(context, sounds, false);
    }

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_mysound, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvSoundName = (TextView) convertView.findViewById(R.id.tvSoundName);
            viewHolder.tvPlays = (TextView) convertView.findViewById(R.id.tvPlays);
            viewHolder.tvDateOfCreate = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.imgPlayPause = (ImageView) convertView.findViewById(R.id.imgPlay);
            viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.llSoundInfor);
            viewHolder.rlOption = (RelativeLayout) convertView.findViewById(R.id.rlOption);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        viewHolder.rlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        Sound sound = getItem(position);
        viewHolder.imgPlayPause.setImageResource(sound.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvSoundName.setText(sound.getName());
        viewHolder.tvPlays.setText(sound.getPlays() + " plays");
        viewHolder.tvDateOfCreate.setText(sound.getDateOfCreate());
        return convertView;
    }

    public interface OnItemClicked {
        void onClick(int pos, View v);
    }

    private class ViewHolder {
        private TextView tvSoundName;
        private TextView tvPlays;
        private TextView tvDateOfCreate;
        private ImageView imgPlayPause;
        private LinearLayout linearLayout;
        private RelativeLayout rlOption;
    }

}
