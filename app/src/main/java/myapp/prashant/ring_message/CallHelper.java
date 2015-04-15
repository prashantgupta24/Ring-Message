package myapp.prashant.ring_message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.myapplication.R;


/**
 * Created by Prashant on 05-Apr-2015.
 */
public class CallHelper {


    private Context ctx;
    private TelephonyManager tm;
    private CallStateListener callStateListener;
    private OutgoingReceiver outgoingReceiver;
    FeedReaderDbHelper mDbHelper;
    SQLiteDatabase db;

    /**
     * Listener to detect incoming calls.
     */
    private class CallStateListener extends PhoneStateListener {

        private WindowManager wm;
        private View layout;
        private WindowManager.LayoutParams params;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: // called when someone is ringing to this phone

                    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
                    Cursor cursor = ctx.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, incomingNumber, null, null );
                    if(cursor.moveToFirst()){
                        incomingNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                    cursor.close();

                    Cursor mCursor = db.query(MainActivity.tableName, new String[] {"name","message"}, "name" + "='" + incomingNumber+"'", null,
                            null, null, null, null);

                        if (mCursor != null && mCursor.moveToFirst()) {
                            String msg = mCursor.getString(mCursor.getColumnIndex("message"));

                            wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                            params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                                    WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);

                            params.gravity = Gravity.CENTER_VERTICAL;
                           //params.gravity = Gravity.BOTTOM;

                            LayoutInflater inflater = LayoutInflater.from(ctx);
                            layout = inflater.inflate(R.layout.toast_layout, null);
                            TextView text = (TextView) layout.findViewById(R.id.text);
                            text.setText(msg);
                            wm.addView(layout, params);

                            layout.setOnTouchListener(new View.OnTouchListener() {

                                @Override
                                public boolean onTouch(View view, MotionEvent event) {

                                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                                        case MotionEvent.ACTION_DOWN:
                                            wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                                            break;

                                        case MotionEvent.ACTION_UP:
                                            wm.removeView(layout);
                                            layout = null;
                                            break;

                                        case MotionEvent.ACTION_POINTER_DOWN:
                                            break;

                                        case MotionEvent.ACTION_POINTER_UP:
                                            break;

                                        case MotionEvent.ACTION_MOVE:
                                            break;
                                    }
                                    return true;
                                }
                            });
                        }

                    if(mCursor!=null)
                        mCursor.close();
                    break;

                case TelephonyManager.CALL_STATE_IDLE:

                    if(layout!=null) {
                       // Log.d("gupta", "IDLE!");
                        wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(layout);
                        layout = null;
                    }
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:

                    if(layout!=null) {
                       // Log.d("gupta", "OFF HOOK!");
                        wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(layout);
                        layout = null;
                    }
                    break;
            }
        }
    }


    private class OutgoingReceiver extends BroadcastReceiver {
        public OutgoingReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor cursor = ctx.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, number, null, null );
            if(cursor.moveToFirst()){
                number = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();

            Cursor mCursor = db.query(MainActivity.tableName, new String[] {"name","message"}, "name" + "='" + number+"'", null,
                    null, null, null, null);

            if (mCursor != null && mCursor.moveToFirst()) {
                String msg = mCursor.getString(mCursor.getColumnIndex("message"));

                LayoutInflater inflater = LayoutInflater.from(ctx);
                View layout = inflater.inflate(R.layout.message_layout_outgoing,null);

                TextView text = (TextView) layout.findViewById(R.id.text);
                text.setText(msg);

                Toast toast = new Toast(ctx);
                toast.setGravity(0, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
                }
            if(mCursor !=null)
                mCursor.close();
            }
        }

    public CallHelper(Context ctx) {
        this.ctx = ctx;

        callStateListener = new CallStateListener();
        mDbHelper = new FeedReaderDbHelper(ctx);
        db = mDbHelper.getReadableDatabase();
        outgoingReceiver = new OutgoingReceiver();
    }

    /**
     * Start calls detection.
     */
    public void run() {
        tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

       IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
       ctx.registerReceiver(outgoingReceiver, intentFilter);
    }

    /**
     * Stop calls detection.
     */
    public void stop() {
        tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(outgoingReceiver);
    }
}
