package myapp.prashant.ring_message;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends Activity implements NoticeDialogFragment.NoticeDialogListener, DeleteDialogFragment.DeleteDialogListener,
        AdapterView.OnItemSelectedListener {

    static String tableName = "mytable";

    private TextView contactText;
    private EditText messageText;
    private Button datePickerField;
    private ImageButton imgBtn;
    private LinearLayout layout;
    private SQLiteDatabase db;
    private String contactName = "contact name";
    private String datePicked = "date picked";
    private String layoutOrientation = "layout";
    private ViewSwitcher viewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactText = (TextView)findViewById(R.id.contactName);
        messageText = (EditText)findViewById(R.id.message);
        layout = (LinearLayout)findViewById(R.id.layout1);
        imgBtn = (ImageButton)findViewById(R.id.imageButton);
        datePickerField = (Button)findViewById(R.id.datePick);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewswitcher);
        initializeSpinner();
        Intent intent = new Intent(this, CallDetectService.class);
        startService(intent);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String cName = savedInstanceState.getString(contactName);
        if(!cName.equals(""))
        {
            contactText.setText(cName);
            contactText.setBackgroundResource(R.drawable.contact_choosen_style);
        }
        datePickerField.setText(savedInstanceState.getString(datePicked));
        int vis = savedInstanceState.getInt(layoutOrientation);

        if(vis == View.GONE)
        {
            layout.setVisibility(View.GONE);
            imgBtn.setBackgroundResource(R.drawable.plus);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(contactName, contactText.getText().toString());
        outState.putString(datePicked, datePickerField.getText().toString());
        outState.putInt(layoutOrientation, layout.getVisibility());

        super.onSaveInstanceState(outState);
    }

    private void initializeSpinner()
    {

        Spinner spinnerOptions = (Spinner) findViewById(R.id.spinnerOptions);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.spinnerOptions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerOptions.setAdapter(adapter1);
        spinnerOptions.setOnItemSelectedListener(this);

        Spinner spinnerTimes = (Spinner) findViewById(R.id.spinnerText);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinnerText, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTimes.setAdapter(adapter);

        Animation anim1, anim2;

        anim1 = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left);
        anim2 = AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right);

        viewSwitcher.setInAnimation(anim1);
        viewSwitcher.setOutAnimation(anim2);
    }

    public void showDatePicker(View v)
    {
        DatePickerDialog myDatePickerDialog;

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        myDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                datePickerField.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        myDatePickerDialog.show();
    }

    public void showHideLayout(View v)
    {
        layout.getVisibility();
        if(layout.getVisibility() == View.VISIBLE) {
            layout.setVisibility(View.GONE);
            imgBtn.setBackgroundResource(R.drawable.plus);
        }
        else {
            layout.setVisibility(View.VISIBLE);
            imgBtn.setBackgroundResource(R.drawable.minus);
        }
    }

    public void showTooltip(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText(R.string.tool_tip);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setAllCaps(true);
        title.setTextSize(20);
        title.setBackgroundColor(Color.WHITE);

       // builder.setTitle(R.string.tool_tip);
        builder.setCustomTitle(title);
        builder.setMessage(R.string.tool_tip_data).setNegativeButton("Close",null);

       AlertDialog dialog = builder.create();
        dialog.show();
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

            else if(message.length()>100)
            {
                Toast t = Toast.makeText(getApplicationContext(), R.string.message_too_long, Toast.LENGTH_SHORT);
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
        contactText.setBackgroundResource(0);
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
            contactText.setBackgroundResource(0);
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
                contactText.setBackgroundResource(R.drawable.contact_choosen_style);

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
            contactText.setBackgroundResource(0);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewswitcher);
        viewSwitcher.showPrevious();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
