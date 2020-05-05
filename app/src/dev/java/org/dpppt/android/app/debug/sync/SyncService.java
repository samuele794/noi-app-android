package org.dpppt.android.app.debug.sync;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class SyncService extends IntentService {

	private int lastNumberOfHandshake = 0;

	public static final int REPEAT_MILLIS = 60000;

	private static final int SYNC_REQUEST_CODE = 794;

	public static PendingIntent getSyncIntent(Context context) {
		Intent intent = new Intent(context, SyncService.class);
		return PendingIntent.getService(context, SYNC_REQUEST_CODE, intent, FLAG_UPDATE_CURRENT);
	}

	public static boolean syncServiceIsRunning(Context context) {
		Intent intent = new Intent(context, SyncService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, SYNC_REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE);
		return pendingIntent != null;
	}


	public SyncService() {
		super("SYNC_JOB");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		checkState();
	}

	private void checkState() {
		if (DP3T.isStarted(getApplicationContext())) {
			try {
				Log.d("[Protetti]", "Sync con il backend");
				DP3T.sync(getApplicationContext());
				TracingStatus status = DP3T.getStatus(getApplicationContext());
				Log.d("[Protetti]", "Aggiornamento stato");
				Log.d("[Protetti]", "numero Handshake precedenti: " + lastNumberOfHandshake);
				Log.d("[Protetti]", "numero Handshake attuali: " + status.getNumberOfHandshakes());
				if (status.getNumberOfHandshakes() != lastNumberOfHandshake) {
					lastNumberOfHandshake = status.getNumberOfHandshakes();
					Toast.makeText(getApplicationContext(), "handshake attuali : " + status.getNumberOfHandshakes(), Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Log.d("[Protetti]", "si è verificato un errore");
			}
		}
	}
}
