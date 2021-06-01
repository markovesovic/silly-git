package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class AppConfig {

	public static ServentInfo myServentInfo;

	// public static boolean INITIALIZED = false;
	public static String BOOTSTRAP_HOST;
	public static int BOOTSTRAP_PORT;
	public static int SERVENT_COUNT;

	public static ChordState chordState;

	public static String ROOT_PATH;
	public static String WAREHOUSE_PATH;

	public static void readConfig(String configName, int serventID) {
		configName += "servent_list.properties";
		AppConfig.timestampedStandardPrint("prop file: " + configName);

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configName));

		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(-1);
		}


		try {
			BOOTSTRAP_HOST = String.valueOf(properties.get("bs.host"));
		} catch (Exception e) {
			timestampedErrorPrint("Problem reading bootstrap_host. Exiting...");
			System.exit(-1);
		}


		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs.port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(-1);
		}





		try {
			SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(-1);
		}




		try {

			ChordState.CHORD_SIZE = Integer.parseInt(properties.getProperty("chord_size"));
			chordState = new ChordState();

		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
			System.exit(-1);
		}



		String portProperty = "servent" + serventID + ".port";
		int serventPort = -1;

		try {
			serventPort = Integer.parseInt(properties.getProperty(portProperty));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
			System.exit(-1);
		}

		String rootProperty = "servent" + serventID + ".root_path";
		try {
			ROOT_PATH = String.valueOf(properties.getProperty(rootProperty));
		} catch (Exception ex) {
			timestampedStandardPrint("Problem reading root path");
			System.exit(-1);
		}

		String warehouseProperty = "servent" + serventID + ".warehouse_path";
		try {
			WAREHOUSE_PATH = String.valueOf(properties.getProperty(warehouseProperty));
		} catch (Exception ex) {
			timestampedErrorPrint("Problem reading warehouse path");
			System.exit(-1);
		}


		String serventHost = "";

		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
			serventHost = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Host: " + serventHost + ", port: " + serventPort + ", chordID: " + ChordState.chordHash(serventHost, serventPort));

		myServentInfo = new ServentInfo(serventHost, serventPort);
	}



	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.out.println(timeFormat.format(now) + " - " + message);
	}

	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();

		System.err.println(timeFormat.format(now) + " - " + message);
	}

}
