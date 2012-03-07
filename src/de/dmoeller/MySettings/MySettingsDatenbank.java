package de.dmoeller.MySettings;

import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

	public class MySettingsDatenbank extends SQLiteOpenHelper {
		
		private static final String DATENBANK_NAME = "mysettings.db";
		private static final int DATENBANK_VERSION = 2;
		
		public MySettingsDatenbank(Context context) {
			super(context,DATENBANK_NAME,null,DATENBANK_VERSION);
		}
	
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE einstellungen (feld TEXT PRIMARY KEY, wert TEXT);");
			db.execSQL("CREATE TABLE apps (nr TEXT PRIMARY KEY, appname TEXT, tosave TEXT, todelete TEXT);");
			
// Initialisierung der Tabellen mit Basiswerten, die dann später über die App geändert werden können!
			db.execSQL("INSERT INTO einstellungen (feld, wert) values ('SDCard_Dir','MySettings');");
		}
	
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	           // Kills the table and existing data
	           db.execSQL("DROP TABLE IF EXISTS einstellungen;");

	           // Recreates the database with a new version
	           onCreate(db);
		}
		
		public String readsdcarddir () {
			// Cursor für select-Abfrage anlegen, der das Ergebnis der SQL-Abfrage aufnimmt!
			// z.B. SDCard-Dir aus der Einstellungen-Tabelle auslesen
						       Cursor cReadSDCardDir = this.getReadableDatabase().query(
						    		   false, // distinct?
						    		   "einstellungen",
						    		   new String[] { // SELECT
						    		   "feld",
						    		   "wert"},
						    		   "feld = ?", // WHERE-Bedingung
						    		   new String[] { // Parameter für WHERE
						    		   "SDCard_Dir"},
						    		   null, // GROUP BY
						    		   null, // HAVING
						    		   null, // ORDER BY
						    		   null // LIMIT
						    		   );
						       
			// Werte aus dem Cursor auslesen (0 ist die erste Spalte im Ergebnis und 1 die zweite Spalte)
			// Da im select erst das Feld und dann der Wert gelesen wird, ist der Wert im Cursor Spalte 1
			// Da das Feld gemäß Datenmodell ein primary Key ist, kann hier nur 1 Wert zurückkommen!
						       String Feldname = "";
						       String Feldwert = "";
						       
						       while( cReadSDCardDir.moveToNext() ) {
						    	   Feldname = cReadSDCardDir.getString(0); // feld
						    	   Feldwert = cReadSDCardDir.getString(1); // wert
						    	   };
						    	   
						       cReadSDCardDir.close();

					return Feldwert;
				}
		
		public void writesdcarddir(String Global_MySettings_Dir_to_write) {
			String DBFeld = "SDCard_Dir";
	// Einfügen eines Datensatzes in die Datenbank (für das Einfügen sollten 
	// aus Performancegründen prepared Statements verwendet werden

	// Achtung, hier muss noch mit Try ein doppeltes Einfügen als Exception abgefangen werden!
	// Oder vorher gucken, ob der Datensatz schon da ist, dann updaten!!

			SQLiteStatement stmtInsert =
				   this.getWritableDatabase().compileStatement(
				   "insert into einstellungen "+
				   "(feld,wert) "+
				   "values (?,?)"
				   );
				   stmtInsert.bindString(1,DBFeld);
				   stmtInsert.bindString(2,Global_MySettings_Dir_to_write);
				   long id = stmtInsert.executeInsert();
		}

		public void updatesdcarddir(String Global_MySettings_Dir_to_update) {
			String DBFeld = "SDCard_Dir";
			
			SQLiteStatement stmtUpdate =
		    		   this.getWritableDatabase().compileStatement(
		    		   "update einstellungen set wert=?"+
		    		   "where feld=?"
		    			);
						stmtUpdate.bindString(1,Global_MySettings_Dir_to_update);
						stmtUpdate.bindString(2,DBFeld);
						stmtUpdate.execute();	
		}

		public void initsavedapps () {
			SQLiteStatement stmtDelete =
		    		   this.getWritableDatabase().compileStatement("update apps set tosave=0;");
						stmtDelete.execute();	
		}
		
		public void initdeleteapps () {
			SQLiteStatement stmtDelete =
		    		   this.getWritableDatabase().compileStatement("update apps set todelete=0;");
						stmtDelete.execute();	
		}
		
		public boolean existsapp (String AppName) {
			Cursor cExistApp = this.getReadableDatabase().query(
		    		   false, // distinct?
		    		   "apps",
		    		   new String[] { // SELECT
		    		   "appname"},
		    		   "appname = ?", // WHERE-Bedingung
		    		   new String [] { // Parameter für WHERE
		    		   AppName},
		    		   null, // GROUP BY
		    		   null, // HAVING
		    		   null, // ORDER BY
		    		   null // LIMIT
		    		   );
		       
// Werte aus dem Cursor auslesen (0 ist die erste Spalte im Ergebnis und 1 die zweite Spalte)
// Da im select erst das Feld und dann der Wert gelesen wird, ist der Wert im Cursor Spalte 1
// Da das Feld gemäß Datenmodell ein primary Key ist, kann hier nur 1 Wert zurückkommen!
		       Boolean AppExists = false;
		       
		       while( cExistApp.moveToNext() ) {
		    	   AppExists = true;
		    	   };
		    	   
		       cExistApp.close();

		       return AppExists;
		}
		
		public void insertsaveapp (int packageId, String AppName) {	
			String spackageId = Integer.toString (packageId);
// Bevor der neue Datensatz geschrieben werden kann, muss die todelete-Spalte aus der Datenbank gelesen werden			
			Cursor cReadAppToDelete = this.getReadableDatabase().query(
		    		   false, // distinct?
		    		   "apps",
		    		   new String[] { // SELECT
		    		   "todelete",
		    		   },
		    		   "nr = ?", // WHERE-Bedingung
		    		   new String[] { // Parameter für WHERE
		    			   spackageId},
		    		   null, // GROUP BY
		    		   null, // HAVING
		    		   null, // ORDER BY
		    		   null // LIMIT
		    		   );
		       
			String ColumnToDelete = "";
		       
			while( cReadAppToDelete.moveToNext() ) {
		    	   ColumnToDelete = cReadAppToDelete.getString(0); // todelete-Spalte
		    	   };
		    cReadAppToDelete.close();

// Vor dem Schreiben immer prüfen, ob der Datensatz nicht schon existiert!
		    if (!existsapp(AppName)) 
		    {
			SQLiteStatement stmtInsert =
						   this.getWritableDatabase().compileStatement(
						   "insert into apps "+
						   "(nr, appname, tosave, todelete) "+
						   "values (?,?,?,?)"
						   );
						   stmtInsert.bindString(1,spackageId);
						   stmtInsert.bindString(2,AppName);
						   stmtInsert.bindString(3,"1");
						   stmtInsert.bindString(4,ColumnToDelete);
						   
						   long id = stmtInsert.executeInsert();
		    }
		    else
		    {
		    SQLiteStatement stmtUpdate =
					   this.getWritableDatabase().compileStatement(
					   "update apps set tosave='1'" +
					   "where nr=?");
		    			stmtUpdate.bindString(1,spackageId);
					   	stmtUpdate.execute();
		    }
		}
		
		public void insertdeleteapp (int packageId, String AppName) {	
			String spackageId = Integer.toString (packageId);
// Bevor der neue Datensatz geschrieben werden kann, muss die todelete-Spalte aus der Datenbank gelesen werden			
			Cursor cReadAppToSave = this.getReadableDatabase().query(
		    		   false, // distinct?
		    		   "apps",
		    		   new String[] { // SELECT
		    		   "tosave",
		    		   },
		    		   "nr = ?", // WHERE-Bedingung
		    		   new String[] { // Parameter für WHERE
		    			   spackageId},
		    		   null, // GROUP BY
		    		   null, // HAVING
		    		   null, // ORDER BY
		    		   null // LIMIT
		    		   );
		       
			String ColumnToSave = "";
		       
			while( cReadAppToSave.moveToNext() ) {
				ColumnToSave = cReadAppToSave.getString(0); // todelete-Spalte
		    	   };
		    cReadAppToSave.close();

// Vor dem Schreiben immer prüfen, ob der Datensatz nicht schon existiert!
		    if (!existsapp(AppName)) 
		    {
			SQLiteStatement stmtInsert =
						   this.getWritableDatabase().compileStatement(
						   "insert into apps "+
						   "(nr, appname, tosave, todelete) "+
						   "values (?,?,?,?)"
						   );
						   stmtInsert.bindString(1,spackageId);
						   stmtInsert.bindString(2,AppName);
						   stmtInsert.bindString(3,ColumnToSave);
						   stmtInsert.bindString(4,"1");
						   
						   long id = stmtInsert.executeInsert();
		    }
		    else 
		    {
		    SQLiteStatement stmtUpdate =
					   this.getWritableDatabase().compileStatement(
					   "update apps set todelete='1'" +
					   "where nr=?");
		    			stmtUpdate.bindString(1,spackageId);
					   	stmtUpdate.execute();
		    }
		}
		
		public HashMap<String, Integer> readsaveapps () {
			Cursor cReadSaveApps = this.getReadableDatabase().query(
		    		   false, // distinct?
		    		   "apps",
		    		   new String[] { // SELECT
		    		   "appname"},
		    		   "tosave = ?", // WHERE-Bedingung
		    		   new String [] { // Parameter für WHERE
		    		   "1"},
		    		   null, // GROUP BY
		    		   null, // HAVING
		    		   null, // ORDER BY
		    		   null // LIMIT
		    		   );
		       
			HashMap<String, Integer> CheckedSaveApps;
			CheckedSaveApps = new HashMap<String, Integer>();
					
		    int i = 0;
		    while( cReadSaveApps.moveToNext() ) {
		    	CheckedSaveApps.put(cReadSaveApps.getString(0), i); // Werte aus der appname-Spalte in die HashMap
		    	i = i +1;
		    	};
		    	   
		    	cReadSaveApps.close();

		      return CheckedSaveApps;
		}
		
		public HashMap<String, Integer> readdeleteapps () {
			Cursor cReadDeleteApps = this.getReadableDatabase().query(
		    		   false, // distinct?
		    		   "apps",
		    		   new String[] { // SELECT
		    		   "appname"},
		    		   "todelete = ?", // WHERE-Bedingung
		    		   new String [] { // Parameter für WHERE
		    		   "1"},
		    		   null, // GROUP BY
		    		   null, // HAVING
		    		   null, // ORDER BY
		    		   null // LIMIT
		    		   );
		       
			HashMap<String, Integer> CheckedDeleteApps;
			CheckedDeleteApps = new HashMap<String, Integer>();
					
		    int i = 0;
		    while( cReadDeleteApps.moveToNext() ) {
		    	CheckedDeleteApps.put(cReadDeleteApps.getString(0), i); // Werte aus der appname-Spalte in die HashMap
		    	i = i +1;
		    	};
		    	   
		    	cReadDeleteApps.close();

		      return CheckedDeleteApps;
		}
	}