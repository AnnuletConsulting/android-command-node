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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewFragment extends Fragment {
	private StringBuffer logTxt = new StringBuffer();
	private static ViewFragment instance = null;
	private TextView log = null;
	
	public static ViewFragment getInstance() {
		if (instance == null)
			instance = new ViewFragment();
		return instance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.view_fragment, null);
		log = (TextView) v.findViewById(R.id.log);
		log.setText(logTxt.toString());
		instance = this;
		return v;
	}
	
	public void appendLog(String s) {
		logTxt.append(s);
		logTxt.append("\n");
		if (log != null)
			log.setText(logTxt.toString());
	}
}