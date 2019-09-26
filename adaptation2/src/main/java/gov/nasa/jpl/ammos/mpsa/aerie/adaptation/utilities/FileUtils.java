package gov.nasa.jpl.ammos.mpsa.aerie.adaptation.utilities;

import gov.nasa.jpl.ammos.mpsa.aerie.adaptation.models.Adaptation;

import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {
    public static Path getUniqueFilePath(final Adaptation adaptation, final Path basePath) {
        final String basename = adaptation.name;
        Path path = basePath.resolve(basename + ".jar");
        for (int i = 0; Files.exists(path); ++i) {
            path = basePath.resolve(basename + "_" + i + ".jar");
        }
        return path;
    }
}