package net.scadsdnd.adviser.myadviser;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.sqlite.*;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default onCrete method.
        // Move here anything should happen just after app started.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    // This function is called whe user clicks 'get advices sections'
    public void getSections(View vw){

        // Creating db connection
        scadSQLite dbheader = new scadSQLite(this);
        SQLiteDatabase db = dbheader.getReadableDatabase();

        // Setting up field names from db for request
        String[] secFields = {"id", "Title"};

        // Make simple SELECT request: get all data from table
        Cursor scadCurs = db.query(scadSQLite.TableSections, secFields, null, null, null, null,null);

        // Move cursor to first position in reulst set
        scadCurs.moveToFirst();

        // This storage variable for our values from db
        List<String> mItems = new ArrayList<String>();

        // Now go through resultSet, row by row.
        while (scadCurs.isAfterLast()==false){

             // Search for 'needle' in fieled 1
            //if (scadCurs.getString(0).toLowerCase().contains('needle')) {}

            // Get the value of 'Title'
            mItems.add(scadCurs.getString(1));

            // Don't forget to step forward, otherwise you will stack in infinity cycle!
            scadCurs.moveToNext();
        }

        // And now let's find our spinner
        // and replace it's values with db data
        Spinner aSpin = (Spinner) findViewById(R.id.spinAdviseType);                                                        // Find our spinner
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mItems);  // Create array adapter for our data
        aAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                                       // Format our list
        aSpin.setAdapter(aAdpt);                                                                                            // And insert it into actual spinner

        // The structure bellow are responsible to handling change of spinner values
        aSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parentView, View vw, int pos, long id){
                // This method will be called after user makes his decision.
                Log.e("POS:", Integer.toString(pos));

                // This code bellow are pretty close to used earlier
                scadSQLite dbheader = new scadSQLite(parentView.getContext());
                SQLiteDatabase db = dbheader.getReadableDatabase();

                // Here we have a little bigger set of columns from our second table
                String[] secFields = {"id", "Title", "Body", "Sections"};
                // In this query we just added WHERE section, to filter our data by selected Section id.
                Cursor scadCurs = db.query(scadSQLite.TableAdvices, secFields, "Sections=?", new String[]{Integer.toString(pos)}, null, null,null);

                // These will store values from db
                String advBody = "";

                scadCurs.moveToFirst();
                while (scadCurs.isAfterLast()==false){

                    // Just reading data and concatenating.
                    // I inserted some basic html to format the output
                    advBody += "<b>"+scadCurs.getString(1)+"</b><br>"+ scadCurs.getString(2)+"<br><br>";

                    // Don't forget to step forward, otherwise you will stack in infinity cycle!
                    scadCurs.moveToNext();

                }

                // And finally finding our text view and populating it with data from db.
                TextView lblBody = (TextView) findViewById(R.id.tvBody);
                lblBody.setText( Html.fromHtml(advBody) );
                // This conversion ^ a required to tell android use my html tags.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView){
                // This method called when user not selected anything
            }

        });

        // Print out some data in tvBody label
        TextView lblMessage = (TextView) findViewById(R.id.tvBody);
        lblMessage.setText( "Got "+ Integer.toString( scadCurs.getCount() )+" advice sections" );

        // Because the button did it's role, we hide it for now...
        Button btnGd =(Button) findViewById(R.id.btnGetData);
        btnGd.setVisibility(View.INVISIBLE);
    }


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
}
