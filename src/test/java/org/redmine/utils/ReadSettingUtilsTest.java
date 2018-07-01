package org.redmine.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.redmine.data.data.RedimneUpdaterSetting;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReadSettingUtils.class})
public class ReadSettingUtilsTest {

	/**
	 * @caution: if you put stting.json below `\redmine_updater`, this test is
	 *           failure.
	 */
	@Test(expected = RuntimeException.class)
	public void testForException() {
		ReadSettingUtils.getRedmineUpdaterSetting();
	}

	/**
	 * useing testSetting.json <br>
	 * @caution: if you use latest jdk, this methos is failure. we will modify it by updating version of powermock.
	 */
	@Test
	public void testForReadingResourcesJson() {
		PowerMockito.mockStatic(ReadSettingUtils.class);
		PowerMockito.when(ReadSettingUtils.getSettingPath()).thenReturn("./src/main/resouces/testSetting.json");

		RedimneUpdaterSetting seting = ReadSettingUtils.getRedmineUpdaterSetting();
		assertThat(seting.getApiKey(), is("testKey"));
	}

}
