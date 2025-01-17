/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.bean;

import org.connectbot.transport.Local;
import org.connectbot.transport.SSH;
import org.connectbot.transport.Telnet;
import org.connectbot.transport.TransportFactory;
import org.connectbot.util.HostDatabase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

/**
 * @author Kenny Root
 *
 */
public class HostBean extends AbstractBean {

	public static final String BEAN_NAME = "host";
	public static final int DEFAULT_FONT_SIZE = 10;

	/* Database fields */
	private long id = -1;
	private String nickname = null;
	private String username = null;
	private String hostname = null;
	private int port = 22;
	private String protocol = "ssh";
	private long lastConnect = -1;
	private String color;
	private boolean useKeys = true;
	private String useAuthAgent = HostDatabase.AUTHAGENT_NO;
	private String postLogin = null;
	private long pubkeyId = HostDatabase.PUBKEYID_ANY;
	private boolean wantSession = true;
	private String delKey = HostDatabase.DELKEY_DEL;
	private int fontSize = DEFAULT_FONT_SIZE;
	private boolean compression = false;
	private String encoding = HostDatabase.ENCODING_DEFAULT;
	private boolean stayConnected = false;
	private boolean quickDisconnect = false;

	private String password = null;
	private int ulRows=0,ulCols=0,ulVirtCols=0,ulVirtRows=0;
	private String barcodeSuffix="", bcSsccF1="\\x1d", bcSsccPrefix="";
	private boolean autoConnect=false, clipAllow=true, bcAimPrefixes=false;
	private String emulation=null;
	public HostBean() {
	}

	@Override
	public String getBeanName() {
		return BEAN_NAME;
	}

