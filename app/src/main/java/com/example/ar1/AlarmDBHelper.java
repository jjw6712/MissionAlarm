package com.example.ar1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlarmDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "alarms";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";


    private static final String CREATE_ALARM_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HOUR + " INTEGER NOT NULL, " +
                    COLUMN_MINUTE + " INTEGER NOT NULL, ";

    private SQLiteDatabase db;

    public AlarmDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tableName = "alarms";
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, hour INTEGER, minute INTEGER)";
        String checkTableExistsQuery = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        Cursor cursor = db.rawQuery(checkTableExistsQuery, null);
        boolean tableExists = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                tableExists = true;
            }
            cursor.close();
        }

        if (!tableExists) {
            db.execSQL(createTableQuery);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 버전 업그레이드 시 수행할 작업이 있다면 여기에 추가
        // 이 예시에서는 기존의 코드와 동일하게 처리
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public Cursor getAlarmCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarms", null);
        return cursor;
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                COLUMN_ID,
                COLUMN_HOUR,
                COLUMN_MINUTE
        };

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        Alarm alarm;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE));


            alarm = new Alarm(id, hour, minute);
            alarms.add(alarm);
        }

        cursor.close();
        db.close();

        return alarms;
    }

    public long addAlarm(Alarm alarm) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, alarm.getHour());
        values.put(COLUMN_MINUTE, alarm.getMinute());

        long id = db.insert(TABLE_NAME, null, values);
        alarm.setId((int) id);

        db.close();
        return id;
    }

    public int deleteAlarm(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        int deletedRows = db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
        return deletedRows;
    }

    public int[] loadTime(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int[] time = new int[2];
        if (cursor.moveToFirst()) {
            int columnIndexHour = cursor.getColumnIndex(COLUMN_HOUR);
            int columnIndexMinute = cursor.getColumnIndex(COLUMN_MINUTE);
            if (columnIndexHour != -1 && columnIndexMinute != -1) {
                time[0] = cursor.getInt(columnIndexHour);
                time[1] = cursor.getInt(columnIndexMinute);
            } else {
                // 컬럼이 존재하지 않는 경우 처리
                // 예: 로그 출력 또는 기본값 설정
                Log.e("getColumnIndex", "Column not found");
            }
        }
        cursor.close();
        db.close();
        return time;
    }

}