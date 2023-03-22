package cz.madeta;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import android.util.Log;

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
			String x = getSysProp("sys.hsm.provisioning");
			if (getSysProp("ro.hsm.model.num") == null)
				_isProvisioning = true; // No Honeywell - Provisioning always
			else
				_isProvisioning = (x != null && x.equals("true"));
		}
		return _isProvisioning;
	}
}
