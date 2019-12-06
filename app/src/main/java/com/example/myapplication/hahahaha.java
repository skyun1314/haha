package com.example.myapplication;


import org.jf.android.dex.Dex;
import org.jf.android.dx.command.dexer.DxContext;
import org.jf.android.dx.merge.CollisionPolicy;
import org.jf.android.dx.merge.DexMerger;

import org.jf.baksmali.DexInputCommand;
import org.jf.net.fornwall.apksigner.ApkSignatureHelper;
import org.jf.net.fornwall.apksigner.Main;
import org.jf.pxb.AXmlConverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import static com.example.myapplication.StrUtil.modifyFileContent;

public class hahahaha {


    public static String addMethod(String yuan_apkpath, String libsandhook_dir) throws  Exception {

        if (yuan_apkpath == null || yuan_apkpath.equals("")) {
            Log.e("yuan_apkpath:+目录为空");
            return null;
        }



        String originalSignature = ApkSignatureHelper.getApkSignInfo(yuan_apkpath);
        if (originalSignature == null || originalSignature.isEmpty()) {
            Log.e(" Get original signature failed !!!!");
            return null;
        }





        String tempdir = "/sdcard/360/multi/multi/";


        String apkpath = tempdir + "com.multi.multi.apk";
        String outpath = tempdir + "未命名文件夹/";
        runCMD("rm -rf  " + apkpath);
        runCMD("rm -rf  " + outpath);

        new File(outpath + "assets").mkdirs();



        copyFileFromJar(yuan_apkpath, apkpath);


        runCMD("unzip " + apkpath + " classes*dex -o  -d  " + outpath);


        String[] list = new File(outpath).list();

        Log.e(Arrays.toString(list));
        String new_dex = "classes" + (list.length) + ".dex";


        copyFileFromJar(libsandhook_dir + "/classes.dex", outpath + new_dex);


        runCMD("unzip " + apkpath + " AndroidManifest.xml -o  -d  " + outpath);
     //   runCMD("unzip " + apkpath + " META-INF/*.RSA -o  -d  " + outpath + "assets");



        //String ras=outpath + "assets/META-INF/CERT.RSA";
        //new File(outpath + "assets/META-INF").listFiles()[0].renameTo(new File(ras));

         String SIGNATURE_INFO_ASSET_PATH =outpath+ "assets/xpatch_asset/original_signature_info.ini";


        // Then, save the signature chars to the asset file
        File file = new File(SIGNATURE_INFO_ASSET_PATH);
        File fileParent = file.getParentFile();
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }

        FileUtils.writeFile(SIGNATURE_INFO_ASSET_PATH, originalSignature);



        runCMD("unzip " + apkpath + " lib/* -o  -d  " + outpath);
        copySoFile(libsandhook_dir, outpath);

        runCMD("zip -d " + apkpath + " classes*dex");
        runCMD("zip -d " + apkpath + " AndroidManifest.xml");
        runCMD("zip -d " + apkpath + " META-INF/*.RSA ");
        runCMD("zip -d " + apkpath + " META-INF/*.SF ");
        runCMD("zip -d " + apkpath + " META-INF/*.MF ");
        runCMD("zip -d " + apkpath + " lib/* ");


        Log.e("解压完成");


        String cha = AXmlConverter.convert(outpath + "AndroidManifest.xml", outpath + "A.xml");


        new File(outpath + "AndroidManifest.xml").delete();
        new File(outpath + "A.xml").renameTo(new File(outpath + "AndroidManifest.xml"));


        Log.e("application——name：" + cha);


        if (cha != null && !cha.equals("")) {

            cha = cha.replace(".", "/") + ";";
            Log.e("查到 application：" + cha);
            String application_dex = application_dex(outpath, cha);
            Log.e("application_dex:" + application_dex);

            String outdir = application_dex.substring(0, application_dex.length() - 4);

            boolean ishanClinit = ishanClinit(application_dex, cha, outdir);

            Log.e("ishanClinit:" + ishanClinit);


            String smaliFile = outdir + "/" + cha.substring(0, cha.length() - 1) + ".smali";

            Log.e("smaliFile:" + smaliFile);

                //直接添加一个 <clinit>函数
                modifyFileContent(smaliFile, !ishanClinit,application_dex,outdir);


            String temp_dex=outpath+"haha.dex";
            org.jf.smali.Main.main(new String[]{"a", outdir, "-o",temp_dex});



          //  String temp_dex2=outpath+"haha1.dex";


            Dex[] dexes = {new Dex(new File(temp_dex  )), new Dex(new File(application_dex))};
            DexMerger dexMerger=new DexMerger(dexes, CollisionPolicy.KEEP_FIRST,new DxContext());
            dexMerger.merge().writeTo(new File(application_dex));

            runCMD("rm -rf  " + outdir);
            runCMD("rm -rf  " + temp_dex);
           // runCMD("rm -rf  " + application_dex);
          //  new File(temp_dex2).renameTo(new File(application_dex));
            Log.e("dex_ok");
        }


