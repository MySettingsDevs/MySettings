package de.dmoeller.MySettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.dmoeller.MySettings.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MySettingsActivity extends Activity implements OnClickListener {
	
	private Button apps_sichern_button;
    private Button apps_loeschen_button;
    private Button bluetooth_pairing_sichern_button;
    private Button density_wert_aendern_button;
    private Button uv_werte_sichern_button;
    private Button restore_button;
    private List<PackageInfo> packs;
    private MySettingsDatenbank MysettingsDB;
	private String Global_MySettings_Dir; 
    
// Konstanten für das Anlegen der fixen Ordnerstruktur	
	private static String backups_apps_dir 						= "/Backup-Apps";
	private static String backup_systemapps_dir 				= "/Backup-Apps/Backup-SystemApps";
	private static String backup_userapps_dir 					= "/Backup-Apps/Backup-UserApps";
	
	private static String delete_apps_dir 						= "/Delete-Apps";
	private static String delete_systemapps_dir 				= "/Delete-Apps/Delete-SystemApps";
	private static String delete_userapps_dir 					= "/Delete-Apps/Delete-UserApps";
	
	private static String install_apps_dir 						= "/Install-Apps";
	private static String install_systemapps_dir				= "/Install-Apps/Install-SystemApps";
	private static String install_userapps_dir					= "/Install-Apps/Install-UserApps";
	
	private static String install_mediafiles_dir				= "/Install-Mediafiles";
	private static String install_mediafiles_alarms_dir 		= "/Install-Mediafiles/alarms";
	private static String install_mediafiles_bootanimation_dir 	= "/Install-Mediafiles/bootanimation"; 
	private static String install_mediafiles_notifications_dir	= "/Install-Mediafiles/notifications";
	private static String install_mediafiles_ringtones_dir 		= "/Install-Mediafiles/ringtones";
	
	private static String install_mods_dir	 					= "/Install-Mods";
	private static String install_mods_framework_dir 			= "/Install-Mods/framework";
	private static String install_mods_init_d_dir 				= "/Install-Mods/init.d";
	private static String install_mods_systemUI_dir 			= "/Install-Mods/SystemUI";
	
	private static String bluetooth_paaring_dir	 				= "/Bluetooth-Paaring";
	
	
	
    	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
// Erster Zugriff auf die Datenbank, damit wird Sie	falls noch nicht existent angelegt.
	    new MySettingsDatenbank(this).getWritableDatabase();

//Datenbanklasse referenzieren	       
	    MysettingsDB = new MySettingsDatenbank(this);	       

//Globales SD-Kartenverzeichnis aus der Datenbank lesen
	    Global_MySettings_Dir = MysettingsDB.readsdcarddir();

// Fixe Ordner auf der SD-Karte anlegen und initiale Dateien aus dem assets-Ordner im MySettings-Ordner ablegen
	 	initsdcardfolders ();
	  	inityorsettingsfile ();
	  	initmysettingscwmfile ();
	  	
// Installierte Apps auslesen und in die Datenbank schreiben bzw. aktualisieren
//		getinstalledapps ();

			
// Buttons anlegen und OnClickEreignisse registieren
        apps_sichern_button = (Button)findViewById(R.id.apps_sichern_button);
        apps_sichern_button.setOnClickListener(this);
        
        apps_loeschen_button = (Button)findViewById(R.id.apps_loeschen_button);
        apps_loeschen_button.setOnClickListener(this);
        
        bluetooth_pairing_sichern_button = (Button)findViewById(R.id.bluetooth_pairing_sichern_button);
        bluetooth_pairing_sichern_button.setOnClickListener(this);
        
        density_wert_aendern_button = (Button)findViewById(R.id.density_wert_aendern_button);
        density_wert_aendern_button.setOnClickListener(this);
        
        uv_werte_sichern_button = (Button)findViewById(R.id.uv_werte_sichern_button);
        uv_werte_sichern_button.setOnClickListener(this);
        
        restore_button = (Button)findViewById(R.id.restore_button);
        restore_button.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
			
		if (v == apps_sichern_button) {
			Intent intent = new Intent(this, SaveAppsActivity.class);
			startActivity(intent);
		}
		
		if (v == apps_loeschen_button) {
			Intent intent = new Intent(this, DeleteAppsActivity.class);
			startActivity(intent);
		}

		if (v == bluetooth_pairing_sichern_button) {
// system-Ordner mit den Bluetooth Daeien/Ordnern rekursiv auf die SD-Karte kopieren
			String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
			String Quellordner = "/system/etc/bluetooth/";
			String Zielordner = SdCardDestPath + bluetooth_paaring_dir;
			Boolean DirectoryCopyOK = false;
			
			try {
					DirectoryCopyOK = new IOTools().rootcopydir(Quellordner, Zielordner);
				} 
			catch (InterruptedException e) {
					e.printStackTrace();
					}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			if (DirectoryCopyOK) {
				new AlertDialog.Builder(this) 
            	.setMessage("Bluetooth-Paaring auf SD-Karte kopiert!")
            	.setNeutralButton(R.string.error_ok, null)
            	.show();
			return;
			}
			else {
				new AlertDialog.Builder(this) 
            	.setMessage("ERROR: Keine Bluetooth-Paarings gefunden!")
            	.setNeutralButton(R.string.error_ok, null)
            	.show();
			return;
			}			
		}
	
		if (v == density_wert_aendern_button) {
// Zeile ro.sf.lcd_density=nnn in der /system/build.prop ändern
			Intent intent = new Intent(this, ChangeDensityActivity.class);
			startActivity(intent);
		}

		if (v == uv_werte_sichern_button) {
// system/etc/init.d/S_volt_scheduler auf die sd-Karte kopieren
			String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
			String QuellDatei = "/system/etc/init.d/S_volt_scheduler";
			String Zielordner = SdCardDestPath + install_mods_init_d_dir;
			Boolean FileCopyOK = false;
			
			try {
					FileCopyOK = new IOTools().rootcopyfile(QuellDatei, Zielordner);
				} 
			catch (InterruptedException e) {
					e.printStackTrace();
					}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			if (FileCopyOK) {
				new AlertDialog.Builder(this) 
            	.setMessage("UV-Werte auf SD-Karte kopiert!")
            	.setNeutralButton(R.string.error_ok, null)
            	.show();
			return;
			}
			else {
				new AlertDialog.Builder(this) 
            	.setMessage("ERROR: Keine UV-Werte gefunden!")
            	.setNeutralButton(R.string.error_ok, null)
            	.show();
			return;
			}			
		}

		if (v == restore_button) {
			new AlertDialog.Builder(this) 
            	.setMessage("Bisher noch nicht implementiert")
            	.setNeutralButton(R.string.error_ok, null)
            	.show();
			return;
		}
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.einstellungen:
	    	Intent intent = new Intent(this, EinstellungenActivity.class);
			startActivity(intent);
	        return true;
	    case R.id.info:
	    	new AlertDialog.Builder(this) 
        	.setMessage("Bisher noch nicht implementiert")
        	.setNeutralButton(R.string.error_ok, null)
        	.show();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void getinstalledapps () {
// Auslesen der installierten Apps
// Die Liste der Apps wird in das Array packs geschrieben. 
// Anschließend der erste Eintrag (zum Test) in die Variable p vom Typ PackageInfo.
// Daraus wird dann der Name der App gelesen und kann dann weiter verwendet werden		
		packs = getPackageManager().getInstalledPackages(0);
		
		int maxpacks = packs.size();				
							
		for (int i = 0; i < maxpacks; i++) 
		{
			PackageInfo p = packs.get(i);
			String packageName = (p.applicationInfo.loadLabel(getPackageManager()).toString () + " " + "(" + i + ")");	
			if (MysettingsDB.existsapp(packageName) == false) {
				
			}
		}	
	}
	
	public void initsdcardfolders () {
// Array mit den Konstanten füllen, damit in der Schleife die Ordner angelegt werden können.
// Achtung Array mit ficer Größe! Bei weiteren Ordnern muss das Array vergrößert werden und unten das Array weiter füllen!
		String [] InitFolders;
		int maxInitFolders = 20;
		InitFolders = new String[ maxInitFolders ];
		
		InitFolders [0] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		InitFolders [1] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + backups_apps_dir).toString();
		InitFolders [2] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + backup_systemapps_dir).toString();				
		InitFolders [3] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + backup_userapps_dir).toString();
		InitFolders [4] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + delete_apps_dir).toString();
		InitFolders [5] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + delete_systemapps_dir).toString();
		InitFolders [6] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + delete_userapps_dir).toString();
		InitFolders [7] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_apps_dir).toString();
		InitFolders [8] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_systemapps_dir).toString();
		InitFolders [9] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_userapps_dir).toString();
		InitFolders [10] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mediafiles_dir).toString();
		InitFolders [11] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mediafiles_alarms_dir).toString();
		InitFolders [12] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mediafiles_bootanimation_dir).toString();
		InitFolders [13] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mediafiles_notifications_dir).toString();
		InitFolders [14] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mediafiles_ringtones_dir).toString();
		InitFolders [15] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mods_dir).toString();
		InitFolders [16] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mods_framework_dir).toString();
		InitFolders [17] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mods_init_d_dir).toString();
		InitFolders [18] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + install_mods_systemUI_dir).toString();
		InitFolders [19] = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + bluetooth_paaring_dir).toString();
				
