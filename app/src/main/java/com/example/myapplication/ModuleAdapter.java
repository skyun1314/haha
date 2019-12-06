package com.example.myapplication;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ModuleAdapter extends ArrayAdapter<ModuleUtil.InstalledModule> {
    Context context;
    ModuleUtil.appType appType;
    private int installedXposedVersion;
    ModuleUtil mModuleUtil;
    public ModuleAdapter(Context context, ModuleUtil.appType appType, ModuleUtil mModuleUtil) {

        super(context, R.layout.list_item_module, R.id.title);
        this.mModuleUtil=mModuleUtil;
        this.appType=appType;
        this.context=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View   view = super.getView(position, convertView, parent);

        if (convertView == null) {
            // The reusable view was created for the first time, set up the
            // listener on the checkbox
            ((CheckBox) view.findViewById(R.id.checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String packageName = (String) buttonView.getTag();
                    boolean changed = mModuleUtil.isModuleEnabled(packageName) ^ isChecked;
                    if (changed) {
                        mModuleUtil.setModuleEnabled(packageName, isChecked);
                        mModuleUtil.updateModulesList(context);
                    }
                }
            });
        }

        ModuleUtil.InstalledModule item = getItem(position);

        TextView version = (TextView) view.findViewById(R.id.version_name);
        version.setText(item.versionName);

        // Store the package name in some views' tag for later access
        view.findViewById(R.id.checkbox).setTag(item.packageName);
        view.setTag(item.packageName);

        ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());

        TextView descriptionText = (TextView) view.findViewById(R.id.description);
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        if(appType== ModuleUtil.appType.xpmode){
            if (!item.getDescription().isEmpty()) {
                descriptionText.setText(item.getDescription());
                descriptionText.setTextColor(ThemeUtil.getThemeColor(getContext(), android.R.attr.textColorSecondary));
            } else {
                descriptionText.setText("(未提供描述)");
                descriptionText.setTextColor(context.getResources().getColor(R.color.warning));
            }


            checkbox.setChecked(mModuleUtil.isModuleEnabled(item.packageName));
            TextView warningText = (TextView) view.findViewById(R.id.warning);

            if (item.minVersion == 0) {
                checkbox.setEnabled(false);
                warningText.setText("该模块未指定需要的 Xposed 版本。");
                warningText.setVisibility(View.VISIBLE);
            } else if (installedXposedVersion != 0 && item.minVersion > installedXposedVersion) {
                checkbox.setEnabled(false);
                warningText.setText(String.format("该模块需要 Xposed 新版（%d），因此无法激活。", item.minVersion));
                warningText.setVisibility(View.VISIBLE);
            }   else {
                checkbox.setEnabled(true);
                warningText.setVisibility(View.GONE);
            }
        }else{
            descriptionText.setText(item.packageName);
            checkbox.setVisibility(View.GONE);
        }


        return view;
    }


    public static final class ThemeUtil {


        public static int getThemeColor(Context context, int id) {
            Resources.Theme theme = context.getTheme();
            TypedArray a = theme.obtainStyledAttributes(new int[] { id });
            int result = a.getColor(0, 0);
            a.recycle();
            return result;
        }
    }

}