          file = new File(outpath);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            runCMD(new String[]{"zip -r " + apkpath + " " + files[i].getName()}, outpath);
        }


        String argList[] = {libsandhook_dir + "/hahahaha", apkpath, outpath + "sinde.apk", "hahahaha"};

            Main.main(argList);

/*        new File(outpath + "META-INF").mkdirs();
        copyFileFromJar(ras, outpath+"/META-INF/CERT.RSA");

        runCMD(new String[]{"zip -r " +  outpath + "sinde.apk" + " " + "/META-INF/CERT.RSA"}, outpath);*/

        Log.e("炒作完成");
        return outpath + "sinde.apk";
    }


    public static String application_dex(String outpath, String classname) {
        File[] files = new File(outpath).listFiles();
        for (int i = 0; i < files.length; i++) {

            if (!files[i].getName().endsWith(".dex")) {
                continue;
            }
            List<String> aClass = DexInputCommand.getClass(files[i].getAbsolutePath());

            if (aClass.toString().contains(classname)) {
                Log.e("查到了------");
                return files[i].getAbsolutePath();
            } else {
                Log.e("查不到" + files[i].getAbsolutePath() + "    " + classname);
            }
        }
        return "";
    }


    public static boolean ishanClinit(String application_dex, String classname, String outdir) {

        if (application_dex == null || application_dex.equals("")) {
            return false;
        }


        org.jf.baksmali.Main.main(new String[]{"d","--classes","L"+classname, application_dex, "-o", outdir});


        List<String> methods = DexInputCommand.getMethods(application_dex);
        Log.e("methos:" + classname + "-><clinit>()V");
        if (methods.toString().contains(classname + "-><clinit>()V")) {
            return true;
        }
        return false;
    }


    public static void runCMD(String cmd) {
        try {
            Log.e("runCMD:" + cmd);
            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                Log.e("runCMD:" + line);
            }

            if (br == null) {
                br.close();
            }

        } catch (IOException e) {
            Log.e("runCMD 执行出错了" + e.toString());
            e.printStackTrace();
        }
    }


    public static void runCMD(String cmd[], String file) {
        try {


            for (int i = 0; i < cmd.length; i++) {
                Log.e("runCMD1:" + cmd[i]);
                Process process = Runtime.getRuntime().exec(cmd[i], null, new File(file));
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    Log.e(line);
                }

                if (br == null) {
                    br.close();
                    ;
                }
                process.waitFor();
            }


        } catch (Exception e) {
            Log.e("runCMD1:" + e.toString());
            e.printStackTrace();
        }
    }




    public static final String[] APK_LIB_PATH_ARRAY = {
            "lib/armeabi-v7a/",
            "lib/armeabi/",
            "lib/arm64-v8a/"
    };
    public static final String SO_FILE_NAME = "libsandhook.so";
    public static final HashMap<String, String> SO_FILE_PATH_MAP = new HashMap<String, String>() {
        {
            put(APK_LIB_PATH_ARRAY[0], "/lib/armeabi-v7a/" + SO_FILE_NAME);
            put(APK_LIB_PATH_ARRAY[1], "/lib/armeabi-v7a/" + SO_FILE_NAME);
            put(APK_LIB_PATH_ARRAY[2], "/lib/arm64-v8a/" + SO_FILE_NAME);
        }
    };


    public static void copySoFile(String libsandhook_dir, String filepath) {
        for (String libPath : APK_LIB_PATH_ARRAY) {
            String apkSoFullPath = filepath + libPath;
            if (new File(apkSoFullPath).exists()) {//看看目标 lib 目录有没有这个架构
                copyFileFromJar(libsandhook_dir + SO_FILE_PATH_MAP.get(libPath), apkSoFullPath + SO_FILE_NAME);
            }
        }
    }

    public static void copyFileFromJar(String inJarPath, String distPath) {

        InputStream inputStream;

        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            inputStream = new FileInputStream(inJarPath);
            in = new BufferedInputStream(inputStream);
            out = new BufferedOutputStream(new FileOutputStream(distPath));

            int len = -1;
            byte[] b = new byte[1024];
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private static boolean enableLog = BuildConfig.DEBUG;

    public static class Log{
        public static void e(String str){

            if(enableLog){
                android.util.Log.e("wodelog",str);
            }


        }
    }





}
