package me.earth.phobos.manager;

import me.earth.phobos.Phobos;
import me.earth.phobos.features.Feature;
import me.earth.phobos.features.modules.Module;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//From Forgehax
//very low priority but maybe allow to set the directory for the client?
public class FileManager extends Feature {

    private final Path base;
    private final Path config;

    private String[] expandPath(String fullPath) {
        return fullPath.split(":?\\\\\\\\|\\/");
    }

    private Stream<String> expandPaths(String... paths) {
        return Arrays.stream(paths).map(this::expandPath).flatMap(Arrays::stream);
    }

    private Path lookupPath(Path root, String... paths) {
        return Paths.get(root.toString(), paths);
    }

    private Path getRoot() {
        return Paths.get("");
    }

    private void createDirectory(Path dir) {
        try {
            if (!Files.isDirectory(dir)) {
                if (Files.exists(dir)) {
                    Files.delete(dir); // delete if it exists but isn't a directory
                }

                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path getMkDirectory(Path parent, String... paths) {
        if (paths.length < 1) {
            return parent;
        }

        Path dir = lookupPath(parent, paths);
        createDirectory(dir);
        return dir;
    }

    public FileManager() {
        base = getMkDirectory(getRoot(), "phobos");
        config = getMkDirectory(base, "config");
        getMkDirectory(base, "util");
        for(Module.Category category : Phobos.moduleManager.getCategories()) {
            getMkDirectory(config, category.getName());
        }
        //getMkDirectory(config, "HUD");
        //getMkDirectory(base, "cache");
        //getMkDirectory(base, "messages");
    }

    public Path getBasePath() {
        return base;
    }

    public Path getBaseResolve(String... paths) {
        String[] names = expandPaths(paths).toArray(String[]::new);
        if (names.length < 1) {
            throw new IllegalArgumentException("missing path");
        }

        return lookupPath(getBasePath(), names);
    }

    public Path getMkBaseResolve(String... paths) {
        Path path = getBaseResolve(paths);
        createDirectory(path.getParent());
        return path;
    }

    public Path getConfig() {
        return getBasePath().resolve("config");
    }

    public Path getCache() {
        return getBasePath().resolve("cache");
    }

    public Path getMkBaseDirectory(String... names) {
        return getMkDirectory(getBasePath(), expandPaths(names).collect(Collectors.joining(File.separator)));
    }

    public Path getMkConfigDirectory(String... names) {
        return getMkDirectory(getConfig(), expandPaths(names).collect(Collectors.joining(File.separator)));
    }

    public static boolean appendTextFile(String data, String file) {
        try {
            final Path path = Paths.get(file);
            Files.write(path, Collections.singletonList(data), StandardCharsets.UTF_8, Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (final IOException e) {
            System.out.println("WARNING: Unable to write file: " + file);
            return false;
        }
        return true;
    }

    public static List<String> readTextFileAllLines(String file) {
        try {
            final Path path = Paths.get(file);
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            System.out.println("WARNING: Unable to read file, creating new file: " + file);
            appendTextFile("", file);
        }
        return Collections.emptyList();
    }
}