package net.scadsdnd.adviser.myadviser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Default onCrete method.
        // Move here anything should happen just after app started.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    // This function is called whe user clicks 'let's start'
    public void getSections(View vw){
        // This will create new intent from our activity class
        Intent intSec = new Intent(this, SectionsActivity.class);
        // And this will actually fire up our new activity
        startActivity(intSec);
    }

}
