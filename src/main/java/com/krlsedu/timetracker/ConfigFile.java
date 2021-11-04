/* ==========================================================
File:        ConfigFile.java
Description: Read and write settings from the INI config file.
Maintainer:  WakaTime <support@wakatime.com>
License:     BSD, see LICENSE for more details.
Website:     https://wakatime.com/
===========================================================*/

package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.WakaTimeCli;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigFile {
	private static final String FILE_NAME = ".wakatime.cfg";
	private static final String WAKATIME_APPS_MONITORIN_JSON = ".wakatime-desktop-plugin-config.json";
	private static final String WAKATIME_HOME = "WAKATIME_HOME";
	private static String cachedConfigFile = null;
	private static String cachedConfigFileConfigApps = null;
	private static String apiKey = "";
	
	private ConfigFile(){}
	
	private static String getConfigFilePath() {
		if (ConfigFile.cachedConfigFile == null) {
			if (System.getenv(WAKATIME_HOME) != null && !System.getenv(WAKATIME_HOME).trim().isEmpty()) {
				File folder = new File(System.getenv(WAKATIME_HOME));
				if (folder.exists()) {
					ConfigFile.cachedConfigFile = new File(folder, ConfigFile.FILE_NAME).getAbsolutePath();
					WakaTimeCli.log.debug("Using $WAKATIME_HOME for config folder: " + ConfigFile.cachedConfigFile);
					return ConfigFile.cachedConfigFile;
				}
			}
			ConfigFile.cachedConfigFile = new File(System.getProperty("user.home"), ConfigFile.FILE_NAME).getAbsolutePath();
			WakaTimeCli.log.debug("Using $HOME for config folder: " + ConfigFile.cachedConfigFile);
		}
		return ConfigFile.cachedConfigFile;
	}
	
	public static String getConfigAppsFilePath() {
		if (ConfigFile.cachedConfigFileConfigApps == null) {
			if (System.getenv(WAKATIME_HOME) != null && !System.getenv(WAKATIME_HOME).trim().isEmpty()) {
				File folder = new File(System.getenv(WAKATIME_HOME));
				if (folder.exists()) {
					ConfigFile.cachedConfigFileConfigApps = new File(folder, ConfigFile.WAKATIME_APPS_MONITORIN_JSON).getAbsolutePath();
					WakaTimeCli.log.debug("Using $WAKATIME_HOME for config folder: " + ConfigFile.cachedConfigFileConfigApps);
					return ConfigFile.cachedConfigFileConfigApps;
				}
			}
			ConfigFile.cachedConfigFileConfigApps = new File(System.getProperty("user.home"), ConfigFile.WAKATIME_APPS_MONITORIN_JSON).getAbsolutePath();
			WakaTimeCli.log.debug("Using $HOME for config folder: " + ConfigFile.cachedConfigFileConfigApps);
		}
		return ConfigFile.cachedConfigFileConfigApps;
	}
	
	public static String get(String section, String key) {
		String file = ConfigFile.getConfigFilePath();
		String val = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String currentSection = "";
			try {
				String line = br.readLine();
				while (line != null) {
					if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
						currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
					} else {
						if (section.toLowerCase().equals(currentSection)) {
							String[] parts = line.split("=");
							if (parts.length == 2 && parts[0].trim().equals(key)) {
								val = parts[1].trim();
								br.close();
								return val;
							}
						}
					}
					line = br.readLine();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e1) { /* ignored */ }
		return val;
	}
	
	public static void set(String section, String key, String val) {
		String file = ConfigFile.getConfigFilePath();
		StringBuilder contents = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			try {
				String currentSection = "";
				String line = br.readLine();
				boolean found = false;
				while (line != null) {
					if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
						if (section.toLowerCase().equals(currentSection) && !found) {
							contents.append(key + " = " + val + "\n");
							found = true;
						}
						currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
						contents.append(line + "\n");
					} else {
						if (section.toLowerCase().equals(currentSection)) {
							String[] parts = line.split("=");
							String currentKey = parts[0].trim();
							if (currentKey.equals(key)) {
								if (!found) {
									contents.append(key + " = " + val + "\n");
									found = true;
								}
							} else {
								contents.append(line + "\n");
							}
						} else {
							contents.append(line + "\n");
						}
					}
					line = br.readLine();
				}
				if (!found) {
					if (!section.toLowerCase().equals(currentSection)) {
						contents.append("[" + section.toLowerCase() + "]\n");
					}
					contents.append(key + " = " + val + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e1) {
			
			// cannot read config file, so create it
			contents = new StringBuilder();
			contents.append("[" + section.toLowerCase() + "]\n");
			contents.append(key + " = " + val + "\n");
		}
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file, StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (writer != null) {
			writer.print(contents);
			writer.close();
		}
	}
	
	public static String getApiKey() {
		if (!ConfigFile.apiKey.equals("")) {
			return ConfigFile.apiKey;
		}
		
		String apiKey = get("settings", "api_key");
		if (apiKey == null) apiKey = "";
		
		ConfigFile.apiKey = apiKey;
		return apiKey;
	}
	
	public static void setApiKey(String apiKey) {
		set("settings", "api_key", apiKey);
		ConfigFile.apiKey = apiKey;
	}
	
}
