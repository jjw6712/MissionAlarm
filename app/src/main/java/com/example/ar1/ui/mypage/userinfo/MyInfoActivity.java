package com.example.ar1.ui.mypage.userinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ar1.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyInfoActivity extends AppCompatActivity {
    static final int REQUEST_CODE_CHANGE_NAME = 1;
    static final int REQUEST_CODE_CHANGE_ID = 2;
    static final int REQUEST_CODE_CHANGE_PH = 3;
    static final int REQUEST_CODE_CHANGE_SEX = 4;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1003;
    ImageView ivUser;
    String userId, userName;
    TextView profileName, ivUsertext;
    ImageButton btBack;
    MyInfoListAdapter myInfoListAdapter;
    private LruCache<String, Bitmap> memoryCache; // 이미지 캐시
    private static final String GET_PROFILE_URL = "https://sw--zqbli.run.goorm.site/getProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_info_activity);

        // 퍼미션 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }

        // 상태바 색상 변경
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(android.R.color.white));

        btBack = findViewById(R.id.btBack);
        ivUser = findViewById(R.id.ivUser);
        ivUsertext = findViewById(R.id.ivUserText);
        profileName = findViewById(R.id.userName);
        ListView listView = findViewById(R.id.buttonList);

        List<String> itemList = new ArrayList<>();
        itemList.add("이름");
        itemList.add("아이디");
        itemList.add("번호");
        itemList.add("성별");

        MyInfoListAdapter adapter = new MyInfoListAdapter(this, itemList);
        listView.setAdapter(adapter);

        myInfoListAdapter = new MyInfoListAdapter(this, itemList); // 인스턴스 저장
        listView.setAdapter(myInfoListAdapter);

        SharedPreferences preferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");
        profileName.setText(userName+" 님");

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        Bitmap cachedBitmap = getBitmapFromMemCache(userId);
        if (cachedBitmap != null) {
            ivUser.setImageBitmap(cachedBitmap);
            ivUsertext.setVisibility(View.GONE);
        } else {
            OkHttpClient client = new OkHttpClient();
            Request requestProfile = new Request.Builder().url(GET_PROFILE_URL + "?userId=" + userId).build();
            client.newCall(requestProfile).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 실패 시 처리
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    final Bitmap circularBitmap = getRoundedCornerBitmap(bitmap); // 원형 이미지 변환
                    addBitmapToMemoryCache(userId, circularBitmap); // 캐시에 저장
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivUser.setImageBitmap(circularBitmap);
                            ivUsertext.setVisibility(View.GONE);
                        }
                    });
                }
                // ...
            });
        }

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CHANGE_NAME && resultCode == RESULT_OK) {
            String newUserName = data.getStringExtra("newUserName");

            // SharedPreferences에 새로운 userName 저장
            SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userName", newUserName);
            editor.apply();

            // 프로필 이름을 새로운 사용자 이름으로 업데이트
            profileName.setText(newUserName + " 님");

            // 기타 UI 업데이트 로직
            if (myInfoListAdapter != null) {
                myInfoListAdapter.notifyDataSetChanged();
            }
        }else if (requestCode == REQUEST_CODE_CHANGE_ID && resultCode == RESULT_OK) {
            String newUserId = data.getStringExtra("newUserId");
            SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userId", newUserId);
            editor.apply();

            if (myInfoListAdapter != null) {
                myInfoListAdapter.notifyDataSetChanged();
            }
        }else if (requestCode == REQUEST_CODE_CHANGE_PH && resultCode == RESULT_OK) {
            String newUserPhoneNum = data.getStringExtra("newUserPhoneNum");
            SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userPhoneNum", newUserPhoneNum);
            editor.apply();

            if (myInfoListAdapter != null) {
                myInfoListAdapter.notifyDataSetChanged();
            }
        }else if (requestCode == REQUEST_CODE_CHANGE_SEX && resultCode == RESULT_OK) {
            String newUserSex = data.getStringExtra("userSex");
            SharedPreferences prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userSex", newUserSex);
            editor.apply();

            if (myInfoListAdapter != null) {
                myInfoListAdapter.notifyDataSetChanged();
            }
        }
    }
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        Log.d("Image Path", "Path: " + path);
        return path;
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null) {
            if (getBitmapFromMemCache(key) == null) {
                memoryCache.put(key, bitmap);
            }
        } else {
            // key나 bitmap이 null인 경우에 대한 처리 (예: 로그 출력)
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }
}
