package com.example.mark.proj8;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    String share_name = "LAZY_BOYFRIEND";
    //static final String URL_MESSAGE_SOURCE = "http://10.0.2.2/lazy_boy_friend_message_source.htm" for local testing
    //below is real URL to fetch LB messages source file
    static final String URL_MESSAGE_SOURCE = "http://markedwardnewman.com/other/lbf_ms.htm";
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;
    public int message_file_version;
    public String naughtyFile = "lazyboyfriend_messages_naughty.txt";
    public String niceFile = "lazyboyfriend_messages_nice.txt";
    public String historyFile = "lazyboyfriend_messages_history.txt";
    public String test123 = "myfilename.txt";
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstRunPreferences();
        if (getFirstRun()) {
            setMessageTypePreferencesCounters();
            new SetUpMessageFiles().execute(URL_MESSAGE_SOURCE);
            setRunned();
        } else {
            displayNextMessage();
        }
        displayMessageTypePreference();
        displayContactNamePreference();
        displayContactNumberPreference();
    }

    public void showPreviousMessage(View view) {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int message_type = app_preferences.getInt("message_type", 0);
        if (message_type == 1) {
            int message_type_naughty_counter = app_preferences.getInt("message_type_naughty_counter", 0);

            if (message_type_naughty_counter < 1) {
                Toast.makeText(MainActivity.this, "You are at the beginning of the naughty messages list...", Toast.LENGTH_SHORT).show();
            } else {
                decreaseMessageLineCount(message_type);
                String message_text = returnMessage(1, app_preferences.getInt("message_type_naughty_counter", 0), naughtyFile);
                TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
                tv_message_text.setText(message_text);
                SharedPreferences.Editor editor1 = app_preferences.edit();
                editor1.putString("current_message", message_text);
                editor1.commit();
            }
        } else if (message_type == 2) {
            int message_type_nice_counter = app_preferences.getInt("message_type_nice_counter", 0);

            if (message_type_nice_counter < 1) {
                Toast.makeText(MainActivity.this, "You are at the beginning of the nice messages list...", Toast.LENGTH_SHORT).show();
            } else {
                decreaseMessageLineCount(message_type);
                String message_text = returnMessage(2, app_preferences.getInt("message_type_nice_counter", 0), niceFile);
                TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
                tv_message_text.setText(message_text);
                SharedPreferences.Editor editor3 = app_preferences.edit();
                editor3.putString("current_message", message_text);
                editor3.commit();
            }
        } else {
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText("Not set!");
        }
    }

    public void showNextMessage(View view) {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int message_type = app_preferences.getInt("message_type", 0);
        if (message_type == 1) {
            int message_type_naughty_counter = app_preferences.getInt("message_type_naughty_counter", 0);
            System.out.println("naughty_counter = " + message_type_naughty_counter + "/n/n");
            increaseMessageLineCount(message_type);
            String message_text = returnMessage(1, app_preferences.getInt("message_type_naughty_counter", 0), naughtyFile);
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText(message_text);
            SharedPreferences.Editor editor1 = app_preferences.edit();
            editor1.putString("current_message", message_text);
            editor1.commit();
        } else if (message_type == 2) {
            int message_type_nice_counter = app_preferences.getInt("message_type_nice_counter", 0);
            System.out.println("nice_counter = " + message_type_nice_counter + "/n/n");
            increaseMessageLineCount(message_type);
            String message_text = returnMessage(2, app_preferences.getInt("message_type_nice_counter", 0), niceFile);
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText(message_text);
            SharedPreferences.Editor editor3 = app_preferences.edit();
            editor3.putString("current_message", message_text);
            editor3.commit();
        } else {
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText("Not set!");
        }
    }

    public void increaseMessageLineCount(int message_type) {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        if (message_type == 1) {
            editor.putInt("message_type_naughty_counter", (app_preferences.getInt("message_type_naughty_counter", 0) + 1));
            editor.commit();
        } else if (message_type == 2) {
            editor.putInt("message_type_nice_counter", (app_preferences.getInt("message_type_nice_counter", 0) + 1));
            editor.commit();
        } else {
            System.out.println("ERROR INCREASING MESSAGE LINE COUNT");
        }
    }

    public void decreaseMessageLineCount(int message_type) {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        if (message_type == 1) {
            editor.putInt("message_type_naughty_counter", (app_preferences.getInt("message_type_naughty_counter", 0) - 1));
            editor.commit();
        } else if (message_type == 2) {
            editor.putInt("message_type_nice_counter", (app_preferences.getInt("message_type_nice_counter", 0) - 1));
            editor.commit();
        } else {
            System.out.println("ERROR INCREASING MESSAGE LINE COUNT");
        }
    }

    public void showHistoryFile() {
        setContentView(R.layout.history);
        StringBuilder text = new StringBuilder();
        try {
            InputStream instream = openFileInput(historyFile);//open the text file for reading
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = null;
                while ((line = buffreader.readLine()) != null) {
                    //buffered reader reads only one line at a time, hence we give a while loop to read all till the text is null
                    text.append(line);
                    text.append('\n');
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView tv = (TextView) findViewById(R.id.tv_history);
        tv.setText(text);
    }

    public void populateHistoryFile(String contact_name, String contact_number, String current_message) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(historyFile, MODE_APPEND));
            String current_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            out.write("\"" + current_message + "\"" + " was sent to " + contact_name + " at " + contact_number + " on " + current_date + "\r\n" + "\r\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearHistoryFile(View view) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(historyFile, 0));
            out.write("History of messages sent has been erased!");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showHistoryFile();
    }


    /////////////////////////////////////////////////"MESSAGE TYPE" FUNCTIONS BELOW///////////////////
    public void displayMessageTypePreference() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int message_type = app_preferences.getInt("message_type", 0);
        if (message_type == 1) {
            TextView tv_message_type = (TextView) findViewById(R.id.tv_message_type);
            tv_message_type.setText("Naughty!");
        } else if (message_type == 2) {
            TextView tv_message_type = (TextView) findViewById(R.id.tv_message_type);
            tv_message_type.setText("Nice!");
        } else {
            TextView tv_message_type = (TextView) findViewById(R.id.tv_message_type);
            tv_message_type.setText("Not Set!");
        }
    }


    public void getMessageType(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            // NAUGHTY
            case R.id.radio_button_naughty:
                if (checked) {
                    SharedPreferences app_preferences1 = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor1 = app_preferences1.edit();
                    editor1.putInt("message_type", 1);
                    editor1.commit();
                    displayMessageTypePreference();
                    displayNextMessage();
                }
                break;
            // NICE
            case R.id.radio_button_nice:
                if (checked) {
                    SharedPreferences app_preferences2 = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor2 = app_preferences2.edit();
                    editor2.putInt("message_type", 2);
                    editor2.commit();
                    displayMessageTypePreference();
                    displayNextMessage();
                }
                break;
        }
    }

    public void displayNextMessage() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int message_type = app_preferences.getInt("message_type", 0);
        if (message_type == 1) {
            int message_type_naughty_counter = app_preferences.getInt("message_type_naughty_counter", 0);
            String message_text = returnMessage(1, message_type_naughty_counter, naughtyFile);
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText(message_text);
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putString("current_message", message_text);
            editor.commit();
        } else if (message_type == 2) {
            int message_type_nice_counter = app_preferences.getInt("message_type_nice_counter", 0);
            String message_text = returnMessage(2, message_type_nice_counter, niceFile);
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText(message_text);
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putString("current_message", message_text);
            editor.commit();
        } else {
            TextView tv_message_text = (TextView) findViewById(R.id.tv_message_text);
            tv_message_text.setText("Not set!");
        }
    }


    public String returnMessage(int message_type, int line_number, String message_file) {
        try {
            FileInputStream fs = openFileInput(message_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            for (int i = 0; i < line_number; ++i) {
                br.readLine();
            }
            String lineIWant = br.readLine();
            if (lineIWant == null) {
                new UpdateMessageFiles().execute(URL_MESSAGE_SOURCE);
                return null;
            } else {
                br.close();
                fs.close();
                return lineIWant;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /////////////////////////////////////////////////CONTACT FUNCTIONS BELOW//////////////////////////////////////
    public void displayContactNamePreference() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String contact_name = app_preferences.getString("contact_name", null);
        if (contact_name != null) {
            TextView tv_contact_name = (TextView) findViewById(R.id.tv_contact_name);
            tv_contact_name.setText(contact_name);
        } else {
            TextView tv_contact_name = (TextView) findViewById(R.id.tv_contact_name);
            tv_contact_name.setText("Not set!");
        }
    }

    private String retrieveContactName() {
        String contactName = null;
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString("contact_name", contactName);
        editor.commit();
        return contactName;
    }

    public void displayContactNumberPreference() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String contact_number = app_preferences.getString("contact_number", null);
        if (contact_number != null) {
            TextView tv_contact_number = (TextView) findViewById(R.id.tv_contact_number);
            tv_contact_number.setText(contact_number);
        } else {
            TextView tv_contact_number = (TextView) findViewById(R.id.tv_contact_number);
            tv_contact_number.setText("Not set or no mobile #");
        }
    }

    private String retrieveContactNumber() {
        String contactNumber = null;
        Cursor cursorID = getContentResolver().query(uriContact, new String[]{BaseColumns._ID}, null, null, null);
        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(BaseColumns._ID));
        }
        cursorID.close();
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactID}, null);
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        } else {
            contactNumber = "Not set or no mobile #";
        }
        cursorPhone.close();
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString("contact_number", contactNumber);
        editor.commit();
        return contactNumber;
    }

    public void onClickSelectContact(View btnSelectContact) {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();
            retrieveContactName();
            retrieveContactNumber();
            displayContactNamePreference();
            displayContactNumberPreference();
        } else {
            Log.i(TAG, "ERROR FINDING CONTACT");
        }
    }

    /////////////////////////////////////////////////////ACTIVATION FUNCTIONS BELOW////////////////////////////////////////
    public void verifySendMessage(View view) {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String contact_name = app_preferences.getString("contact_name", null);
        String current_message = app_preferences.getString("current_message", null);
        new AlertDialog.Builder(this)
                .setTitle("Please confirm...")
            .setMessage("Really send " + "\"" + current_message + "\"" + " to " + contact_name + "?")
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                //COULDN'T GET THE METHOD TO PASS THE CORRECT PARAMETERS, JUST PASTED THE WHOLE METHOD IN AND IT WORKED///
                public void onClick(DialogInterface arg0, int arg1) {
                    SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String contact_name = app_preferences.getString("contact_name", null);
                    String contact_number = app_preferences.getString("contact_number", null);
                    String current_message = app_preferences.getString("current_message", null);
                    int message_type = app_preferences.getInt("message_type", 0);
                    try {
                        if (contact_number.length() > 0 && current_message.length() > 0 && contact_number != "Not set or no mobile #") {
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(contact_number, null, current_message, null, null);
                            populateHistoryFile(contact_name, contact_number, current_message);
                            Toast.makeText(MainActivity.this, "\"" + current_message + "\"" + " was successfully sent to " + contact_name, Toast.LENGTH_SHORT).show();
                            increaseMessageLineCount(message_type);
                            displayNextMessage();
                        } else {
                            Toast.makeText(getBaseContext(), "Message NOT SENT! Make sure the selected contact has a mobile phone number listed...", Toast.LENGTH_LONG).show();
                            System.out.println("NO MESSAGES ACK!" + "\n");
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Sending message failed- make sure all fields are complete...", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }).create().show();
    }

    ////////////////////////////////////////////////////MISC FUNCTIONS BELOW////////////////////////////////////////////////
    public boolean getFirstRun() {
        return mPrefs.getBoolean("firstRun", true);
    }

    public void setRunned() {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("firstRun", false);
        edit.commit();
    }

    public void firstRunPreferences() {
        Context mContext = this.getApplicationContext();
        mPrefs = mContext.getSharedPreferences("com.thelazyboyfriend.app_preferences", 0); //0 = mode private. only this app can read these preferences
    }

    public void setMessageTypePreferencesCounters() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("message_type_naughty_counter", 0);
        editor.putInt("message_type_nice_counter", 0);
        editor.putFloat("message_file_version", 0);
        editor.commit();
    }

    /////////////////////////////////////////////////////MENU FUNCTIONS BELOW///////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_show_history:
                showHistoryFile();
                break;
            case R.id.menu_exit: {
                new AlertDialog.Builder(this)
                        .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new OnClickListener() {
                        //COULDN'T GET THE METHOD TO PASS THE CORRECT PARAMETERS, JUST PASTED THE WHOLE METHOD IN AND IT WORKED///
                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
            }
            break;
            case R.id.menu_back: {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
            .setMessage("Are you sure you want to exit?")
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new OnClickListener() {
                //COULDN'T GET THE METHOD TO PASS THE CORRECT PARAMETERS, JUST PASTED THE WHOLE METHOD IN AND IT WORKED///
                public void onClick(DialogInterface arg0, int arg1) {
                    MainActivity.super.onBackPressed();
                }
            }).create().show();
    }

    public void onBackPressedHistory(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /////////////////////////////////////////////////////ASYNC CODE BELOW///////////////////////////////////////////
    private class SetUpMessageFiles extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            TextView tv_status1 = (TextView) findViewById(R.id.tv_status);
            tv_status1.setText("Retreiving messages");
        }

        @Override
        protected String doInBackground(String... params) {
            populateNaughtyFile();
            populateNiceFile();
            createHistoryFile();
            setMessageFileVersion(getMessageFileVersion());
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            TextView tv_status = (TextView) findViewById(R.id.tv_status);
            tv_status.setText("Active");
            displayNextMessage();
            super.onPostExecute(result);
        }

        public float getMessageFileVersion() {
            Document doc;
            String text = null;
            try {
                doc = Jsoup.connect(URL_MESSAGE_SOURCE).get();
                for (Element element : doc.select("version")) {
                    text = element.text();
                }
                float set_mfv_to_int = Float.parseFloat(text);
                return set_mfv_to_int;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public void setMessageFileVersion(float message_file_version) {
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putFloat("message_file_version", message_file_version);
            editor.commit();
        }

        ///MAY NOT NEED BELOW CODE///
        public float getMessageFileVersionSP() {
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            float message_file_version_sp = app_preferences.getFloat("message_file_version", 0);
            return message_file_version_sp;
        }

        public void populateNaughtyFile() {
            Document doc;
            try {
                OutputStreamWriter out = new OutputStreamWriter(openFileOutput(naughtyFile, 0));
                doc = Jsoup.connect(URL_MESSAGE_SOURCE).get();
                for (Element element : doc.select("naughty")) {
                    String text = element.text();
                    out.write(text);
                    out.write("\r\n");
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void populateNiceFile() {
            Document doc;
            try {
                OutputStreamWriter out = new OutputStreamWriter(openFileOutput(niceFile, 0));
                doc = Jsoup.connect(URL_MESSAGE_SOURCE).get();
                for (Element element : doc.select("nice")) {
                    String text = element.text();
                    out.write(text);
                    out.write("\r\n");
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void createHistoryFile() {
            try {
                OutputStreamWriter out = new OutputStreamWriter(openFileOutput(historyFile, 0));
                String text = "History of messages sent:";
                out.write(text);
                out.write("\r\n");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    } //END OF private class SetUpMessageFiles extends AsyncTask<String, Integer, String>

    private class UpdateMessageFiles extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            TextView tv_status2 = (TextView) findViewById(R.id.tv_status);
            tv_status2.setText("updating messages...");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            float message_file_version = getMessageFileVersion();
            float message_file_versionSP = getMessageFileVersionSP();
            if (message_file_version > message_file_versionSP) {
                runOnUiThread(new Runnable() {
                    //COULDN'T GET THE METHOD TO PASS THE CORRECT PARAMETERS, JUST PASTED THE WHOLE METHOD IN AND IT WORKED///
                    public void run() {
                        Toast.makeText(MainActivity.this, "New messages have been downloaded...", Toast.LENGTH_LONG).show();
                    }
                });
                setMessageFileVersion(message_file_version);
                populateNaughtyFile();
                populateNiceFile();
                return null;
            } else {
                runOnUiThread(new Runnable() {
                    //COULDN'T GET THE METHOD TO PASS THE CORRECT PARAMETERS, JUST PASTED THE WHOLE METHOD IN AND IT WORKED///
                    public void run() {
                        Toast.makeText(MainActivity.this, "End of message list reached!  Resetting messages back to beginning...", Toast.LENGTH_LONG).show();
                    }
                });
                resetMessages();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            displayNextMessage();
            TextView tv_status3 = (TextView) findViewById(R.id.tv_status);
            tv_status3.setText("Active");
            super.onPostExecute(result);
        }

        public void populateNaughtyFile() {
            Document doc;
            try {
                OutputStreamWriter out = new OutputStreamWriter(openFileOutput(naughtyFile, 0));
                doc = Jsoup.connect(URL_MESSAGE_SOURCE).get();
                for (Element element : doc.select("naughty")) {
                    String text = element.text();
                    out.write(text);
                    out.write("\r\n");
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void populateNiceFile() {
            Document doc;
            try {
                OutputStreamWriter out = new OutputStreamWriter(openFileOutput(niceFile, 0));
                doc = Jsoup.connect(URL_MESSAGE_SOURCE).get();
                for (Element element : doc.select("nice")) {
                    String text = element.text();
                    out.write(text);
                    out.write("\r\n");
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public float getMessageFileVersion() {
            Document doc;
            String text = "Not Set";
            try {
                doc = Jsoup.connect(URL_MESSAGE_SOURCE).get();
                for (Element element : doc.select("version")) {
                    text = element.text();
                }
                float set_mfv_to_int = Float.parseFloat(text);
                return set_mfv_to_int;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        public void setMessageFileVersion(float message_file_version) {
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putFloat("message_file_version", message_file_version);
            editor.commit();
        }

        public float getMessageFileVersionSP() {
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            float message_file_version_sp = app_preferences.getFloat("message_file_version", 0);
            return message_file_version_sp;
        }

        public int getMessageTypeSP() {
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int message_type = app_preferences.getInt("message_type", 0);
            if (message_type == 1) {
                return 1;
            } else if (message_type == 2) {
                return 2;
            } else {
                TextView tv_message_type = (TextView) findViewById(R.id.tv_message_type);
                tv_message_type.setText("Not Set!");
                return 0;
            }
        }

        public void resetMessages() {
            int message_type = getMessageTypeSP();
            SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = app_preferences.edit();
            if (message_type == 1) {
                editor.putInt("message_type_naughty_counter", 0);
                editor.commit();
            } else if (message_type == 2) {
                editor.putInt("message_type_nice_counter", 0);
                editor.commit();
            } else {
                System.out.println("ERROR RESETTING MESSAGES");
            }
        }
    }
}
