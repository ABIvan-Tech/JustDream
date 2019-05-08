package com.s0l.equationsdream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class EquationsDreamSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener
{
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle) 
	{
		  super.onCreate(icicle);
		  addPreferencesFromResource(R.xml.equationssettings);
//		  getListView().setBackgroundColor(0x1f000000);
	}
	
	@Override
	protected void onResume() {
	      super.onResume();
	        refresh(); 
	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		return true;
	}

    @SuppressWarnings("deprecation")
    private void refresh() 
    {
/*
        ListPreference listPref = (ListPreference) findPreference(KEY_CLOCK_STYLE);
        listPref.setSummary(listPref.getEntry());
        listPref.setOnPreferenceChangeListener(this);

        Preference pref = findPreference(KEY_NIGHT_MODE);
        pref.setOnPreferenceChangeListener(this);

        pref = findPreference(KEY_NOTIF_GMAIL);
        pref.setOnPreferenceChangeListener(this);

        pref = findPreference(KEY_NOTIF_SMS);
        pref.setOnPreferenceChangeListener(this);

        pref = findPreference(KEY_HIDE_ACTIVITY);
        if (Build.VERSION.SDK_INT < 17) {
            pref.setEnabled(false);
            pref.setSelectable(false);
            pref.setSummary(R.string.action_not_available_in_this_android);
        } else {
            pref.setOnPreferenceChangeListener(this);
        }
*/
    	Preference pref = findPreference(getString(R.string.equations_animation_about_key));

        String versionName = getVersionName(this);
        int versionNumber = getVersionCode(this);
        pref.setSummary(getString(R.string.equations_version_name) + " " + versionName + " (" +getString(R.string.equations_version_code)+ " " + String.valueOf(versionNumber) + ")" + "\n" + getString(R.string.equations_version_author));
//        Log.d("ED", getString(R.string.equations_version) + " " + versionName + " (" + String.valueOf(versionNumber) + ")");
    }

    /**
     * Gets version code of given application.
     * 
     * @param context
     * @return
     */
    public int getVersionCode(Context context) {
        PackageInfo pinfo;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int versionNumber = pinfo.versionCode;
            return versionNumber;
        } catch (NameNotFoundException e) {
            Log.e(context.getApplicationInfo().name, getString(R.string.equations_version_nocode));
        }
        return 0;
    }

    /**
     * Gets version name of given application.
     * 
     * @param context
     * @return
     */
    public String getVersionName(Context context) {
        PackageInfo pinfo;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String versionName = pinfo.versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            Log.e(context.getApplicationInfo().name, getString(R.string.equations_version_noname));
        }
        return null;
    }
}
