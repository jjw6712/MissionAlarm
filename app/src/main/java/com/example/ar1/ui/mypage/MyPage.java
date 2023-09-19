    package com.example.ar1.ui.mypage;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ar1.R;

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

    /**
 * 프로필 탭바 클릭시 호출되는 클래스
 * 갤러리에서 프로필 사진을 가져오는 기능 구현
 */
public class MyPage extends Fragment {
        int REQUEST_IMAGE_CODE_USER = 1001;
        int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1003;
        ImageView ivUser;
        String userId, userName;
        TextView profileName, ivUsertext;
        private LruCache<String, Bitmap> memoryCache; // 이미지 캐시

        private static final String UPLOAD_PROFILE_URL = "https://sw--zqbli.run.goorm.site/uploadProfile";
        private static final String GET_PROFILE_URL = "https://sw--zqbli.run.goorm.site/getProfile";

        @SuppressLint("MissingInflatedId")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_my_page, container, false);

            // 퍼미션 체크
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }

            // 프래그먼트에서 상태바 색상 변경
            if (getActivity() != null) {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(android.R.color.black));
            }


            ivUser = root.findViewById(R.id.ivUser);
            ivUsertext = root.findViewById(R.id.ivUserText);
            profileName = root.findViewById(R.id.userName);
            ListView listView = root.findViewById(R.id.buttonList);

            List<String> itemList = new ArrayList<>();
            // 아이템 리스트에 버튼에 표시할 내용 추가
            itemList.add("내계정");
            itemList.add("설정");
            itemList.add("개발자노트");
            itemList.add("로그아웃");

            MyPageListAdapter adapter = new MyPageListAdapter(getActivity(), itemList);
            listView.setAdapter(adapter);

            SharedPreferences preferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
            userId = preferences.getString("userId", "");
            userName = preferences.getString("userName", "");
            profileName.setText(userName);

            // 메모리 캐시 초기화
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };

            // 이미지를 캐시 메모리에서 가져오거나 서버에서 다운로드 받는 로직
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
                        requireActivity().runOnUiThread(new Runnable() {
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

            // ...


            ivUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_IMAGE_CODE_USER);
                }
            });

            return root;
        }

        //이미지 업로드
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
                        // 성공적으로 업로드 되었을 때의 처리
                        // 이미지를 즉시 불러와서 적용
                        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        final Bitmap circularBitmap = getRoundedCornerBitmap(bitmap); // 원형 이미지 변환

                        // 기존의 이미지 캐시 삭제
                        memoryCache.remove(userId);

                        // 새 이미지 캐시에 저장
                        addBitmapToMemoryCache(userId, circularBitmap);

                        requireActivity().runOnUiThread(new Runnable() {
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
        // 커서 관리
        private String getRealPathFromURI(Uri contentUri) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close(); // 커서 닫기
            Log.d("Image Path", "Path: " + path); // 이미지 경로 로깅
            return path;
        }

        // 원형 이미지 변환 함수
        private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
            if (bitmap == null) {
                return null; // Null 객체인 경우 null 반환
            }

            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2f, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }

        // 캐시에 비트맵 추가
        // 캐시에 비트맵 추가
        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
            if (key != null && bitmap != null) {
                if (getBitmapFromMemCache(key) == null) {
                    memoryCache.put(key, bitmap);
                }
            }
        }

        // 캐시에서 비트맵 가져오기
        public Bitmap getBitmapFromMemCache(String key) {
            return memoryCache.get(key);
        }

    }