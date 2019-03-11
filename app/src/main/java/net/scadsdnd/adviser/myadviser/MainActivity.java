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

}
