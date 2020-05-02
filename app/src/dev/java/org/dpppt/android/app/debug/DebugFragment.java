package org.dpppt.android.app.debug;

import android.app.AlertDialog;
import android.app.Application;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.dpppt.android.app.R;
import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.TracingViewModel;
import org.dpppt.android.app.util.InfoDialog;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;

public class DebugFragment extends Fragment {

	private static final DateFormat DATE_FORMAT_SYNC = SimpleDateFormat.getDateTimeInstance();
	private TracingViewModel tracingViewModel;
	private Timer debugTimer;
	private int lastNumberOfHandshake = 0;

	public static void startDebugFragment(FragmentManager parentFragmentManager) {
		parentFragmentManager.beginTransaction()
				.replace(R.id.main_fragment_container, DebugFragment.newInstance())
				.addToBackStack(DebugFragment.class.getCanonicalName())
				.commit();
	}

	private static DebugFragment newInstance() {
		return new DebugFragment();
	}

	public DebugFragment() {
		super(R.layout.fragment_debug);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		setupSdkViews(view);
		setupStateOptions(view);
	}

	private void setupSdkViews(View view) {
		TextView statusText = view.findViewById(R.id.debug_sdk_state_text);
		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			statusText.setText(formatStatusString(status));
			boolean isTracing = (status.isAdvertising() || status.isReceiving()) && status.getErrors().size() == 0;
			statusText.setBackgroundTintList(ColorStateList.valueOf(
					isTracing ? getResources().getColor(R.color.status_green_bg, null)
							  : getResources().getColor(R.color.status_red_bg, null)));
		});

		view.findViewById(R.id.debug_button_reset).setOnClickListener(v -> {
			AlertDialog progressDialog = new AlertDialog.Builder(getContext())
					.setView(R.layout.dialog_loading)
					.show();
			tracingViewModel.resetSdk(() -> {
				progressDialog.dismiss();
				InfoDialog.newInstance(R.string.debug_sdk_reset_text)
						.show(getChildFragmentManager(), InfoDialog.class.getCanonicalName());
				updateRadioGroup(getView().findViewById(R.id.debug_state_options_group));
			});
			//Restart tracing after reset
			DP3T.start(getContext());
		});

		Switch switchDebug = view.findViewById(R.id.debug_force_sync_switch);
		switchDebug.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				//  force sync every 10s
				debugTimer = new Timer();
				debugTimer.scheduleAtFixedRate(new TimerTask() {
					private Handler updateUI = new Handler(){
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);
							checkState();
						}
					};
					@Override
					public void run() {
						try {
							updateUI.sendEmptyMessage(0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, 1000, 10000);
				// end
			} else {
				debugTimer.cancel();
				DP3T.start(getContext());

			}
		});
	}

	private void setupStateOptions(View view) {
		RadioGroup optionsGroup = view.findViewById(R.id.debug_state_options_group);
		optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
			switch (checkedId) {
				case R.id.debug_state_option_none:
					tracingViewModel.setDebugAppState(DebugAppState.NONE);
					break;
				case R.id.debug_state_option_healthy:
					tracingViewModel.setDebugAppState(DebugAppState.HEALTHY);
					break;
				case R.id.debug_state_option_exposed:
					tracingViewModel.setDebugAppState(DebugAppState.CONTACT_EXPOSED);
					break;
				case R.id.debug_state_option_infected:
					tracingViewModel.setDebugAppState(DebugAppState.REPORTED_EXPOSED);
					break;
			}
		});

		updateRadioGroup(optionsGroup);
	}

	private void updateRadioGroup(RadioGroup optionsGroup) {
		int preSetId = -1;
		switch (tracingViewModel.getDebugAppState()) {
			case NONE:
				preSetId = R.id.debug_state_option_none;
				break;
			case HEALTHY:
				preSetId = R.id.debug_state_option_healthy;
				break;
			case CONTACT_EXPOSED:
				preSetId = R.id.debug_state_option_exposed;
				break;
			case REPORTED_EXPOSED:
				preSetId = R.id.debug_state_option_infected;
				break;
		}
		optionsGroup.check(preSetId);
	}

	private SpannableString formatStatusString(TracingStatus status) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		boolean isTracing = (status.isAdvertising() || status.isReceiving()) && status.getErrors().size() == 0;
		builder.append(getString(isTracing ? R.string.tracing_active_title : R.string.tracing_error_title)).append("\n")
				.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		long lastSyncDateUTC = status.getLastSyncDate();
		String lastSyncDateString =
				lastSyncDateUTC > 0 ? DATE_FORMAT_SYNC.format(new Date(lastSyncDateUTC)) : "n/a";
		builder.append(getString(R.string.debug_sdk_state_last_synced))
				.append(lastSyncDateString).append("\n")
				.append(getString(R.string.debug_sdk_state_self_exposed))
				.append(getBooleanDebugString(status.isReportedAsExposed())).append("\n")
				.append(getString(R.string.debug_sdk_state_contact_exposed))
				.append(getBooleanDebugString(status.wasContactExposed())).append("\n")
				.append(getString(R.string.debug_sdk_state_number_handshakes))
				.append(String.valueOf(status.getNumberOfHandshakes()));

		ArrayList<TracingStatus.ErrorState> errors = status.getErrors();
		if (errors != null && errors.size() > 0) {
			int start = builder.length();
			builder.append("\n");
			for (TracingStatus.ErrorState error : errors) {
				builder.append("\n").append(error.toString());
			}
			builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red_main, null)),
					start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return new SpannableString(builder);
	}

	private String getBooleanDebugString(boolean value) {
		return getString(value ? R.string.debug_sdk_state_boolean_true : R.string.debug_sdk_state_boolean_false);
	}

	// added first very simple debug code
	// Please add code for option in config file for disable this code

	private void checkState() {
		if (DP3T.isStarted(this.getContext()) ) {
			try {
				Log.d("[Protetti]","Sync con il backend");
				// DP3T.sync(getContext());
			} catch (Exception e) {
				Log.d("[Protetti]", "Errore");
			}
			TracingStatus status = DP3T.getStatus(this.getContext());
			Log.d("[Protetti]", "Aggiornamento stato");
			Log.d("[Protetti]", "numero Handshake precedenti: " + lastNumberOfHandshake);
			Log.d("[Protetti]", "numero Handshake attuali: " + status.getNumberOfHandshakes());
			if (status.getNumberOfHandshakes() != lastNumberOfHandshake) {
				lastNumberOfHandshake = status.getNumberOfHandshakes();
				Toast.makeText(getContext(), "handshake attuali : " + status.getNumberOfHandshakes(), Toast.LENGTH_LONG).show();
			}
		}
	}
	// End

}
