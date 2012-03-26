package de.dmoeller.MySettings;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

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

public boolean mountsystemrw () throws InterruptedException, IOException {
// system rw mounten
// root-Rechte holen
	Boolean SystemMountRWOK = false;
	Process process = Runtime.getRuntime().exec("su");
	DataOutputStream os = new DataOutputStream(process.getOutputStream());
// Datei-Operationen per shell-command, damit root-Rechte greifen
	os.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock2 /system" + "\n"); // \n executes the command
	os.flush();	
	os.writeBytes("exit\n");
	os.flush();
	process.waitFor();
	SystemMountRWOK = true;
			 
	return SystemMountRWOK;		
}

public boolean mountsystemro () throws InterruptedException, IOException {
	// system ro mounten
	// root-Rechte holen
		Boolean SystemMountROOK = false;
		Process process = Runtime.getRuntime().exec("su");
		DataOutputStream os = new DataOutputStream(process.getOutputStream());
	// Datei-Operationen per shell-command, damit root-Rechte greifen
		os.writeBytes("mount -o remount,ro -t yaffs2 /dev/block/mtdblock2 /system" + "\n"); // \n executes the command
		os.flush();	
		os.writeBytes("exit\n");
		os.flush();
		process.waitFor();
		SystemMountROOK = true;
				 
		return SystemMountROOK;		
	}

public boolean rootcopydir (String quelle, String ziel) throws InterruptedException, IOException {
	// root-Rechte holen
			Boolean CopyStatusOK = false;
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
	// Datei-Operationen per shell-command, damit root-Rechte greifen
			os.writeBytes("cp -R " + quelle + "* " + ziel +"\n"); // \n executes the command
			os.flush();	
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			CopyStatusOK = true;
					 
			return CopyStatusOK;
		}

public boolean rootcopyfile (String quelle, String ziel) throws InterruptedException, IOException {
	// root-Rechte holen
			Boolean CopyStatusOK = false;
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
	// Datei-Operationen per shell-command, damit root-Rechte greifen
			os.writeBytes("cp " + quelle + " " + ziel +"\n"); // \n executes the command
			os.flush();	
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
			CopyStatusOK = true;
					 
			return CopyStatusOK;
		}

public boolean copydirectory(File quelle, File ziel) throws FileNotFoundException, IOException {
//ACHTUNG: Nur nutzbar für Dateien und Ordner, die die App ohne Root-Rechte sehen darf!
//Recursives Kopieren eines Directorys mit Files und Unterordnern	        
    Boolean CopyStatusOK = false;
	File[] files = quelle.listFiles();
    ziel.mkdirs();
    if (files != null) {
    	for (File file : files) {
            if (file.isDirectory()) {
                copydir(file, new File(ziel.getAbsolutePath() + System.getProperty("file.separator") + file.getName()));
            }
            else {
                copyfile(file, new File(ziel.getAbsolutePath() + System.getProperty("file.separator") + file.getName()));
            }
        }
    	CopyStatusOK = true;
    }
    return CopyStatusOK;
} 

public void copydir(File quelle, File ziel) throws FileNotFoundException, IOException {
//Achtung, CopyDir als Kopie von CopyDirectory, da es eine Endlosschleife gibt, wenn CopyDirectory innerhalb 
//der Methode erneut aufgerufen wird!	        
	File[] files = quelle.listFiles();
    ziel.mkdirs();
    if (files != null) {
    	for (File file : files) {
            if (file.isDirectory()) {
                copydir(file, new File(ziel.getAbsolutePath() + System.getProperty("file.separator") + file.getName()));
            }
            else {
                copyfile(file, new File(ziel.getAbsolutePath() + System.getProperty("file.separator") + file.getName()));
            }
        }
    }
} 

public void copyfile(File file, File ziel) throws FileNotFoundException, IOException {
	 try {
		    InputStream is = new FileInputStream (file);
	        OutputStream os = new FileOutputStream(ziel);
	        byte[] data = new byte[is.available()];
	        is.read(data);
	        os.write(data);
	        is.close();
	        os.close();
	        } 
	    catch (IOException e) {
	        // Unable to create file, likely because external storage is
	        // not currently mounted.
	        Log.w("ExternalStorage", "Error writing " + file, e);
	    }
	}
}