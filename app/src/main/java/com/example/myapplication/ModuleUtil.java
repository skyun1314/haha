package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jf.pxb.AXmlConverter;


public final class ModuleUtil {
    // xposedminversion below this
    private static final String BASE_DIR_LEGACY = "/data/data/com.example.myapplication/";

    public static final String BASE_DIR = Build.VERSION.SDK_INT >= 24
            ? "/data/user_de/0/com.example.myapplication/" : BASE_DIR_LEGACY;

    public static final String ENABLED_MODULES_LIST_FILE = BASE_DIR + "conf/enabled_modules.list";

    private static final String MODULES_LIST_FILE = BASE_DIR + "conf/modules.list";
    private static final String PLAY_STORE_PACKAGE = "com.android.vending";
    public static int MIN_MODULE_VERSION = 2; // reject modules with

    private final PackageManager mPm;
    private final String mFrameworkPackageName;
    private final List<ModuleListener> mListeners = new CopyOnWriteArrayList<ModuleListener>();
    private SharedPreferences mPref;
    private InstalledModule mFramework = null;
    private Map<String, InstalledModule> mInstalledModules;
    private boolean mIsReloading = false;
    private Toast mToast;


    Context context;

    public ModuleUtil(Context context, appType appType) {
        this.context = context;
        appType1 = appType;
        mPref = context.getSharedPreferences("enabled_modules", Context.MODE_PRIVATE);
        mPm = context.getPackageManager();
        mFrameworkPackageName = context.getPackageName();
    }


    public enum appType {
        xpmode, nomrapp, mypp
    }

    appType appType1;


    public static int extractIntPart(String str) {
        int result = 0, length = str.length();
        for (int offset = 0; offset < length; offset++) {
            char c = str.charAt(offset);
            if ('0' <= c && c <= '9')
                result = result * 10 + (c - '0');
            else
                break;
        }
        return result;
    }


    public void get_META_DATA_app() {
        Map<String, InstalledModule> modules = new HashMap<String, InstalledModule>();
        for (PackageInfo pkg : mPm.getInstalledPackages(PackageManager.GET_META_DATA)) {
            ApplicationInfo app = pkg.applicationInfo;
            if (!app.enabled)
                continue;
            InstalledModule installed = null;


            if (appType1 == appType.xpmode) {
                if (app.metaData != null && app.metaData.containsKey("xposedmodule")) {
                    installed = new InstalledModule(pkg, false);

                }
            } else if (appType1 == appType.mypp) {
                if (app.metaData != null && app.metaData.containsKey(AXmlConverter.meta_data_key)) {
                    installed = new InstalledModule(pkg, false);

                }
            }


            if (installed != null) {
                modules.put(pkg.packageName, installed);
            }
        }
        mInstalledModules = modules;

    }


    public void get_nomor_app() {
        Map<String, InstalledModule> modules = new HashMap<String, InstalledModule>();
        for (PackageInfo pkg : mPm.getInstalledPackages(0)) {
            ApplicationInfo app = pkg.applicationInfo;
            if (!app.enabled) {
                continue;
            }

            InstalledModule installed = null;


            if ((ApplicationInfo.FLAG_SYSTEM & app.flags) != 0) {
                continue;
            }
            if (app.metaData != null) {

                hahahaha.Log.e(" app.metaData:" + app.metaData.toString());
                if (app.metaData.containsKey(AXmlConverter.meta_data_key) || app.metaData.containsKey("xposedmodule")) {
                    continue;
                }

            }


            if (!app.packageName.equals(context.getPackageName())) {
                installed = new InstalledModule(pkg, false);

            }

            if (installed != null) {
                modules.put(pkg.packageName, installed);
            }
        }


        for (PackageInfo pkg : mPm.getInstalledPackages(PackageManager.GET_META_DATA)) {
            ApplicationInfo app = pkg.applicationInfo;
            if (!app.enabled)
                continue;


            if (app.metaData != null && (app.metaData.containsKey("xposedmodule") || app.metaData.containsKey(AXmlConverter.meta_data_key))) {
                modules.remove(pkg.packageName);

            }
        }


        mInstalledModules = modules;
    }


    public void reloadInstalledModules() {
      /*  synchronized (this) {
            if (mIsReloading)
                return;
            mIsReloading = true;
        }*/


        try {

            if (appType1 == appType.nomrapp) {
                get_nomor_app();
            } else {
                get_META_DATA_app();
            }

        } finally {

        }


      /*  synchronized (this) {
            mIsReloading = false;
        }*/

       /* for (ModuleListener listener : mListeners) {
            listener.onInstalledModulesReloaded(mInstance);
        }*/
    }


    public InstalledModule getFramework() {
        return mFramework;
    }

    public String getFrameworkPackageName() {
        return mFrameworkPackageName;
    }

    public boolean isFramework(String packageName) {
        return mFrameworkPackageName.equals(packageName);
    }

    public InstalledModule getModule(String packageName) {
        return mInstalledModules.get(packageName);
    }

    public Map<String, InstalledModule> getModules() {
        reloadInstalledModules();
        return mInstalledModules;
    }

    public void modulesClear() {
        mInstalledModules.clear();
    }

    public void setModuleEnabled(String packageName, boolean enabled) {
        if (enabled) {
            mPref.edit().putInt(packageName, 1).apply();
        } else {
            mPref.edit().remove(packageName).apply();
        }
    }

    public boolean isModuleEnabled(String packageName) {
        return mPref.contains(packageName);
    }

