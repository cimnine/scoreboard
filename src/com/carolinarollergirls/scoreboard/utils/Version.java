package com.carolinarollergirls.scoreboard.utils;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Version {
    public static boolean load() throws IOException {
        Properties release = new Properties();
        ClassLoader cL = Version.class.getClassLoader();

        try (InputStream releaseIs = cL.getResourceAsStream(VERSION_RELEASE_PROPERTIES_NAME)) {
            try {
                release.load(releaseIs);
            } catch (NullPointerException npE) {
                Logger.printMessage("Could not find version release properties file '" + VERSION_RELEASE_PROPERTIES_NAME + "'");
                return false;
            } catch (IOException ioE) {
                Logger.printMessage("Could not load version release properties file '" + VERSION_RELEASE_PROPERTIES_NAME + "'");
                throw ioE;
            }
        }

        Map<String, String> m = new HashMap<>();
        m.put(VERSION_RELEASE_KEY, "unset");
        for (String k : release.stringPropertyNames()) {
            m.put(k, release.getProperty(k));
        }
        props = Collections.unmodifiableMap(m);
        Logger.printMessage("CRG ScoreBoard version " + Version.get());
        return true;
    }

    public static String get() {
        return props.get(VERSION_RELEASE_KEY);
    }

    public static Map<String, String> getAll() {
        return ImmutableMap.copyOf(props);
    }

    private static Map<String, String> props = new HashMap<>();

    public static final String VERSION_PATH = "com/carolinarollergirls/scoreboard/version";
    public static final String VERSION_RELEASE_PROPERTIES_NAME = VERSION_PATH + "/release.properties";
    public static final String VERSION_RELEASE_KEY = "release";
}
