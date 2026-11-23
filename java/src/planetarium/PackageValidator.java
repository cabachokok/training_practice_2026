package planetarium;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Прогоняет набор правил по всем файлам пакета и собирает журнал проверки. */
public final class PackageValidator {

    public static final String MANIFEST = "manifest.json";

    private final List<Rule> rules;

    public PackageValidator(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public Journal validate(Path dir) throws IOException {
        Journal journal = new Journal();
        journal.info("Проверка пакета: " + dir);
        List<String> problems = new ArrayList<>();
        int checked = 0;

        Path manifestPath = dir.resolve(MANIFEST);
        if (!Files.isDirectory(dir)) {
            problems.add("Папка с пакетом не найдена");
        } else if (!Files.isRegularFile(manifestPath)) {
            problems.add("В пакете нет файла " + MANIFEST);
        } else {
            Map<String, ManifestReader.Entry> manifest = ManifestReader.read(manifestPath);
            List<Path> files;
            try (Stream<Path> s = Files.list(dir)) {
                files = s.filter(Files::isRegularFile).sorted().toList();
            }
            for (Path file : files) {
                String name = file.getFileName().toString();
                if (name.equals(MANIFEST)) {
                    continue;
                }
                checked++;
                PackageItem item = new PackageItem(name, PackageItem.extensionOf(name), file, manifest.get(name));
                List<String> fileErrors = new ArrayList<>();
                for (Rule rule : rules) {
                    rule.check(item).ifPresent(fileErrors::add);
                }
                if (fileErrors.isEmpty()) {
                    journal.info("[OK] Файл " + name + ": прошёл проверку");
                }
                for (String e : fileErrors) {
                    problems.add("[ОШИБКА] Файл " + name + ": " + e);
                }
            }
        }

        if (problems.isEmpty()) {
            journal.info("СТАТУС: УСПЕШНО. Проверено файлов: " + checked + ", все валидны.");
        } else {
            journal.info("СТАТУС: ОШИБКА. Проверено файлов: " + checked + ". Найдено проблем: " + problems.size());
        }
        problems.forEach(journal::error);
        return journal;
    }
}
