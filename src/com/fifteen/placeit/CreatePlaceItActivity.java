package com.fifteen.placeit;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.fifteen.placeit.R;
import com.fifteen.placeit.WeeklySchedule.NumOfWeekRepeat;
import com.google.android.gms.maps.model.LatLng;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class CreatePlaceItActivity extends Activity {
	
	private String title;
	private String description;
	private boolean repeatByWeek;
	private boolean repeatByMinute;
	private int repeatedDayInWeek;
	private int repeatedMinute;
	private NumOfWeekRepeat numOfWeekRepeat;
	private Date createDate;
	private Date postDate;	
	private LatLng coordinate;
	
	private EditText editTitle;
	private EditText editDescription;
	private EditText editPostdate;
	
	private TableRow tableRowRepeatWeekNumber;
	private TableRow tableRowRepeatWeeklyDetail;
	private TableRow tableRowRepeatMinuteDetail;
	private TableRow tableRowRepeatChoice;
	private View line;
	private DatePickerDialog dialog = null;
	private CheckBox[] checkBoxWeekDay;
	private RadioButton radioButtonByMinute;
	private RadioButton radioButtonByWeek;
	
	private ServiceManager sManager;
	private MyService service;
	
	
	/// implementing OnDateSetListener for listening DatePicker
	private DatePickerDialog.OnDateSetListener pickDate = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			view.updateDate(year, monthOfYear, dayOfMonth);
			editPostdate.setText(monthOfYear + 1 + "/" + dayOfMonth + "/" + year);
			dialog.hide();
		}

	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_place_it);
		
		editTitle = (EditText)findViewById(R.id.editTitle);
		editDescription = (EditText)findViewById(R.id.editDescription);
		editPostdate = (EditText)findViewById(R.id.editPostDate);
		tableRowRepeatWeekNumber = (TableRow)findViewById(R.id.repeatWeekNumber);
		tableRowRepeatWeeklyDetail = (TableRow)findViewById(R.id.repeatWeeklyDetail);
		tableRowRepeatMinuteDetail = (TableRow)findViewById(R.id.repeatMinuteDetail);
		tableRowRepeatChoice = (TableRow)findViewById(R.id.repeatChoice);
		line = (View)findViewById(R.id.line);
		checkBoxWeekDay = new CheckBox[7];
		checkBoxWeekDay[1] = (CheckBox)findViewById(R.id.checkBoxMon);
		checkBoxWeekDay[2] = (CheckBox)findViewById(R.id.checkBoxTue);
		checkBoxWeekDay[3] = (CheckBox)findViewById(R.id.checkBoxWed);
		checkBoxWeekDay[4] = (CheckBox)findViewById(R.id.checkBoxThur);
		checkBoxWeekDay[5] = (CheckBox)findViewById(R.id.checkBoxFri);
		checkBoxWeekDay[6] = (CheckBox)findViewById(R.id.checkBoxSat);
		checkBoxWeekDay[0] = (CheckBox)findViewById(R.id.checkBoxSun);
		
		radioButtonByMinute = (RadioButton)findViewById(R.id.radioButtonByMinute);
		radioButtonByWeek = (RadioButton)findViewById(R.id.radioButtonByWeek);
		
		Calendar c = Calendar.getInstance();
		checkBoxWeekDay[c.get(Calendar.DAY_OF_WEEK) - 1].setChecked(true);
		editPostdate.setHint(c.get(Calendar.MONTH) + 1 + "/" + c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.YEAR)
				+ " (Tap here to change)");
		
		editPostdate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Calendar c = Calendar.getInstance();
				if(dialog == null)
					dialog = new DatePickerDialog(CreatePlaceItActivity.this, pickDate,
							c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			}
		
		});
		
		sManager = new ServiceManager(this);
		
		Bundle bundle = getIntent().getParcelableExtra("bundle");
		coordinate = bundle.getParcelable("position");
		DecimalFormat df = new DecimalFormat("#.####");
		((EditText)findViewById(R.id.editLatLng)).setText("( " + df.format(coordinate.latitude) + " , " + df.format(coordinate.longitude) + " )");
		addlisteners(); // call a helper method to add listener
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_place_it, menu);
		return true;
	}

	public void onResume() {
		super.onResume();
		// ensure service is not null, also not block the UI, so launch a AsyncTask
		new AsyncTask<Void, Void, Integer>() {
	        protected void onPreExecute() {}

	        protected Integer doInBackground(Void... params) {
	            while(service == null)
	            	service = sManager.bindService();
	            return Integer.valueOf(1);
	        }
	        
	        protected void onPostExecute(Integer result) { }
	    }.execute();
	}


	public void onDestroy(){
		service = sManager.unBindService(); // unbind from service
		super.onDestroy();
	}
	
	// when user click on create button, then call this method to create a Place-it
	public void create(View view){
		if(validate()){
			boolean s = service.createPlaceIt(title, description, repeatedDayInWeek, repeatedMinute, numOfWeekRepeat, createDate,
					postDate, coordinate.latitude, coordinate.longitude, AbstractPlaceIt.Status.ACTIVE, null);
			
			Log.wtf("create", "repeatedMinute: " + repeatedMinute + " repeatbyMinute: " + repeatByMinute);
			if(s){
				Toast.makeText(this, "New Place-it Created", Toast.LENGTH_SHORT).show();
				this.finish();
			}else
				Toast.makeText(this, "New Place-it was not Created", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	// when user click on cancel button, then call this method to cancel creating Place-it
	public void cancel(View view){
		this.finish();
	}

	// add listeners to a radiobuttongroup which select repeat by minute and repeat by week and checkbox for repeat option
	public void addlisteners(){
		RadioGroup rg = (RadioGroup)findViewById(R.id.radioGroupRepeatChoice);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// checkedId is the RadioButton selected
				if(checkedId == R.id.radioButtonByMinute){
					tableRowRepeatWeekNumber.setVisibility(View.GONE);
					tableRowRepeatWeeklyDetail.setVisibility(View.GONE);
					tableRowRepeatMinuteDetail.setVisibility(View.VISIBLE);
				}
				if(checkedId == R.id.radioButtonByWeek){
					tableRowRepeatWeekNumber.setVisibility(View.VISIBLE);
					tableRowRepeatWeeklyDetail.setVisibility(View.VISIBLE);
					tableRowRepeatMinuteDetail.setVisibility(View.GONE);
				}
			}
		});

		CheckBox checkBoxRepeat = (CheckBox)findViewById(R.id.checkBoxRepeat);

		checkBoxRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				
				if(isChecked){
					tableRowRepeatChoice.setVisibility(View.VISIBLE);
					line.setVisibility(View.VISIBLE);
					if(radioButtonByMinute.isChecked()){
						tableRowRepeatMinuteDetail.setVisibility(View.VISIBLE);
					}
					if(radioButtonByWeek.isChecked()){
						tableRowRepeatWeekNumber.setVisibility(View.VISIBLE);
						tableRowRepeatWeeklyDetail.setVisibility(View.VISIBLE);
					}
				}
				else{
					tableRowRepeatChoice.setVisibility(View.INVISIBLE);
					tableRowRepeatWeekNumber.setVisibility(View.GONE);
					tableRowRepeatWeeklyDetail.setVisibility(View.GONE);
					tableRowRepeatMinuteDetail.setVisibility(View.GONE);
					line.setVisibility(View.GONE);
				}
				
				InputMethodManager input = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				input.hideSoftInputFromWindow(CreatePlaceItActivity.this.findViewById(android.R.id.content).getWindowToken(),0);
			}
			
		});
		
	}
	
	
	// validating user input
	public boolean validate(){
		title = editTitle.getText().toString();
		description = editDescription.getText().toString();
		if(title.equals("") && description.equals("")){
			Toast.makeText(this, "Title and Description can not both be empty", Toast.LENGTH_SHORT).show();
			editTitle.requestFocus();
			return false;
		}
		if(title.equals("") && !description.equals("")){
			String s[] = description.split(" |\n");
			if(s.length <= 3)
				title = description;
			else{
				for(int i = 0; i < 3; i++)
					title += (s[i] + " ");
			}
			Log.wtf("heng decription","<" + description + ">");
		}
		if(editPostdate.getText().toString().equals(""))
			postDate = new Date();
		else{
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			try {
				postDate = formatter.parse(editPostdate.getText().toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "date format: MM/dd/yyyy", Toast.LENGTH_SHORT).show();
				editPostdate.requestFocus();
				return false;
			}
		}
		boolean isRepeat = ((CheckBox)findViewById(R.id.checkBoxRepeat)).isChecked();
		if(isRepeat){
			repeatByMinute = radioButtonByMinute.isChecked();
			repeatByWeek = radioButtonByWeek.isChecked();
		}else{
			repeatByMinute = false;
			repeatByWeek = false;
		}
		
		if(repeatByMinute){
			String s = ((EditText)findViewById(R.id.editMinute)).getText().toString();
			if(s.equals("")){
				Toast.makeText(this, "Please Enter the Minutes", Toast.LENGTH_SHORT).show();
				return false;
			}
			try{
				repeatedMinute = Integer.parseInt(s);
			}catch(NumberFormatException e){
				((EditText)findViewById(R.id.editMinute)).requestFocus();
				Toast.makeText(this, "Invalid input for minutes", Toast.LENGTH_SHORT).show();
			}
		}else
			repeatedMinute = 0;
		
		repeatedDayInWeek = 0;
		numOfWeekRepeat = WeeklySchedule.NumOfWeekRepeat.ONE;
		if(repeatByWeek){
			if(((RadioButton)findViewById(R.id.radioButtonEveryWeek)).isChecked())
				numOfWeekRepeat = WeeklySchedule.NumOfWeekRepeat.ONE;
			if(((RadioButton)findViewById(R.id.radioButtonEveryOtherWeek)).isChecked())
				numOfWeekRepeat = WeeklySchedule.NumOfWeekRepeat.TWO;
			if(((RadioButton)findViewById(R.id.radioButtonEveryThreeWeek)).isChecked())
				numOfWeekRepeat = WeeklySchedule.NumOfWeekRepeat.THREE;
			if(((RadioButton)findViewById(R.id.radioButtonEveryFourWeek)).isChecked())
				numOfWeekRepeat = WeeklySchedule.NumOfWeekRepeat.FOUR;
			
			if(checkBoxWeekDay[1].isChecked())
				repeatedDayInWeek += WeeklySchedule.MON;
			if(checkBoxWeekDay[2].isChecked())
				repeatedDayInWeek += WeeklySchedule.TUE;
			if(checkBoxWeekDay[3].isChecked())
				repeatedDayInWeek += WeeklySchedule.WED;
			if(checkBoxWeekDay[4].isChecked())
				repeatedDayInWeek += WeeklySchedule.THURS;
			if(checkBoxWeekDay[5].isChecked())
				repeatedDayInWeek += WeeklySchedule.FRI;
			if(checkBoxWeekDay[6].isChecked())
				repeatedDayInWeek += WeeklySchedule.SAT;
			if(checkBoxWeekDay[0].isChecked())
				repeatedDayInWeek += WeeklySchedule.SUN;
			
			if(repeatedDayInWeek == 0){
				Toast.makeText(this, "Please pick the day you want to repeat", Toast.LENGTH_SHORT).show();
				return false;
			}
				
		}
		createDate = new Date();
		if(coordinate == null){
			Toast.makeText(this, "coordinate is null", Toast.LENGTH_SHORT).show();
			return false;
		}
		Log.wtf("create","isRepeat: " + isRepeat + " repeatByMinute: "+ repeatByMinute + " repeatByWeek: " + repeatByWeek);
		return true;
	}

}
