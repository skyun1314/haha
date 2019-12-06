package com.example.myapplication;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.dom4j.Document;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;


import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static com.example.myapplication.hahahaha.addMethod;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener{

    // Used to load the 'native-lib' library on application startup.
    static {
//        System.loadLibrary("native-lib");
    }

public static Activity activity;

    private int getDp(float value) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }

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
    ModuleUtil   mModuleUtil;
    ModuleAdapter mAdapter;
    ListView viewById;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity=this;

        refreshInstallStatus();

        new Thread(new Runnable() {
            @Override
            public void run() {
                copyFilesFromAssets(MainActivity.this,"libs",getApplicationInfo().dataDir+"/libs");

            }
        }).start();




          viewById = findViewById(R.id.zhuansheng);

        viewById.setDivider(null);
        viewById.setDividerHeight(getDp(6));
        viewById.setPadding(getDp(8), getDp(8), getDp(8), getDp(8));

        viewById.setClipToPadding(false);

        mModuleUtil  = new  ModuleUtil(this, ModuleUtil.appType.mypp);

        mAdapter = new ModuleAdapter(this, ModuleUtil.appType.mypp, null);



        viewById.setOnItemClickListener(this);


        registerForContextMenu(viewById);
        reloadModules.run();

        viewById.setAdapter(mAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();
        reloadModules.run();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    public static void copyFilesFromAssets(Context context, String assetsPath, String savePath) {
        try {
            String fileNames[] = context.getAssets().list(assetsPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(savePath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, assetsPath + "/" + fileName,
                            savePath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(assetsPath);
                FileOutputStream fos = new FileOutputStream(new File(savePath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
            hahahaha.Log.e("复制完成");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static byte[] toByteArray(Context context,String assetsPath) throws IOException {
        InputStream input =  context.getAssets().open(assetsPath);


        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }



    private void refreshInstallStatus() {

        TextView txtInstallError = (TextView) findViewById(R.id.framework_install_errors);
        View txtInstallContainer = findViewById(R.id.status_container);
        ImageView txtInstallIcon = (ImageView) findViewById(R.id.status_icon);



            txtInstallError.setTextColor(getResources().getColor(R.color.colorPrimary));
            txtInstallContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            txtInstallIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle));


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, ModulesFragment.class);
        switch (item.getItemId()) {

            case R.id.mokuai:

                intent.putExtra("apptype",ModuleUtil.appType.xpmode);
                intent.putExtra("appname","模块管理");
               // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;

            case R.id.createxp:
                 intent = new Intent(this, ModulesFragment.class);
                intent.putExtra("appname","创建应用");
                intent.putExtra("apptype",ModuleUtil.appType.nomrapp);
                // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                break;

            case R.id.about:
                abort(this);
                break;


        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_installer, menu);
        return true;
    }


    public static   void zhuansheng(ModuleUtil.InstalledModule Module ,Activity activity){
        Module1=Module;
        activity.finish();

        Context context=MainActivity.activity;
        ProgressDialog progressdialog = new ProgressDialog(context);
        progressdialog.setTitle("This is ProgressDialog");
        progressdialog .setMessage("魔力转圈圈");
        progressdialog.setCancelable(false);
        progressdialog.show();





            new Thread(new Runnable() {
                @Override
                public void run() {
                    String app = null;
                    try {
                        app = addMethod(Module.app.sourceDir, context.getApplicationInfo().dataDir + "/libs");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressdialog.dismiss();
                    if(app!=null&&new File(app).exists()){
                        app_nam=app;
                        handler.sendEmptyMessage(1);
                    }else{
                        handler.sendEmptyMessage(2);
                    }
                }
            }).start();




    }

    static Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case 1:
                    showTwo(Module1,activity);
                    break;
                case 2:
                    showTwo2(Module1,activity);
                    break;
            }


            return false;
        }
    });


    private static void showTwo(ModuleUtil.InstalledModule Module,Context context) {

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context). setMessage("注意，如果要正常试用应用转身功能，您需要卸载"+" '"+Module .getAppName()+"'原版，是否确定  ").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情

                        MainActivity.showTwo1(context);

                        context.   startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.fromParts("package", Module.packageName, null)));

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //ToDo: 你想做的事情
                showTwo1(context);
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }





    private static void abort( Context context) {

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context).setTitle("关于"). setMessage("本软件是基于WindySha 大佬的 Xpatch 项目制作而成" +
                "\n\n Xpatch 项目链接为：https://github.com/WindySha/Xpatch\n\n本项目为练手玩具，可能有非常多的问题，请谨慎试用").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情

                        dialogInterface.dismiss();

                    }
                });
        builder.create().show();
    }




    private static void showTwo2(ModuleUtil.InstalledModule Module,Context context) {

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context). setMessage("转身失败 ").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情

                        dialogInterface.dismiss();

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

    static ModuleUtil.InstalledModule Module1;
    static String app_nam;

    public static void showTwo1(Context context) {

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context). setMessage(" 转身完成是否安装 ").
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //ToDo: 你想做的事情
                        install(app_nam);
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


    public static void install(String file_dir){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        File file = new File(file_dir);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        activity. startActivity(intent);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        String packageName = (String) v.getTag();
        if (packageName == null)
            return;



        ModuleUtil.InstalledModule installedModule = (position >= 0) ? (ModuleUtil.InstalledModule) mAdapter.getItem(position) : null;


        Intent launchIntent =ModulesFragment. getSettingsIntent(packageName,activity);
        if (launchIntent != null)
            startActivity(launchIntent);

    }


    private ModuleUtil.InstalledModule getItemFromContextMenuInfo(ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int position = info.position - viewById.getHeaderViewsCount();
        return (position >= 0) ? (ModuleUtil.InstalledModule) mAdapter.getItem(position) : null;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
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
                startActivity(ModulesFragment.getSettingsIntent(module.packageName,activity));
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




}
