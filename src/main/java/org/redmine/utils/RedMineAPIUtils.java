package org.redmine.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

public class RedMineAPIUtils {

	private static final String URLAPI = ".xml?format=xml&key=";

	public static void putForXml(String baseUrl, String updateXmlData) {
		doUpdate("PUT", baseUrl, updateXmlData, ReadSettingUtils.getRedmineUpdaterSetting().getApiKey());
	}

	public static void postForXml(String baseUrl, String updateXmlData) {
		doUpdate("POST", baseUrl, updateXmlData, ReadSettingUtils.getRedmineUpdaterSetting().getApiKey());
	}

	public static void doUpdate(String httpOperation, String baseUrl, String updateXmlData, String apiKey) {
		URL url = null;
		HttpURLConnection urlConn = null;
		try {
			url = new URL(baseUrl + URLAPI + apiKey);
			urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestMethod(httpOperation);
			urlConn.setInstanceFollowRedirects(false);
			urlConn.setDoOutput(true);
			urlConn.setRequestProperty("content-type",
					"application/xml;charset=utf-8");
			urlConn.connect();
			try (OutputStreamWriter updateXmlWriter = new OutputStreamWriter(
					urlConn.getOutputStream(), "UTF-8");
					BufferedWriter updateXmlReader = new BufferedWriter(
							updateXmlWriter);) {
				System.out.println(updateXmlData);
				updateXmlWriter.write(updateXmlData);
				updateXmlWriter.flush();
				System.out.println(url);
				System.out.println(urlConn.getResponseCode());
				System.out.println(urlConn.getResponseMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (urlConn != null) {
				urlConn.disconnect();
			}
		}
	}

	public static void doRead(String ticketId) {
		URL url = null;
		HttpURLConnection urlConn = null;
		try {
			url = new URL(
					"http://hue-redmine/redmine/issues/"
							+ ticketId
							+ ".json?format=xml&include=children&key="
							+ ReadSettingUtils.getRedmineUpdaterSetting().getApiKey());
			urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestMethod("GET");
			urlConn.setInstanceFollowRedirects(false);
			urlConn.setDoOutput(true);
			// urlConn.setRequestProperty("Accept-Language",
			// "ja,en-US;q=0.8,en;q=0.6");
			urlConn.setRequestProperty("content-type",
					"application/xml;charset=utf-8");

			urlConn.connect();
			try (InputStreamReader reader = new InputStreamReader(
					urlConn.getInputStream());
					BufferedReader br = new BufferedReader(reader);) {
				String str = br.readLine();
				while (str != null) {
					JSONObject issuseObjct = new JSONObject(str);
					issuseObjct = issuseObjct.getJSONObject("issue");
					JSONArray array = issuseObjct.getJSONArray("children");
					for (int i = 0; i < array.length(); i++) {
						JSONObject jsObject = array.getJSONObject(i);
						if (jsObject.getString("subject").contains(
								"IVTL_BasicDesign Send")) {
							System.out.println(jsObject.get("id") + " : "
									+ jsObject.getString("subject"));
						}
					}
					str = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (urlConn != null) {
				urlConn.disconnect();
			}
		}

	}

}
