package com.example.ar1.ui.graph;

import android.Manifest;
import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Graph extends Fragment {

    String userId, userName;
    private FragmentGraphBinding binding;
    RecyclerView dateRecyclerView;
    DateAdapter dateAdapter;
    List<DateItem> dateItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GraphViewModel notificationsViewModel =
                new ViewModelProvider(this).get(GraphViewModel.class);
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.activity_graph_list, container, false);


        dateRecyclerView = root.findViewById(R.id.dateList);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        initDates();
        dateAdapter = new DateAdapter(dateItems);
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
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ListView listView = root.findViewById(R.id.missionList);

        List<String> itemList = new ArrayList<>();
        // 아이템 리스트에 버튼에 표시할 내용 추가
        itemList.add("스쿼트");
        itemList.add("푸쉬업");

        MissionListAdapter adapter = new MissionListAdapter(getActivity(), itemList);
        listView.setAdapter(adapter);

        SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");

        selectToday();

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
            dateItems.add(item);
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

    private List<DateItem> dateItems;
    private int selectedPosition = -1;

    public DateAdapter(List<DateItem> dateItems) {
        this.dateItems = dateItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DateItem dateItem = dateItems.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        holder.dateText.setText(sdf.format(dateItem.getDate()));
        holder.dayOfWeek.setText(dayFormat.format(dateItem.getDate()).toUpperCase());  // 수정된 부분

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
                notifyDataSetChanged();
            }
        });

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.BLUE);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dayOfWeek;  // 타입 변경: WindowDecorActionBar.TabImpl -> TextView
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);  // 적절한 ID로 변경해주세요
        }
    }

}
