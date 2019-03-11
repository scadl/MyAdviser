package net.scadsdnd.adviser.myadviser;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// This is custom sqlite_helper, responsible for opening and preparing internal db
public class scadSQLite extends SQLiteOpenHelper {

    public final static String TableSections = "Sections";
    public final static String TableAdvices = "Advices";

    // These constants and all methods except onCreate and onUpdate are needed only because we are using existing db
    // You see, sqlite_helper can't read from any place except app's 'data/database' subfolder.
    // The problem is, we can't bundle this subfolder to to our apk, but we can add our db to 'assets'.
    // So on creating our helper we should check if app has internal db,
    // and if not, crete it, than copy our prepared data there.
    private final Context dbCtx;
    private final static String dbName = "advices.db";
    private String dbPath = "";

    // The main constructor of class. It should have this form:
    //public MySQLHelper(Context context, String name, CursorFactory factory, int version)
    public scadSQLite(Context cnt){
        // This is required part:
        // it tells the name of db file other required info
        super(cnt, dbName, null, 1);

        // This is optional part, required only for use of existing db
        // This code bellow check existence of internal db
        // And if something wrong, preparing it to work
        this.dbCtx = cnt;
        this.dbPath = "/data/data/"+cnt.getApplicationContext().getPackageName()+"/databases/";
        if(checkDB()){
            // Our db already exists.
            // Don't do anything
        } else {
            // Oh shacks, no internal db.
            try{
                // Let's call our copier, and make one!
                this.getReadableDatabase();
                copyDB();
                this.close();
            } catch (IOException e){
                Log.e("DB", "Error in copying db!");
            }
            // I think our user should know, that out app just finished initialization process!
            Toast.makeText(cnt, "Initialization completed successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    // This is required (even when creating empty db)
    @Override
    public void onCreate(SQLiteDatabase db){
        //Called when the database is created for the first time.
        // This is where the creation of tables and the initial population of the tables should happen.
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TableAdvices+" (id INTEGER PRIMARY KEY ASC, Sections INTEGER, Title TEXT, Body TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TableSections+" (id INTEGER PRIMARY KEY ASC, Title TEXT)");
        // These two requests are not really needed,
        // but they are ensures, that if we can't copy our prepared database,
        // The new empty one will at lest has required tables and fields
    }

    // This is required (even when creating empty db)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV){
        // Called when the database needs to be upgraded.
        // The implementation should use this method to drop tables, add tables,
        // or do anything else it needs to upgrade to the new schema version.
        onCreate(db);
    }

    // Copying our prepared db data to internal one
    private void copyDB() throws IOException{

        // Here all the magic happens!
        Log.e("DB", "Now creating copy of inital db...");

        // Let's build input and output streams first
        InputStream myInput = dbCtx.getAssets().open(dbName); // Look, here the 'assets' called
        OutputStream myOutput = new FileOutputStream(dbPath+dbName);

        // Now cearfully read all data kilobyte by kilobyte
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            // And write it to our new internal db
            myOutput.write(buffer, 0, length);
        }

        // Don't forget to clean after yourself.
        myOutput.flush();
        myOutput.close();
        myInput.close();
        Log.e("DB", "Copying finished...");

    }

    // Checks if internal db exists or not
    private boolean checkDB(){
        // Prepare variables
        SQLiteDatabase testDB = null;
        Boolean dbOK = false;
        // Try to open internal db
        try {
            testDB = SQLiteDatabase.openDatabase(dbPath+dbName, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e){
            Log.e("DB","DB does not exists, sorry!");
        }
        // If it is in place, than everything ok.
        if( testDB!= null){
            dbOK = true;
            testDB.close();
        }
        // Don't forget return result!
        return dbOK;
    }
}