	public HostBean(String nickname, String protocol, String username, String hostname, int port) {
		this.nickname = nickname;
		this.protocol = protocol;
		this.username = username;
		this.hostname = hostname;
		this.port = port;
	}

	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getNickname() {
		return nickname;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getHostname() {
		return hostname;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getPort() {
		return port;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setLastConnect(long lastConnect) {
		this.lastConnect = lastConnect;
	}
	public long getLastConnect() {
		return lastConnect;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getColor() {
		return color;
	}
	public void setUseKeys(boolean useKeys) {
		this.useKeys = useKeys;
	}
	public boolean getUseKeys() {
		return useKeys;
	}
	public void setUseAuthAgent(String useAuthAgent) {
		this.useAuthAgent = useAuthAgent;
	}
	public String getUseAuthAgent() {
		return useAuthAgent;
	}
	public void setPostLogin(String postLogin) {
		this.postLogin = postLogin;
	}
	public String getPostLogin() {
		return postLogin;
	}
	public void setPubkeyId(long pubkeyId) {
		this.pubkeyId = pubkeyId;
	}
	public long getPubkeyId() {
		return pubkeyId;
	}
	public void setWantSession(boolean wantSession) {
		this.wantSession = wantSession;
	}
	public boolean getWantSession() {
		return wantSession;
	}
	public void setDelKey(String delKey) {
		this.delKey = delKey;
	}
	public String getDelKey() {
		return delKey;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setCompression(boolean compression) {
		this.compression = compression;
	}
	public boolean getCompression() {
		return compression;
	}
	public String getPassword() {return password;}
	public void setPassword(String value) {this.password = value;}
	public void setVirtualArea(String value) {
		ulVirtCols=ulVirtRows=0;
		if (value==null || value.equals("") || value.equals("0x0")) return;
		try {
			String[] a = value.split("x");
			ulVirtCols = Integer.parseInt(a[0]);
			ulVirtRows = Integer.parseInt(a[1]);
		} catch (Exception e) {
			ulVirtCols=ulVirtRows=0;
		}
	}
	public void setCornerArea(String value) {
		ulCols=ulRows=0;
		if (value==null || value.equals("") || value.equals("0x0")) return;
		try {
			String[] a = value.split("x");
			ulCols = Integer.parseInt(a[0]);
			ulRows = Integer.parseInt(a[1]);
		} catch (Exception e) {
			ulCols=ulRows=0;
		}
	}
	public int[] getUlSize() {
		int[] rv=new int[4];
		rv[0] = ulCols; rv[1] = ulRows;
		rv[2] = ulVirtCols; rv[3] = ulVirtRows;
		return rv;
	}
	public void setAutoconnect(boolean value) {autoConnect = value;}
	public boolean getAutoconnect() { return autoConnect;}
	public void setClipAllow(boolean value) {clipAllow = value;}
	public boolean getClipAllow() { return clipAllow;}
	private String val2BcConfig(String s) {
		if (s==null) return "";
		return s.replaceAll(",","\\x2c").replaceAll(":","\\x3a");
	}
	private String bcCfg2val(String s) {
		if (s==null) return "";
		return s.replaceAll("\\x2c",",").replaceAll("\\x3a",":");
	}
	public void setBarcodeConfig(String value) {
		if (value!=null && !value.equals("")) {
			String[] a = value.split(",");
			for (String pair: a) {
				String[] kv = pair.split(":",2);
				if (kv.length==2)
					switch (kv[0]) {
					case "AimPrefix": bcAimPrefixes = kv[1].equals("1"); break;
					case "SsccPrefix": bcSsccPrefix = bcCfg2val(kv[1]); break;
					case "SsccF1": bcSsccF1 = bcCfg2val(kv[1]); break;
				}
			}
			getBarcodeConfig();
		}
	}
	public String getBarcodeConfig() {
		String barcodeConfig =  "AimPrefix:" + (bcAimPrefixes?"1":"0")
				+ ",SsccPrefix:" + val2BcConfig(bcSsccPrefix)
				+ ",SsccF1:" + val2BcConfig(bcSsccF1);
		return barcodeConfig;
	}
	public void setBarcodeSuffix(String value) {barcodeSuffix = value;}
	public String getBarcodeSuffix() { return barcodeSuffix;}
	public boolean getAimPrefixes() { return bcAimPrefixes;}
	public void setAimPrefixes(boolean enabled) {setBarcodeConfig("AimPrefix:" + (enabled?"1":"0"));}
	public String getBcSsccF1() {return bcSsccF1;}
	public void setBcSsccF1(String value) {setBarcodeConfig("SsccF1:" + val2BcConfig(value));}
	public String getBcSsccPrefix() {return bcSsccPrefix;}
	public void setBcSsccPrefix(String value) {setBarcodeConfig("SsccPrefix:" + val2BcConfig(value));}
	public void setEncoding(String encoding) {
		this.encoding  = encoding;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setStayConnected(boolean stayConnected) {
		this.stayConnected = stayConnected;
	}

	public boolean getStayConnected() {
		return stayConnected;
	}

	public void setQuickDisconnect(boolean quickDisconnect) {
		this.quickDisconnect = quickDisconnect;
	}

	public boolean getQuickDisconnect() {
		return quickDisconnect;
	}

	@SuppressLint("DefaultLocale")
	public String getDescription() {
		String description = String.format("%s@%s", username, hostname);

		if (port != 22)
			description += String.format(":%d", port);

		return description;
	}

	@Override
	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(HostDatabase.FIELD_HOST_NICKNAME, nickname);
		values.put(HostDatabase.FIELD_HOST_PROTOCOL, protocol);
		values.put(HostDatabase.FIELD_HOST_USERNAME, username);
		values.put(HostDatabase.FIELD_HOST_HOSTNAME, hostname);
		values.put(HostDatabase.FIELD_HOST_PORT, port);
		values.put(HostDatabase.FIELD_HOST_LASTCONNECT, lastConnect);
		values.put(HostDatabase.FIELD_HOST_COLOR, color);
		values.put(HostDatabase.FIELD_HOST_USEKEYS, Boolean.toString(useKeys));
		values.put(HostDatabase.FIELD_HOST_USEAUTHAGENT, useAuthAgent);
		values.put(HostDatabase.FIELD_HOST_POSTLOGIN, postLogin);
		values.put(HostDatabase.FIELD_HOST_PUBKEYID, pubkeyId);
		values.put(HostDatabase.FIELD_HOST_WANTSESSION, Boolean.toString(wantSession));
		values.put(HostDatabase.FIELD_HOST_DELKEY, delKey);
		values.put(HostDatabase.FIELD_HOST_FONTSIZE, fontSize);
		values.put(HostDatabase.FIELD_HOST_COMPRESSION, Boolean.toString(compression));
		values.put(HostDatabase.FIELD_HOST_ENCODING, encoding);
		values.put(HostDatabase.FIELD_HOST_STAYCONNECTED, Boolean.toString(stayConnected));
		values.put(HostDatabase.FIELD_HOST_QUICKDISCONNECT, Boolean.toString(quickDisconnect));

		values.put(HostDatabase.FIELD_HOST_PASSWORD, password);
		values.put(HostDatabase.FIELD_HOST_AUTOCONNECT,Boolean.toString(autoConnect));
		values.put(HostDatabase.FIELD_HOST_BARCODE_SUFFIX,barcodeSuffix);
		values.put(HostDatabase.FIELD_HOST_BARCODE_CONFIG,getBarcodeConfig());
		values.put(HostDatabase.FIELD_HOST_CORNER,ulCols==0 && ulRows==0 ? "" : ("" + ulCols + "x" + ulRows));
		values.put(HostDatabase.FIELD_HOST_VIRTUAL,ulVirtCols==0 && ulVirtRows==0 ? "" : ("" + ulVirtCols + "x" + ulVirtRows));
		values.put(HostDatabase.FIELD_HOST_ALLOWCLIP,Boolean.toString(clipAllow));
		values.put(HostDatabase.FIELD_HOST_EMULATION,emulation);
		return values;
	}

	public static HostBean fromContentValues(ContentValues values) {
		HostBean host = new HostBean();
		try {host.setNickname(values.getAsString(HostDatabase.FIELD_HOST_NICKNAME));} catch (Exception e) {}
		try {host.setProtocol(values.getAsString(HostDatabase.FIELD_HOST_PROTOCOL));} catch (Exception e) {}
		try {host.setUsername(values.getAsString(HostDatabase.FIELD_HOST_USERNAME));} catch (Exception e) {}
		try {host.setHostname(values.getAsString(HostDatabase.FIELD_HOST_HOSTNAME));} catch (Exception e) {}
		try {host.setPort(values.getAsInteger(HostDatabase.FIELD_HOST_PORT));} catch (Exception e) {}
		try {host.setLastConnect(values.getAsLong(HostDatabase.FIELD_HOST_LASTCONNECT));} catch (Exception e) {}
		try {host.setColor(values.getAsString(HostDatabase.FIELD_HOST_COLOR));} catch (Exception e) {}
		try {host.setUseKeys(Boolean.valueOf(values.getAsString(HostDatabase.FIELD_HOST_USEKEYS)));} catch (Exception e) {}
		try {host.setUseAuthAgent(values.getAsString(HostDatabase.FIELD_HOST_USEAUTHAGENT));} catch (Exception e) {}
		try {host.setPostLogin(values.getAsString(HostDatabase.FIELD_HOST_POSTLOGIN));} catch (Exception e) {}
		try {host.setPubkeyId(values.getAsLong(HostDatabase.FIELD_HOST_PUBKEYID));} catch (Exception e) {}
		try {host.setWantSession(Boolean.valueOf(values.getAsString(HostDatabase.FIELD_HOST_WANTSESSION)));} catch (Exception e) {}
		try {host.setDelKey(values.getAsString(HostDatabase.FIELD_HOST_DELKEY));} catch (Exception e) {}
		try {host.setFontSize(values.getAsInteger(HostDatabase.FIELD_HOST_FONTSIZE));} catch (Exception e) {}
		try {host.setCompression(Boolean.valueOf(values.getAsString(HostDatabase.FIELD_HOST_COMPRESSION)));} catch (Exception e) {}
		try {host.setEncoding(values.getAsString(HostDatabase.FIELD_HOST_ENCODING));} catch (Exception e) {}
		try {host.setStayConnected(values.getAsBoolean(HostDatabase.FIELD_HOST_STAYCONNECTED));} catch (Exception e) {}
		try {host.setQuickDisconnect(values.getAsBoolean(HostDatabase.FIELD_HOST_QUICKDISCONNECT));} catch (Exception e) {}

		try {host.setPassword(values.getAsString(HostDatabase.FIELD_HOST_PASSWORD));} catch (Exception e) {}
		try {host.setVirtualArea(values.getAsString(HostDatabase.FIELD_HOST_VIRTUAL));} catch (Exception e) {}
		try {host.setCornerArea(values.getAsString(HostDatabase.FIELD_HOST_CORNER));} catch (Exception e) {}
		try {host.setAutoconnect(values.getAsBoolean(HostDatabase.FIELD_HOST_AUTOCONNECT));} catch (Exception e) {}
		try {host.setBarcodeConfig(values.getAsString(HostDatabase.FIELD_HOST_BARCODE_CONFIG));} catch (Exception e) {}
		try {host.setBarcodeSuffix(values.getAsString(HostDatabase.FIELD_HOST_BARCODE_SUFFIX));} catch (Exception e) {}
		try {host.setClipAllow(values.getAsBoolean(HostDatabase.FIELD_HOST_ALLOWCLIP));} catch (Exception e) {}
		try {host.setEmulation(values.getAsString(HostDatabase.FIELD_HOST_EMULATION));} catch (Exception e) {}
		return host;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof HostBean))
			return false;

		HostBean host = (HostBean) o;

		if (id != -1 && host.getId() != -1)
			return host.getId() == id;

		if (nickname == null) {
			if (host.getNickname() != null)
				return false;
		} else if (!nickname.equals(host.getNickname()))
			return false;

		if (protocol == null) {
			if (host.getProtocol() != null)
				return false;
		} else if (!protocol.equals(host.getProtocol()))
			return false;

		if (username == null) {
			if (host.getUsername() != null)
				return false;
		} else if (!username.equals(host.getUsername()))
			return false;

		if (hostname == null) {
			if (host.getHostname() != null)
				return false;
		} else if (!hostname.equals(host.getHostname()))
			return false;

		return port == host.getPort();
	}

	@Override
	public int hashCode() {
		int hash = 7;

		if (id != -1)
			return (int) id;

		hash = 31 * hash + (null == nickname ? 0 : nickname.hashCode());
		hash = 31 * hash + (null == protocol ? 0 : protocol.hashCode());
		hash = 31 * hash + (null == username ? 0 : username.hashCode());
		hash = 31 * hash + (null == hostname ? 0 : hostname.hashCode());
		hash = 31 * hash + port;

		return hash;
	}

	/**
	 * @return URI identifying this HostBean
	 */
	public Uri getUri() {
		StringBuilder sb = new StringBuilder();
		sb.append(protocol)
			.append("://");

		if (username != null)
			sb.append(Uri.encode(username))
				.append('@');

		sb.append(Uri.encode(hostname))
			.append(':')
			.append(port)
			.append("/#")
			.append(nickname);
		return Uri.parse(sb.toString());
	}

	/**
	 * Generates a "pretty" string to be used in the quick-connect host edit view.
	 */
	@Override
	public String toString() {
		if (protocol == null)
			return "";

		int defaultPort = TransportFactory.getTransport(protocol).getDefaultPort();

		if (SSH.getProtocolName().equals(protocol)) {
			if (username == null || hostname == null ||
					username.equals("") || hostname.equals(""))
				return "";

			if (port == defaultPort)
				return username + "@" + hostname;
			else
				return username + "@" + hostname + ":" + port;
		} else if (Telnet.getProtocolName().equals(protocol)) {
			if (hostname == null || hostname.equals(""))
				return "";
			else if (port == defaultPort)
				return hostname;
			else
				return hostname + ":" + port;
		} else if (Local.getProtocolName().equals(protocol)) {
			return nickname;
		}

		// Fail gracefully.
		return "";
	}

	public String getEmulation() {return emulation;	}
	public void setEmulation(String value) {emulation = value;}
}
