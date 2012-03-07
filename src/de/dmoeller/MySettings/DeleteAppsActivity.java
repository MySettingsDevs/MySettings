package de.dmoeller.MySettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeleteAppsActivity extends Activity implements View.OnClickListener {

	private String[] allappnames;
	private ProgressDialog AppsLoeschen;
	private ProgressDialog SelektionSichern;
	private ProgressDialog SelektionLaden;
	
	private HashMap<String, Integer> packageNameById;
	private Drawable Icon;
	private List<PackageInfo> packs;

	private String SourceDirSelectedApp;
	private String PathSelectedApp;
	private String APKNameSelectedApp;
	private MySettingsDatenbank MysettingsDB;
	private String Global_MySettings_Dir;
	
	private static String AndroidUserAppFolder = "/data/app/";
	private static String AndroidSystemAppFolder = "/system/app/";
	private static String DeleteUserAppFolder = "/Delete-Apps/Delete-UserApps/";
	private static String DeleteSystemAppFolder = "/Delete-Apps/Delete-SystemApps/";
	
	
	public ListView DeletelistView = null;
	public Button AppLoeschenButton = null;
	public Button SaveDeleteCheckedPositionsButton = null;
	public Button LoadDeleteCheckedPositionsButton = null;
	
	List<ListViewMod> itemList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//Datenbanklasse referenzieren	       
	    MysettingsDB = new MySettingsDatenbank(this);	       

//Globales SD-Kartenverzeichnis aus der Datenbank lesen
	    Global_MySettings_Dir = MysettingsDB.readsdcarddir();

//Layout setzen
		setContentView(R.layout.deleteapps_list_view);

		itemList = getitemlist();
		DeletelistView = (ListView) findViewById(R.id.deleteapps_list_view);
		DeletelistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		DeletelistView.setAdapter(new ListViewModAdapter());

		AppLoeschenButton = (Button) findViewById(R.id.app_loeschen_button);
		AppLoeschenButton.setOnClickListener(this);
		
		SaveDeleteCheckedPositionsButton = (Button) findViewById(R.id.deletecheckeditems_button);
		SaveDeleteCheckedPositionsButton.setOnClickListener(this);
		
		LoadDeleteCheckedPositionsButton = (Button) findViewById(R.id.loaddeletecheckeditems_button);
		LoadDeleteCheckedPositionsButton.setOnClickListener(this);
		
	}

	public List<ListViewMod> getitemlist() {
		List<ListViewMod> itemList = new LinkedList<ListViewMod>();
// Auslesen der installierten Apps
// Die Liste der Apps wird in das Array packs geschrieben. 
// Anschließend der erste Eintrag (zum Test) in die Variable p vom Typ PackageInfo.
// Daraus wird dann der Name der App gelesen und kann dann weiter verwendet werden		
		packs = getPackageManager().getInstalledPackages(0);

// Größe des Arrays ermitteln, damit das StringArray in der richtigen Größe erzeugt werden kann.
// Dieses nimmt dann die Apps auf, damit alle angezeigt werden können, MÜSSEN alle Array-Elemente
// gefüllt sein!!!
		int maxpacks = packs.size();				
		allappnames = new String[ maxpacks ];
				
// HashMap erzeugen, damit dem Name der App eine ID zugewiesen werden kann und diese Zuordnung auch
// nach der Sortierung noch erhalten bleibt! Weitere Hashmap für das Icon der App zum Aufbau der Liste
		packageNameById = new HashMap<String, Integer>();
	
		for (int i = 0; i < maxpacks; i++) 
		{
			PackageInfo p = packs.get(i);
			String packageName = (p.applicationInfo.loadLabel(getPackageManager()).toString () + " " + i);	
			packageNameById.put(packageName, i);
		}

// String-Array allappnames füllen mit den Namen der einzelnen Apps, am Ende sortieren.
				
		packageNameById.keySet().toArray(allappnames);
		Arrays.sort(allappnames);
		
		for (int i = 0; i < maxpacks; i++)  
		{
			ListViewMod d = new ListViewMod();
			d.setId(i);
			d.setName(allappnames[i].toString());	
			itemList.add(d);
		}
		return itemList;
	}

	public class ListViewModAdapter extends ArrayAdapter<ListViewMod> {

		ListViewModAdapter() {
			super(getApplicationContext(), R.layout.deleteapps_list_entry, itemList);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
// Das getView-Event wird jedesmal ausgeführt, wenn eine Zeile der ListView angezeigt wird!
// Dadurch wird hier ein eigenes Design dargestellt. Die Icons der Apps werden ausgelesen und
// für jede Zeile der Liste wird der App-Name sowie der Check-Button dargestellt. Zusätzlich auf
// checked gesetzt, wenn eine Zeile ausgewählt wird (getcheckedItemPosition)
			View row = convertView;
			
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.deleteapps_list_entry, parent, false);
			}

			TextView label = (TextView) row.findViewById(R.id.label);
			CheckedTextView checkBox = (CheckedTextView) row.findViewById(R.id.checkstate);
			ImageView icon = (ImageView) row.findViewById(R.id.icon);

