package com.integreight.onesheeld.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.integreight.onesheeld.model.PlaylistItem;
import com.integreight.onesheeld.utils.Log;

import java.util.ArrayList;

public class MusicPlaylist {

    public static final String MYDATABASE_NAME = "onesheeld";
    public static final String MYDATABASE_TABLE = "playlist";
    public static final int MYDATABASE_VERSION = 1;
    public static final String KEY_ID = "_id", PATH = "path", NAME = "name";

    private static final String SCRIPT_CREATE_DATABASE = "create table "
            + MYDATABASE_TABLE + "(" + KEY_ID + " integer primary key, " + PATH
            + " text, " + NAME + " text);";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private Context context;

    public MusicPlaylist(Context c) {
        context = c;
    }

    public MusicPlaylist openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null,
                MYDATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public MusicPlaylist openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null,
                MYDATABASE_VERSION);
        if (sqLiteHelper != null)
            sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        sqLiteHelper.close();
    }

    public long insert(PlaylistItem content) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PATH, content.path);
        contentValues.put(NAME, content.name);
        return sqLiteDatabase.insert(MYDATABASE_TABLE, null, contentValues);
    }

    public int deleteAll() {
        return sqLiteDatabase.delete(MYDATABASE_TABLE, null, null);
    }

    public int delete(int id) {
        return sqLiteDatabase.delete(MYDATABASE_TABLE,
                KEY_ID + "='" + id + "'", null);
    }

    public ArrayList<PlaylistItem> getPlaylist() {
        ArrayList<PlaylistItem> topics = new ArrayList<PlaylistItem>();
        String[] columns = new String[]{KEY_ID, PATH, NAME};
        Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE, columns, null,
                null, null, null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                PlaylistItem topic = new PlaylistItem();
                topic.id = cursor.getInt(0);
                topic.path = cursor.getString(1);
                topic.name = cursor.getString(2);
                topics.add(topic);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            Log.e("TAG", "Exception", e);
        } finally {
            cursor.close();
        }
        return topics;
    }

    public class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(Context context, String name,
                            CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL(SCRIPT_CREATE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub

        }

    }

}
