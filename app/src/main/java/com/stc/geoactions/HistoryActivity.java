package com.stc.geoactions;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stc.geoactions.data.DbAdapter;
import com.stc.geoactions.data.DbEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.stc.geoactions.AddFenceActivity.MY_ID;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    RecyclerView rv;
    DbAdapter adapter;
    TextView textTotal;
    DatabaseReference ref;
    String myId;
    Button btnAddFence;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        btnAddFence=(Button)findViewById(R.id.buttonAddFence);
        btnAddFence.setVisibility(View.GONE);
        btnAddFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, AddFenceActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        myId= PreferenceManager.getDefaultSharedPreferences(this).getString(MY_ID, null);
        if(myId==null){
            btnAddFence.setVisibility(View.VISIBLE);
        }else {
            adapter=new DbAdapter();
            rv=(RecyclerView)findViewById(R.id.rv);
            rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            rv.setAdapter(adapter);
            textTotal=(TextView)findViewById(R.id.text_total_time);
            ref= FirebaseDatabase.getInstance().getReference(myId);
            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    DbEntry entry = dataSnapshot.getValue(DbEntry.class);
                    if(entry!=null){
                        addItem(entry);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    DbEntry entry=dataSnapshot.getValue(DbEntry.class);
                    updateItem(entry);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    DbEntry entry = dataSnapshot.getValue(DbEntry.class);
                    deleteItem(entry);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(HistoryActivity.this, "error: "+databaseError.getMessage()+"\n"+databaseError.getDetails(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void addItem(DbEntry entry) {
        adapter.items.add(entry);
        adapter.notifyDataSetChanged();
        updateTotalTime();
    }

    private void updateTotalTime() {
        List<DbEntry>todayEntries=new ArrayList<>();
        for(DbEntry item : getItems()){
            Date date=new Date(item.timestamp);
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(date);
            if(calendar.get(Calendar.DAY_OF_YEAR)==Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
             todayEntries.add(item);
            }
        }
        Log.d(TAG, "total entries for today:"+todayEntries.size());
        if(!todayEntries.isEmpty()){
            long timeIn=todayEntries.get(0).timestamp;
            long timeOut=todayEntries.get(todayEntries.size()-1).timestamp;
            int diffMins = (int) ((timeOut-timeIn)/60000);
            int min = diffMins%60;
            int hrs=diffMins/60;
            String time="total for today: "+hrs+" hours "+min+" minutes";
            textTotal.setText(time);
        }
    }
    public List<DbEntry>getItems(){
        return adapter.items;
    }

    private void updateItem(DbEntry newItem){
        for(DbEntry e:adapter.items){
            if(e.key.equals(newItem.key)){
                adapter.items.add(adapter.items.indexOf(e),newItem);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void deleteItem(DbEntry item){
        adapter.items.remove(item);
        adapter.notifyDataSetChanged();
    }
}
