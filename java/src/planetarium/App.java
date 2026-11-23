package planetarium;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Точка входа: проверка пакета и применение профилей, по тем же сценариям, что и Python-программа. */
public final class App {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        Path pkg = Path.of(args.length > 0 ? args[0] : "media_package");

        PackageValidator validator = new PackageValidator(List.of(
                Rule.allowedFormats(Set.of(".mp4", ".mov")),
                Rule.described(),
                Rule.minResolution(4000, 4000),
                Rule.checksum()));
        print(validator.validate(pkg));

        System.out.println("\n" + "=".repeat(40) + "\n");

        Map<String, Profile> db = Profile.standard();
        ProfileApplier applier = new ProfileApplier(new SimulatedEquipment());
        print(applier.apply("1", db));

        System.out.println("\n" + "=".repeat(40) + "\n");

        db.put("3", new Profile("3", "Тестовый сбой", 50, 100, "1"));
        print(applier.apply("3", db));
    }

    private static void print(Journal journal) {
        journal.lines().forEach(System.out::println);
    }
}
