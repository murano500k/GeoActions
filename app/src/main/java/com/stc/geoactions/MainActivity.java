package com.stc.geoactions;

import android.arch.lifecycle.LifecycleActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends LifecycleActivity {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_GEOFENCE = 242;
    public static final int REQUEST_GEOFENCE_LOCATION = 3354;
    public static final String BUNDLE_LOCATION = "BUNDLE_LOCATION";
    public static final String GEOFENCE_WRK = "GEOFENCE_WRK";

    private TextView tvStatus;
    private FloatingActionButton fab, fabResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = (TextView) findViewById(R.id.text_status);
        fabResult = (FloatingActionButton) findViewById(R.id.floatingActionButtonHistory);
        fabResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AddFenceActivity.class), REQUEST_GEOFENCE_LOCATION);
            }
        });
        if(PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.MY_ID,null)!=null){
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_GEOFENCE_LOCATION) {
            if(resultCode==RESULT_OK){
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        }
    }

}
