package com.example.ar1.ui.graph;

import static android.app.PendingIntent.getActivity;
import static androidx.fragment.app.FragmentManager.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ar1.R;
import com.example.ar1.databinding.FragmentGraphBinding;
import com.example.ar1.ui.mypage.MyPageListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Graph extends Fragment {

    String userId, userName;
    private FragmentGraphBinding binding;
    RecyclerView dateRecyclerView;
    DateAdapter dateAdapter;
    List<DateItem> dateItems;
    TextView tvCount;
    MissionListAdapter adapter;

    @SuppressLint({"MissingInflatedId", "RestrictedApi"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GraphViewModel notificationsViewModel =
                new ViewModelProvider(this).get(GraphViewModel.class);
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.activity_graph_list, container, false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ListView listView = root.findViewById(R.id.missionList);

        List<String> itemList = new ArrayList<>();
        // 아이템 리스트에 버튼에 표시할 내용 추가
        itemList.add("스쿼트");
        itemList.add("푸쉬업");
        itemList.add("만보계(준비중)");
        itemList.add("털걸이(준비중)");
        itemList.add("영단어 발음하기");
        itemList.add("영문장 발음하기");
        itemList.add("영단어 퀴즈퍼즐");
        itemList.add("두더지게임(준비중)");
        itemList.add("파리잡기(준비중)");
        itemList.add("");

        adapter = new MissionListAdapter(getActivity(), itemList);
        listView.setAdapter(adapter);

        tvCount = root.findViewById(R.id.tvCount);

        dateRecyclerView = root.findViewById(R.id.dateList);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        initDates();
        dateAdapter = new DateAdapter(getActivity(), dateItems, adapter);
        dateRecyclerView.setAdapter(dateAdapter);

        // RecyclerView에 적용
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        dateRecyclerView.setLayoutManager(layoutManager);
        // 초기 위치를 일요일(첫 번째 아이템)로 설정합니다.

        CustomLinearSnapHelper snapHelper = new CustomLinearSnapHelper();
        snapHelper.attachToRecyclerView(dateRecyclerView);

        // 프래그먼트에서 상태바 색상 변경
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.black));
        }

        SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");

        selectToday();
        DateItem selectedItem = dateAdapter.getSelectedItem();
        Date selectedDate = null;
        if (selectedItem != null) {
            selectedDate = selectedItem.getDate();
            // selectedDate를 사용하여 원하는 작업을 수행합니다.
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(selectedDate);

        fetchWorkoutData(userId, formattedDate);
        Log.d(TAG, "선택 날짜: "+formattedDate);

        return root;
    }

    // CustomLinearSnapHelper 클래스 정의
    public class CustomLinearSnapHelper extends LinearSnapHelper {

        @Override
        public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
            // 현재 보이는 첫 번째 아이템의 위치
            int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

            // 한 페이지는 7개의 아이템을 가지므로, 첫 아이템(일요일)의 인덱스를 찾습니다.
            int firstDayOfWeekIndex = firstVisibleItemPosition - (firstVisibleItemPosition % 7);

            // 스크롤 방향에 따라 페이지를 증가시키거나 감소시킵니다.
            if (velocityX > 0) {
                firstDayOfWeekIndex += 7;
            } else if (velocityX < 0 && firstDayOfWeekIndex >= 7) {
                firstDayOfWeekIndex -= 7;
            }

            // 유효한 범위 내로 조정
            int itemCount = layoutManager.getItemCount();
            if (firstDayOfWeekIndex >= itemCount) {
                firstDayOfWeekIndex = itemCount - 1;
            } else if (firstDayOfWeekIndex < 0) {
                firstDayOfWeekIndex = 0;
            }

            return firstDayOfWeekIndex;
        }
    }

    private void initDates() {
        dateItems = new ArrayList<>();

        // 작년 1월 1일을 기준으로
        Calendar start = Calendar.getInstance();
        start.set(start.get(Calendar.YEAR) - 1, Calendar.JANUARY, 1);

        // 작년의 첫 번째 일요일로 설정
        int firstDayOfWeek = start.get(Calendar.DAY_OF_WEEK);
        int offsetToFirstSunday = Calendar.SUNDAY - firstDayOfWeek;
        if (offsetToFirstSunday < 0) {
            offsetToFirstSunday += 7;
        }
        start.add(Calendar.DATE, offsetToFirstSunday);

        // 오늘 날짜
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // 첫 번째 아이템이 일요일이 되도록 리스트를 채움
        while (start.before(today) || start.equals(today)) {
            Log.d("initDates", "Adding date: " + start.getTime().toString());  // Log 추가
            DateItem item = new DateItem(start.getTime());
            item.setSelectable(true);
            dateItems.add(item);
            start.add(Calendar.DATE, 1);
        }

        // 이번 주 일요일을 구함
        Calendar thisSunday = (Calendar) today.clone();
        int dayOfWeek = thisSunday.get(Calendar.DAY_OF_WEEK);
        int offsetToSunday = Calendar.SUNDAY - dayOfWeek;
        thisSunday.add(Calendar.DATE, offsetToSunday);

        // 이번 주 일요일부터 토요일까지 날짜를 추가
        for (int i = 0; i < 7; i++) {
            DateItem item = new DateItem(thisSunday.getTime());
            if (thisSunday.before(today) || thisSunday.equals(today)) {
                item.setSelectable(true); // 선택 가능
            } else {
                item.setSelectable(false); // 선택 불가능
            }

            // 중복되는 항목이 없을 때만 추가
            if (!isDateAlreadyAdded(thisSunday.getTime())) {
                dateItems.add(item);
            }
            thisSunday.add(Calendar.DATE, 1);
        }

        // 토요일 자정이 지나면 다음주 일요일부터 토요일까지 날짜를 추가
        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            Calendar nextSunday = (Calendar) today.clone();
            int nextOffset = 7 - dayOfWeek + 1;  // 다음주 일요일까지의 오프셋
            nextSunday.add(Calendar.DATE, nextOffset);

            for (int i = 0; i < 7; i++) {
                nextSunday.add(Calendar.DATE, 1);
                DateItem item = new DateItem(nextSunday.getTime());
                item.setSelectable(false); // 선택 불가능하게 설정
                dateItems.add(item);
            }
        }
    }
    // 기존의 dateItems 리스트에 해당 날짜가 있는지 검사
    private boolean isDateAlreadyAdded(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        for (DateItem item : dateItems) {
            cal2.setTime(item.getDate());
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                return true;
            }
        }
        return false;
    }
    private void selectToday() {
        // Find today's date and set it as selected
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < dateItems.size(); i++) {
            Calendar itemCal = Calendar.getInstance();
            itemCal.setTime(dateItems.get(i).getDate());
            itemCal.set(Calendar.HOUR_OF_DAY, 0);
            itemCal.set(Calendar.MINUTE, 0);
            itemCal.set(Calendar.SECOND, 0);
            itemCal.set(Calendar.MILLISECOND, 0);

            if (itemCal.equals(today)) {
                dateAdapter.setSelectedPosition(i);
                dateRecyclerView.scrollToPosition(i);  // 이 부분이 스크롤을 오늘 날짜로 이동시킵니다.
                break;
            }
        }
    }
    private final OkHttpClient httpClient = new OkHttpClient();

    private void fetchWorkoutData(String userId, String selectedDate) {

        String url = "https://sw--zqbli.run.goorm.site/getMissionCountByDate/" + userId + "/" + selectedDate;

        Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "onResponse: "+responseBody);
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        final int squatCount = jsonObject.getInt("squatCount");
                        final int pushUpCount = jsonObject.getInt("pushUpCount");
                        final int speachWordsCount = jsonObject.getInt("speachWordsCount");
                        final int speachSentencesCount = jsonObject.getInt("speachSentencesCount");
                        Log.d(TAG, "스쿼트: "+squatCount);
                        Log.d(TAG, "푸쉬업: "+pushUpCount);
                        //Log.d(TAG, "영단어: "+speachWordsCount);
                        if (getActivity() != null) {  // getActivity() null 체크 추가
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (adapter != null) {  // null 체크 추가
                                        adapter.updateSquatCount(squatCount);
                                        adapter.updatePushUpCount(pushUpCount);
                                        adapter.updateSpeachWordsCount(speachWordsCount);
                                        adapter.updateSpeachSentencesCount(speachSentencesCount);
                                    }
                                }
                            });
                        }

                    } catch (JSONException e) {
                        // Handle JSON parsing error
                    }
                } else {
                    // Handle error cases
                }
            }
        });
    }

}



