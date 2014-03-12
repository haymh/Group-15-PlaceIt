package com.fifteen.placeit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginFragment extends DialogFragment {
	
	private AlertDialog.Builder alert;
	private SharedPreferences preference;
	private View layout;
	
	private EditText username;
	private EditText password;
	
	private static final String tag = LoginFragment.class.getSimpleName();
	
	// Creates new login fragment
    public static LoginFragment newInstance() {
        LoginFragment frag = new LoginFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
    	// Get saved credentials
    	preference = getActivity().getSharedPreferences(Constant.SP.SAVE, Context.MODE_PRIVATE);
    	
    	alert = new AlertDialog.Builder(getActivity());
    
    	alert.setTitle(Constant.LOGIN.TITLE);
    	    	
    	initializeLogin();
    	initializeLogout();
    	
    	initializeLayout();
    	    
        return alert.create();
    }

    
    // Initialize login button
    private void initializeLogin() {
    	alert.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
                ((MainActivity) getActivity()).doPositiveClick();
			}
		});
    }
    
    // Initialize logout button
    private void initializeLogout() {
    	alert.setNegativeButton("LOGOUT", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
                ((MainActivity) getActivity()).doNegativeClick();
			}
		});
    }
    
    // Initialize layout
    private void initializeLayout() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.dialog_layout, null);
    
    	initializeRegister();
    	
    	alert.setView(layout);
    }
    
    // Initialize register link
    private void initializeRegister() {
    	TextView register = (TextView) layout.findViewById(R.id.loginSpecial);
    	
    	// String format
    	SpannableString format = new SpannableString("Register");
        format.setSpan(new UnderlineSpan(), 0, format.length(), 0);
        register.setText(format);
    }
    
    @Override
    public void onCancel(DialogInterface dialog) {
    	((MainActivity) getActivity()).dialogCancel();
    	
    	super.onCancel(dialog);
    }
    
    // TODO do this
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       
        /* TODO THIS THIS!
	    View view = (View) layout.get
	    
	    EditText text = (EditText) view.findViewById(R.id.loginUsername);
	    */
        
        //Log.wtf(tag, "Attached!");
    }
}