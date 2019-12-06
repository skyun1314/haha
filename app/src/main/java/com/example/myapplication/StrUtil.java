package com.example.myapplication;


import org.jf.baksmali.DexInputCommand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {








    public static void main(String[] args) throws Exception {








    }


    public static int page = 0;

    public static void func(String input,boolean insert) {

       /* File file = new File(input);

        File[] fs = file.listFiles();


        if(fs==null||fs.length==0){
            modifyFileContent(file.getAbsolutePath(), insert);
        }


        for (File f : fs) {
            if (f.isDirectory()) {    //若是目录，则递归打印该目录下的文件
                func(f.getAbsolutePath(),insert);
            } else if (f.isFile()) { //若是文件，直接打印

                page++;
               // System.out.println("第" + page + "个文件 ： ：" + f);
                // readFile(f);
                modifyFileContent(f.getAbsolutePath(), insert);
            }

        }*/
    }




    public static boolean real_applacation=false;


    public static boolean modifyFileContent(String fileName, boolean insert,String application_dex,String outdir) {
        String fileNametmep = fileName.split(outdir)[1];
        String classname = "L" + fileNametmep.substring(1, fileNametmep.length() - 6) + ";";

        String new_file = fileName.substring(0, fileName.length() - 6) + "new_.smali";


        try {
            FileWriter fileWriter = new FileWriter(new_file);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");


            BufferedReader raf = new BufferedReader(inputStreamReader);

            String line = null;


            int xiugai=0;


            while ((line = raf.readLine()) != null) {


                bufferedWriter.write(line);


                if(line.contains(".super ")){
                    String[] split = line.split(".super ");
                    if(!split[1].equals("Landroid/app/Application;")){

                        org.jf.baksmali.Main.main(new String[]{"d","--classes",split[1], application_dex, "-o", outdir});



                        new File(new_file).delete();
                        new File(fileName).delete();

                        String smaliFile = outdir + "/" + split[1].substring(1, split[1].length() - 1) + ".smali";
                        modifyFileContent(smaliFile,insert,application_dex,outdir);
                        return true;

                    }else{
                        real_applacation=true;
                        List<String> methods = DexInputCommand.getMethods(application_dex);
                        hahahaha.Log.e("methos:" + classname + "-><clinit>()V");
                        if (methods.toString().contains(classname + "-><clinit>()V")) {
                            insert=false;
                        }else{
                            insert=true;
                        }

                    }
                }else if(!real_applacation){
                    continue;
                }


             //   System.out.println(line);
             //   bufferedWriter.write(line);
                if (line.contains(".end method")&&insert&&xiugai==0) {
                    xiugai=1;
//直接插入
                    hahahaha.Log.e("直接插入 <clinit> 函数 ");

                    bufferedWriter.newLine();
                  /*  bufferedWriter.write(".method static constructor <clinit>()V\n" +
                            "    .registers 0\n" +
                            "\n" +
                            "    .line 11\n" +
                            "    invoke-static {}, Lcom/wind/xposed/entry/XposedModuleEntry;->init()V\n" +
                            "\n" +
                            "    .line 12\n" +
                            "    return-void\n" +
                            ".end method");
*/


                    bufferedWriter.write(   "# direct methods\n" +
                            ".method static constructor <clinit>()V\n" +
                            "    .registers 4\n" +
                            "\n" +
                            "    :try_start_0\n" +
                            "    const-string/jumbo v0, \"com.wind.xposed.entry.XposedModuleEntry\"\n" +
                            "\n" +
                            "    invoke-static {v0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;\n" +
                            "\n" +
                            "    move-result-object v0\n" +
                            "\n" +
                            "    const-string/jumbo v1, \"init\"\n" +
                            "\n" +
                            "    const v2, 0x0\n" +
                            "\n" +
                            "    new-array v3, v2, [Ljava/lang/Class;\n" +
                            "\n" +
                            "    invoke-virtual {v0, v1, v3}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;\n" +
                            "\n" +
                            "    move-result-object v0\n" +
                            "\n" +
                            "    const v1, 0x0\n" +
                            "\n" +
                            "    new-array v2, v2, [Ljava/lang/Class;\n" +
                            "\n" +
                            "    invoke-virtual {v0, v1, v2}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;\n" +
                            "    :try_end_1b\n" +
                            "    .catch Ljava/lang/Throwable; {:try_start_0 .. :try_end_1b} :catch_1d\n" +
                            "\n" +
                            "    goto/16 :goto_1e\n" +
                            "\n" +
                            "    :catch_1d\n" +
                            "    move-exception v0\n" +
                            "\n" +
                            "    :goto_1e\n" +
                            "    return-void\n" +
                            ".end method");


                    bufferedWriter.newLine();

                }else if(line.contains("constructor <clinit>()V")&&!insert){
                    line = raf.readLine();
                    bufferedWriter.newLine();
                    bufferedWriter.write(".registers 15");
                    bufferedWriter.newLine();
                    hahahaha.Log.e("直接插入 <clinit> 函数 ");

                    bufferedWriter.write("    :try_start_cle_\n" +
                            "    const-string/jumbo v0, \"com.wind.xposed.entry.XposedModuleEntry\"\n" +
                            "\n" +
                            "    invoke-static {v0}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;\n" +
                            "\n" +
                            "    move-result-object v0\n" +
                            "\n" +
                            "    const-string/jumbo v1, \"init\"\n" +
                            "\n" +
                            "    const v2, 0x0\n" +
                            "\n" +
                            "    new-array v3, v2, [Ljava/lang/Class;\n" +
                            "\n" +
                            "    invoke-virtual {v0, v1, v3}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;\n" +
                            "\n" +
                            "    move-result-object v0\n" +
                            "\n" +
                            "    const v1, 0x0\n" +
                            "\n" +
                            "    new-array v2, v2, [Ljava/lang/Class;\n" +
                            "\n" +
                            "    invoke-virtual {v0, v1, v2}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;\n" +
                            "    :try_end_cle_\n" +
                            "    .catch Ljava/lang/Throwable; {:try_start_cle_ .. :try_end_cle_} :catch_cle_\n" +
                            "\n" +
                            "    goto/16 :goto_cle_\n" +
                            "\n" +
                            "    :catch_cle_\n" +
                            "    move-exception v0\n" +
                            "\n" +
                            "    :goto_cle_");
                    bufferedWriter.newLine();



                }

                bufferedWriter.newLine();
            }

            raf.close();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new File(fileName).delete();

        return true;
    }











}
