package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

public class Utility {

	/* JDK path (java) */
	public static final String JAVA_PATH = "JAVA";

	/* Model checker inputs */
	public static final String MODEL_TEMPLATE_FILE = "MODEL_TEMPLATE_FILE";
	public static final String PROPERTIES_FILE = "PROPERTIES_FILE";
	public static final String MODEL_CHECKER_CHOICE = "MODEL_CHECKER_CHOICE";
	public static final String ARCHIVE_ENABLED = "ARCHIVE_ENABLED";

	/* define thresholds for evaluation of solutions */
	public static final double RELIABILITY_THRESHOLD = 00.80;
	public static final double TIME_THRESHOLD = 50.00;

	/* EA parameters */
	public static final String ALGORITHM_CHOICE = "ALGORITHM";
	public static final String POPULATION_SIZE = "POPULATION_SIZE";
	public static final String MAX_EVALUATIONS = "MAX_EVALUATIONS";
	public static final String PROCESSORS = "PROCESSORS";
	public static final String INIT_PORT_NUM = "INIT_PORT_NUM";

	public static final String configFileName = "config.properties";
	private static Properties properties;

	private static void loadPropertiesInstance() throws Exception {
		if (properties == null) {
			properties = new Properties();
			properties.load(new FileInputStream(configFileName));
		}
	}

	public static String getValue(String key) throws Exception {
		loadPropertiesInstance();
		String result = properties.getProperty(key);
		if (result == null)
			throw new IllegalArgumentException(key.toUpperCase() + " name not found!");
		return result;
	}

	public static String getValue(String key, String defaultValue) throws Exception {
		loadPropertiesInstance();
		String output = properties.getProperty(key);
		return (output != null ? output : defaultValue);
	}

	public static void exportToFile(String fileName, String output, boolean append) throws IOException {
		FileWriter writer = new FileWriter(fileName, append);
		writer.append(output);
		writer.flush();
		writer.close();
	}

	@SuppressWarnings("resource")
	public static void createFileAndExport(String inputFileName, String outputFileName, String outputStr) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;

		File input = new File(inputFileName);
		File output = new File(outputFileName);

		inputChannel = new FileInputStream(input).getChannel();
		outputChannel = new FileOutputStream(output).getChannel();
		outputChannel.transferFrom(inputChannel, 0, inputChannel.size());

		inputChannel.close();
		outputChannel.close();

		exportToFile(outputFileName, outputStr, false);
	}

	public static void setProperties(Properties _properties) {
		properties = _properties;
	}

	public static void setValue(Object key, Object value) {
		if (properties == null)
			properties = new Properties();
		properties.put(key, value);
	}
}
