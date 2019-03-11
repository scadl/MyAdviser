package net.scadsdnd.adviser.myadviser;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class SectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Default onCrete method.
        // Move here anything should happen just after activity started.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);

        // Creating db connection
        scadSQLite dbheader = new scadSQLite(this);
        SQLiteDatabase db = dbheader.getReadableDatabase();

        // Setting up field names from db for request
        String[] secFields = {"id", "Title", "Descript"};

        // Make simple SELECT request: get all data from table
        Cursor scadCurs = db.query(scadSQLite.TableSections, secFields, null, null, null, null,null);

        // Move cursor to first position in reulst set
        scadCurs.moveToFirst();

        // This storage variable for our values from db
        List<String[]> mItems = new ArrayList<String[]>();

        // Now go through resultSet, row by row.
        while (scadCurs.isAfterLast()==false){

            // Search for 'needle' in fieled 1
            //if (scadCurs.getString(0).toLowerCase().contains('needle')) {}

            // Get the value of 'Title', 'Descript'
            String[] dbdata = new String[3];        // This is storage array for db data
            dbdata[0] = scadCurs.getString(0);   // This will hold 'ID' or PK
            dbdata[1] = scadCurs.getString(1);   // This will hold 'Title'
            dbdata[2] = scadCurs.getString(2);  // This will hold 'Descript'
            mItems.add(dbdata);                     // Now add our collection to varaible

            // Don't forget to step forward, otherwise you will stack in infinity cycle!
            scadCurs.moveToNext();
        }

        // first, let's find our container
        final ListView lv = (ListView) findViewById(R.id.listView);

        // This block responsible for genrating list view with db data
        ListAdapter la = new SecAdapter(this, mItems);  // then creating custom adapter, and feeding datta to it
        lv.setAdapter(la);                                   // now, with filled adapter, giving it to our list view

        // this little "trick" will store context of our activity
        final Context theForm = this;

        // The block bellow is simple eventListener, so app will react to user click on item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // when user click, we just extract the data from our list view

                // DON'T forget, you fed the ARRAY, so read it correctly!
                String[] itemD = (String[]) lv.getItemAtPosition(i);


                // Now let's prepare to send data to next activity

                // First, declare the new activity class (using parent's context)
                Intent intCard = new Intent(getBaseContext(), CardActivity.class);

                // IMPORTNAT: place into putExrtra method data to sen in key:value manner
                intCard.putExtra("theIdVal", itemD[0]);

                // And finally fire up your new activity
                startActivity(intCard);

                // this is just for debugging purposes
                Log.e("THE_ID", itemD[0] );
            }
        });

    }

    // this custom array adapter, required to prepare our data to display as formatted text
     class SecAdapter extends ArrayAdapter<String[]>{

        // first, let's dealre simple constructor, with our data types
        public SecAdapter(Context cnt, List<String[]> objects){
            super(cnt, R.layout.layout_item, objects);
        }

        // second, let's implement simple logic, able to feed our db data to resposible widgets
        public View getView(int pos, View conVw, ViewGroup vwgr){

            // Layout inflater - is a powerful method for parsing data and "converting" it to widgets
            LayoutInflater infl = LayoutInflater.from(getContext());                // this is where we create our inflater
            View vw = infl.inflate(R.layout.layout_item, vwgr, false);  // and than construct it, using our mini-layout as scaffold

            // But before giving out our new listView, let's fill corresponding labels with our data, got from constructor.
            TextView tvTitle = (TextView) vw.findViewById(R.id.lblCaption);
            tvTitle.setText(getItem(pos)[1]);
            TextView tvDescr = (TextView) vw.findViewById(R.id.lblDescr);
            tvDescr.setText(getItem(pos)[2]);

            return vw; // <- now let's give our new item_layout to calling container
        }
     }
}
