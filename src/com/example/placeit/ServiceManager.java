package com.example.placeit;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;



public class ServiceManager {

	private Context context;
	private MyService service;
	private boolean isBinded;
	private ServiceConnection connection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			// TODO Auto-generated method stub
			service = ((MyService.LocalBinder)arg1).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			service = null;
		}
		
	};
	public ServiceManager(Context context){
		this.context = context;
		context.bindService(new Intent(context, MyService.class), connection, Context.BIND_AUTO_CREATE);
		isBinded = true;
	}
	
	public MyService getService(){
		return service;
	}
	
	public MyService bindService(){
		if(!isBinded){
			context.bindService(new Intent(context, MyService.class), connection, Context.BIND_AUTO_CREATE);
			isBinded = true;
		}
		return service;
	}
	
	public MyService unBindService(){
		if(isBinded){
			context.unbindService(connection);
			isBinded = false;
		}
		return service;
	}
	
}
