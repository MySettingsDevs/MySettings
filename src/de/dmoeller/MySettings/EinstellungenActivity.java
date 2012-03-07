package de.dmoeller.MySettings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EinstellungenActivity extends Activity implements OnClickListener {
	  /** Called when the activity is first created. */
	private Button Ordner_SD_Karte_button;
	private ToggleButton CM7Icons_ToggleButton;
	private ToggleButton Circle_ToggleButton;
	private ToggleButton Quad_ToggleButton;
	private ToggleButton CM7taskswitcher_ToggleButton;
	private ToggleButton CM9taskswitcher_ToggleButton;
	private ToggleButton SamsungApps_ToggleButton;
	private MySettingsDatenbank MysettingsDB;
	private String Global_MySettings_Dir;
		
	public void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
	       setContentView(R.layout.einstellungen);

// Click-Ereignis für den OK-Button des SDCard-Ordners setzen
	       Ordner_SD_Karte_button = (Button)findViewById(R.id.ordner_sd_karte_save_button);
	       Ordner_SD_Karte_button.setOnClickListener(this); 

	       CM7Icons_ToggleButton = (ToggleButton)findViewById(R.id.CM7Icons);
	       CM7Icons_ToggleButton.setOnClickListener(this);
	       
	       Circle_ToggleButton = (ToggleButton)findViewById(R.id.Circle);
	       Circle_ToggleButton.setOnClickListener(this);
	       
	       Quad_ToggleButton = (ToggleButton)findViewById(R.id.Quad);
	       Quad_ToggleButton.setOnClickListener(this);
	       
	       CM7taskswitcher_ToggleButton = (ToggleButton)findViewById(R.id.CM7taskswitcher);
	       CM7taskswitcher_ToggleButton.setOnClickListener(this);
	       
	       CM9taskswitcher_ToggleButton = (ToggleButton)findViewById(R.id.CM9taskswitcher);
	       CM9taskswitcher_ToggleButton.setOnClickListener(this);
	       
	       SamsungApps_ToggleButton = (ToggleButton)findViewById(R.id.SamsungApps);
	       SamsungApps_ToggleButton.setOnClickListener(this);
	       
// Datenbanklasse referenzieren	       
   	       MysettingsDB = new MySettingsDatenbank(this);	       

// Globales SD-Kartenverzeichnis aus der Datenbank lesen und in das Eingabefeld füllen
	       Global_MySettings_Dir = MysettingsDB.readsdcarddir();
	       EditText SDCardDir = (EditText)findViewById(R.id.ordner_sdkarte);
	       SDCardDir.setText(Global_MySettings_Dir);
	       
	    // Toggle-Buttons je nach Einstellung der YourSettings.sh setzen	       
	       inittogglebuttons();

	 }

	public void onClick(View v) {
		// TODO Auto-generated method stub
			
		if (v == Ordner_SD_Karte_button) {

			EditText SDCardDir = (EditText)findViewById(R.id.ordner_sdkarte);
			Global_MySettings_Dir = SDCardDir.getText().toString();
		       
		       if (Global_MySettings_Dir == "")  {
		    	   new AlertDialog.Builder(this) 
		        	.setMessage("SD-Ordner Name muss angegeben werden. Standard-Wert 'MySettings' wird gesetzt!")
		        	.setNeutralButton(R.string.error_ok, null)
		        	.show();
		    	   Global_MySettings_Dir = "MySettings";
		        }		
		       MysettingsDB.updatesdcarddir(Global_MySettings_Dir);   
		    
		    String InfoNachricht = "Ordner SD-Karte in der Datenbank gespeichert!" ;
		    Toast.makeText(this, InfoNachricht, Toast.LENGTH_LONG).show();
		}
		
		if ( v == CM7Icons_ToggleButton) {
			if (CM7Icons_ToggleButton.isChecked()) {
				updateyoursettingsfile("CM7Icons", "CM7Icons=true");
			}
			else {
				updateyoursettingsfile("CM7Icons", "CM7Icons=false");
			}
		}
		
		if ( v == Circle_ToggleButton) {
			if (Circle_ToggleButton.isChecked()) {
				updateyoursettingsfile("Circle", "Circle=true");
			}
			else {
				updateyoursettingsfile("Circle", "Circle=false");
			}
		}
		
		if ( v == Quad_ToggleButton) {
			if (Quad_ToggleButton.isChecked()) {
				updateyoursettingsfile("Quad", "Quad=true");
			}
			else {
				updateyoursettingsfile("Quad", "Quad=false");
			}
		}
		
		if ( v == CM7taskswitcher_ToggleButton) {
			if (CM7taskswitcher_ToggleButton.isChecked()) {
				updateyoursettingsfile("CM7taskswitcher", "CM7taskswitcher=true");
			}
			else {
				updateyoursettingsfile("CM7taskswitcher", "CM7taskswitcher=false");
			}
		}
		
		if ( v == CM9taskswitcher_ToggleButton) {
			if (CM9taskswitcher_ToggleButton.isChecked()) {
				updateyoursettingsfile("CM9taskswitcher", "CM9taskswitcher=true");
			}
			else {
				updateyoursettingsfile("CM9taskswitcher", "CM9taskswitcher=false");
			}
		}
		
		if ( v == SamsungApps_ToggleButton) {
			if (SamsungApps_ToggleButton.isChecked()) {
				updateyoursettingsfile("SamsungApps", "SamsungApps=true");
			}
			else {
				updateyoursettingsfile("SamsungApps", "SamsungApps=false");
			}
		}
	}
	
	public void updateyoursettingsfile (String search, String replace) {
		String PathFilename = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + "/YourSettings.sh").toString();
		List<String> lines = new ArrayList<String>();
		String line = "";
		
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
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter (new FileWriter(PathFilename));
			for (int i = 0; i < lines.size(); i++)
            {
				writer.write(lines.get(i)); 
				writer.newLine();
            }
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void inittogglebuttons () {
		String PathFilename = Environment.getExternalStoragePublicDirectory(Global_MySettings_Dir + "/YourSettings.sh").toString();
		String line = "";
// mit setChecked=false initialisieren, falls weder true noch false in der Datei steht
		CM7Icons_ToggleButton.setChecked(false);
		Circle_ToggleButton.setChecked(false);
		Quad_ToggleButton.setChecked(false);
		CM7taskswitcher_ToggleButton.setChecked(false);
		CM9taskswitcher_ToggleButton.setChecked(false);
		SamsungApps_ToggleButton.setChecked(false);
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(PathFilename));
			while ((line = reader.readLine()) != null) {
				if (line.contains("CM7Icons=true")) {
					CM7Icons_ToggleButton.setChecked(true);
				}
				if (line.contains("CM7Icons=false")) {
					CM7Icons_ToggleButton.setChecked(false);
				}
				
				if (line.contains("Circle=true")) {
					Circle_ToggleButton.setChecked(true);
				}
				if (line.contains("Circle=false")) {
					Circle_ToggleButton.setChecked(false);
				}
				
				if (line.contains("Quad=true")) {
					Quad_ToggleButton.setChecked(true);
				}
				if (line.contains("Quad=false")) {
					Quad_ToggleButton.setChecked(false);
				}
				
				if (line.contains("CM7taskswitcher=true")) {
					CM7taskswitcher_ToggleButton.setChecked(true);
				}
				if (line.contains("CM7taskswitcher=false")) {
					CM7taskswitcher_ToggleButton.setChecked(false);
				}
				
				if (line.contains("CM9taskswitcher=true")) {
					CM9taskswitcher_ToggleButton.setChecked(true);
				}
				if (line.contains("CM9taskswitcher=false")) {
					CM9taskswitcher_ToggleButton.setChecked(false);
				}
				
				if (line.contains("SamsungApps=true")) {
					SamsungApps_ToggleButton.setChecked(true);
				}
				if (line.contains("SamsungApps=false")) {
					SamsungApps_ToggleButton.setChecked(false);
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
	}
}