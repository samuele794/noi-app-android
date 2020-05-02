package org.dpppt.android.app.main.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;

public class SyncService extends IntentService {

	private int lastNumberOfHandshake = 0;

	public SyncService() {
		super("SYNC_SERVICE");
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (DP3T.isStarted(getApplicationContext())) {
			Log.d("[Protetti]", "Sync con il backend");
			DP3T.sync(getApplicationContext());

			TracingStatus status = DP3T.getStatus(getApplicationContext());
			Log.d("[Protetti]", "Aggiornamento stato");
			Log.d("[Protetti]", "numero Handshake precedenti: " + lastNumberOfHandshake);
			Log.d("[Protetti]", "numero Handshake attuali: " + status.getNumberOfHandshakes());
			if (status.getNumberOfHandshakes() != lastNumberOfHandshake) {
				lastNumberOfHandshake = status.getNumberOfHandshakes();
			}
		}
	}


}
