package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


import org.jf.android.dex.Dex;
import org.jf.android.dx.command.dexer.DxContext;
import org.jf.android.dx.merge.CollisionPolicy;
import org.jf.android.dx.merge.DexMerger;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class haha extends Application {

    public static Context createAppContext() {

//        LoadedApk.makeApplication()
//        ContextImpl appContext = ContextImpl.createAppContext(mActivityThread, this);

        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);

            Object activityThreadObj = currentActivityThreadMethod.invoke(null);

            Field boundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
            boundApplicationField.setAccessible(true);
            Object mBoundApplication = boundApplicationField.get(activityThreadObj);   // AppBindData

            Field infoField = mBoundApplication.getClass().getDeclaredField("info");   // info
            infoField.setAccessible(true);
            Object loadedApkObj = infoField.get(mBoundApplication);  // LoadedApk

            Class contextImplClass = Class.forName("android.app.ContextImpl");
            Method createAppContextMethod = contextImplClass.getDeclaredMethod("createAppContext", activityThreadClass, loadedApkObj.getClass());
            createAppContextMethod.setAccessible(true);

            Object context = createAppContextMethod.invoke(null, activityThreadObj, loadedApkObj);

            if (context instanceof Context) {
                return (Context) context;
            }

        } catch (Exception e) {
            e.printStackTrace();
            hahahaha.Log.e("applicationContext:"+e.toString());
        }
        return null;
    }



    {
        Context applicationContext = createAppContext();
        hahahaha.Log.e("applicationContext:"+applicationContext);
    }

    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }




}
