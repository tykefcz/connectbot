package cz.madeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.connectbot.bean.HostBean;
import org.connectbot.util.HostDatabase;
import org.xmlpull.v1.XmlSerializer;

import com.google.android.gms.common.util.SharedPreferencesUtils;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Xml;

public class HonUtils {
	private static final String tag = "HonUtils";
	// sys.hsm.provisioning]: [true
	public static String getSysProp(String propName) {
		String retVal = null;

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			retVal = (String) get.invoke(c, propName);
		} catch (Exception e) {
			Log.d(tag,"getSysProp ex",e);
		}
		return retVal;
	}
	private static boolean _isProvisioning;
	private static long lastProvCheck = 0;
	public static boolean isProvisioningMode() {
		if (lastProvCheck == 0 || (System.currentTimeMillis() - lastProvCheck) > 1000) {
			String x = getSysProp("sys.hsm.provisioning"),
					y = getSysProp("ro.hsm.model.num");
			if (y == null || y.equals(""))
				_isProvisioning = true; // No Honeywell - Provisioning always
			else
				_isProvisioning = (x != null && x.equals("true"));
		}
		return _isProvisioning;
	}

	// use Environment.getExternalStorageDirectory().getPath()
	public static void exportSettingsAndHosts(Context ctx, String filename) {
		File newxmlfile = new File(filename);
		FileOutputStream fileos = null;
		try{
			fileos = new FileOutputStream(newxmlfile);

		} catch(FileNotFoundException e) {
			Log.e(tag,"FileNotFoundException ('" + filename + "'):" + e.toString());
			return;
		}
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(fileos, "UTF-8");
			serializer.startDocument(null, Boolean.valueOf(true));
			serializer.startTag(null, "droidssh");
			serializer.startTag(null, "settings");
			SharedPreferences shp = ctx.getSharedPreferences(ctx.getPackageName(),Context.MODE_PRIVATE);
			Log.d(tag,"SharedPrefs from " + ctx.getPackageName() + " count:" + (shp==null?"nil":String.valueOf(shp.getAll().size())));
			Map<String,?> shpall = shp.getAll();
			shpall.forEach((key,val) -> {try {
				serializer.startTag(null, key)
						.attribute(null,"type",val.getClass().getSimpleName())
						.text(String.valueOf(val))
						.endTag(null, key);
			} catch (Exception e) {}});
			serializer.endTag(null,"settings");
			serializer.startTag(null, "hosts");
			HostDatabase db = HostDatabase.get(ctx);
			List<HostBean> hosts = db.getHosts(false);
			for (HostBean h : hosts) {
				ContentValues vals = h.getValues();
				serializer.startTag(null, "host");
				for (String k : vals.keySet()) {
					Object ov = vals.get(k);
					if (ov!=null) {
						serializer.startTag(null, k)
								.text(vals.getAsString(k))
								.endTag(null, k);
					} else {
						serializer.startTag(null, k)
								.text("nil")
								.endTag(null, k);
					}
				}
				serializer.endTag(null, "host");
			}
			serializer.endTag(null, "hosts");
			serializer.endTag(null,"droidssh");
			serializer.endDocument();
			serializer.flush();
			fileos.close();
		} catch(Exception e) {
			Log.e(tag,"Exception occured export ",e);
		}
	}
}