    public List<InstalledModule> getEnabledModules() {
        LinkedList<InstalledModule> result = new LinkedList<InstalledModule>();

        for (String packageName : mPref.getAll().keySet()) {
            InstalledModule module = getModule(packageName);
            if (module != null)
                result.add(module);
            else
                setModuleEnabled(packageName, false);
        }

        return result;
    }

    public synchronized void updateModulesList(Context showToast) {
        try {

            String[] strings = {MODULES_LIST_FILE, ENABLED_MODULES_LIST_FILE};

            for (int i = 0; i < strings.length; i++) {
                File file = new File(strings[i]);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
            }

            Cursor query = showToast.getContentResolver().query(StudentsProvider.CONTENT_URIstudents, null, null, null, null);


            if (query != null && query.getCount() > 0) {
                showToast.getContentResolver().delete(StudentsProvider.CONTENT_URIstudents, "_id>0", null);
            }


            PrintWriter modulesList = new PrintWriter(MODULES_LIST_FILE);
            PrintWriter enabledModulesList = new PrintWriter(ENABLED_MODULES_LIST_FILE);

            List<InstalledModule> enabledModules = getEnabledModules();
            for (InstalledModule module : enabledModules) {


                modulesList.println(module.app.sourceDir);
                try {
                    String installer = mPm.getInstallerPackageName(module.app.packageName);
                    if (!PLAY_STORE_PACKAGE.equals(installer)) {
                        enabledModulesList.println(module.app.packageName);


                        ContentValues values = new ContentValues();

                        values.put(StudentsProvider.NAME,
                                module.app.packageName);

                        values.put(StudentsProvider.GRADE,
                                module.app.sourceDir);


                        Uri uri = showToast.getContentResolver().insert(
                                StudentsProvider.CONTENT_URIstudents, values);


                    }
                } catch (IllegalArgumentException ignored) {
                    // In rare cases, the package might not be installed anymore at this point,
                    // so the PackageManager can't return its installer package name.
                }
            }
            modulesList.close();
            enabledModulesList.close();

            FileUtils.setPermissions(MODULES_LIST_FILE, 00664, -1, -1);
            FileUtils.setPermissions(ENABLED_MODULES_LIST_FILE, 00664, -1, -1);


        } catch (IOException e) {
            hahahaha.Log.e(e.toString());
        }
    }


    public void addListener(ModuleListener listener) {
        if (!mListeners.contains(listener))
            mListeners.add(listener);
    }

    public void removeListener(ModuleListener listener) {
        mListeners.remove(listener);
    }

    public interface ModuleListener {
        /**
         * Called whenever one (previously or now) installed module has been
         * reloaded
         */
        void onSingleInstalledModuleReloaded(ModuleUtil moduleUtil, String packageName, InstalledModule module);

        /**
         * Called whenever all installed modules have been reloaded
         */
        void onInstalledModulesReloaded(ModuleUtil moduleUtil);
    }

    public class InstalledModule {
        private static final int FLAG_FORWARD_LOCK = 1 << 29;
        public final String packageName;
        public final boolean isFramework;
        public final String versionName;
        public final int versionCode;
        public int minVersion = 0;
        public ApplicationInfo app;
        private String appName; // loaded lazyily
        private String description = ""; // loaded lazyily

        private Drawable.ConstantState iconCache = null;

        private InstalledModule(PackageInfo pkg, boolean isFramework) {
            this.app = pkg.applicationInfo;

            this.packageName = pkg.packageName;
            this.isFramework = isFramework;
            this.versionName = pkg.versionName;
            this.versionCode = pkg.versionCode;

            if (isFramework) {
                this.minVersion = 0;
                this.description = "";
            } else {

                if (appType1 == appType.xpmode) {
                    Object minVersionRaw = app.metaData.get("xposedminversion");
                    if (minVersionRaw instanceof Integer) {
                        this.minVersion = (Integer) minVersionRaw;
                    } else if (minVersionRaw instanceof String) {
                        this.minVersion = extractIntPart((String) minVersionRaw);
                    } else {
                        this.minVersion = 0;
                    }
                }


            }
        }

        public boolean isInstalledOnExternalStorage() {
            return (app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;
        }

        /**
         * @hide
         */
        public boolean isForwardLocked() {
            return (app.flags & FLAG_FORWARD_LOCK) != 0;
        }

        public String getAppName() {
            if (appName == null)
                appName = app.loadLabel(mPm).toString();
            return appName;
        }

        public String getDescription() {
            if (appType1 == appType.xpmode) {
                Object descriptionRaw = app.metaData.get("xposeddescription");
                String descriptionTmp = null;
                if (descriptionRaw instanceof String) {
                    descriptionTmp = ((String) descriptionRaw).trim();
                } else if (descriptionRaw instanceof Integer) {
                    try {
                        int resId = (Integer) descriptionRaw;
                        if (resId != 0)
                            descriptionTmp = mPm.getResourcesForApplication(app).getString(resId).trim();
                    } catch (Exception ignored) {
                    }
                }
                this.description = (descriptionTmp != null) ? descriptionTmp : "";
            }
            return this.description;
        }


        public Drawable getIcon() {
            if (iconCache != null)
                return iconCache.newDrawable();

            Intent mIntent = new Intent(Intent.ACTION_MAIN);
            mIntent.addCategory(ModulesFragment.SETTINGS_CATEGORY);
            mIntent.setPackage(app.packageName);
            List<ResolveInfo> ris = mPm.queryIntentActivities(mIntent, 0);

            Drawable result;
            if (ris == null || ris.size() <= 0)
                result = app.loadIcon(mPm);
            else
                result = ris.get(0).activityInfo.loadIcon(mPm);
            iconCache = result.getConstantState();

            return result;
        }

        @Override
        public String toString() {
            return getAppName();
        }
    }
}
