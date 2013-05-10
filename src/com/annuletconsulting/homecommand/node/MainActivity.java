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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * This app will convert spoken commands starting with an assignable keyword, into text
 * then send them to the server app which will process them and return a text command
 * that determines what the node does next. 
 * 
 * It will also provide a settings page that will store things like the command phrase
 * node name and the ip/port of the local HC server, to the local database. 
 * 
 * @author Walt Moorhouse
 * @company Annulet Consulting, LLC
 */
public class MainActivity extends FragmentActivity {
	static final String SHARED_KEY = "SHARED_KEY";
	static final String ENCRPYT_CMD = "ENCRPYT_CMD";
	static final String SERVER = "SERVER_IP";
	static final String PORT = "PORT_NUM";
	
	private static SharedPreferences sharedPreferences;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getSharedPreferences("com.annuletconsulting.homecommand.node", Context.MODE_PRIVATE);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		if (getValueFromSharedPreferences(SERVER) == null)
			openSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings)
			return openSettings();
		return super.onOptionsItemSelected(item);
	}

	private boolean openSettings() {
	    final View layout = getLayoutInflater().inflate(R.layout.server_dialog, null);
	    final AlertDialog dialog = new AlertDialog.Builder(this).setView(layout).create();
		((EditText) layout.findViewById(R.id.server_ip)).setText(getValueFromSharedPreferences(SERVER));
		((EditText) layout.findViewById(R.id.port)).setText(getValueFromSharedPreferences(PORT));
		((EditText) layout.findViewById(R.id.shared_key)).setText(getValueFromSharedPreferences(SHARED_KEY));
		((CheckBox) layout.findViewById(R.id.encrypt_checkbox)).setChecked("Y".equals(getValueFromSharedPreferences(ENCRPYT_CMD)));
	    ((Button)layout.findViewById(R.id.dialog_save)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				storeValueInSharedPreferences(SERVER, ((EditText) layout.findViewById(R.id.server_ip)).getText().toString());
				storeValueInSharedPreferences(PORT, ((EditText) layout.findViewById(R.id.port)).getText().toString());
				storeValueInSharedPreferences(SHARED_KEY, ((EditText) layout.findViewById(R.id.shared_key)).getText().toString());
				storeValueInSharedPreferences(ENCRPYT_CMD, ((CheckBox) layout.findViewById(R.id.encrypt_checkbox)).isChecked()?"Y":"N");
				dialog.dismiss();
			}
		});
	    ((Button)layout.findViewById(R.id.dialog_close)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	    dialog.show();
		return false;
	}

	protected static void storeValueInSharedPreferences(String key, String value) {
    	SharedPreferences.Editor editor = sharedPreferences .edit();
    	editor.putString(key, value);
    	editor.commit();
	}
    
	public static String getValueFromSharedPreferences(String key) {
		return sharedPreferences.getString(key, null);
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 1:
					return new ViewFragment();
				case 0:
				default:
					return new MainFragment();
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return getString(R.string.title_section1);
				case 1:
					return getString(R.string.title_section2);
			}
			return null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		MainFragment.getInstance().onActivityResult(requestCode, resultCode, data);
	}
}
