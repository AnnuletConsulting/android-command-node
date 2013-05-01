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
import java.util.Calendar;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

public class AsyncSend implements LoaderCallbacks<String> {
    private static final String ENCODING_FORMAT = "UTF8";
	private static final String SIGNATURE_METHOD = "HmacSHA256";
	private static ViewFragment viewFragment;
	private static Activity activity;
	private static String ipAddr;
	private static String cmd;
	private static int port;
	private static TextToSpeech tts;
	private static Runnable finishListener;
	private static String sharedKey = null;
	
	public AsyncSend(Activity activity, ViewFragment viewFragment, String ipAddr, int port, String sharedKey, String cmd, Runnable finishListener) {
		tts = new TextToSpeech(activity, null);
		AsyncSend.viewFragment = viewFragment;
		AsyncSend.activity = activity;
		AsyncSend.ipAddr = ipAddr;
		AsyncSend.cmd = cmd;
		AsyncSend.port = port;
		AsyncSend.sharedKey = sharedKey;
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
		sb.append("\", \"node_type\": \"ANDROID");
		if (sharedKey  != null) {
			String timeStamp = getTimestamp();
			sb.append("\", \"time_stamp\": \"");
			sb.append(timeStamp);
			sb.append("\", \"signature\": \"");
			sb.append(getSignature(timeStamp));
		}
		sb.append("\" }");
		return sb.toString();
	}

	private String getSignature(String timeStamp) {
    	try {
			byte[] data = timeStamp.getBytes(ENCODING_FORMAT);
			Mac mac = Mac.getInstance(SIGNATURE_METHOD);
			mac.init(new SecretKeySpec(sharedKey.getBytes(ENCODING_FORMAT), SIGNATURE_METHOD));
			char[] signature = Hex.encodeHex(mac.doFinal(data));
			return new String( signature );
		}
		catch ( Exception exception ) {
			return "Error in getSignature()";
		}
	}

	private String getTimestamp() {
		Calendar today = Calendar.getInstance();
		StringBuffer out = new StringBuffer();
		out.append(today.get(Calendar.YEAR));
		out.append(today.get(Calendar.MONTH)+1);
		out.append(today.get(Calendar.DATE));
		out.append(today.get(Calendar.HOUR_OF_DAY));
		out.append(today.get(Calendar.MINUTE));
		out.append(today.get(Calendar.SECOND));
		out.append(today.get(Calendar.MILLISECOND));
		return out.toString();
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

	/**
	 * @param sharedKey the sharedKey to set
	 */
	public static void setSharedKey(String sharedKey) {
		AsyncSend.sharedKey = sharedKey;
	}
}
