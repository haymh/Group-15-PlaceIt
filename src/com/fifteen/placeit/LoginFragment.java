package com.fifteen.placeit;

import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout.Alignment;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Class in charge of the login fragment
// Allows user to login and logout
// Connects to service & server
// Dictates if user has access of app
public class LoginFragment extends DialogFragment {

	private AlertDialog.Builder alert;
	private SharedPreferences preference;
	private View layout;

	private boolean access = false;
	private boolean loginOrRegister = Constant.LOGIN.LOGIN;

	private String username = "";
	private String password = "";
	private static String regId = "";

	ServiceManager manager;
	MyService service;

	private static final String tag = LoginFragment.class.getSimpleName();
	
	public static final float FONT_SIZE = (float) 1.3;

	// Creates new login fragment
	public static LoginFragment newInstance(String id) {
		LoginFragment frag = new LoginFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		regId = id;

		return frag;
	}

	// Create the dialog, its layout and buttons
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get saved credentials
		preference = getActivity().getSharedPreferences(Constant.SP.SAVE, Context.MODE_PRIVATE);

		alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(Constant.LOGIN.TITLE);

		initializeButtons();
		initializeLayout();

		// Initialize service manager, required for serving bind
		manager = new ServiceManager(getActivity());

		return alert.create();
	}

	// Required to override dismiss()
	@Override 
	public void onStart() {
		super.onStart();

		if(access) {
			Log.wtf("ACCESS", "TRUE");
		}
		else {
			Log.wtf("ACCESS", "FALSE");
		}

		initializePositive();
		initializeNegative();
		initializeRegister();
		initializeText();

		// Binds service
		new AsyncTask<Void, Void, Void>(){ 
			protected Void doInBackground(Void... params) {
				while(service == null)
					service = manager.bindService();
				return null;
			}
		}.execute();
	}

	// Unbinds service
	public void onDestroy() {
		service = manager.unBindService();
		super.onDestroy();
	}

	// Initialize buttons, override them on onStart()
	// Required to override default dismiss() behavior
	private void initializeButtons() {
		alert.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});

		alert.setNegativeButton("LOGOUT", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});
	}

	// Initialize layout
	private void initializeLayout() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.dialog_layout, null);

		initializeRegister();

		alert.setView(layout);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		((MainActivity) getActivity()).dialogCancel();
		super.onCancel(dialog);
	}

	// Initialize positive button
	// LOGIN & REGISTER
	private void initializePositive() {
		AlertDialog dialog = (AlertDialog) getDialog();

		Button positive = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);
		positive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handlePositive();
			}
		});
	}

	// Initialize negative button
	// LOGOUT & CANCEL
	private void initializeNegative() {
		AlertDialog dialog = (AlertDialog) getDialog();

		Button negative = (Button) dialog.getButton(Dialog.BUTTON_NEGATIVE);
		negative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleNegative();
			}
		});
	}

	// Initialize register link
	private void initializeRegister() {
		TextView register = (TextView) layout.findViewById(R.id.loginOrRegister);
		SpannableString format = new SpannableString("Register");
		format.setSpan(new UnderlineSpan(), 0, format.length(), 0);
		register.setText(format);		

		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				flipLoginOrRegister((TextView) view);
			}
		});
	}

	// Initialize EditText
	private void initializeText() {
		getCredentials();

		if(!username.isEmpty() && !password.isEmpty()) {
			setText(username, password);
		}
		else {
			clearCredentials();
		}
	}

	// SUPPORT	
	// Get username & password
	private void getCredentials() {
		username = preference.getString(Constant.SP.U.USERNAME, "");
		password = preference.getString(Constant.SP.U.PASSWORD, "");
	}

	// Set both EditText
	private void setText(String username, String password) {
		EditText text = (EditText) layout.findViewById(R.id.loginUsername);		
		text.setText(username);

		text = (EditText) layout.findViewById(R.id.loginPassword);
		text.setText(password);
	}

	// Get EditText contents
	private String getTextUsername() {
		EditText text = (EditText) layout.findViewById(R.id.loginUsername);		
		return text.getText().toString();
	}

	private String getTextPassword() {
		EditText text  = (EditText) layout.findViewById(R.id.loginPassword);
		return text.getText().toString();
	}

	// Clear both EditText
	private void clearText() {
		setText("", "");
	}

	// OPTION HANDLERS
	private void handlePositive() {
		if(loginOrRegister == Constant.LOGIN.LOGIN) {
			// Login button
			handleLogin();
		}
		else {
			// Register button
			handleRegister();
		}
	}

	// Negative button handler
	private void handleNegative() {
		if(loginOrRegister == Constant.LOGIN.LOGIN) {
			// Logout button
			handleLogout();
		}
		else {
			// Cancel button
			handleCancel();
		}
	}

	// ON LOGIN. Register button press
	private void handleLogin() {
		accessServer();
	}

	// ON LOGIN. Logout button press
	private void handleLogout() {
		clearText();
		clearCredentials();
		service.deleteDatabase();
	}

	// ON REGISTER. Register button press
	private void handleRegister() {
		accessServer();
	}

	// ON REGISTER. Cancel button press
	private void handleCancel() {
		// Resets back to regular login
		flipLoginOrRegister();
	}

	// Validates user input
	private boolean validate() {

		if(getTextUsername().isEmpty() || getTextPassword().isEmpty()) {
			Toast.makeText(getActivity(), "Username & password cannot be empty!", Toast.LENGTH_LONG).show();
			return false;
		}
		else {
			return true;
		}
	}

	// Clears local credentials
	private void clearCredentials() {
		preference.edit().remove(Constant.SP.U.USERNAME).commit();
		preference.edit().remove(Constant.SP.U.PASSWORD).commit();
	}

	// Checks for local credentials
	private boolean hasCredentials() {
		getCredentials();

		if(!username.equals(getTextUsername()) || !password.equals(getTextPassword())) {
			return false;
		}

		// Has saved credentials
		if(!username.isEmpty() && !password.isEmpty()) {
			return true;
		}

		return false;
	}

	// TODO WORK ON THIS
	private void accessServer() {
		if(service == null) {
			// Service is off, stop
			return;
		}

		if(!validate()) {
			// Username or password EditText is empty, stop
			return;
		}

		if(hasCredentials()) {
			// Has localized credentials, access set to true
			Log.wtf("SET access to be ", " true");
			access = true;
		}else
			Log.wtf("SET access to be ", " false");

		// Saved to connect to server
		username = getTextUsername();
		password = getTextPassword();
		preference.edit().putBoolean(Constant.SP.U.LOGIN, false).commit();
		
		String title, message;
		if(loginOrRegister == Constant.LOGIN.LOGIN) {
			title = "LOGGING IN";
			message = "I got you, buddy!";
		}
		else {
			title = "REGISTERING";
			message = "Hooking you up!";
		}
		
		SpannableString span =  new SpannableString(message);
        span.setSpan(new RelativeSizeSpan(FONT_SIZE), 0, span.length(), 0);  
		final ProgressDialog dialog = ProgressDialog.show(getActivity(), title, span, false);

		new AsyncTask<String, Void, Integer>() {
			long time;

			protected void onPreExecute() {
				time = new Date().getTime();
			}

			@Override
			protected Integer doInBackground(String... argv) {
				int request = 0;
				try {
					if(loginOrRegister == Constant.LOGIN.LOGIN) {
						request = service.login(argv[0], argv[1], argv[2]);
					}
					else {
						request = service.register(argv[0], argv[1], argv[2]);
					}
				} catch(Exception e) {
					Log.wtf(tag, "At login() " + e.toString());
				}
				return request;
			}

			protected void onPostExecute(Integer results) {
				long temp = new Date().getTime() - time;

				Log.wtf("LOGGED", results.toString() + " in " + String.valueOf(temp) + " ms");

				switch(results) {
				case Constant.LOGIN.OK:
					if(!access){
						// new user, so delete old data
						Log.wtf("new user login or register", "here");
						service.deleteDatabase();
						service.createDatabase();
						
						// for user login on a new device, request all the information from server
						if(loginOrRegister == Constant.LOGIN.LOGIN)
								service.init();
					}				
					
					preference.edit().putString(Constant.SP.U.USERNAME, username).commit();
					preference.edit().putString(Constant.SP.U.PASSWORD, password).commit();
					preference.edit().putBoolean(Constant.SP.U.LOGIN, true).commit();
					access = true;
					break;
				case Constant.LOGIN.CONFLICT:
					popup("USERNAME EXISTS", "I'd get a better name if I were you.");
					access = false;
					break;
				case Constant.LOGIN.NOT_FOUND:
					popup("JUST KIDDING", "Can't find you!");
					break;
				case Constant.LOGIN.FAIL:
					popup("JUST KIDDING", "Must be a type in there!");
					break;
				default:
					popup("JUST KIDDING", "Server down, sorry!");
				}

				if(access) {
					dismiss();
				}
				dialog.dismiss();
			}
		}.execute(username, password, regId);
		dialog.show();
	}
	
	private void popup(String title, String message) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(title);
		
		SpannableString span =  new SpannableString(message);
		span.setSpan(new RelativeSizeSpan(Constant.F.POPUP_SIZE), 0, span.length(), 0);  	
		
		alert.setMessage(span);
		
		//alert.setMessage(message);
		alert.setPositiveButton("OK", null);
		
		AlertDialog dialog = alert.show();
		TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
		messageText.setGravity(Gravity.CENTER_VERTICAL);
		dialog.show();
	}
	
	// LOGIN OR REGISTER? 
	// Flips the login & register label
	private void flipLoginOrRegister() {
		TextView view = (TextView) layout.findViewById(R.id.loginOrRegister);

		flipLoginOrRegister(view);
	}

	private void flipLoginOrRegister(TextView view) {
		loginOrRegister = !loginOrRegister;

		String text;
		if(loginOrRegister == Constant.LOGIN.LOGIN) {
			text = "Register";
			getCredentials();
			setText(username, password);
		}
		else {
			text = "Sign in";
			clearText();
		}

		SpannableString format = new SpannableString(text);
		format.setSpan(new UnderlineSpan(), 0, format.length(), 0);
		view.setText(format);

		flipButtons();
	}

	// Flip buttons
	private void flipButtons() {
		AlertDialog dialog = (AlertDialog) getDialog();

		Button negative = (Button) dialog.getButton(Dialog.BUTTON_NEGATIVE);
		Button positive = (Button) dialog.getButton(Dialog.BUTTON_POSITIVE);

		if(loginOrRegister == Constant.LOGIN.LOGIN) {
			positive.setText("LOGIN");
			negative.setText("LOGOUT");
		}
		else {
			positive.setText("REGISTER");
			negative.setText("CANCEL");
		}
	}
}