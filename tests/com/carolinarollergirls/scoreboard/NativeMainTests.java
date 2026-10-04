package com.carolinarollergirls.scoreboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NativeMainTests {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void prepareDataUpdatesWebFilesAndPreservesUserData() throws Exception {
        Path installation = temporary.newFolder("installation").toPath();
        Path data = temporary.newFolder("data").toPath();
        Files.createDirectories(installation.resolve("html"));
        Files.createDirectories(installation.resolve("config"));
        Files.writeString(installation.resolve("html/index.html"), "first version");
        Files.writeString(installation.resolve("config/settings.properties"), "default settings");

        NativeMain.prepareData(installation, data);
        assertEquals("first version", Files.readString(data.resolve("html/index.html")));
        assertEquals("default settings", Files.readString(data.resolve("config/settings.properties")));
        Files.writeString(data.resolve("config/settings.properties"), "user settings");
        Files.createDirectories(data.resolve("config/autosave"));
        Files.writeString(data.resolve("config/autosave/game.json"), "saved game");
        Files.createDirectories(data.resolve("html/game-data"));
        Files.writeString(data.resolve("html/game-data/game.json"), "game data");
        Files.writeString(installation.resolve("html/index.html"), "second version");

        NativeMain.prepareData(installation, data);
        assertEquals("second version", Files.readString(data.resolve("html/index.html")));
        assertEquals("user settings", Files.readString(data.resolve("config/settings.properties")));
        assertEquals("saved game", Files.readString(data.resolve("config/autosave/game.json")));
        assertTrue(Files.exists(data.resolve("html/game-data/game.json")));
    }
}
