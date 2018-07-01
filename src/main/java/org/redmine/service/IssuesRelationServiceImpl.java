package org.redmine.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redmine.utils.ReadSettingUtils;
import org.redmine.utils.RedMineAPIUtils;

import com.opencsv.CSVReader;

/**
 * 特定のチケット郡を一気に特定のサポートチケットに紐づかせたい場合に活用する処理。<br>
 * 次のようなCSVを処理可能。<br>
 * -----------------------<br>
 * id, supportId <br>
 * 1, 11 <br>
 * 1, 12 <br>
 * 1, 13 <br>
 * -----------------------<br>
 * この場合、ID = 1のチケットに11, 12, 13が関連付けれる。
 * @author nemuu3
 *
 */
public class IssuesRelationServiceImpl implements IRedmineService {

	private static final int CSV_INDEX_MAIN_ID = 0;
	private static final int CSV_INDEX_RELATION_ID = 1;


	private static final String BASE_URL = "http://hue-redmine/redmine/issues/%s/relations.xml?format=xml&key=";

	private static final StringBuilder BASE_XML = new StringBuilder()
			.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
			.append("<relation>")
			.append("<relation_type>relates</relation_type>")
			.append("<issue_to_id>%s</issue_to_id>").append("<delay/>")
			.append("</relation>");

	private final String baseUrlWithApiKey;

	public IssuesRelationServiceImpl() {
		baseUrlWithApiKey = BASE_URL + ReadSettingUtils.getRedmineUpdaterSetting().getApiKey();
	}


	public void execute(File csvFile) {
		Map<String, List<String>> relationIdMap = parseCsv(csvFile);
		try {
			relationIdMap.entrySet().forEach(entity -> {
				try {
					doUpdate(entity.getKey(), entity.getValue());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("完了");
	}

	/**
	 * keyにID, valueに関連するチケットのList。という形でCSVを分解する。
	 *
	 * @param csvFile
	 * @return
	 */
	private Map<String, List<String>> parseCsv(File csvFile) {
		Map<String, List<String>> relationMap = new HashMap<String, List<String>>();
		try (FileReader reader = new FileReader(csvFile);
				CSVReader csvReader = new CSVReader(reader);) {
			for (String[] record : csvReader.readAll()) {
				addRecord(relationMap, record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return relationMap;
	}

	private void addRecord(Map<String, List<String>> relationMap,
			String[] record) {

		if (relationMap.containsKey(record[CSV_INDEX_MAIN_ID])) {
			relationMap.get(record[CSV_INDEX_MAIN_ID]).add(record[CSV_INDEX_RELATION_ID]);
		} else {
			List<String> list = new ArrayList<String>();
			list.add(record[CSV_INDEX_RELATION_ID]);
			relationMap.put(record[CSV_INDEX_MAIN_ID], list);
		}
	}

	private void doUpdate(String targetId, List<String> relatedIdList)
			throws IOException {
		System.out.println(String.format(baseUrlWithApiKey, targetId));
		for (String relatedId : relatedIdList) {
			RedMineAPIUtils.postForXml(String.format(baseUrlWithApiKey, targetId),
					String.format(BASE_XML.toString(), relatedId));
		}
	}
}
