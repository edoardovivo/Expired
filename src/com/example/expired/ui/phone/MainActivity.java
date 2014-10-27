package com.example.expired.ui.phone;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
//import android.app.Activity;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.database.Cursor;
import android.database.sqlite.*;
import android.provider.BaseColumns;
import java.lang.Object;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.expired.R;
import com.example.expired.R.id;
import com.example.expired.R.layout;
import com.example.expired.R.menu;
import com.example.expired.database.DbManage;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.ClipData.Item;
import android.content.Context;

import com.example.expired.R;
import com.example.expired.service.ScheduleClient;

//import android.support.v4.app.NavUtils;


@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	
	
	
	
	DbManage dbExp;
	
	Calendar c = Calendar.getInstance();
	int startYear = c.get(Calendar.YEAR);
	int startMonth = c.get(Calendar.MONTH);
	int startDay = c.get(Calendar.DAY_OF_MONTH);
	
	// This is a handle so that we can call methods on our service
    private ScheduleClient scheduleClient;
    
    ArrayList<HashMap<String, String > > arraylist = 
    		new ArrayList<HashMap<String, String >>();
    HashMap<String, String > map = new HashMap<String, String >();
    
   
    SimpleAdapter adapter;
    ListView list;
    
    EditText textbought;
    EditText textexpdate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Create a new service client and bind our activity to this service
        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();
		
		openDB();
		
		textbought = (EditText) findViewById(R.id.TextBought);
		textexpdate = (EditText) findViewById(R.id.TextExpDate);
		
		//dbExp.deleteAll();
		list = (ListView) findViewById(R.id.DATABASE);
		
		adapter = new SimpleAdapter(this, arraylist, R.layout.row,
				new String[] {"Product Name", "Bought", "Expiration Date"},
				new int[] {R.id.PRODNAME, R.id.BOUGHT, R.id.EXPDATE}
		          );
		
		
		list.setAdapter(adapter);
		displayText(-1, "Product Name", "Date Bought", "Expiration Date");
		
		textbought.setText(""+startYear+"-"+(startMonth+1)+"-"+startDay);
		textexpdate.setText(""+startYear+"-"+(startMonth+1)+"-"+(startDay+1));
		
		textbought.setOnClickListener(new OnClickListener() {
			@SuppressLint("NewApi")
			public void onClick(View w) {
				DialogFragment dialogFragment = new StartDatePicker();
				Bundle args = new Bundle();
				args.putString("ID", "TextBought");
				CalDisplay(dialogFragment, args);
			}
		});
		
		textexpdate.setOnClickListener(new OnClickListener() {
			@SuppressLint("NewApi")
			public void onClick(View w) {
				DialogFragment dialogFragment = new StartDatePicker();
				Bundle args = new Bundle();
				args.putString("ID", "TextExpDate");
				CalDisplay(dialogFragment, args);
			}
		});
		
		/*
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int pos, long id) {
                // TODO Auto-generated method stub

                Log.v("long clicked","pos: " + pos);

                return true;
            }
        });
        */
		
		registerForContextMenu(list);
		
		
		
		
		
	}
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		closeDB();
	}
	
	

	private void openDB() {
		
		dbExp = new DbManage(this);
		dbExp.open();
		
	}
	
	private void closeDB() {
		
		dbExp.close();
	}
	
	private void displayText(int id, String prodname, String bought, String expdate) {
        //TextView textView = (TextView) findViewById(R.id.textDisplay);
        //textView.setText(message);
		map = new HashMap<String, String >();
		
		
		map.put("Product Name", prodname);
		map.put("Bought", bought);
		map.put("Expiration Date", expdate);
		arraylist.add(map);
		
				
		
		adapter.notifyDataSetChanged();
		
		
	}
	
	@SuppressLint("SimpleDateFormat")
	public Date ParseDate(String str) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		Date date = null;
		try {
			date = sdf.parse(str);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
 		
 	}
	
	public void onClick_AddRecord(View w) {
		//Retrieves the text fields
		EditText textprodname = (EditText) findViewById(R.id.TextProdName);
		EditText textbought = (EditText) findViewById(R.id.TextBought);
		EditText textexpdate = (EditText) findViewById(R.id.TextExpDate);
		
		//Insert new row in the database
		long newId = dbExp.insertRow(textprodname.getText().toString(),
				textbought.getText().toString(),
				textexpdate.getText().toString());
		
		//Displays database
		Cursor cursor = dbExp.getAllRows();
		displayRecordSet(cursor);
		
		//Set Notification Date
		Date dt;
		dt = ParseDate(textexpdate.getText().toString());
		
		Calendar c = Calendar.getInstance();
        c.setTime(dt);
        int day = c.get(Calendar.DAY_OF_MONTH);
        c.add(Calendar.DATE, -1);
        c.add(Calendar.HOUR_OF_DAY, 12);
        c.add(Calendar.SECOND, (int) Math.random()*10);
        //c.add(Calendar.MINUTE, (int) Math.random()*10);
        
        Date notifDate = c.getTime();
		
        
		// Ask our service to set an alarm for that date, 
        //this activity talks to the client that talks to the service
        scheduleClient.setAlarmForNotification(notifDate, 
        		textprodname.getText().toString(), (int)newId);
        // Notify the user what they just did
        Toast.makeText(this, "Notification set for: "+ 
        		textprodname.getText().toString(), Toast.LENGTH_SHORT).show();
		
		
	}
	public void OnClearTextClick (View w) {
		
		EditText textprodname = (EditText) findViewById(R.id.TextProdName);
		EditText textbought = (EditText) findViewById(R.id.TextBought);
		EditText textexpdate = (EditText) findViewById(R.id.TextExpDate);
		
		textprodname.setText("");
		textbought.setText("");
		textexpdate.setText("");
	}
	
	public void onClick_Display (View w) {
		
		Cursor cursor = dbExp.getAllRows();
		displayRecordSet(cursor);
	}
	
	public void CalDisplay(DialogFragment dialogFragment, Bundle args) {
		dialogFragment.setArguments(args);
	    dialogFragment.show(getFragmentManager(), "start_date_picker");
	}
	
	public void onClick_ClearData (View w) {
		
		arraylist.clear();
		displayText(-1, "Product Name", "Date Bought", "Expiration Date");
		dbExp.deleteAll();
	}
	
	
	
	// Display an entire record set to the screen.
	private void displayRecordSet(Cursor cursor) {
		
		arraylist.clear();
		displayText(-1, "Product Name", "Date Bought", "Expiration Date");
		// Reset cursor to start, checking to see if there's data:
		if (cursor.moveToFirst()) {
			do {
				// Process the data:
				int id = cursor.getInt(DbManage.COL_ROWID);
				String prodname = cursor.getString(DbManage.COL_PRODNAME);
				String bought = cursor.getString(DbManage.COL_BOUGHT);
				String expdate = cursor.getString(DbManage.COL_EXPDATE);
				
				// Append data to the message:
				//message += "id = " + id
				//		   +", Description = " + prodname
				//		   +", Date Bought: " + bought
				//		   +", Expiration Date: " + expdate
				//		   +"\n";
				displayText(id, prodname, bought, expdate);
			} while(cursor.moveToNext());
		}
		
		// Close the cursor to avoid a resource leak.
		cursor.close();
		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	@SuppressLint("NewApi")
	class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // TODO Auto-generated method stub
	        // Use the current date as the default date in the picker
	    	
	        DatePickerDialog dialog = new DatePickerDialog(
	        		MainActivity.this, this, startYear, startMonth, startDay);
	        return dialog;

	    }
	    public void onDateSet(DatePicker view, int year, int monthOfYear,
	            int dayOfMonth) {
	        // TODO Auto-generated method stub
	        // Do something with the date chosen by the user
	        startYear = year;
	        startMonth = monthOfYear;
	        startDay = dayOfMonth;
	        
	        //SimpleDateFormat sdf = new SimpleDateFormat(
	        //		"dd-MM-YYYY");
	        
	        Bundle args = this.getArguments();
	        String idstr = args.getString("ID");
	        String date = startYear + "-" + (startMonth+1) + "-" + startDay;
	        int resID = getResources().getIdentifier(
	        		idstr, "id", "com.example.expired");
	        EditText textedt = (EditText) findViewById(resID);
	        textedt.setText(date);
	        
	        
	        
	    }
	} 
	
	/**
	 * MENU
	 */

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      if (v.getId()==R.id.DATABASE) {
	          MenuInflater inflater = getMenuInflater();
	          inflater.inflate(R.menu.menu_list, menu);
	      }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      switch(item.getItemId()) {
	         case R.id.item_edit:
	         // add stuff here
	            return true;
	          case R.id.item_remove:
	            // edit stuff here
	                return true;
	           default:
	                return super.onContextItemSelected(item);
	      }
	}
	
	
	
	
	
	
	
	
	
	
	
}