// Der Text der Zeile wird gesetzt
			label.setText(itemList.get(position).toString());
// Das Icon der App wird je Zeile gesetzt			
			icon.setImageDrawable(geticon (itemList.get(position).toString()));

// Die selektierten Items werden beim MultiSelect der ListView in ein SparseBooleanArray geschrieben
// Das enthält einmal die Keys, die selektiert wurden und die Values, also selektiert=true oder
// nicht selektiert=false. Mit Hilfe des Keys wird geprüft, ob die gerade dargestellte Position
// selektiert ist. Wenn ja, dann wird der Value ausgelesen und entsprechend die Checkbox gesetzt.
// Somit können die selektierten Items als gewählt dargestellt werden. Wird ein Item erneut selektiert,
// ändert sich der Value auf false, so dass auch einmal selektierte Items wieder nicht selektiert
// dargestellt werden könnnen!
			SparseBooleanArray CheckedItemPositions = DeletelistView.getCheckedItemPositions();
			checkBox.setChecked(false);
			
			for (int i = 0; i < CheckedItemPositions.size(); i++)
            {
				if (CheckedItemPositions.keyAt(i) == position) {				
					if (CheckedItemPositions.valueAt(i) == true) {
						checkBox.setChecked(true);	
					}
				}
            }        
						
			return (row);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == AppLoeschenButton) {			
			// Wenn der Löschen Button betätigt wird, wird ein Bitte Warten Dialog registiert
			// Dann werden die ausgewählten Items mitteles des SparseBooleanArray ausgelesen
			// Über einen neuen Thread im Hintergrund (sonst ist das UI so lange blockiert, und der 
			// Bitte warten Dialog erscheint nicht! werden dann alle selektierten Apps zum Löschen vorgesehen.
			// Dazu wird anhand des ausgewählten Items der Name der App aus dem Adapter ausgelesen und an die Methode
			// deleteapp übergeben.
			AppsLoeschen = ProgressDialog.show(this, "Bitte warten...",
					"Ausgewählte Apps werden zum Löschen vorgesehen",
					true, // zeitlich unbeschränkt
					false); // nicht unterbrechbar
			
			final SparseBooleanArray CheckedItemPositions = DeletelistView.getCheckedItemPositions();

			new Thread() {

				public void run() {

					for (int i = 0; i < CheckedItemPositions.size(); i++)
		            {
						if (CheckedItemPositions.valueAt(i) == true) {
							deleteapp(DeletelistView.getAdapter().getItem(CheckedItemPositions.keyAt(i)).toString());
							}
		            }        	
					AppsLoeschen.dismiss(); // dialog schließen
				}

			}.start();
		}
		
		if (v == SaveDeleteCheckedPositionsButton) {
			SelektionSichern = ProgressDialog.show(this, "Bitte warten...",
					"Selektion wird in der Datenbank gespeichert",
					true, // zeitlich unbeschränkt
					false); // nicht unterbrechbar

			// Alle bisher als zu löschen markierten Apps aus der Datenbank löschen
			MysettingsDB.initdeleteapps();	

			// Die selektierten Apps werden in ein BooleanArray geschrieben und dann Zeile für Zeile ausgelesen
			// Jede selektierte App wird dann in die Datenbank geschrieben			
			final SparseBooleanArray CheckedItemPositionsToSaveSelektion = DeletelistView.getCheckedItemPositions();
						
			new Thread() {

				public void run() {
					int packageId = 0;
					String AppName = "";
								
					for (int i = 0; i < CheckedItemPositionsToSaveSelektion.size(); i++)
			           {
						if (CheckedItemPositionsToSaveSelektion.valueAt(i) == true) {
							AppName = DeletelistView.getAdapter().getItem(CheckedItemPositionsToSaveSelektion.keyAt(i)).toString();
										
							// Mit dem Namen aus der HashMap die PackageID holen, damit die korrekten weiteren Daten zur App aus dem
							// PackageManager gelesen werden können
								if (packageNameById.containsKey(AppName))
								{
									packageId = packageNameById.get(AppName);
								}	
													
							// Zu löschende Apps in die Datenbank schreiben		
								MysettingsDB.insertdeleteapp(packageId, AppName);
							}
			           }        	
					SelektionSichern.dismiss(); // dialog schließen
				}
			}.start();
		}
		
		if (v == LoadDeleteCheckedPositionsButton) {
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			
			Toast.makeText(context, "Gespeicherte Selektion wurde aus der Datenbank geladen", duration).show();
			
		// selektierte Items im SparseBooleanArray, welches im ListViewModAdapter verwendet wird leeren
			for (int i = 0; i < packs.size(); i++)
		       	{				
				DeletelistView.setItemChecked(i, false);
		       	}

		// Gespeicherte Selektion aus der Datenbank lesen
			HashMap<String, Integer> CheckedSaveAppNames=MysettingsDB.readdeleteapps();

		// CheckedPositions in der ListView wieder setzen
			for (int i = 0; i < packs.size(); i++)
				{
					if (CheckedSaveAppNames.containsKey(DeletelistView.getAdapter().getItem(i).toString()))
					{				
						DeletelistView.setItemChecked(i, true);
					}
				}	
		}
	}
	
	public Drawable geticon (String AppName) {
		int packageId = 0;
// Mit dem Namen der App aus der HashMap die PackageID holen
			if (packageNameById.containsKey(AppName))
			{
				packageId = packageNameById.get(AppName);
			}	

// Anschließend mit der ID das Icon aus dem PacketManager holen
			PackageInfo p = packs.get(packageId);
			Icon = p.applicationInfo.loadIcon(getPackageManager());
			
		return Icon;
	}

	public void deleteapp (String AppName) { 
		int packageId = 0;

// Mit dem Namen aus der HashMap die PackageID holen, damit die korrekten weiteren Daten zur App aus dem
// PackageManager gelesen werden können
		if (packageNameById.containsKey(AppName))
		{
			packageId = packageNameById.get(AppName);
		}	
			
// Pfad auslesen der aus dem Menü gewählten App	
		PackageInfo p = packs.get((int) packageId);
		SourceDirSelectedApp = p.applicationInfo.sourceDir;
		PathSelectedApp = new IOTools().generatepath(SourceDirSelectedApp);
				
// APK-Name aus dem SourceDir auslesen
		APKNameSelectedApp = new IOTools().generateapkname(SourceDirSelectedApp);
		
// Selektierte App vom SourceDir auf die SD-Karte kopieren
		appkopieren (PathSelectedApp, APKNameSelectedApp);
	}

