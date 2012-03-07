package de.dmoeller.MySettings;

import android.os.Environment;

public class IOTools {

public boolean checkexternalstorage () {
// Lt. Android Devloper erstmal prüfen, ob der externe Speicher verfügbar ist. 
// Anschließend lesbar und schreibbar. Nur wenn das geht, kann man weitermachen
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		return mExternalStorageWriteable;
	}

public String generatepath(String param) {
	 return param.substring(0,1+param.lastIndexOf('/'));
	}

public String generateapkname(String param) {
	 return param.substring(1+param.lastIndexOf('/'),param.length());
	}
}