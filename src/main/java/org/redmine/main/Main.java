package org.redmine.main;

import java.io.File;

import org.redmine.service.IRedmineService;
import org.redmine.service.IssuesRelationServiceImpl;
import org.redmine.service.TaskConfirmServiceImpl;
import org.redmine.service.UpdaterServiceImpl;

/**
 * Execute Class
 */
public class Main {
	private static final int ARGS_IDX_EXECUTE_TYPE = 0;
	private static final int ARGS_IDX_INPUT_FILE_PATH = 1;

	private static final String RELATION = "1";
	private static final String UPDATE = "2";
	private static final String TASK = "3";

	public static void main(String[] args) {
		String filePath = args[ARGS_IDX_INPUT_FILE_PATH];
		File file = new File(filePath);
		if (file.exists()) {
			IRedmineService service = null;
			switch (args[ARGS_IDX_EXECUTE_TYPE]) {
			case RELATION:
				service = new IssuesRelationServiceImpl();
				break;
			case UPDATE:
				service = new UpdaterServiceImpl();
				break;
			case TASK:
				service = new TaskConfirmServiceImpl();
				break;
			default:
				throw new IllegalArgumentException("実施する処理が正しく指定されていません。");
			}
			if (service != null) {
				service.execute(file);
			}
		} else {
			System.err.println("正しい取込ファイルを渡してください。");
		}
	}
}
