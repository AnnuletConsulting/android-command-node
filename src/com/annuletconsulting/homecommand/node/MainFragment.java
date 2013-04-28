/*
 * Copyright (C) 2013 Annulet Consulting, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.annuletconsulting.homecommand.node;

import com.google.android.noisealert.SoundMeter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class MainFragment extends Fragment {
	private static final String TAG = "HomeCommandNode";
	protected static final String RESULTS_KEY = "results_recognition";
	private SoundMeter soundMeter = new SoundMeter();
	private SpeechRecognizer speechRecognizer;
	private Handler handler = new Handler();
	private Button button;
	// private boolean isProcessing = false;
	private boolean isListening = false;
	private int loader = 0;
	private boolean ignore = false;
	private int mTickCount = 0;
	private int mHitCount = 0;
	private static final int POLL_INTERVAL = 1;
	protected static final int HIT_THRESHOLD = 1;
	private static final int AMP_THRESHOLD = 5;
	private int mPollDelay = 0;
	private static MainFragment instance = null;

	public static MainFragment getInstance() {
		if (instance == null)
			instance = new MainFragment();
		return instance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
		speechRecognizer.setRecognitionListener(new RecognitionListener() {
			@Override
			public void onReadyForSpeech(Bundle params) {
				for (String key : params.keySet())
					Log.d(TAG, (String) params.get(key));
			}

			@Override
			public void onBeginningOfSpeech() {
				Log.d(TAG, "Begin");
				ignore = false;
			}

			@Override
			public void onRmsChanged(float rmsdB) {
				// Log.d(TAG, "Rms changed: "+rmsdB);
			}

			@Override
			public void onBufferReceived(byte[] buffer) {
				Log.d(TAG, "Buffer Received: " + buffer.toString());
			}

			@Override
			public void onEndOfSpeech() {
				Log.d(TAG, "Endofspeech()");
			}

			@Override
			public void onError(int error) {
				Log.d(TAG, "error: " + error);
			}

			@Override
			public void onResults(Bundle results) {
				Log.d(TAG, "onResults()");
				for (String key : results.keySet()) {
					Log.d(TAG, key + ": " + results.get(key).toString());
					// Iterator<String> it = ((ArrayList<String>)
					// results.get(key)).listIterator();
					// while (it.hasNext())
					// Log.d(TAG, it.next());
				}
				if (!ignore)
					sendToServer(results.getStringArrayList(RESULTS_KEY).get(0));
			}

			@Override
			public void onPartialResults(Bundle partialResults) {
//				Log.d(TAG, "onPartialResults()");
//				String firstWord = partialResults.getStringArrayList(RESULTS_KEY).get(0).split(" ")[0];
//				Log.d(TAG, firstWord);
//				if (firstWord.length() > 0 && !firstWord.equalsIgnoreCase("computer") && !firstWord.equalsIgnoreCase("android")) {
//					Log.d(TAG, "Killing this Recognition.");
//					ignore = true;
//					stopRecognizing();
//					startListening();
//				}
			}

			@Override
			public void onEvent(int eventType, Bundle params) {
				Log.d(TAG, "onEvent() type: " + eventType);
				for (String key : params.keySet())
					Log.d(TAG, (String) params.get(key));
			}
		});
		View v = inflater.inflate(R.layout.main_fragment, null);
		button = (Button) v.findViewById(R.id.listen_button);
		button.setBackgroundResource(R.drawable.stopped);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleListenMode();
			}
		});
		instance = this;
		return v;
	}

	protected void toggleListenMode() {
		try {
			if (isListening)
				stopRecognizing(); //TODO stopListening();
			else
				startRecognizing(); //TODO startListening();
		} catch (Exception e) {
			showToastMessage(e.getMessage());
		}
	}

	protected void startListening() {
		isListening = true;
		button.setBackgroundResource(R.drawable.waiting);
		start();
	}

	protected void stop() {
        handler.removeCallbacks(mSleepTask);
        handler.removeCallbacks(mPollTask);
		isListening = false;
		soundMeter.stop();
	}
	
	protected void stopListening() {
	    stop();
		button.setBackgroundResource(R.drawable.stopped);
	}

	private Runnable mPollTask = new Runnable() {
		public void run() {
			double amp = soundMeter.getAmplitude();
			Log.d(TAG, String.valueOf(amp));

			if ((amp > AMP_THRESHOLD)) {
				mHitCount++;
				if (mHitCount > HIT_THRESHOLD) {
					stop();
					startRecognizing();
					return;
				}
			}
			mTickCount++;
			if (mPollDelay > 0 && mTickCount > 100) {
				sleep();

			} else {
				handler.postDelayed(mPollTask, POLL_INTERVAL);
			}
		}
	};

	private Runnable mSleepTask = new Runnable() {
		public void run() {
			start();
		}
	};

	private void start() {
		mTickCount = 0;
		mHitCount = 0;
		soundMeter.start();
		handler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void sleep() {
		soundMeter.stop();
		handler.postDelayed(mSleepTask, 1000 * mPollDelay);
	}

	protected void stopRecognizing() {
		button.setBackgroundResource(R.drawable.waiting);
		speechRecognizer.stopListening();
	}

	protected void startRecognizing() {
		isListening = false;
		button.setBackgroundResource(R.drawable.listening);
		Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3); //3 seems to be the minimum. when I set to 1 I get 5.
		speechRecognizer.startListening(recognizerIntent);
	}

	// In case we ever need to override this...
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private Runnable runner = new Runnable() {
		@Override
		public void run() {
			button.setBackgroundResource(R.drawable.stopped); // TODO startListening();
		}
	};
	
	private void sendToServer(String cmd) {
		Log.d(TAG, cmd);
		try {
			getActivity().getLoaderManager().initLoader(loader++, null, new AsyncSend(ViewFragment.getInstance(), 
																					  getActivity(),
																					  MainActivity.getValueFromSharedPreferences(MainActivity.SERVER),
																					  Integer.parseInt(MainActivity.getValueFromSharedPreferences(MainActivity.PORT)),
																					  cmd.toUpperCase(), 
																					  runner)).forceLoad();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showToastMessage(String message) {
		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
	}
}
