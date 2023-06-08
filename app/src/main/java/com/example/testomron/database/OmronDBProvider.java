package com.example.testomron.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * Created by Omron HealthCare Inc
 */

public class OmronDBProvider extends ContentProvider {

    private DbHelper mDbHelper;
    private static final UriMatcher uriMatcher;
    private SQLiteDatabase mSqldb;


    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.VITAL_DATA_TABLE, OmronDBConstans.VITAL_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.ACTIVITY_DATA_TABLE, OmronDBConstans.ACTIVITY_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.ACTIVITY_DIVIDED_DATA_TABLE, OmronDBConstans.ACTIVITY_DIVIDED_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.RECORD_DATA_TABLE, OmronDBConstans.RECORD_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.SLEEP_DATA_TABLE, OmronDBConstans.SLEEP_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.REMINDER_DATA_TABLE, OmronDBConstans.REMINDER_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.WEIGHT_DATA_TABLE, OmronDBConstans.WEIGHT_DATA);
        uriMatcher.addURI(OmronDBConstans.AUTHORITY, OmronDBConstans.OXYMETER_DATA_TABLE, OmronDBConstans.OXYMETER_DATA);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new DbHelper(context);
        mSqldb = mDbHelper.getWritableDatabase();
        return (mSqldb != null);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int choice = uriMatcher.match(uri);

        String tableName;
        Cursor cursor;
        switch (choice) {


            case OmronDBConstans.VITAL_DATA:
            case OmronDBConstans.ACTIVITY_DATA:
            case OmronDBConstans.RECORD_DATA:
            case OmronDBConstans.SLEEP_DATA:
            case OmronDBConstans.REMINDER_DATA:
            case OmronDBConstans.ACTIVITY_DIVIDED_DATA:
            case OmronDBConstans.WEIGHT_DATA:
            case OmronDBConstans.OXYMETER_DATA:
                mSqldb = mDbHelper.getReadableDatabase();
                tableName = uri.getLastPathSegment();
                queryBuilder.setTables(tableName);

                cursor = queryBuilder.query(mSqldb, projection, selection,
                        selectionArgs, null, null, sortOrder);
                if (cursor != null)
                    cursor.setNotificationUri(getContext().getContentResolver(),
                            uri);
                break;


            default:
                throw new IllegalArgumentException("UnKnown URI " + uri);

        }
        return cursor;

    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        mSqldb = mDbHelper.getWritableDatabase();
        long rowID;
        Uri retUri;
        switch (uriMatcher.match(uri)) {


            case OmronDBConstans.VITAL_DATA:
                rowID = mSqldb.insert(OmronDBConstans.VITAL_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.VITAL_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }
                throw new SQLException("Failed to insert row into " + uri);

            case OmronDBConstans.ACTIVITY_DATA:
                rowID = mSqldb.insert(OmronDBConstans.ACTIVITY_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.ACTIVITY_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }
                throw new SQLException("Failed to insert row into " + uri);

            case OmronDBConstans.ACTIVITY_DIVIDED_DATA:
                rowID = mSqldb.insert(OmronDBConstans.ACTIVITY_DIVIDED_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.ACTIVITY_DIVIDED_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }
                throw new SQLException("Failed to insert row into " + uri);

            case OmronDBConstans.RECORD_DATA:
                rowID = mSqldb.insert(OmronDBConstans.RECORD_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.RECORD_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            case OmronDBConstans.SLEEP_DATA:

                rowID = mSqldb.insert(OmronDBConstans.SLEEP_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.SLEEP_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }

            case OmronDBConstans.REMINDER_DATA:

                rowID = mSqldb.insert(OmronDBConstans.REMINDER_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.REMINDER_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }


                throw new SQLException("Failed to insert row into " + uri);

            case OmronDBConstans.WEIGHT_DATA:
                rowID = mSqldb.insert(OmronDBConstans.WEIGHT_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.WEIGHT_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
                
            case OmronDBConstans.OXYMETER_DATA:
                rowID = mSqldb.insert(OmronDBConstans.OXYMETER_DATA_TABLE, "",
                        values);
                if (rowID > 0) {
                    retUri = ContentUris.withAppendedId(OmronDBConstans.OXYMETER_DATA_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                    return retUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        mSqldb = mDbHelper.getWritableDatabase();
        String databasetable;
        switch (uriMatcher.match(uri)) {
            case OmronDBConstans.VITAL_DATA:
            case OmronDBConstans.ACTIVITY_DATA:
            case OmronDBConstans.ACTIVITY_DIVIDED_DATA:
            case OmronDBConstans.RECORD_DATA:
            case OmronDBConstans.SLEEP_DATA:
            case OmronDBConstans.REMINDER_DATA:
            case OmronDBConstans.WEIGHT_DATA:
            case OmronDBConstans.OXYMETER_DATA:
                databasetable = uri.getPathSegments().get(0);
                count = mSqldb.delete(databasetable, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


    // Helper Class to operate on DB
    public static class DbHelper extends SQLiteOpenHelper {
        private final Context mContext;

        public DbHelper(Context context) {

            super(context, OmronDBConstans.DB_NAME, null, OmronDBConstans.DB_VERSION);
            this.mContext = context;


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            switch (oldVersion) {


            }

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(OmronDBConstans.SQL_CREATE_VITAL_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_RECORD_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_SLEEP_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_ACTIVITY_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_ACTIVITY_DIVIDED_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_REMINDER_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_WEIGHT_DATA_TABLE);
            db.execSQL(OmronDBConstans.SQL_CREATE_OXYMETER_DATA_TABLE);
        }

    }
}
