package net.scadsdnd.adviser.myadviser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;

import android.text.Html;

public class CardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Default onCrete method.
        // Move here anything should happen just after activity started.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        // First of all, let's extract the id, we sent from previous activity, based on the user click
        String theID = getIntent().getStringExtra("theIdVal");

        // This code bellow are pretty close to used earlier
        scadSQLite dbheader = new scadSQLite(this);
        SQLiteDatabase db = dbheader.getReadableDatabase();

        // Here we have a little bigger set of columns from our second table
        String[] secFields = {"id", "Title", "Body", "Sections"};
        // In this query we just added WHERE section, to filter our data by selected Section id.
        Cursor scadCurs = db.query(
                scadSQLite.TableAdvices,
                secFields,
                "Sections=?",
                new String[]{theID},
                null,
                null,
                null
        );

        // These will store values from db
        String advBody = "";

        // Strat from the top of result set
        scadCurs.moveToFirst();
        // Loop through the db data
        while (scadCurs.isAfterLast()==false){

            // Just reading data and concatenating.
            // Inserted some basic html to format the output
            advBody += "<b>"+scadCurs.getString(1)+"</b><br>"+ scadCurs.getString(2)+"<br><br>";

            // Don't forget to step forward, otherwise you will stack in infinity cycle!
            scadCurs.moveToNext();

        }

        // And finally, let's find our text view, and populating it with data from db.
        TextView lblBody = (TextView) findViewById(R.id.textViewCard);
        // This conversion is a required, to tell android use my html tags.
        lblBody.setText( Html.fromHtml(advBody) );

    }
}
