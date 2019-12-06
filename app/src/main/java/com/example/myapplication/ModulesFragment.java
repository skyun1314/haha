package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ListFragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.example.myapplication.ModuleUtil;
import com.example.myapplication.ModuleUtil;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static com.example.myapplication.hahahaha.addMethod;

public class ModulesFragment extends AppCompatActivity implements ModuleUtil.ModuleListener , ListView.OnItemClickListener  {
    public static final String SETTINGS_CATEGORY = "com.example.myapplication.category.MODULE_SETTINGS";
    private static final String NOT_ACTIVE_NOTE_TAG = "NOT_ACTIVE_NOTE";
    private static String PLAY_STORE_LABEL = null;

    private ModuleUtil mModuleUtil;
    private ModuleAdapter mAdapter = null;
    private PackageManager mPm = null;

    private Runnable reloadModules = new Runnable() {
        public void run() {
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.addAll(mModuleUtil.getModules().values());
            final Collator col = Collator.getInstance(Locale.getDefault());
            mAdapter.sort(new Comparator<ModuleUtil.InstalledModule>() {
                @Override
                public int compare(ModuleUtil.InstalledModule lhs, ModuleUtil.InstalledModule rhs) {
                    return col.compare(lhs.getAppName(), rhs.getAppName());
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    };
    private MenuItem mClickedMenuItem = null;

    private int getDp(float value) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }
    ListView viewById;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        String appname =  getIntent().getStringExtra("appname");

        setTitle(appname);
        viewById = findViewById(R.id.modelist);
        ModuleUtil.appType apptype = (ModuleUtil.appType) getIntent().getSerializableExtra("apptype");

        viewById.setDivider(null);
        viewById.setDividerHeight(getDp(6));
        viewById.setPadding(getDp(8), getDp(8), getDp(8), getDp(8));

        viewById.setClipToPadding(false);
        mModuleUtil =new ModuleUtil(this, apptype);

        mAdapter = new ModuleAdapter(this,apptype,mModuleUtil);
        reloadModules.run();

        viewById.setAdapter(mAdapter);

        viewById.setOnItemClickListener(this);


        if(mModuleUtil.appType1== ModuleUtil.appType.xpmode){
            registerForContextMenu(viewById); // viewById.setOnCreateContextMenuListener(this);
        }


        mModuleUtil.addListener(this);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mModuleUtil.removeListener(this);
        mModuleUtil.modulesClear();
        mAdapter = null;
    }


    @Override
    public void onSingleInstalledModuleReloaded(ModuleUtil moduleUtil, String packageName, ModuleUtil.InstalledModule module) {
       runOnUiThread(reloadModules);
    }

    @Override
    public void onInstalledModulesReloaded(ModuleUtil moduleUtil) {
        runOnUiThread(reloadModules);
    }






    private void showTwo(ModuleUtil.InstalledModule Module) {

         AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this). setMessage("是否选择 "+" "+Module .getAppName()+"  ?").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情

                        MainActivity.zhuansheng(Module,ModulesFragment.this);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情

                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        String packageName = (String) v.getTag();
        if (packageName == null)
            return;

        if (packageName.equals(NOT_ACTIVE_NOTE_TAG)) {
            return;
        }


        ModuleUtil.InstalledModule installedModule = (position >= 0) ? (ModuleUtil.InstalledModule) mAdapter.getItem(position) : null;


        if(mModuleUtil.appType1== ModuleUtil.appType.xpmode){
            Intent launchIntent = getSettingsIntent(packageName,ModulesFragment.this);
            if (launchIntent != null)
                startActivity(launchIntent);
            else
                Toast.makeText(this,
                        "该模块未提供用户界面",
                        Toast.LENGTH_LONG).show();
        }else{
            showTwo(installedModule);
        }





    }




    private ModuleUtil.InstalledModule getItemFromContextMenuInfo(ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        int position = info.position - viewById.getHeaderViewsCount();
        return (position >= 0) ? (ModuleUtil.InstalledModule) mAdapter.getItem(position) : null;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        ModuleUtil.InstalledModule installedModule = getItemFromContextMenuInfo(menuInfo);
        if (installedModule == null)
            return;

        menu.setHeaderTitle(installedModule.getAppName());
       getMenuInflater().inflate(R.menu.context_menu_modules, menu);



    }






    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ModuleUtil.InstalledModule module = getItemFromContextMenuInfo(item.getMenuInfo());
        if (module == null)
            return false;

        switch (item.getItemId()) {
            case R.id.menu_launch:
                startActivity(getSettingsIntent(module.packageName,ModulesFragment.this));
                return true;
            case R.id.menu_app_info:
                startActivity(new Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", module.packageName, null)));
                return true;

            case R.id.menu_uninstall:
                startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.fromParts("package", module.packageName, null)));
                return true;
        }

        return false;
    }


    public static Intent getSettingsIntent(String packageName,Context context) {
        // taken from
        // ApplicationPackageManager.getLaunchIntentForPackage(String)
        // first looks for an Xposed-specific category, falls back to
        // getLaunchIntentForPackage
        PackageManager pm = context. getPackageManager();

        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(SETTINGS_CATEGORY);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = pm.queryIntentActivities(intentToResolve, 0);

        if (ris == null || ris.size() <= 0) {
            return pm.getLaunchIntentForPackage(packageName);
        }

        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }




}
