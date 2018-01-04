package com.nthu.internetofthing.priend.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Ywuan on 31/05/2016.
 */
public class AccountHelper extends SQLiteOpenHelper{
    private final String LOG_TAG = AccountHelper.class.getSimpleName();

    public AccountHelper(Context context) {
        super(context, AccountContract.DATABASE_NAME, null, AccountContract.DATABASE_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AccountContract.CREATE_TABLE);
        Log.v(LOG_TAG, "onCreate database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(AccountContract.DROP_TABLE);
        onCreate(db);
        Log.v(LOG_TAG, "onUpgrade database");
    }

    public boolean insertData(String user_name, String access_token){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AccountContract.COL_2_NAME, user_name);
        contentValues.put(AccountContract.COL_3_ACCESS_TOKEN, access_token);
        try{
            long result = sqLiteDatabase.insert(AccountContract.TABLE_NAME, null, contentValues);

            return result==-1? false: true;
        }finally {
            sqLiteDatabase.close();
        }
    }

    public void deleteData(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        try{
            sqLiteDatabase.delete(AccountContract.TABLE_NAME, null, null);
        }finally {
            sqLiteDatabase.close();
        }
    }

    public boolean isEmpty(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String getData = "SELECT COUNT(*) FROM " + AccountContract.TABLE_NAME;
        Cursor cursor = sqLiteDatabase.rawQuery(getData, null);

        try{
            if(cursor != null){
                cursor.moveToNext();
                int count = cursor.getInt(0);
                if(count > 0)
                    return false;
            }

            return true;
        }finally {
            sqLiteDatabase.close();
            cursor.close();
        }
    }

    public String getAccessToken(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String getAccessToken = "SELECT * FROM " + AccountContract.TABLE_NAME;
        Cursor cursor = sqLiteDatabase.rawQuery(getAccessToken, null);

        try{
            if(cursor == null) return null;

            cursor.moveToNext();
            return cursor.getString(cursor.getColumnIndex(AccountContract.COL_3_ACCESS_TOKEN));
        }finally {
            if(sqLiteDatabase != null) sqLiteDatabase.close();
            if(cursor != null) cursor.close();
        }
    }
}
