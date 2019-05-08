/**
 * (c) Aleksey Ivantsov
 * Based on Equations for organic motion
 * @author Justin Windle
 *
 * @see http://soulwire.co.uk
 * @see https://github.com/soulwire/sketch.js
 */ 
package com.s0l.equationsdream;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class EquationsView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "EquationsView";
	public static final String	SHARED_PREFS_NAME	= "equationssettings"; 

    private static final String[] colors = new String[]
    {
    	"#FE4365", "#FC9D9A", "#F9CDAD",
    	"#C8C8A9", "#83AF9B", "#FC913A",
    	"#F9D423", "#435356", "#566965",
    	"#FF7373", "#A9DA88", "#E3AAD6",
    	"#73A8AF", "#F6BCAD", "#BE4C54",
    	"#7CD7CF", "#FFA446", "#B5D8EB",
    	"#E05561", "#F4CE79", "#77B29C"
    }; 
    private static final String[] formulas = new String[]
    {
          "sin(t)",
          "cos(t)",
          "cos(t)*sin(t)",
          "sin(t)*sin(t*1.5)",
          "sin(tan(cos(t)*1.2))",
          "sin(tan(t)*0.05)",
          "cos(sin(t*3))*sin(t*0.2)",
          "sin(pow(8,sin(t)))",
          "sin(exp(cos(t*0.8))*2)",
          "sin(t-PI*tan(t)*0.01)",
          "pow(sin(t*PI),12)",
          "cos(sin(t)*tan(t*PI)*PI/8)",
          "sin(tan(t)*pow(sin(t),10))",
          "cos(sin(t*3)+t*3)",
          "pow(abs(sin(t*2))*0.6,sin(t*2))*0.6"
    }; 
       
    private int width;
    private int height;
    private SurfaceHolder holder;
    Paint paint = null;
    private long sleepTime;            //how long we sleep the thread for between animation frames
    private int frameDelay      = 150;  //amount of time to sleep for (in milliseconds)
    private long executionTime;       //how long the game logic took to execute
    Context mContext;
    boolean newCicle = true;
    double x_ = 0, y_ = 0;
    int i_ = 0, c_ = 0;
    private boolean mShowText=true;
    private boolean mStandartAnimation=true;
    private boolean mNightMode=true;
    
    private boolean itemsAlarmGmail=false;
    private boolean itemsAlarmSMS=false;
    private boolean itemsAlarmCall=false;
    private Bitmap gmail=null;
    private int gunread=0;
    private String gSunread="";
    double xgmail = 0, ygmail = 0;
    private Bitmap sms=null;
    private int sunread=0;
    private String sSunread="";
    double xsms = 0, ysms = 0;
    private Bitmap misscall=null;
    private int cunread=0;
    private String cSunread="";
    double xcall = 0, ycall = 0;
    private Bitmap mail=null;
    
    public EquationsView(Context context) 
    {
        super(context);
        mContext=context;
        paint = new Paint();
        paint.setAntiAlias(true);
        try 
        {        
        	mShowText=isPrefEnabled(mContext.getString(R.string.equations_animation_text_key), true);
	        mStandartAnimation=isPrefEnabled(mContext.getString(R.string.equations_animation_type_key), true);
	        mNightMode=isPrefEnabled(mContext.getString(R.string.equations_animation_night_key), true);
	        paint.setColorFilter(new PorterDuffColorFilter((mNightMode ? 0x60FFFFFF : 0xC0FFFFFF), PorterDuff.Mode.MULTIPLY));
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }        
        try
        {
        	gmail=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_action_unread);
        	gSunread=mContext.getString(R.string.equations_notification_gmail_unread);
        	sms=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_action_chat);
        	sSunread=mContext.getString(R.string.equations_notification_sms_unread);
        	misscall=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_action_ring_volume);
        	cSunread=mContext.getString(R.string.equations_notification_calls_unread);
        	mail=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_action_email);
        }catch(Exception e)
        {
        	
        }
        newCicle = true;
		holder = getHolder();
        holder.addCallback(this);
    }
    
    public boolean isPrefEnabled(String prefName, boolean defValue) 
    {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(prefName, defValue);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder2) 
    {
        Log.v(TAG, "surfaceCreated");
        new Thread() 
        {
	        int frames = 0;
            Canvas canvas = null;
            @Override
            public void run() 
            {                                 
        		while (true) 
        		{		
        			executionTime = System.nanoTime();//record the time right before we execute the update loop
	            	try
	            	{
		            	canvas = holder.lockCanvas();
		                if (canvas == null) 
		                {
		                    return;
		                }
		                
	                    try {
	                        if (isPrefEnabled(mContext.getString(R.string.equations_notification_gmail_key), true))
	                            checkGmail(mContext);
	                        if (isPrefEnabled(mContext.getString(R.string.equations_notification_sms_key), true))
	                            checkSMS(mContext);
	                        if (isPrefEnabled(mContext.getString(R.string.equations_notification_calls_key), true))
	                            checkMissedCalls(mContext);
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }	
	                    	                
		                frameDelay = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getContext()).getString(mContext.getString(R.string.equations_animation_key), "150"));
//		                Log.d(TAG,String.valueOf(frameDelay));
		                if (mStandartAnimation==true)
		                {
		    				if(frames>30)
		    				{
		    					frames=0;
		    				}
		    				drawFrame(canvas);
		                }
		                else
		                {
		    				if(frames>30)
		    				{
		    					frames=0;
		    					newCicle=true;
		    				}
		                	drawFrameHaose(canvas);		                	
		                }
	                } 
	                catch (Exception e) 
	                {
	                } 
	                finally 
	                {
	                	if (canvas != null)
		          	  		holder.unlockCanvasAndPost(canvas);                                      
	                }
	    			//calculate the sleep time of the thread right before we exit the thread loop
	    			//this calculates how long it took to execute the update function
	    			sleepTime = frameDelay-((System.nanoTime()-executionTime)/1000000L);   		     
	    			//we need to sleep the thread every few milliseconds 
	    			try
	    			{				
	    				//if 
	    				if(sleepTime>0)
	    				{
	    					//sleep the thread
	    					//if we don't do this, the animation will execute as fast as the processor will allow it
	    					//and the user will not see a nice animation because it will go by at the speed of light
	    		            Thread.sleep(sleepTime);
		    				frames++;
//			                Log.d(TAG,String.valueOf(frames));
	    			    }
	    			} 
	    			catch (InterruptedException ex) 
	    			{
	                }
        		}
            }
        }.start();
    }
    
    private void drawFrame(Canvas canvas)
    {
        double t = System.currentTimeMillis() * 0.0015;

        int rows = width>height?3:5;
        int cols = width>height?5:3;
        int minR = 10;
        int maxR = 50;
        double xs = Math.max(maxR * 3, width / cols);//480 		150 	75
        double ys = Math.max(maxR * 3, height / rows);//1182 	394 	197
        double x, y, f = 0;
        int i = 0;
        int c = 0;
        canvas.drawColor(Color.BLACK);
        for (y = ys * 0.5; y < height; y += ys) {
            for (x = xs * 0.5; x < width; x += xs) {
                f = minR + Math.abs(getFormulasEq(i,t)) * (maxR - minR);           
	            paint.setColor(Color.parseColor(getColor(c)));
                canvas.drawCircle((float)(x), (float)(y), (float)(f), paint);
                if (isPrefEnabled(mContext.getString(R.string.equations_animation_text_key), true)) {
	                paint.setTextAlign(Align.CENTER);
	                paint.setTextSize(16);
	                paint.setColor(Color.argb(128, 255, 255, 255));
	                canvas.drawText(getFormulas(i), (float)(x), (float)(y + maxR + 30), paint);
                }
                i++;
                if(i>formulas.length)
                	i=0;
                c++;
                if(c>colors.length)
                	c=0;
            }
        }   
		double yg=-10;
		double xg=-10;
		paint.setAlpha(255);
		if(itemsAlarmGmail==true&&gmail!=null){
        	yg=ys-gmail.getWidth()/2;
        	xg=xs-gmail.getHeight()/2;   
        	canvas.drawBitmap(gmail, (float)yg, (float)xg, paint);
            canvas.drawText(gSunread+" "+String.valueOf(gunread), (float)(yg+gmail.getHeight()/2), (float)(xg + gmail.getHeight()*1.1), paint);
        }
        if(itemsAlarmSMS==true&&sms!=null){
        	if(rows==5)	{    		
        		yg=ys-sms.getWidth()/2;
        		xg=xs*2-sms.getHeight()/2;
        	} else {
        		yg=ys*2-sms.getWidth()/2;
        		xg=xs-sms.getHeight()/2;        		
        	}
        	canvas.drawBitmap(sms, (float)yg, (float)xg, paint);
            canvas.drawText(sSunread+" "+String.valueOf(sunread), (float)(yg+sms.getHeight()/2), (float)(xg + sms.getHeight()*1.1), paint);
        }
        if(itemsAlarmCall==true&&misscall!=null){
        	if(rows==5)	{
        		yg=ys-misscall.getWidth()/2;
        		xg=xs*3-misscall.getHeight()/2;
        	} else {
        		yg=ys*3-misscall.getWidth()/2;
        		xg=xs-misscall.getHeight()/2;        		
        	}
        	canvas.drawBitmap(misscall, (float)yg, (float)xg, paint);
            canvas.drawText(cSunread+" "+String.valueOf(cunread), (float)(yg+misscall.getHeight()/2), (float)(xg + misscall.getHeight()*1.1), paint);
        }
        paint.setColorFilter(new PorterDuffColorFilter((mNightMode ? 0x60FFFFFF : 0xC0FFFFFF), PorterDuff.Mode.MULTIPLY));
    }
    
    private void drawFrameHaose(Canvas canvas)
    {
        double t = System.currentTimeMillis() * 0.0015;

        int minR = 10;
        int maxR = 50;
        double xs = maxR * 2;
        double ys = maxR * 2;
        double x = xs, y = ys, f = 0;
        int i = 0, c = 0;
        canvas.drawColor(Color.BLACK);
        if(newCicle==true)
        {
	    	newCicle=false;
	    	y_=Math.random()*height;
	    	if(y_>(double)(height-(int)ys))
	    		y_=ys;
	    	if(y_<ys)
	    		y_=ys;

	    	x_=Math.random()*width;
	    	if(x_>(double)(width-(int)xs))
	    		x_=xs;
	    	if(x_<xs)
	    		x_=xs;
	    	i_=(int) (Math.random()*formulas.length);
	    	c_=(int) (Math.random()*colors.length);
	    	
        	ygmail=Math.random()*width-gmail.getWidth()/2;
	    	if(ygmail>(double)(height-(int)xs))
	    		ygmail=xs;
	    	if(ygmail<xs)
	    		ygmail=xs;
        	xgmail=Math.random()*height-gmail.getHeight()/2;
	    	if(xgmail>(double)(width-(int)ys))
	    		xgmail=ys;
	    	if(xgmail<ys)
	    		xgmail=ys;
	    	
        	ysms=Math.random()*width-sms.getWidth()/2;
	    	if(ysms>(double)(height-(int)xs))
	    		ysms=xs;
	    	if(ysms<xs)
	    		ysms=xs;
	    	xsms=Math.random()*height-sms.getHeight()/2;
	    	if(xsms>(double)(width-(int)ys))
	    		xsms=ys;
	    	if(xsms<ys)
	    		xsms=ys;
	    	
	    	ycall=Math.random()*width-misscall.getWidth()/2;
	    	if(ycall>(double)(height-(int)xs))
	    		ycall=xs;
	    	if(ycall<xs)
	    		ycall=xs;
	    	xcall=Math.random()*height-misscall.getHeight()/2;
	    	if(xcall>(double)(width-(int)ys))
	    		xcall=ys;
	    	if(xcall<ys)
	    		xcall=ys;
	    }
        x=x_;
        y=y_;
        i=i_;   
        c=c_;
        
        f = minR + Math.abs(getFormulasEq(i,t)) * (maxR - minR);
        paint.setColor(Color.parseColor(getColor(c)));
        canvas.drawCircle((float)(x), (float)(y), (float)(f), paint);

        if (mShowText==true)
        {
            paint.setTextAlign(Align.CENTER);
            paint.setTextSize(16);
            paint.setColor(Color.argb(128, 255, 255, 255));
            canvas.drawText(getFormulas(i), (float)(x), (float)(y + maxR + 30), paint);
        }      	
		paint.setAlpha(255);
    	if(itemsAlarmGmail==true&&gmail!=null){
        	canvas.drawBitmap(gmail, (float)ygmail, (float)xgmail, paint);
            canvas.drawText(gSunread+" "+String.valueOf(gunread), (float)(ygmail+gmail.getHeight()/2), (float)(xgmail + gmail.getHeight()*1.1), paint);
        }
        if(itemsAlarmSMS==true&&sms!=null){
        	canvas.drawBitmap(sms, (float)ysms, (float)xsms, paint);
            canvas.drawText(sSunread+" "+String.valueOf(sunread), (float)(ysms+sms.getHeight()/2), (float)(xsms + sms.getHeight()*1.1), paint);
        }
        if(itemsAlarmCall==true&&misscall!=null){

        	canvas.drawBitmap(misscall, (float)ycall, (float)xcall, paint);
            canvas.drawText(cSunread+" "+String.valueOf(cunread), (float)(ycall+misscall.getHeight()/2), (float)(xcall + misscall.getHeight()*1.1), paint);
        }
        paint.setColorFilter(new PorterDuffColorFilter((mNightMode ? 0x60FFFFFF : 0xC0FFFFFF), PorterDuff.Mode.MULTIPLY));
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.v(TAG, "surfaceChanged");
        this.width = width;
        this.height = height;
		newCicle=true;
	}

    private void setViewVisibility(final int item, final boolean visibility) 
    {
    	switch(item)
    	{
    		case 0://gmail
    		    itemsAlarmGmail=visibility;
    		    break;
    		case 1://sms
    		    itemsAlarmSMS=visibility;
    		    break;
    		case 2://call
    		    itemsAlarmCall=visibility;
    		    break;
    	}
    }
    
    private void checkGmail(final Context context) 
    {
        // Get the account list, and pick the first one
        final String ACCOUNT_TYPE_GOOGLE = "com.google";
        final String[] FEATURES_MAIL = {"service_mail"};
        AccountManager.get(context).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, FEATURES_MAIL, new AccountManagerCallback<Account[]>() {
            @Override
            public void run(AccountManagerFuture<Account[]> future) 
            {
                Account[] accounts = null;
                try {
                	gunread = 0;
                    setViewVisibility(0, false);
                    accounts = (Account[]) future.getResult();
                    if (accounts != null && accounts.length > 0) {
                        for (Account account : accounts) {
                            String selectedAccount = account.name;
                            queryLabels(selectedAccount, context);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void queryLabels(String selectedAccount, Context context) {
//              Log.d(TAG,"Gmail - " + selectedAccount);
                Cursor labelsCursor = context.getContentResolver().query(GmailContract.Labels.getLabelsUri(selectedAccount), null, null, null, null);
                labelsCursor.moveToFirst();
                do {
                    String name = labelsCursor.getString(labelsCursor.getColumnIndex(GmailContract.Labels.CANONICAL_NAME));
                    gunread = labelsCursor.getInt(labelsCursor.getColumnIndex(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS));// here's the value you need
                    if (name.equals(GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX) && gunread > 0) {
//                        Log.d(TAG,"Gmail - " + name + "-" + gunread);
                        setViewVisibility(0, true);
                        return;
                    }
                } while (labelsCursor.moveToNext());
                labelsCursor.close();
            }
        }, null );
    }
    
    private void checkSMS(Context context) 
    {
        Cursor cursor = null;
        try 
        {
            Uri uriSMSURI = Uri.parse("content://sms/inbox");
            cursor = context.getContentResolver().query(uriSMSURI, null, "read = 0", null, null);
//          Log.d(TAG,"SMS - " + cur.getCount());
            if (cursor.getCount() > 0) 
            {
	            sunread = cursor.getCount();
                setViewVisibility(1, true);
            } else {
	            sunread = 0;
                setViewVisibility(1, false);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	cursor.close();
    	}
    }

    private void checkMissedCalls(Context context) 
    {
        final String[] projection = null;
        final String selection = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.IS_READ + "=0";
        final String[] selectionArgs = null;
        final String sortOrder = null;
        Cursor cursor = null;
        try 
        {
            cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
            if (cursor.getCount() > 0) {
            	cunread = cursor.getCount();
                setViewVisibility(2, true);
            } else {
            	cunread = 0;
                setViewVisibility(2, false);
            }
        } catch (Exception ex) {
            Log.e(TAG,"ERROR: " + ex.toString());
        } finally {
            cursor.close();
        }
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) 
    {
        Log.v(TAG, "surfaceDestroyed");
    }
    private String getColor(int i) 
    {
        return colors[i];
    }  
    private String getColor() {
        int color = (int)(Math.random() * colors.length);
        return colors[color];
    } 
    private String getFormulas(int i) 
    {
    	return formulas[i];
    } 
    private double getFormulasEq(int i, double t)
    {
    	switch(i)
		{
    		default:
			case 0:
				return Math.sin(t);
			case 1:
				return Math.cos(t);
			case 2:
				return Math.cos(t)*Math.sin(t);
			case 3:
				return Math.sin(t)*Math.sin(t*1.5);
			case 4:
				Math.sin(Math.tan(Math.cos(t)*1.2));
			case 5:
				return Math.sin(Math.tan(t)*0.05);
			case 6:
				return Math.cos(Math.sin(t*3))*Math.sin(t*0.2);
			case 7:
				return Math.sin(Math.pow(8,Math.sin(t)));
			case 8:
				return Math.sin(Math.exp(Math.cos(t*0.8))*2);
			case 9:
				return Math.sin(t-Math.PI*Math.tan(t)*0.01);
			case 10:
				return Math.pow(Math.sin(t*Math.PI),12);
			case 11:
				return Math.cos(Math.sin(t)*Math.tan(t*Math.PI)*Math.PI/8);
			case 12:
				return Math.sin(Math.tan(t)*Math.pow(Math.sin(t),10));
			case 13:
				return Math.cos(Math.sin(t*3)+t*3);
			case 14:
				return Math.pow(Math.abs(Math.sin(t*2))*0.6,Math.sin(t*2))*0.6;
		}
    }

}
/*
float x = (float)(Math.random() * width);
float y = (float)(Math.random() * height);

paint.setColor(getColor());

c.drawCircle(x, y, 5f, paint);
*/
/*              	  // draw 100 random lines
canvas.drawColor(Color.BLACK);
for ( int i = 0; i < 10; i++ ) 
{
	x1 = rand.nextInt( width );
	y1 = rand.nextInt( height );
	x2 = rand.nextInt( width );
	y2 = rand.nextInt( height );

	paint.setColor( Color.rgb( rand.nextInt( 256 ),
			rand.nextInt( 256 ), rand.nextInt( 256 ) ) );
	canvas.drawLine( x1, y1, x2, y2 ,paint);         		                
while(System.currentTimeMillis()-timeStep < 1000/10);
timeStep = System.currentTimeMillis();
	} // end outer for
	holder.unlockCanvasAndPost(canvas);
//try { Thread.sleep(600); } catch( InterruptedException e) {}
*/