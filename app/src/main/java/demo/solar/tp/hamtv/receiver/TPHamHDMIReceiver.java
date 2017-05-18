package demo.solar.tp.hamtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by filiperodrigues on 12/05/17.
 */

public class TPHamHDMIReceiver extends BroadcastReceiver {
    private static String HDMIINTENT = "android.intent.action.HDMI_PLUGGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("RECEIVE","THINS");
        if (action.equals(HDMIINTENT)) {
            boolean state = intent.getBooleanExtra("state", false);

            if (state) {
                Toast.makeText(context, "HDMI >>", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "HDMI DisConnected>>", Toast.LENGTH_LONG).show();
            }
        }
    }
}
