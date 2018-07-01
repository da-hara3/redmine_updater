package org.redmine.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.redmine.utils.RedMineAPIUtils;

import com.opencsv.CSVReader;

/**
 * 既存のチケットの一括更新用クラス。<br>
 * 複数選択でも処理が大変なような内容に対して処理を行うことを目的としている。
 *
 * @author nemuu3
 *
 */
public class UpdaterServiceImpl implements IRedmineService {
	private static final String BASE_URL = "http://hue-redmine/redmine/issues/";
	private static final String CSV_INDEX_KEY = "id";

	private static final String[] ISSUE_ITEMS = new String[] { "subject",
			"description", "start_date", "due_date", "done_ratio",
			"estimated_hours", "spent_hours", "id", "project", "tracker",
			"status", "priority", "author", "assigned_to", "parent" };

	/**
	 * TODO: CUSTOM_FIELDはRedmineごとに異なる構成になるので、この作りはダメ。
	 */
	private static final Map<String, Integer> CUSTOM_FIELD_ID;
	static {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("Version", Integer.valueOf(8));
		CUSTOM_FIELD_ID = Collections.unmodifiableMap(map);
	}

	@Override
	public void execute(File csvFile) {
		Map<String, String> updateMap = parseCsv(csvFile);
		updateMap.entrySet().forEach(
				entry -> RedMineAPIUtils.putForXml(BASE_URL +  entry.getKey(), entry.getValue()));
	}

	private Map<String, String> parseCsv(File csvFile) {
		Map<String, String> relationMap = new HashMap<String, String>();
		try (FileReader reader = new FileReader(csvFile);
				CSVReader csvReader = new CSVReader(reader);) {

			boolean hdFlg = true;
			Map<String, Integer> hdLineMap = null;
			for (String[] line : csvReader.readAll()) {
				if (hdFlg) {
					hdLineMap = createHDMap(line);
					hdFlg = false;

					continue;
				}
				relationMap.put(line[hdLineMap.get(CSV_INDEX_KEY)],
						createUpdateXml(hdLineMap, line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return relationMap;
	}

	private static Map<String, Integer> createHDMap(String[] hdLine) {
		Map<String, Integer> hdLineMap = new HashMap<>();
		for (int i = 0; i < hdLine.length; i++) {
			hdLineMap.put(hdLine[i], Integer.valueOf(i));
		}
		if (!hdLineMap.containsKey(CSV_INDEX_KEY)) {
			throw new RuntimeException();
		}
		return hdLineMap;
	}

	/**
	 * TODO カスタムフィールド対応
	 *
	 * @return
	 */
	private String createUpdateXml(Map<String, Integer> hdLineMap, String[] line) {
		StringBuilder updateBuilder = new StringBuilder();
		StringBuilder customFieldsBuilder = new StringBuilder();
		updateBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		updateBuilder.append("<issue>\r\n");
		for (Entry<String, Integer> entry : hdLineMap.entrySet()) {
			String itemKey = entry.getKey();
			int lineHdIndex = entry.getValue().intValue();

			if (entry.getKey().equals("id")) {
				continue;
			}
			if (isIssueField(itemKey)) {
				updateBuilder.append(String.format("<%s>%s</%s>\r\n", itemKey,
						line[lineHdIndex], itemKey));
			} else {
				if (!CUSTOM_FIELD_ID.containsKey(itemKey)) {
					continue;
				}
				if (customFieldsBuilder.length() == 0) {
					customFieldsBuilder
							.append("<custom_fields type=\"array\">");
				}
				customFieldsBuilder
						.append(String.format("<custom_field id=\"%s\"><value>%s</value></custom_field>\r\n",
										CUSTOM_FIELD_ID.get(itemKey),
										line[lineHdIndex]));
			}
		}
		if (customFieldsBuilder.length() != 0) {
			customFieldsBuilder.append("</custom_fields>");
			updateBuilder.append(customFieldsBuilder.toString());
		}
		updateBuilder.append("</issue>");
		return updateBuilder.toString();
	}

	private static boolean isIssueField(String itemKey) {
		return Stream.of(ISSUE_ITEMS).anyMatch(item-> item.equals(itemKey));
	}
}