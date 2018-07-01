package org.redmine.utils;

import java.io.File;
import java.io.IOException;

import org.redmine.data.data.RedimneUpdaterSetting;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ReadSettingUtils {

	private static final String SETTING_PATH = "./setting.json";

	/**
	 * it difined as method for writing junit test.
	 * @return
	 */
	static String getSettingPath() {
		return SETTING_PATH;
	}

	/**
	 * Setting.jsonは同階層に配置されていることを前提とする。
	 *
	 * @return
	 */
	public static RedimneUpdaterSetting getRedmineUpdaterSetting() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			RedimneUpdaterSetting setting = mapper.readValue(new File(getSettingPath()), RedimneUpdaterSetting.class);
			return setting;
		} catch (IOException e) {
			throw new RuntimeException("設定データ(setting.xml)が配置されていない。もしくは不正です。");
		}
	}
}
