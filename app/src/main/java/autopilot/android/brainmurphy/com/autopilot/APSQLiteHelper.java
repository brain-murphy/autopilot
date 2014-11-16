package autopilot.android.brainmurphy.com.autopilot;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brian on 11/14/2014.
 */
public class APSQLiteHelper extends SQLiteOpenHelper{

    public static final String TABLE_ENABLED_CONTACTS = "contactsenabledtable";
    public static final String TABLE_PERSONALITY_PROFILE = "personalityprofileTable";

    public static final String COLUMN_ID = "_ID";
    public static final String COLUMN_ENABLED = "enabledColumn";
    public static final String COLUMN_PERSONALITY_PROF = "persprof";
    public static final String[] ENABLED_CONTACTS_COLUMNS = {
            COLUMN_ID, COLUMN_ENABLED, COLUMN_PERSONALITY_PROF
    };

    public static final String COLUMN_PATH = "path";
    public static final String[] PERSONALITY_PROFILE_COLUMNS = {
            COLUMN_ID, COLUMN_PATH
    };

    private static final String CREATE_ENABLED_CONTACTS_TABLE = "CREATE TABLE "+TABLE_ENABLED_CONTACTS+"( "+
            COLUMN_ID+" INTEGER PRIMARY KEY, "+
            COLUMN_ENABLED+" INTEGER, " +
            COLUMN_PERSONALITY_PROF+" INTEGER, " +
            "FOREIGN KEY("+COLUMN_PERSONALITY_PROF+") REFERENCES "+TABLE_PERSONALITY_PROFILE+"("+COLUMN_ID+") );";

    private static final String CREATE_PERSONALITY_PROF_TABLE = "CREATE TABLE "+TABLE_PERSONALITY_PROFILE+"( "+
            COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
            COLUMN_PATH+" TEXT);";

    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "autopilotdb";

    public APSQLiteHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PERSONALITY_PROF_TABLE);
        db.execSQL(CREATE_ENABLED_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
