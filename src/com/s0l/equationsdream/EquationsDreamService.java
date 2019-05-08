package com.s0l.equationsdream;

import android.preference.PreferenceManager;
import android.service.dreams.DreamService;

public class EquationsDreamService extends DreamService 
{
    private boolean mNightMode=true;

    @Override
    public void onAttachedToWindow() 
    {
        super.onAttachedToWindow();
        mNightMode=isPrefEnabled(this.getString(R.string.equations_animation_night_key), true);

        setInteractive(false);
        setFullscreen(true);
        setScreenBright(mNightMode);
        setContentView(new EquationsView(this));
    }
    public boolean isPrefEnabled(String prefName, boolean defValue) 
    {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(prefName, defValue);
    }
}