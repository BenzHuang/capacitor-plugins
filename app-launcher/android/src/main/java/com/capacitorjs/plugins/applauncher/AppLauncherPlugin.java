package com.capacitorjs.plugins.applauncher;

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

import java.io.File;
import java.util.HashMap;

@CapacitorPlugin(name = "AppLauncher")
public class AppLauncherPlugin extends Plugin {

	private static final HashMap<String, String> mimeTypeMapAsExtension = new HashMap<String, String>() {{
		//{后缀名，    文件类型}
		put(".3gp", "video/3gpp");
		put(".apk", "application/vnd.android.package-archive");
		put(".asf", "video/x-ms-asf");
		put(".avi", "video/x-msvideo");
		put(".bin", "application/octet-stream");
		//put(".bmp", "image/bmp");
		put(".c", "text/plain");
		put(".class", "application/octet-stream");
		put(".conf", "text/plain");
		put(".cpp", "text/plain");
		put(".doc", "application/msword");
		put(".exe", "application/octet-stream");

		put(".gtar", "application/x-gtar");
		put(".gz", "application/x-gzip");
		put(".h", "text/plain");
		put(".htm", "text/html");
		put(".html", "text/html");
		put(".jar", "application/java-archive");
		put(".java", "text/plain");
//		put(".gif", "image/gif");
//		put(".jpeg", "image/jpeg");
//		put(".jpg", "image/jpeg");
//		put(".png", "image/png");
		put(".js", "application/x-javascript");
		put(".log", "text/plain");
		put(".m3u", "audio/x-mpegurl");
		put(".m4a", "audio/mp4a-latm");
		put(".m4b", "audio/mp4a-latm");
		put(".m4p", "audio/mp4a-latm");
		put(".m4u", "video/vnd.mpegurl");
		put(".m4v", "video/x-m4v");
		put(".mov", "video/quicktime");
		put(".mp2", "audio/x-mpeg");
		put(".mp3", "audio/x-mpeg");
		put(".mp4", "video/mp4");
		put(".mpc", "application/vnd.mpohun.certificate");
		put(".mpe", "video/mpeg");
		put(".mpeg", "video/mpeg");
		put(".mpg", "video/mpeg");
		put(".mpg4", "video/mp4");
		put(".mpga", "audio/mpeg");
		put(".msg", "application/vnd.ms-outlook");
		put(".ogg", "audio/ogg");
		put(".pdf", "application/pdf");

		put(".pps", "application/vnd.ms-powerpoint");
		put(".ppt", "application/vnd.ms-powerpoint");
		put(".prop", "text/plain");
		put(".rar", "application/x-rar-compressed");
		put(".rc", "text/plain");
		put(".rmvb", "audio/x-pn-realaudio");
		put(".rtf", "application/rtf");
		put(".sh", "text/plain");
		put(".tar", "application/x-tar");
		put(".tgz", "application/x-compressed");
		put(".txt", "text/plain");
		put(".wav", "audio/x-wav");
		put(".wma", "audio/x-ms-wma");
		put(".wmv", "audio/x-ms-wmv");
		put(".wps", "application/vnd.ms-works");
		put(".xml", "text/plain");
		put(".z", "application/x-compress");
		put(".zip", "application/zip");
		put("", "*/*");
	}};

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
			pm.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
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
		//get fileName
		String fileName = call.getString("fileName");

		JSObject ret = new JSObject();

		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		//配置阅读模式
		//只读模式
		bundle.putString("OpenMode", "ReadOnly");
		//关闭文件时删除使用记录
		bundle.putBoolean("ClearTrace", true);

		intent.putExtras(bundle);

		//设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

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
