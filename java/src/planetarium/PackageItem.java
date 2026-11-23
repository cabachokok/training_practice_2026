package planetarium;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/** Файл пакета и его запись из manifest.json; entry равен null, если файл не описан. */
public record PackageItem(String name, String extension, Path path, ManifestReader.Entry entry) {

    /** Расширение в нижнем регистре, как os.path.splitext(name)[1].lower() в Python. */
    public static String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    /** SHA-256 содержимого файла в нижнем регистре, читается частями. */
    public String sha256() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(path)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    md.update(buf, 0, n);
                }
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен в этой JVM", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
