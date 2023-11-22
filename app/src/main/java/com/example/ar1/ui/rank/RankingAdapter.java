package com.example.ar1.ui.rank;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ar1.R;

import java.util.List;

public class RankingAdapter extends BaseAdapter {
    private Context context;
    private List<RankingItem> rankingList;

    public RankingAdapter(Context context, List<RankingItem> rankingList) {
        this.context = context;
        this.rankingList = rankingList;
    }

    @Override
    public int getCount() {
        return rankingList != null ? rankingList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return rankingList != null ? rankingList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_ranking, null);
        }

        ImageView rankImage = convertView.findViewById(R.id.rankImage);
        TextView rankingNumber = convertView.findViewById(R.id.rankingNumber);
        ImageView userImage = convertView.findViewById(R.id.userImage);
        TextView userName = convertView.findViewById(R.id.userName);
        TextView missionCount = convertView.findViewById(R.id.missionCount);

        RankingItem currentItem = rankingList.get(position);

        if (position < 3) {  // For ranks 1, 2, 3
            rankImage.setVisibility(View.VISIBLE);
            rankingNumber.setVisibility(View.GONE);
            int resId = context.getResources().getIdentifier("rank" + (position + 1), "drawable", context.getPackageName());
            rankImage.setImageResource(resId);
        } else {  // For ranks 4 and above
            rankingNumber.setVisibility(View.VISIBLE);
            rankImage.setVisibility(View.GONE);
            rankingNumber.setText(String.valueOf(position + 1));
        }


        if (currentItem != null) {
            String userImagePath = currentItem.getUserImage();
            if (userImagePath.length() > "/workspace/SW_18/uploads".length()) {
                String completeImageUrl = "https://sw--zqbli.run.goorm.site/uploads" + userImagePath.substring("/workspace/SW_18/uploads".length());
                Log.d(TAG, "어뎁터 이미지URL: "+completeImageUrl);
                // 이미지 로드 (Glide 라이브러리 사용)
                Glide.with(context).load(completeImageUrl).apply(new RequestOptions().circleCrop()).into(userImage);
            } else {
                // 이미지 길이가 충분하지 않을 때, 기본 이미지를 로드
                Glide.with(context).load(R.drawable.default_profile_image).apply(new RequestOptions().circleCrop()).into(userImage);
            }

            userName.setText(currentItem.getUserName());
            missionCount.setText(String.valueOf(currentItem.getMissionCount()));
        }

        return convertView;
    }
}