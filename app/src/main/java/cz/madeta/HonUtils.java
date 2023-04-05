package cz.madeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.connectbot.HostListActivity;
import org.connectbot.bean.HostBean;
import org.connectbot.bean.PubkeyBean;
import org.connectbot.util.HostDatabase;
import org.connectbot.util.PubkeyDatabase;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import com.google.android.gms.common.util.SharedPreferencesUtils;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

public class HonUtils {
	private static final String tag = "HonUtils";
	// sys.hsm.provisioning]: [true
	private static int counter = 0;
	public static int getCounter() {return counter;	}
	public static void incCounter() {counter++;}
	public static void setCounter(int value) {counter = value;}
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
			SharedPreferences shp = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences",Context.MODE_PRIVATE);
			Log.d(tag,"SharedPrefs from " + ctx.getPackageName() + " count:" + (shp==null?"nil":String.valueOf(shp.getAll().size())));
			Map<String,?> shpall = shp.getAll();
			shpall.forEach((key,val) -> {
				if (!HostListActivity.IMPEXPSETTINGS.equals(key)) {
					try {
						serializer.startTag(null, key)
								.attribute(null, "type", val.getClass().getSimpleName())
								.text(String.valueOf(val))
								.endTag(null, key);
					} catch (Exception e) {
					}
				}
			});
			serializer.endTag(null,"settings");
			serializer.startTag(null, "hosts");
			HostDatabase db = HostDatabase.get(ctx);
			List<HostBean> hosts = db.getHosts(false);
			for (HostBean h : hosts) {
				ContentValues vals = h.getValues();
				serializer.startTag(null, "host")
						.startTag(null, "id")
						.text("" + h.getId())
						.endTag(null, "id");
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
				List<String> hkalgs = db.getHostKeyAlgorithmsForHost(h.getHostname(),h.getPort());
				if (hkalgs!=null && hkalgs.size() > 0) {
					serializer.startTag(null,"keyalgos")
							.text(String.join(",",hkalgs))
							.endTag(null, "keyalgos");
				}
				serializer.endTag(null, "host");
			}
			serializer.endTag(null, "hosts");
			PubkeyDatabase pkdb = PubkeyDatabase.get(ctx);
			List<PubkeyBean> pks = null;
			if (pkdb!=null) pks = pkdb.allPubkeys();
			serializer.startTag(null, "pubkeys");
			if (pks!=null) {
				for (PubkeyBean h : pks) {
					ContentValues vals = h.getValues();
					serializer.startTag(null, "key")
					        .startTag(null, "id")
							.text("" + h.getId())
							.endTag(null, "id");
					for (String k : vals.keySet()) {
						Object ov = vals.get(k);
						if (ov != null) {
							if (ov.getClass().isArray()) {
								serializer.startTag(null, k)
										.attribute(null,"type","ByteArray")
										.text(android.util.Base64.encodeToString(vals.getAsByteArray(k), Base64.NO_WRAP))
										.endTag(null, k);;
							} else {
								serializer.startTag(null, k)
										.text(vals.getAsString(k))
										.endTag(null, k);
							}
						} else {
							serializer.startTag(null, k)
									.text("nil")
									.endTag(null, k);
						}
					}
					serializer.endTag(null, "key");
				}
			}
			serializer.endTag(null, "pubkeys");
			serializer.endTag(null,"droidssh");
			serializer.endDocument();
			serializer.flush();
			fileos.close();
		} catch(Exception e) {
			Log.e(tag,"Exception occured export ",e);
		}
	}
	public static void importSettingsAndHosts(Context ctx, String filename) {
		File xmlfile = new File(filename);
		if (!xmlfile.canRead()) return;
		FileInputStream fileos = null;
		try{
			fileos = new FileInputStream(xmlfile);
		} catch(FileNotFoundException e) {
			Log.e(tag,"FileNotFoundException ('" + filename + "'):" + e.toString());
			return;
		}
		try {
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(fileos,"UTF-8");
			int eventType = xpp.getEventType();
			String inTag[] = new String[20];
			String typ = "";
			String pa = "";
			ContentValues ctv = null;
			HostDatabase hdb = HostDatabase.get(ctx);
			long maxhid = -1;
			PubkeyDatabase pkdb = PubkeyDatabase.get(ctx);
			SharedPreferences shp = ctx.getSharedPreferences(ctx.getPackageName() + "_preferences",Context.MODE_PRIVATE);
			SharedPreferences.Editor shpe = shp.edit();
			for (int i = 0 ; i < inTag.length; i++) inTag[i] = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					inTag[xpp.getDepth()] = xpp.getName();
					typ=xpp.getAttributeValue(null,"type");
					if (typ==null) typ = "";
					pa = ""; for(int i = 2 ; i <= xpp.getDepth(); i++) pa = pa + "/" + inTag[i];
					if (pa.equals("/hosts/host") || pa.equals("/pubkeys/key")) {
						ctv = new ContentValues();
					}
				} else if(eventType == XmlPullParser.END_TAG) {
					inTag[xpp.getDepth()] = "";
					typ = "";
					if (pa.equals("/hosts/host") && ctv!=null && ctv.size() > 3) {
						HostBean h = HostBean.fromContentValues(ctv);
						try {long id = Long.parseLong(ctv.getAsString("id")); h.setId(id);} catch (Exception e) {h.setId(++maxhid);};
						if (maxhid < h.getId()) maxhid=h.getId();
						hdb.saveHost(h);
						Log.d(tag,"Import saveHost " + h.getId() + " : " + h);
					} else if (pa.equals("/pubkeys/key") && ctv!=null && ctv.size() > 2) {
						PubkeyBean pb = PubkeyBean.fromContentValues(ctv);
						try {long id = Long.parseLong(ctv.getAsString("id")); pb.setId(id);} catch (Exception e) {};
						pkdb.savePubkey(pb);
						Log.d(tag,"Import savePubkey " + pb.getId() + " : " + pb);
					}
					if (xpp.getDepth() == 3) ctv = null;
					pa = ""; for(int i = 2 ; i < xpp.getDepth(); i++) pa = pa + "/" + inTag[i];
				} else if(eventType == XmlPullParser.TEXT) {
					String v = xpp.getText();
					if (v==null || "nil".equals(v)) v="";
					if ((pa.startsWith("/hosts/host/") || pa.startsWith("/pubkeys/key/")) && ctv != null) {
						if ("ByteArray".equals(typ))
							ctv.put(inTag[xpp.getDepth()], Base64.decode(xpp.getText(), Base64.NO_WRAP));
						else
							ctv.put(inTag[xpp.getDepth()], v);
					} else if (pa.startsWith("/settings") && xpp.getDepth() == 3 && !typ.equals("")) {
						if (typ.equals("Boolean"))
							try {shpe.putBoolean(inTag[3],Boolean.valueOf(v));} catch (Exception e) {}
						else if (typ.equals("String"))
							shpe.putString(inTag[3],v);
						else if (typ.equals("Integer"))
							try {shpe.putInt(inTag[3],Integer.valueOf(v));} catch (Exception e) {}
					} else {
						Log.d(tag, "XML:" + pa + " = '" + v + "' (" + typ + ")");
					}
				}
				eventType = xpp.next();
			}
			shpe.apply();
			fileos.close();
		} catch(Exception e) {
			Log.e(tag,"Exception occured import ",e);
		}
	}
}
