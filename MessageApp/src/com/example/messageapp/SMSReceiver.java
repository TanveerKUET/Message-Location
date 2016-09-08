package com.example.messageapp;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;
public class SMSReceiver extends BroadcastReceiver{
	TextToSpeech ttobj;
	@Override
	public void onReceive(Context context, Intent intent) {
		 
		// get the message passed in
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		if(bundle!=null){
			//----retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs =new SmsMessage[pdus.length];
			for(int i=0;i<msgs.length;i++){
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				str +="SMS From "+msgs[i].getOriginatingAddress();
				str +=":";
				str +=msgs[i].getMessageBody().toString();
				str +="\n";
			}
			//----Display the new sms
			Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
			
		}
	}

}