class DateItem {
    private java.util.Date date;
    private boolean selectable;

    public DateItem(java.util.Date date) {
        this.date = date;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }
}


class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    MissionListAdapter adapter;
    private List<DateItem> dateItems;
    private int selectedPosition = -1;
    private Context mContext; // Context 객체 추가

    public DateAdapter(Context context, List<DateItem> dateItems,  MissionListAdapter missionListAdapter) {
        this.mContext = context;
        this.dateItems = dateItems;
        this.adapter = missionListAdapter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        SharedPreferences preferences = mContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String userId = preferences.getString("userId", "");
        DateItem dateItem = dateItems.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        String dayOfWeekStr = dayFormat.format(dateItem.getDate()).toUpperCase();
        holder.dateText.setText(sdf.format(dateItem.getDate()));
        holder.dayOfWeek.setText(dayOfWeekStr);

        // 일요일이면 빨간색으로 변경
        if ("일".equals(dayOfWeekStr) || "SUN".equals(dayOfWeekStr)) {
            holder.dateText.setTextColor(Color.RED);
            holder.dayOfWeek.setTextColor(Color.RED);
        } else {
            holder.dateText.setTextColor(Color.WHITE);
            holder.dayOfWeek.setTextColor(Color.WHITE);
        }

        if (!dateItem.isSelectable()) {
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (dateItem.isSelectable()) {
                selectedPosition = position;
                DateItem selectedItem = dateItems.get(selectedPosition); // 현재 리스트에서 선택된 아이템을 가져옵니다.
                Date selectedDate = null;
                if (selectedItem != null) {
                    selectedDate = selectedItem.getDate();
                    // selectedDate를 사용하여 원하는 작업을 수행합니다.
                }

                SimpleDateFormat sdd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = sdd.format(selectedDate);

                fetchWorkoutData(userId, formattedDate); // 비동기 작업
                Log.d(TAG, "선택 날짜 어뎁터: " + formattedDate);
                notifyDataSetChanged();
            }
        });

        if (position == selectedPosition) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }
    }


    @Override
    public int getItemCount() {
        return dateItems.size();
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }
    private final OkHttpClient httpClient = new OkHttpClient();
    private void fetchWorkoutData(String userId, String selectedDate) {
        String url = "https://sw--zqbli.run.goorm.site/getMissionCountByDate/" + userId + "/" + selectedDate;

        Request request = new Request.Builder()
                .url(url)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the error
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        final int squatCount = jsonObject.getInt("squatCount");
                        final int pushUpCount = jsonObject.getInt("pushUpCount");
                        Log.d(TAG, "어뎁터 스쿼트: "+squatCount);
                        Log.d(TAG, "어뎁터 푸쉬업: "+pushUpCount);

                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter != null) {
                                    adapter.updateSquatCount(squatCount);
                                    adapter.updatePushUpCount(pushUpCount);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        // Handle JSON parsing error
                    }
                } else {
                    // Handle error cases
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public DateItem getSelectedItem() {
        if (selectedPosition != -1) {
            Log.d(TAG, "getSelectedItem: "+selectedPosition);
            return dateItems.get(selectedPosition);
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dayOfWeek;
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);
        }
    }
}
