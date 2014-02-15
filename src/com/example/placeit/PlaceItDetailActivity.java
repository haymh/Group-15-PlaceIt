package com.example.placeit;

import java.text.DecimalFormat;
 
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
 
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
 
public class PlaceItDetailActivity extends Activity {
 
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                        Intent intent = getIntent();
                        String name = intent.getStringExtra("Name");
                        final long id = intent.getLongExtra("ID", 0);
                        String description = intent.getStringExtra("Description");
                        double lat = intent.getDoubleExtra("Latitude", 0);
                        double lon = intent.getDoubleExtra("Longitude", 0);
                       
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_place_it_detail);
 
                 Button button1 = (Button) findViewById(R.id.button1);
                 button1.setOnClickListener(new View.OnClickListener()
                 {
                         @Override
                         public void onClick(View v)
                         {
                                Toast.makeText(PlaceItDetailActivity.this, "Pulled Down Successfully!", Toast.LENGTH_LONG).show();
                                        finish();
                                 //Pull down
                         }
                 });
 
                 Button button2 = (Button) findViewById(R.id.button2);
                 button2.setOnClickListener(new View.OnClickListener()
                 {
                         @Override
                         public void onClick(View v)
                         {
                                    //MyService.discardPlaceIt(id); Incorrect, but what to do?
                                    Toast.makeText(PlaceItDetailActivity.this, "Deleted Successfully!", Toast.LENGTH_LONG).show();
                                        finish();
                                 //Delete
                         }
                 });
 
                 TextView text1 = (TextView)  findViewById(R.id.editText1);
                 //text1.setText(this.getDescription());
                 text1.setText("Description goes here");
 
                 TextView text2 = (TextView) findViewById(R.id.editText2);
                 text2.setText("Date Set for goes here");
                 
                 DecimalFormat df = new DecimalFormat("#.##");
                 TextView text3 = (TextView) findViewById(R.id.editText3);
                 text3.setText(df.format(lat) + "," + df.format(lon));
 
                 TextView text4 = (TextView) findViewById(R.id.editText4);
                 text4.setText("Repetition Schedule goes here");
        }
 
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.place_it_detail, menu);
                return true;
        }
 
}
