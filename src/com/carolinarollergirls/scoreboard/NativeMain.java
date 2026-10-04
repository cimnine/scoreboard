package com.carolinarollergirls.scoreboard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import com.carolinarollergirls.scoreboard.utils.BasePath;

public final class NativeMain {
    public static void main(String[] args) throws IOException {
        Path installation = Path.of(System.getProperty("scoreboard.installDir"));
        Path data = Path.of(System.getProperty("user.home"), ".crg-scoreboard");
        prepareData(installation, data);
        BasePath.set(data.toFile());
        Main.main(args);
    }

    static void prepareData(Path installation, Path data) throws IOException {
        for (String directory : new String[] {"html", "config"}) {
            Path source = installation.resolve(directory);
            try (Stream<Path> paths = Files.walk(source)) {
                for (Path path : paths.toList()) {
                    Path target = data.resolve(directory).resolve(source.relativize(path));
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                    } else if (directory.equals("html")) {
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                    } else if (!Files.exists(target)) {
                        Files.copy(path, target);
                    }
                }
            }
        }
    }
}
