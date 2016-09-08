package com.example.messageapp;

import java.util.Locale;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.gsm.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button btnSendSms;
	EditText number,sms;
	String phonNumber,Message;

	
	private TextView txtLat, txtLng, txtAcc, txtPro;
	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		number = (EditText) findViewById(R.id.receiverEditText);
		sms = (EditText) findViewById(R.id.messageeditText);
		btnSendSms = (Button)findViewById(R.id.btnSendSms);
		btnSendSms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				phonNumber = number.getText().toString();
				Message = txtLat.getText().toString()+"\n"+txtLng.getText().toString();
				Toast.makeText(getApplicationContext(), "Latt: "+txtLat.getText().toString()+"\nLong: "+txtLng.getText().toString(), Toast.LENGTH_SHORT).show();
				sendSms(phonNumber,Message);
			}
		});
		txtLat = (TextView) findViewById(R.id.txtLatitude);
		txtLng = (TextView) findViewById(R.id.txtLongitude);
		txtAcc = (TextView) findViewById(R.id.txtAccuracy);
		txtPro = (TextView) findViewById(R.id.txtProvider);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		boolean gpsEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!gpsEnabled) {
			Toast.makeText(getApplicationContext(),
					"Please consider enabling GPS", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setUp();
	}

	public void setUp() {
		Location gpsLocation = null;
		Location networkLocation = null;

		locationManager.removeUpdates(listener);
		gpsLocation = requestUpdateFromProvider(LocationManager.GPS_PROVIDER);
		networkLocation = requestUpdateFromProvider(LocationManager.NETWORK_PROVIDER);

		if (gpsLocation != null && networkLocation != null) {
			Location myLocation = getBetterLocation(gpsLocation,
					networkLocation);
			setValuesInUI(myLocation);
		} else if (gpsLocation != null) {
			setValuesInUI(gpsLocation);
		} else if (networkLocation != null) {
			setValuesInUI(networkLocation);
		} else {
			txtLat.setText("No data availble");
			// no data
		}
	}

	private void setValuesInUI(Location loc) {
		txtLat.setText("" + loc.getLatitude());
		txtLng.setText("" + loc.getLongitude());
		sms.setText(txtLat.getText().toString()+"\n"+txtLng.getText().toString());
		txtAcc.setText("Accuracy: " + loc.getAccuracy());
		txtPro.setText("Provider: " + loc.getProvider());
	}

	private Location getBetterLocation(Location newLocation,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return newLocation;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > 60000;
		boolean isSignificantlyOlder = timeDelta < 60000;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved.
		if (isSignificantlyNewer) {
			return newLocation;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return currentBestLocation;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return newLocation;
		} else if (isNewer && !isLessAccurate) {
			return newLocation;
		}
		return currentBestLocation;
	}

	private Location requestUpdateFromProvider(String provider) {
		Location location = null;
		if (locationManager.isProviderEnabled(provider)) {
			locationManager
					.requestLocationUpdates(provider, 1000, 10, listener);
			location = locationManager.getLastKnownLocation(provider);

		} else {
			Toast.makeText(getApplicationContext(),
					provider + " is not enabled.", Toast.LENGTH_LONG).show();
		}
		return location;
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(listener);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	LocationListener listener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onLocationChanged(Location location) {
			setValuesInUI(location);

		}
	};

	@SuppressWarnings("deprecation")
	private void sendSms(String pNumber, String mesage) {
		// TODO Auto-generated method stub
		String SENT="SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this,0, new Intent(DELIVERED), 0);
		
		//----when the message has been ssent------
		registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "Sms sent", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "No Service", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));
		
		//----when sms delivered-----
		registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
				}
			}
		}, new IntentFilter(DELIVERED));
		
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(pNumber, null, mesage, sentPI, deliveredPI);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
