package com.fifteen.placeit;
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
	// implementing a ServiceConnection
	private ServiceConnection connection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			
			service = ((MyService.LocalBinder)arg1).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			
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
	
	
	// call this to bind service
	public MyService bindService(){
		if(!isBinded){
			context.bindService(new Intent(context, MyService.class), connection, Context.BIND_AUTO_CREATE);
			isBinded = true;
		}
		return service;
	}
	
	// call this to unbind from service
	public MyService unBindService(){
		if(isBinded){
			context.unbindService(connection);
			isBinded = false;
		}
		return service;
	}
	
}
