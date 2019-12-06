package org.jf.pxb;


import com.example.myapplication.hahahaha;

import org.jf.pxb.android.axml.AxmlReader;
import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.AxmlWriter;
import org.jf.pxb.android.axml.NodeVisitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class AXmlConverter {
    static boolean has_Application = false;
    static String Application_name = "com.wind.xposed.entry.XposedApplication";
    public static String meta_data_key = "com.hahah.haha.haha";
    static String meta_data_value = "com.hahah.haha.haha";




    static  String yuan_packageName;
    static  String yuan_Application;
    public static String convert(String a, String b) throws IOException {
        InputStream is = new FileInputStream(a);
        byte[] xml = new byte[is.available()];
        is.read(xml);
        is.close();



        AxmlReader rd = new AxmlReader((byte[]) xml);
        AxmlWriter wr = new AxmlWriter();
        rd.accept(new AxmlVisitor(wr) {
            @Override
            public NodeVisitor visitFirst(String ns, String name) {
                // manifest
                return new NodeVisitor(super.visitFirst(ns, name)) {


                    // 替换APP包名
                    @Override
                    public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {// manifest 这一行的数据


                        if ("package".equals(name)) {
                            hahahaha.Log.e("visitContentAttr：" + name + "  " + obj + "  ");
                            yuan_packageName = (String) obj;

                        }
                        super.visitContentAttr(ns, name, resourceId, type, obj);
                    }

                    // 修改app名称
                    @Override
                    public NodeVisitor visitChild(String ns, String name) {// manifest 下面的说有大标签

                        if ("application".equals(name)) {


                            return new NodeVisitor(super.visitChild(ns, name)) {


                                @Override
                                public void visitEnd() {
                                    NodeVisitor meta = super.visitChild("http://schemas.android.com/apk/res/android", "meta-data");
                                    meta.visitContentAttr("http://schemas.android.com/apk/res/android", "name", 16842755, 3, meta_data_key);
                                    meta.visitContentAttr("http://schemas.android.com/apk/res/android", "value", 16842788, 3, meta_data_value);
                                    if (!has_Application) {
                                        super.visitContentAttr("http://schemas.android.com/apk/res/android", "name", 16842755, 3, Application_name);
                                    }
                                    super.visitEnd();
                                }


                                @Override
                                public void visitContentAttr(String ns, String name, int resourceId, int type,//application 这一行
                                                             Object obj) {


                                    if ("name".equals(name)) {
                                        yuan_Application=obj.toString();
                                        if(obj.toString().startsWith(".")){
                                            yuan_Application=yuan_packageName+yuan_Application;
                                        }

                                        has_Application = true;
                                        hahahaha.Log.e("visitContentAttr:" + name + "  " + obj + "   " + ns + "   " + resourceId + "  " + type);
                                    }
                                    super.visitContentAttr(ns, name, resourceId, type, obj);
                                }

                                @Override
                                public NodeVisitor visitChild(String ns, String name) {//application 下面所有大标签


                                    return new NodeVisitor(super.visitChild(ns, name)) {


                                        @Override
                                        public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                            //     System.out.println("aaaa_visitChild:"+ns+"  "+name+"  "+resourceId+"  "+type+"  "+obj);
                                            super.visitContentAttr(ns, name, resourceId, type, obj);
                                        }

                                        @Override
                                        public NodeVisitor visitChild(String ns, String name) {
                                            // System.out.println("xxxx_visitChild:"+ns+"  "+name);
                                            return super.visitChild(ns, name);
                                        }
                                    };
                                }
                            };


                        }
                        return super.visitChild(ns, name);
                    }
                };
            }
        });
        byte[] modified = wr.toByteArray();
        FileOutputStream fos = new FileOutputStream(b);
        fos.write(modified);
        fos.close();
        return yuan_Application;
    }
}