package de.dmoeller.MySettings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;


public class ChangeDensityActivity extends Activity implements View.OnClickListener {

	private Button Density_Wert_OK_button;
	private String Density_Wert;
    private MySettingsDatenbank MysettingsDB;
	private String Global_MySettings_Dir; 
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

// Layout setzen		
		setContentView(R.layout.change_density);
		
// Click-Ereignis für den OK-Button des SDCard-Ordners setzen
		Density_Wert_OK_button = (Button)findViewById(R.id.density_wert_ok_button);
		Density_Wert_OK_button.setOnClickListener(this);
		
//Datenbanklasse referenzieren	       
	    MysettingsDB = new MySettingsDatenbank(this);	
	    
//Globales SD-Kartenverzeichnis aus der Datenbank lesen
	    Global_MySettings_Dir = MysettingsDB.readsdcarddir();
	    
// build.prop aus /system/-Ordner auf die interne sd-Karte in das MySettingsVerzeichnis kopieren
	    copybuildproptosd ();
	    
// Density-Wert aus der build.prop Datei auslesen
		Density_Wert = readdensity();
	    EditText DensityEdit = (EditText)findViewById(R.id.density_wert);
	    DensityEdit.setText(Density_Wert);
	}

	public void onClick(View v) {
		if ( v == Density_Wert_OK_button) {
			EditText DensityEdit = (EditText)findViewById(R.id.density_wert);
		    Density_Wert = DensityEdit.getText().toString();
		    String SearchLine = "ro.sf.lcd_density=";
		    String ReplaceValue = SearchLine + Density_Wert;
		    
		    Boolean UpdateBuildPropOK;
		    UpdateBuildPropOK = updatebuildpropfile (SearchLine, ReplaceValue);
		    
		    if (UpdateBuildPropOK) {
		    	copybuildproptosystem ();
		    	new AlertDialog.Builder(this) 
	        	.setMessage("Density-Wert in /system/build.prop angepasst! Reboot!")
	        	.setNeutralButton(R.string.error_ok, null)
	        	.show();
			return;
			}		
		}
	}
	
	private void copybuildproptosd () {
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		String QuellDatei = "/system/build.prop";
		String Zielordner = SdCardDestPath;
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
		
		if (!FileCopyOK) {
			new AlertDialog.Builder(this) 
        	.setMessage("ERROR: Keine /system/build.prop gefunden!")
        	.setNeutralButton(R.string.error_ok, null)
        	.show();
		return;
		}			
	}
	
	private void copybuildproptosystem () {
// /system Read-Write mounten
		Boolean MountSystemRWOK = false;
		
		try {
			MountSystemRWOK = new IOTools().mountsystemrw();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
			}
		catch (IOException e) {
			e.printStackTrace();
			}
	
		if (!MountSystemRWOK) {
			new AlertDialog.Builder(this) 
        	.setMessage("ERROR: /system kann nicht read-write gemountet werden!")
        	.setNeutralButton(R.string.error_ok, null)
        	.show();
			return;
		}

// build.prop in /system-Ordner kopieren
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		String QuellDatei = SdCardDestPath + "/build.prop";
		String Zielordner = "/system";
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
		
		if (!FileCopyOK) {
			new AlertDialog.Builder(this) 
        	.setMessage("ERROR: Keine " + SdCardDestPath + "/build.prop gefunden!")
        	.setNeutralButton(R.string.error_ok, null)
        	.show();
		return;
		}
		
// /system Read-Only mounten
		Boolean MountSystemROOK = false;
		
		try {
			MountSystemROOK = new IOTools().mountsystemro();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
			}
		catch (IOException e) {
			e.printStackTrace();
			}
	
		if (!MountSystemROOK) {
			new AlertDialog.Builder(this) 
        	.setMessage("FATAL ERROR: /system kann nicht read-only gemountet werden!")
        	.setNeutralButton(R.string.error_ok, null)
        	.show();
			return;
		}
	}
	
	private String readdensity() {
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		String PathFilename = SdCardDestPath + "/build.prop";
		List<String> lines = new ArrayList<String>();
		String line = "";
		String Density_Wert = "";
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(PathFilename));
			while ((line = reader.readLine()) != null) {
				if (line.contains("ro.sf.lcd_density=")) {
					// ersetzten, anfügen was auch immer;
					Density_Wert = line.substring(line.indexOf('=')+1); 
					lines.add(line);
				} 
				else {
					lines.add(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Density_Wert;
	}

	public Boolean updatebuildpropfile (String search, String replace) {
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir).toString();
		String PathFilename = SdCardDestPath + "/build.prop";
		List<String> lines = new ArrayList<String>();
		String line = "";
		
		Boolean UpdateBuildPropOK = false;
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(PathFilename));
			while ((line = reader.readLine()) != null) {
				if (line.contains(search)) {
					// ersetzten, anfügen was auch immer;
					line = replace;
					lines.add(line);
				} 
				else {
					lines.add(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			UpdateBuildPropOK = false;
		} catch (IOException e) {
			e.printStackTrace();
			UpdateBuildPropOK = false;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					UpdateBuildPropOK = false;
				}
			}
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter (new FileWriter(PathFilename));
			UpdateBuildPropOK = true;
			for (int i = 0; i < lines.size(); i++)
            {
				writer.write(lines.get(i)); 
				writer.newLine();
            }
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			UpdateBuildPropOK = false;
		} catch (IOException e) {
			e.printStackTrace();
			UpdateBuildPropOK = false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					UpdateBuildPropOK = false;
				}
			}
		}
		return UpdateBuildPropOK;
	}
}