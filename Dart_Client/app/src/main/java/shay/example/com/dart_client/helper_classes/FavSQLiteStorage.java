package shay.example.com.dart_client.helper_classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

import shay.example.com.dart_client.models.Favourite;

/**
 * Created by swapnilparashar on 01/07/2017.
 * Modified by Shay de Barra on 20/03/2018 migrated to Favourite Model and with Favourite methods and attributes
 * Email:  x16115864@student.ncirl.ie
 */

public class FavSQLiteStorage extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Favourites_DB";
    private static final String TABLE_NAME = "Favourites_Table";
    // Student Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ORIGIN = "key_origin";
    private static final String KEY_DEST = "key_destination";

    public FavSQLiteStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " ( " + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_ORIGIN + " TEXT, "
                + KEY_DEST + " TEXT )";
        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS);
        this.onCreate(sqLiteDatabase);
    }

    public void addFavourite(Favourite favourite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ORIGIN, favourite.getOrigin_name());
        contentValues.put(KEY_DEST, favourite.getDest_name());
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
   // testing     deleteAllFavourites();
    }

        public boolean checkMyFavourite(String origin, String destination) {
            List<Favourite> favouriteList = getAllFavourites();

            for (Favourite item : favouriteList) {
                if ((item.getOrigin_name().equalsIgnoreCase(origin)) && (item.getDest_name().equalsIgnoreCase(destination))) {
              //      Log.e("MATCH ", "MATCH : " + item);
                    return true;
                }
            }
            return false;
        }


    public List<Favourite> getAllMyFavourites() {
        List<Favourite> favouritesList = new LinkedList<>();
        String getAllFavouritesStatement = "SELECT * FROM "+TABLE_NAME+" ORDER BY id DESC LIMIT 4";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(getAllFavouritesStatement,null);
        Favourite favourite;
        if(cursor.moveToFirst()) {
            do {
                favourite = new Favourite();
                favourite.setId(Integer.parseInt(cursor.getString(0)));
                favourite.setOrigin_name(cursor.getString(1));
                favourite.setDest_name(cursor.getString(2));
                favouritesList.add(favourite);
            } while (cursor.moveToNext());
        }
        db.close();
        return favouritesList;
    }

    public List<Favourite> getAllFavourites() {
        List<Favourite> favouritesList = new LinkedList<>();
        String getAllFavouritesStatement = "SELECT * FROM "+TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(getAllFavouritesStatement,null);
        Favourite favourite;
        if(cursor.moveToFirst()) {
            do {
                favourite = new Favourite();
                favourite.setId(Integer.parseInt(cursor.getString(0)));
                favourite.setOrigin_name(cursor.getString(1));
                favourite.setDest_name(cursor.getString(2));
                favouritesList.add(favourite);
            } while (cursor.moveToNext());
        }
        db.close();
        return favouritesList;
    }

    public void deleteMyFavourite(Favourite favourite) {
        String origin = favourite.getOrigin_name();
        String dest = favourite.getDest_name();
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME,
                KEY_ORIGIN+"=? AND "+ KEY_DEST + "=?",
                new String[]{origin,dest});
        db.close();
    }

    public void deleteAllFavourites() {
        String DELETE_ALL_FAVOURITES_STATEMENT = "DELETE FROM "+TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_ALL_FAVOURITES_STATEMENT);
    }
}
