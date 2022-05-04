

package com.csctracker.desktoppluguin.core;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ConfigFile {
    private static final String TIMETRACKER_CFG = "cscTracker.cfg";
    private static String resourcesLocation = null;
    private static String cscTrackerCachedConfigFile = null;

    private ConfigFile() {
    }

    private static String getConfigFilePath() {
        ConfigFile.cscTrackerCachedConfigFile = new File(getResourcesLocation(), ConfigFile.TIMETRACKER_CFG).getAbsolutePath();
        if (Core.isDebug()) {
            log.debug("Using $HOME for config folder: " + ConfigFile.cscTrackerCachedConfigFile);
        }
        return ConfigFile.cscTrackerCachedConfigFile;
    }

    public static String getResourcesLocation() {
        if (resourcesLocation != null) return resourcesLocation;

        if (ConfigFile.isWindows()) {
            File windowsHome = new File(System.getenv("USERPROFILE"));
            File resourcesFolder = new File(windowsHome, ".cscTracker");
            resourcesLocation = resourcesFolder.getAbsolutePath();
            return resourcesLocation;
        }

        File userHomeDir = new File(System.getProperty("user.home"));
        File resourcesFolder = new File(userHomeDir, ".cscTracker");
        resourcesLocation = resourcesFolder.getAbsolutePath();
        return resourcesLocation;
    }


    public static String get(String section, String key) {
        return get(section, key, ConfigFile.getConfigFilePath());
    }

    public static String get(String section, String key, String file) {
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
                            contents.append(key).append(" = ").append(val).append("\n");
                            found = true;
                        }
                        currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
                        contents.append(line).append("\n");
                    } else {
                        if (section.toLowerCase().equals(currentSection)) {
                            String[] parts = line.split("=");
                            String currentKey = parts[0].trim();
                            if (currentKey.equals(key)) {
                                if (!found) {
                                    contents.append(key).append(" = ").append(val).append("\n");
                                    found = true;
                                }
                            } else {
                                contents.append(line).append("\n");
                            }
                        } else {
                            contents.append(line).append("\n");
                        }
                    }
                    line = br.readLine();
                }
                if (!found) {
                    if (!section.toLowerCase().equals(currentSection)) {
                        contents.append("[").append(section.toLowerCase()).append("]\n");
                    }
                    contents.append(key).append(" = ").append(val).append("\n");
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
            contents.append("[").append(section.toLowerCase()).append("]\n");
            contents.append(key).append(" = ").append(val).append("\n");
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

    public static boolean isCscTrackerOffline() {
        String setting = ConfigFile.get("settings", "cscTrackerOffline");
        return !(setting == null || setting.equals("false"));
    }

    public static String urlProxy() {
        return ConfigFile.get("settings", "urlProxy");
    }

    public static Integer portProxy() {
        try {
            return Integer.parseInt(ConfigFile.get("settings", "portProxy"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String urlCscTracker() {
//        return "http://45.79.41.108:8090/usage-info";
        return ConfigFile.get("settings", "urlCscTracker");
    }

    public static String tokenCscTracker() {
        return ConfigFile.get("settings", "tokenCscTracker");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }
}