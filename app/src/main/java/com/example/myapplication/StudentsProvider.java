package com.example.myapplication;

import java.util.HashMap;

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
import android.text.TextUtils;

public class StudentsProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.example.provider.College";

    static final String URLstudents = "content://" + PROVIDER_NAME + "/students";
    static final Uri CONTENT_URIstudents = Uri.parse(URLstudents);

    static final String URLteacher = "content://" + PROVIDER_NAME + "/teacher#1";


    static final Uri CONTENT_URIteacher = Uri.parse(URLteacher);


    static final String _ID = "_id";
    static final String NAME = "name";
    static final String GRADE = "grade";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    static final int STUDENTS = 1;
    static final int STUDENT_ID = 2;
    static final int TEACHER = 3;
    static final int TEACHER_ID = 4;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "students", STUDENTS);
        uriMatcher.addURI(PROVIDER_NAME, "students/#", STUDENT_ID);
        uriMatcher.addURI(PROVIDER_NAME, "teacher", TEACHER);
        uriMatcher.addURI(PROVIDER_NAME, "teacher/#", TEACHER_ID);
    }

    /**
     * 数据库特定常量声明
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "College";
    static final String STUDENTS_TABLE_NAME = "students";
    static final String STUDENTS_TABLE_NAME1 = "teacher";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + STUDENTS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " name TEXT NOT NULL, " +
                    " grade TEXT NOT NULL);";


    static final String CREATE_DB_TABLE1 =
            " CREATE TABLE " + STUDENTS_TABLE_NAME1 +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " name BLOB NOT NULL); ";


    /**
     * 创建和管理提供者内部数据源的帮助类.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
            db.execSQL(CREATE_DB_TABLE1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  STUDENTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " +  STUDENTS_TABLE_NAME1);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * 如果不存在，则创建一个可写的数据库。
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * 添加新学生记录
         */
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String table;

        String URL;



        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                table=STUDENTS_TABLE_NAME;
                URL = "content://" + PROVIDER_NAME + "/students";
                break;

            case STUDENT_ID:
                table=STUDENTS_TABLE_NAME;
                URL = "content://" + PROVIDER_NAME + "/students";
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            case TEACHER:
                table=STUDENTS_TABLE_NAME1;
                URL = "content://" + PROVIDER_NAME + "/teacher";
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case TEACHER_ID:
                table=STUDENTS_TABLE_NAME1;
                URL = "content://" + PROVIDER_NAME + "/teacher";
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }


        Uri CONTENT_URI = Uri.parse(URL);

        long rowID = db.insert( table, "", values);

        /**
         * 如果记录添加成功
         */

        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                qb.setTables(STUDENTS_TABLE_NAME);
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case STUDENT_ID:
                qb.setTables(STUDENTS_TABLE_NAME);
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            case TEACHER:
                qb.setTables(STUDENTS_TABLE_NAME1);
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case TEACHER_ID:
                qb.setTables(STUDENTS_TABLE_NAME1);
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;



            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }



        Cursor c = qb.query(db, projection, selection, selectionArgs,null, null, null);

        /**
         * 注册内容URI变化的监听器
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case STUDENTS:
                count = db.delete(STUDENTS_TABLE_NAME, selection, selectionArgs);
                break;

            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( STUDENTS_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;


            case TEACHER:
                count = db.delete(STUDENTS_TABLE_NAME1, selection, selectionArgs);
                break;

            case TEACHER_ID:
                String id1 = uri.getPathSegments().get(1);

                count = db.delete( STUDENTS_TABLE_NAME1, _ID +  " = " + id1 +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;







            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case STUDENTS:
                count = db.update(STUDENTS_TABLE_NAME, values, selection, selectionArgs);
                break;

            case STUDENT_ID:
                count = db.update(STUDENTS_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;



            case TEACHER:
                count = db.update(STUDENTS_TABLE_NAME1, values, selection, selectionArgs);
                break;

            case TEACHER_ID:
                count = db.update(STUDENTS_TABLE_NAME1, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;




            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * 获取所有学生记录
             */
            case STUDENTS:
                return "vnd.android.cursor.dir/vnd.example.students";

            /**
             * 获取一个特定的学生
             */
            case STUDENT_ID:
                return "vnd.android.cursor.item/vnd.example.students";



            case TEACHER:
                return "vnd.android.cursor.dir/vnd.example.teacher";

            /**
             * 获取一个特定的学生
             */
            case TEACHER_ID:
                return "vnd.android.cursor.item/vnd.example.teacher";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}