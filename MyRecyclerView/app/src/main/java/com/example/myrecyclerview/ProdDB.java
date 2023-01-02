package com.example.myrecyclerview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProdDB extends SQLiteOpenHelper {
    private Context context;
    public static final String DATABASE_NAME = "prodList.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "prod_List";


    public ProdDB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE prod_List ( " +
                "prod_name TEXT, " +
                "prod_price TEXT, " +
                "prod_link TEXT, " +
                "prod_note TEXT, " +
                "prod_star INTEGER," +
                " prod_date TEXT, " +
                "prod_img BLOB);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
