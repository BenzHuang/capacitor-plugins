package com.capacitorjs.plugins.applauncher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.util.InternalUtils;

import java.io.File;

@CapacitorPlugin(name = "AppLauncher")
public class AppLauncherPlugin extends Plugin {

    @PluginMethod
    public void canOpenUrl(PluginCall call) {
        String url = call.getString("url");
        if (url == null) {
            call.reject("Must supply a url");
            return;
        }

        Context ctx = this.getActivity().getApplicationContext();
        final PackageManager pm = ctx.getPackageManager();

        JSObject ret = new JSObject();
        try {
            InternalUtils.getPackageInfo(pm, url, PackageManager.GET_ACTIVITIES);
            ret.put("value", true);
            call.resolve(ret);
            return;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.error(getLogTag(), "Package name '" + url + "' not found!", null);
        }

        ret.put("value", false);
        call.resolve(ret);
    }

    @PluginMethod
    public void openUrl(PluginCall call) {
        String url = call.getString("url");
        if (url == null) {
            call.reject("Must provide a url to open");
            return;
        }

        JSObject ret = new JSObject();
        final PackageManager manager = getContext().getPackageManager();
        Intent launchIntent = new Intent(Intent.ACTION_VIEW);
        launchIntent.setData(Uri.parse(url));

        try {
            getActivity().startActivity(launchIntent);
            ret.put("completed", true);
        } catch (Exception ex) {
            launchIntent = manager.getLaunchIntentForPackage(url);
            try {
                getActivity().startActivity(launchIntent);
                ret.put("completed", true);
            } catch (Exception expgk) {
                ret.put("completed", false);
            }
        }
        call.resolve(ret);
    }

    @PluginMethod
    public void openAppWithFile(PluginCall call) {
        // stop wps
        ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final PackageManager manager = getContext().getPackageManager();
        try {
            //查看后台运行的app，wps的在打开pdf、word情况下只找出报名cn.wps.moffice_eng
			/*List localList = manager.getInstalledPackages(0);
			for (int i = 0; i < localList.size(); i++) {
				PackageInfo localPackageInfo1 = (PackageInfo) localList.get(i);
				String str1 = localPackageInfo1.packageName.split(":")[0];
				if (((ApplicationInfo.FLAG_SYSTEM & localPackageInfo1.applicationInfo.flags) == 0)
					&& ((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP & localPackageInfo1.applicationInfo.flags) == 0)
					&& ((ApplicationInfo.FLAG_STOPPED & localPackageInfo1.applicationInfo.flags) == 0)) {
					Logger.info("packages:" + str1);
				}
			}*/

            //关闭wps进程
            activityManager.killBackgroundProcesses("cn.wps.moffice_eng");

            //查看正在允许app，只找到当前app
			/*List<RunningAppProcessInfo> appProcessInfo = activityManager.getRunningAppProcesses();
			for (int i = 0; i < appProcessInfo.size(); i++) {
				Logger.info("packages:" + String.join(", ", appProcessInfo.get(i).pkgList));
				Logger.info("processName:" + appProcessInfo.get(i).processName);

			}*/
            Logger.info("kill wps success!");
        } catch (Exception e) {
            Logger.error(getLogTag(), "fail to kill wps!, message:" + e.getMessage(), e);
        }

        //get fileName
        String fileName = call.getString("fileName");

        JSObject ret = new JSObject();

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        //配置阅读模式
        //只读模式
        bundle.putString("OpenMode", "ReadOnly");
        bundle.putString("ThirdPackage", getContext().getPackageName());
        //关闭文件时是否发送广播,默认不发送
        bundle.putBoolean("SendCloseBroad", true);
        //Back 按钮
        bundle.putBoolean("BackKeyDown", true);
        //Home 按钮
        bundle.putBoolean("HomeKeyDown", true);
        //删除使用记录
        bundle.putBoolean("ClearTrace", true);
        bundle.putBoolean("IsClearTrace", true);
        //删除文件自身
        bundle.putBoolean("ClearFile", true);
        //清除缓冲区
        bundle.putBoolean("ClearBuffer", true);
        bundle.putBoolean("DisplayView", false);
        intent.putExtras(bundle);

        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //清除之前已经存在的Activity实例所在的task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        if (fileName != null) {
            //todo get type
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }
            Logger.info("extension:'" + extension);

            String type = mimeTypeMapAsExtension.get(extension);
            Logger.info("type:'" + type);

            if (type != null) {
                //用wps打开
                intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2");
                Logger.info("open by wps, classname:'cn.wps.moffice_eng'");
            }

            //读取文件
            File file = new File(fileName);
            if (file.exists()) {
                Logger.info("fileName:'" + fileName + "' found!");
                Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
                //Uri uri = Uri.fromFile(file);
                Logger.info("uri:'" + uri.toString());
                //intent.setData(uri);

                if (type != null) {
                    intent.setDataAndType(uri, type);
                } else {
                    intent.setData(uri);
                }
            } else {
                Logger.error(getLogTag(), "fileName:'" + fileName + "' not found!", null);
            }
        }

        try {
            getActivity().startActivity(intent);
            ret.put("completed", true);
        } catch (Exception ex) {
            Logger.error(getLogTag(), "open file error, message:" + ex.getMessage(), ex);
        }
        call.resolve(ret);
    }
}
