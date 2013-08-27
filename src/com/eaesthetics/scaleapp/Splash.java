 /** @author      David Westgate <davidjwestgate@gmail.com>
 *   @version     1.0                       
 *   @since       2012-12-04
 */
package com.eaesthetics.scaleapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
/** The splash screen */
public class Splash extends Activity {
    
    protected boolean _active = true;    
    protected int _splashTime = 3000; // time to display the splash screen in ms
    /**
     * The thread to process splash screen events
     */
    private Thread mSplashThread;
    
    
    /** Called when the activity is first created.
     * @ param The saved instance state of the phone */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Splash screen view
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove activity bar
        setContentView(R.layout.splash_main);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //Forces phone to landscape mode
        
        
        final Splash sPlashScreen = this; //create instance of splash screen   
        
        // The thread to wait for splash screen events
        mSplashThread =  new Thread(){
            @Override
            public void run(){
                try {
                    synchronized(this){
                        // Wait given period of time or exit on touch
                        wait(3000);
                    }
                }
                catch(InterruptedException ex){                    
                }

                finish();
                
                // Run next activity
                Intent intent = new Intent();
                intent.setClass(sPlashScreen, MainActivity.class);
                startActivity(intent);
                stop();
                
            }
        };
        
        mSplashThread.start();
        
    }
        
    /**
     * Processes splash screen touch events
     * @param   A motion Event
     */
    @Override
    public boolean onTouchEvent(MotionEvent evt) //when the splash screen is touched, go to App
    {
        if(evt.getAction() == MotionEvent.ACTION_DOWN)
        {
            synchronized(mSplashThread){
                mSplashThread.notifyAll();
            }
        }
        return true;
    }
   
    }
    
    
 