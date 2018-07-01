package org.redmine.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.opencsv.CSVWriter;

public class TaskConfirmServiceImpl implements IRedmineService {

	@Override
	public void execute(File csvFile) {

		// queryの発行
		List<String> ticketList = getTicketList();

		// 各チケットのループ
		List<String[]> displayList = new ArrayList<>();
		ticketList.stream().forEach(
				ticket -> displayList.add(doReadParent(ticket)));

		// CSVの結果表示
		File outputFile = new File("output.csv");
		try (FileWriter writer = new FileWriter(outputFile);
				CSVWriter csvWriter = new CSVWriter(writer);) {
			csvWriter.writeAll(displayList);
			csvWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> getTicketList() {
		System.out.println("start: getTicketList");
		
		List<String> ticektList = new ArrayList<>();
		try {
			URL url = new URL(
					"http://hue-redmine/redmine/projects/x-asset/issues.json?query_id=4903&limit=5&key=5f1da9cfa909ad3b52444aafdb363c08366c731b");
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestMethod("GET");
			urlConn.setInstanceFollowRedirects(false);
			urlConn.setDoOutput(true);
			urlConn.setRequestProperty("content-type",
					"application/xml;charset=utf-8");

			urlConn.connect();
			try (InputStreamReader reader = new InputStreamReader(
					urlConn.getInputStream());
					BufferedReader br = new BufferedReader(reader);) {
				String str = br.readLine();
				while (str != null) {
					JSONObject issuseObjct = new JSONObject(str);
					JSONArray issusesArray = issuseObjct.getJSONArray("issues");
					Iterator<Object> itr = issusesArray.iterator();
					while (itr.hasNext()) {
						JSONObject jsObject = (JSONObject) itr.next();
						ticektList.add(String.valueOf(jsObject.getLong("id")));
					}
					str = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end: getTicketList");
		return ticektList;

	}

	private String[] doReadParent(String ticketId) {
		System.out.println("start: doReadParent: " + ticketId);
		List<String> displayList = new ArrayList<>();
		try {
			URL url = new URL(
					"http://hue-redmine/redmine/issues/"
							+ ticketId
							+ ".json?format=xml&include=children&key=5f1da9cfa909ad3b52444aafdb363c08366c731b");
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			urlConn.setRequestMethod("GET");
			urlConn.setInstanceFollowRedirects(false);
			urlConn.setDoOutput(true);
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
					displayList.add(String.valueOf(issuseObjct.getLong("id")));
					displayList.add(issuseObjct.getString("subject"));
					
					if (issuseObjct.isNull("children")){
						str = br.readLine();
						continue;
					}

					JSONArray array = issuseObjct.getJSONArray("children");
					Iterator<Object> childrenItr = array.iterator();
					while (childrenItr.hasNext()) {
						JSONObject jsObject = (JSONObject) childrenItr.next();
						// if (jsObject.getString("subject")
						// .contains("[man-hour]")) {
						displayList.addAll(doReadChild(String.valueOf(jsObject
								.getLong("id"))));
						// }
					}
					str = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end: doReadParent: " + ticketId);
		return displayList.toArray(new String[0]);
	}

	private List<String> doReadChild(String ticketId) {
		System.out.println("start: doReadChild: " + ticketId);
		List<String> childList = new ArrayList<>();
		try {
			URL url = new URL(
					"http://hue-redmine/redmine/issues/"
							+ ticketId
							+ ".json?format=xml&include=children&key=5f1da9cfa909ad3b52444aafdb363c08366c731b");
			HttpURLConnection urlConn = (HttpURLConnection) url
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
					childList.add(String.valueOf(issuseObjct.getLong("id")));
					childList.add(issuseObjct.getString("subject"));
					childList.add(issuseObjct.getJSONObject("assigned_to")
							.getString("name"));
					str = br.readLine();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end: doReadChild: " + ticketId);
		return childList;
	}
}