// Methode zum Schreiben auf den externen Speicher.
	public void appkopieren (String SourceDir, String SourceAPK) {      
		String DestPath = Global_MySettings_Dir;
		
		if (SourceDir.equals(AndroidUserAppFolder)) {
			DestPath = Global_MySettings_Dir + DeleteUserAppFolder;
		}
		
		if (SourceDir.equals(AndroidSystemAppFolder)) {
			DestPath = Global_MySettings_Dir + DeleteSystemAppFolder;
		}
		
		boolean mExternalStorageWriteable = new IOTools().checkexternalstorage (); 
		if (mExternalStorageWriteable) {
			copyfile (SourceDir, SourceAPK, DestPath, SourceAPK);
		}
	}
			
	public void copyfile(String SourcePath, String SourceFile, String DestPath, String DestFile) {
		// Create a path where we will place our picture in the user's
	    // public pictures directory.  Note that you should be careful about
	    // what you place here, since the user often manages these files.  For
	    // pictures and other media owned by the application, consider
	    // Context.getExternalMediaDir().
		
		// Als DestPath wird nur der Pfad ohne die SDCard angegeben.
		// Hier wird aus dem Betriebssystem der Pfad zur Speicherkarte ausgelesen und der übergebene
		// Pfad um den SD-Kartenpfad ergänzt.
		
		File path=Environment.getExternalStoragePublicDirectory(DestPath);
		String SdCardDestPath = Environment.getExternalStoragePublicDirectory(DestPath).toString();
		
		File SFile = new File(SourcePath, SourceFile);
		File DFile = new File(SdCardDestPath, DestFile);
		    try {
					// Make sure the Destination Path directory exists.
			path.mkdirs();

			InputStream is = new FileInputStream (SFile);
	        OutputStream os = new FileOutputStream(DFile);
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