// Methode für Initializierung -- Prüfen, ob Verzeichnis da ist, in die die Daten kopiert werden sollen        
        boolean mExternalStorageWriteable = new IOTools().checkexternalstorage(); 
		if (mExternalStorageWriteable) {
			for (int i = 0; i < maxInitFolders; i++) 
			{
				File FFolder 	= new File(InitFolders[i]);
				if (!FFolder.exists()) {
					FFolder.mkdirs();
						if (!FFolder.mkdirs()) { 
							Log.w("ExternalStorage", "Error writing " + FFolder);
						}
				}
			}		
		}
	}
	
	public void inityorsettingsfile () {
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		File DFile = new File(SdCardDestPath, "YourSettings.sh");
		
		if(!DFile.exists())
		{		
			try { 
				InputStream is = getAssets().open( "YourSettings.sh" ); 
				OutputStream os  = new FileOutputStream(DFile);
				byte[] data = new byte[is.available()];
				is.read(data);
				os.write(data);
				is.close();
				os.close();
	    		}
			catch (IOException e) {
				// Unable to create file, likely because external storage is
				// not currently mounted.
				Log.w("ExternalStorage", "Error writing " + DFile, e);
			}
	    }
	}
	
	public void initmysettingscwmfile () {
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		File DFile = new File(SdCardDestPath, "My-Settings-CWM.zip");
		
		try { 
			InputStream is = getAssets().open( "My-Settings-CWM.zip" ); 
			OutputStream os  = new FileOutputStream(DFile);
			byte[] data = new byte[is.available()];
			is.read(data);
			os.write(data);
			is.close();
			os.close();
	    	}
		catch (IOException e) {
			// Unable to create file, likely because external storage is
			// not currently mounted.
			Log.w("ExternalStorage", "Error writing " + DFile, e);
		}
	} 
}