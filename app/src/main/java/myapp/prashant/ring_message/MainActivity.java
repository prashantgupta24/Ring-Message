package myapp.prashant.ring_message;

import android.R.*;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import myapp.prashant.ring_message.R;

public class MainActivity extends ActionBarActivity implements NoticeDialogFragment.NoticeDialogListener, DeleteDialogFragment.DeleteDialogListener {

 TextView contactText;
 EditText messageText;
 Button saveButton;
 Button delButton;
 SQLiteDatabase db;
 static String tableName = "mytable";
 final String contactName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactText = (TextView)findViewById(R.id.contactName);
        messageText = (EditText)findViewById(R.id.message);
        saveButton = (Button)findViewById(R.id.save);
        Intent intent = new Intent(this, CallDetectService.class);
        startService(intent);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        contactText.setText(savedInstanceState.getString(contactName));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(contactName, contactText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public void saveData(View v)
    {
        String contactName = this.contactText.getText().toString();
        String message = this.messageText.getText().toString();
        if(contactName.equals("")) {
            Toast t = Toast.makeText(getApplicationContext(), R.string.select_contact, Toast.LENGTH_SHORT);
            t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            t.show();
        }
        else
            if(message.equals("")) {
                Toast t = Toast.makeText(getApplicationContext(), R.string.type_message, Toast.LENGTH_SHORT);
                t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
                t.show();
            }

            else
            insertIntoDB(contactName, message);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        String contactName = this.contactText.getText().toString();
        String message = this.messageText.getText().toString();

        ContentValues values = new ContentValues();
        values.put("name", contactName);
        values.put("message", message);
        db.update(tableName, values, "name='"+ contactName+"'",null);
        Toast t = Toast.makeText(getApplicationContext(), "Over-written!", Toast.LENGTH_SHORT);
        t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
        t.show();
        contactText.setText("");
        messageText.setText("");

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
      /*  Toast.makeText(getApplicationContext(), "Canceled!", Toast.LENGTH_SHORT).show();
        contactText.setText("");
        messageText.setText("");*/
    }
    public void insertIntoDB(String contactName, String message)
    {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        Cursor mCursor =

                db.query(tableName, new String[] {"name","message"}, "name" + "='" + contactName +"'", null,
                        null, null, null, null);

        if (mCursor != null && mCursor.moveToFirst()) {
            NoticeDialogFragment newFragment = new NoticeDialogFragment();
            newFragment.show(getFragmentManager(), "over-write");
        }
        else {
            ContentValues values = new ContentValues();
            values.put("name", contactName);
            values.put("message", message);
            db.insert(tableName, null, values);
            Toast t = Toast.makeText(getApplicationContext(), R.string.saved, Toast.LENGTH_SHORT);
            t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            t.show();
            InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(messageText.getWindowToken(), 0);
            contactText.setText("");
            messageText.setText("");
        }

    }

    public void retrieve(View v)
    {
        pickContact();
    }

    public void delMsg(View v)
    {
        String contactName = this.contactText.getText().toString();
        String message = this.messageText.getText().toString();

        if(contactName.equals("")) {
            Toast t = Toast.makeText(getApplicationContext(), R.string.select_contact, Toast.LENGTH_SHORT);
            t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            t.show();
        }
        else
        if(message.equals("")) {
            Toast t = Toast.makeText(getApplicationContext(), R.string.no_message_saved, Toast.LENGTH_SHORT);
            t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            t.show();        }

        else {
            DeleteDialogFragment delFragment = new DeleteDialogFragment();
            delFragment.show(getFragmentManager(), "delete");
        }
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    static final int PICK_CONTACT_REQUEST = 1;  // The request code

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                int contact = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
                String contactName = cursor.getString(contact);

                contactText.setText(contactName);

                FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
                db = mDbHelper.getReadableDatabase();

                Cursor mCursor =

                        db.query(tableName, new String[] {"name","message"}, "name" + "='" + contactName+"'", null,
                                null, null, null, null);
                if (mCursor != null && mCursor.moveToFirst()) {
                    String msg = mCursor.getString(mCursor.getColumnIndex("message"));
                    messageText.setText(msg);
                }
                else
                {
                    messageText.setText("");
                }
            }
        }
    }

    @Override
    public void onDeletePositiveClick(DialogFragment dialog) {

        String contactName = this.contactText.getText().toString();

        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        db = mDbHelper.getReadableDatabase();

        Cursor mCursor =

                db.query(tableName, new String[] {"name","message"}, "name" + "='" + contactText.getText().toString()+"'", null,
                        null, null, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            String msg = mCursor.getString(mCursor.getColumnIndex("message"));
            messageText.setText(msg);
        }

        if(db.delete(tableName, "name='" + contactName+"'", null) > 0) {
            Toast t = Toast.makeText(getApplicationContext(), R.string.deleted, Toast.LENGTH_SHORT);
            t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            t.show();

            contactText.setText("");
            messageText.setText("");
        }
        else {
            Toast t = Toast.makeText(getApplicationContext(), R.string.nothing_delete, Toast.LENGTH_SHORT);
            t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
            t.show();        }

    }

    @Override
    public void onDeleteNegativeClick(DialogFragment dialog) {
       /* Toast t = Toast.makeText(getApplicationContext(), "Canceled!", Toast.LENGTH_SHORT);
        t.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
        t.show();    */
    }
}
