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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public class AsyncSend implements LoaderCallbacks<String> {
	private static ViewFragment viewFragment;
	private static Activity activity;
	private static String ipAddr;
	private static String cmd;
	private static int port;
	private static TextToSpeech tts;
	private static Runnable finishListener;
	
	public AsyncSend(ViewFragment viewFragment, Activity activity, String ipAddr, int port, String cmd, Runnable finishListener) {
		tts = new TextToSpeech(activity, null);
		AsyncSend.viewFragment = viewFragment;
		AsyncSend.activity = activity;
		AsyncSend.ipAddr = ipAddr;
		AsyncSend.cmd = cmd;
		AsyncSend.port = port;
		AsyncSend.finishListener = finishListener;
	}

	@Override
	public Loader<String> onCreateLoader(int id, Bundle args) {
		AsyncTaskLoader<String> loader = new AsyncTaskLoader<String>(activity) {
			@Override
			public String loadInBackground() {
				StringBuffer instr = new StringBuffer();
				try {
					Socket connection = new Socket(ipAddr, port);
			    	BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
			    	OutputStreamWriter osw = new OutputStreamWriter(bos, "US-ASCII");
			    	osw.write(formatJSON(cmd));
			    	osw.write(13);
			    	osw.flush();
			    	BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
					InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");
					int c;
					while ((c = isr.read()) != 13)
						instr.append((char) c);
					isr.close();
					bis.close();
			    	osw.close();
			    	bos.close();
					connection.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return instr.toString();
			}
		};
//		loader.forceLoad();
		return loader;
	}

	protected String formatJSON(String cmd) {
		StringBuffer sb = new StringBuffer();
		sb.append("{ \"command\": \"");
		sb.append(cmd);
		sb.append("\", \"node_type\": \"ANDROID\" }");
		return sb.toString();
	}

	@Override
	public void onLoadFinished(Loader<String> loader, String data) {
		System.out.println(data);
		viewFragment.appendLog(extractElement(data, "log"));
		if(!speak(extractElement(data, "error"))) {
			speak(extractElement(data, "speech"));
		}
		finishListener.run(); //TODO make this run after speech is complete so it doesn't loop.
	}

	@Override
	public void onLoaderReset(Loader<String> loader) {	
	}

	public static String extractElement(String json, String element) {
        while (json.indexOf(element) != -1) {        
            int startIndex = json.indexOf("\"", json.indexOf(element)+2+element.length());
            int endIndex = json.indexOf("\"", startIndex+1);
            return json.substring(startIndex+1, endIndex).toUpperCase();
        }
        return null;            
    }
    
    @SuppressWarnings("deprecation")
	public static boolean speak(String text) {
    	if (text != null && text.length() > 0) {
    		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    		return true;
    	}
    	return false;
    }
}
