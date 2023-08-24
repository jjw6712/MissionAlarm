package com.example.ar1;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ar1.ui.mypage.MyPageListAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;

class MyInfoActivity extends AppCompatActivity {
    int REQUEST_IMAGE_CODE_USER = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1003;
    ImageView ivUser;
    String userId, userName;
    TextView profileName, ivUsertext;
    private LruCache<String, Bitmap> memoryCache; // 이미지 캐시

    private static final String UPLOAD_PROFILE_URL = "https://sw--zqbli.run.goorm.site/uploadProfile";
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

        ivUser = findViewById(R.id.ivUser);
        ivUsertext = findViewById(R.id.ivUserText);
        profileName = findViewById(R.id.userName);
        ListView listView = findViewById(R.id.buttonList);

        List<String> itemList = new ArrayList<>();
        itemList.add("이름");
        itemList.add("아이디");
        itemList.add("번호");

        MyPageListAdapter adapter = new MyPageListAdapter(this, itemList);
        listView.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        userId = preferences.getString("userId", "");
        userName = preferences.getString("userName", "");
        profileName.setText(userName);

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

        ivUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CODE_USER);
            }
        });
    }

    // 이미지 업로드
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri image = data.getData();
            File file = new File(getRealPathFromURI(image));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("userId", userId)
                    .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                    .build();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(UPLOAD_PROFILE_URL).post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    final Bitmap circularBitmap = getRoundedCornerBitmap(bitmap); // 원형 이미지 변환

                    memoryCache.remove(userId);

                    addBitmapToMemoryCache(userId, circularBitmap);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivUser.setImageBitmap(circularBitmap);
                            ivUsertext.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
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

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }
}
