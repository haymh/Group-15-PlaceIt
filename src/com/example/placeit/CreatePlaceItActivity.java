package com.example.placeit;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.example.placeit.PlaceIt.NumOfWeekRepeat;
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
	
	private ServiceManager sManager;
	private MyService service;
	
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
		addlisteners();
		

		// Get marker location
		

		//TextView text = (TextView) findViewById(R.id.lat);
		//text.setText(String.valueOf(location.latitude));

		//text = (TextView) findViewById(R.id.lng);
		//text.setText(String.valueOf(location.longitude));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_place_it, menu);
		return true;
	}

	public void onResume() {
		super.onResume();
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
		service = sManager.unBindService();
		super.onDestroy();
	}
	
	public void create(View view){
		if(validate()){
			boolean s = service.createPlaceIt(title, description, repeatByMinute, repeatedMinute, repeatByWeek, repeatedDayInWeek, numOfWeekRepeat, createDate, postDate, coordinate);
			if(s){
				Toast.makeText(this, "New Place-it Created", Toast.LENGTH_SHORT).show();
				this.finish();
			}else
				Toast.makeText(this, "New Place-it was not Created", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void cancel(View view){
		this.finish();
	}

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
				}
				else{
					tableRowRepeatChoice.setVisibility(View.INVISIBLE);
					tableRowRepeatWeekNumber.setVisibility(View.GONE);
					tableRowRepeatWeeklyDetail.setVisibility(View.GONE);
					tableRowRepeatMinuteDetail.setVisibility(View.GONE);
					line.setVisibility(View.GONE);
				}
			}
		});
		
	}
	
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
			repeatByMinute = ((RadioButton)findViewById(R.id.radioButtonByMinute)).isSelected();
			repeatByWeek = ((RadioButton)findViewById(R.id.radioButtonByWeek)).isSelected();
		}else{
			repeatByMinute = false;
			repeatByWeek = false;
		}
		
		if(repeatByMinute){
			repeatedMinute = Integer.parseInt(((EditText)findViewById(R.id.editMinute)).getText().toString());
		}else
			repeatedMinute = 0;
		
		repeatedDayInWeek = 0;
		numOfWeekRepeat = PlaceIt.NumOfWeekRepeat.ONE;
		if(repeatByWeek){
			if(((RadioButton)findViewById(R.id.radioButtonEveryWeek)).isSelected())
				numOfWeekRepeat = PlaceIt.NumOfWeekRepeat.ONE;
			if(((RadioButton)findViewById(R.id.radioButtonEveryOtherWeek)).isSelected())
				numOfWeekRepeat = PlaceIt.NumOfWeekRepeat.TWO;
			if(((RadioButton)findViewById(R.id.radioButtonEveryThreeWeek)).isSelected())
				numOfWeekRepeat = PlaceIt.NumOfWeekRepeat.THREE;
			if(((RadioButton)findViewById(R.id.radioButtonEveryFourWeek)).isSelected())
				numOfWeekRepeat = PlaceIt.NumOfWeekRepeat.FOUR;
			
			if(((CheckBox)findViewById(R.id.checkBoxMon)).isChecked())
				repeatedDayInWeek += PlaceIt.MON;
			if(((CheckBox)findViewById(R.id.checkBoxTue)).isChecked())
				repeatedDayInWeek += PlaceIt.TUE;
			if(((CheckBox)findViewById(R.id.checkBoxWed)).isChecked())
				repeatedDayInWeek += PlaceIt.WED;
			if(((CheckBox)findViewById(R.id.checkBoxThur)).isChecked())
				repeatedDayInWeek += PlaceIt.THURS;
			if(((CheckBox)findViewById(R.id.checkBoxFri)).isChecked())
				repeatedDayInWeek += PlaceIt.FRI;
			if(((CheckBox)findViewById(R.id.checkBoxSat)).isChecked())
				repeatedDayInWeek += PlaceIt.SAT;
			if(((CheckBox)findViewById(R.id.checkBoxSun)).isChecked())
				repeatedDayInWeek += PlaceIt.SUN;
		}
		createDate = new Date();
		if(coordinate == null){
			Toast.makeText(this, "coordinate is null", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

}
