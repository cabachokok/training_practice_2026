package planetarium;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Самопроверка без JUnit: запуск java -cp out planetarium.SelfTest; код выхода 1 при провале. */
public final class SelfTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        validatorFindsAllProblems();
        validPackagePasses();
        missingManifestIsReported();
        undescribedFileIsReported();
        newRuleNeedsNoChangeInValidator();
        profileOneAppliesCleanly();
        profileWithVolumeFailureBlocksStart();
        unknownProfileIsReported();

        System.out.println("Проверок прошло: " + passed + ", провалилось: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Тот же пакет, что создаёт make_package.py: ожидаем три ошибки и текст как у Python. */
    private static void validatorFindsAllProblems() throws Exception {
        Map<String, String> contents = Map.of(
                "scene_01.mp4", "scene one data",
                "scene_02.avi", "scene two data",
                "scene_03_lowres.mp4", "scene three data",
                "scene_04.mp4", "scene four data");
        Path dir = packageDir(Map.of(
                "scene_01.mp4", "scene one data",
                "scene_02.avi", "scene two data",
                "scene_03_lowres.mp4", "scene three data",
                "scene_04.mp4", "scene four data CORRUPTED"),
                manifest(Map.of(
                        "scene_01.mp4", new int[] {4096, 4096},
                        "scene_02.avi", new int[] {4096, 4096},
                        "scene_03_lowres.mp4", new int[] {2048, 2048},
                        "scene_04.mp4", new int[] {4096, 4096}), contents));
        Journal j = validator().validate(dir);
        check("проверено 4 файла", j.lines().contains("СТАТУС: ОШИБКА. Проверено файлов: 4. Найдено проблем: 3"));
        check("ошибка формата", j.lines().contains("[ОШИБКА] Файл scene_02.avi: недопустимый формат .avi"));
        check("ошибка разрешения", j.lines().contains(
                "[ОШИБКА] Файл scene_03_lowres.mp4: разрешение 2048x2048 ниже требуемого 4000x4000"));
        check("ошибка контрольной суммы", j.lines().contains(
                "[ОШИБКА] Файл scene_04.mp4: контрольная сумма не совпадает (файл повреждён)"));
        check("верный файл прошёл", j.lines().contains("[OK] Файл scene_01.mp4: прошёл проверку"));
        check("ровно три ошибки", j.errorCount() == 3);
    }

    /** Негативный контроль: исправный пакет не должен давать ошибок. */
    private static void validPackagePasses() throws Exception {
        Map<String, String> contents = Map.of("ok.mp4", "data");
        Path dir = packageDir(contents, manifest(Map.of("ok.mp4", new int[] {4000, 4000}), contents));
        Journal j = validator().validate(dir);
        check("исправный пакет без ошибок", j.errorCount() == 0);
        check("исправный пакет: статус УСПЕШНО", j.lines().contains(
                "СТАТУС: УСПЕШНО. Проверено файлов: 1, все валидны."));
    }

    private static void missingManifestIsReported() throws Exception {
        Path dir = packageDir(Map.of("ok.mp4", "data"), null);
        Journal j = validator().validate(dir);
        check("без manifest.json есть ошибка", j.lines().contains("В пакете нет файла manifest.json"));
        check("без manifest.json проверено 0 файлов", j.lines().contains(
                "СТАТУС: ОШИБКА. Проверено файлов: 0. Найдено проблем: 1"));
    }

    private static void undescribedFileIsReported() throws Exception {
        Map<String, String> contents = Map.of("ok.mp4", "data");
        Path dir = packageDir(Map.of("ok.mp4", "data", "extra.mp4", "x"),
                manifest(Map.of("ok.mp4", new int[] {4000, 4000}), contents));
        Journal j = validator().validate(dir);
        check("файл вне манифеста отмечен", j.lines().contains(
                "[ОШИБКА] Файл extra.mp4: файл не описан в manifest.json"));
    }

    /** Новое правило добавляется без правки PackageValidator: это и есть выигрыш структуры с интерфейсом. */
    private static void newRuleNeedsNoChangeInValidator() throws Exception {
        Rule noBadNames = item -> item.name().startsWith("bad")
                ? Optional.of("имя файла запрещено")
                : Optional.empty();
        Map<String, String> contents = Map.of("bad_1.mp4", "data");
        Path dir = packageDir(contents, manifest(Map.of("bad_1.mp4", new int[] {4000, 4000}), contents));
        Journal j = new PackageValidator(List.of(Rule.described(), noBadNames)).validate(dir);
        check("новое правило срабатывает", j.lines().contains("[ОШИБКА] Файл bad_1.mp4: имя файла запрещено"));
    }

    private static void profileOneAppliesCleanly() {
        Journal j = new ProfileApplier(new SimulatedEquipment()).apply("1", Profile.standard());
        check("профиль 1: ошибок нет", j.errorCount() == 0);
        check("профиль 1: кнопка активна", j.lines().contains("Кнопка «Пуск» для оператора активирована."));
    }

    private static void profileWithVolumeFailureBlocksStart() {
        Map<String, Profile> db = Profile.standard();
        db.put("3", new Profile("3", "Тестовый сбой", 50, 100, "1"));
        Journal j = new ProfileApplier(new SimulatedEquipment()).apply("3", db);
        check("профиль 3: одна ошибка", j.errorCount() == 1);
        check("профиль 3: отказ по громкости", j.lines().contains(
                "[ОШИБКА] Оборудование не приняло команду для volume."));
        check("профиль 3: кнопка заблокирована", j.lines().contains(
                "Кнопка «Пуск» заблокирована до устранения неполадок."));
    }

    private static void unknownProfileIsReported() {
        Journal j = new ProfileApplier(new SimulatedEquipment()).apply("9", Profile.standard());
        check("неизвестный профиль", j.lines().contains("[ОШИБКА] Профиль с таким ID не найден в базе данных."));
    }

    private static PackageValidator validator() {
        return new PackageValidator(List.of(
                Rule.allowedFormats(Set.of(".mp4", ".mov")),
                Rule.described(),
                Rule.minResolution(4000, 4000),
                Rule.checksum()));
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("ПРОВАЛ: " + name);
        }
    }

    /** Создаёт пакет во временной папке; manifestJson null означает отсутствие manifest.json. */
    private static Path packageDir(Map<String, String> files, String manifestJson) throws IOException {
        Path dir = Files.createTempDirectory("pkg");
        for (Map.Entry<String, String> f : files.entrySet()) {
            Files.writeString(dir.resolve(f.getKey()), f.getValue(), StandardCharsets.UTF_8);
        }
        if (manifestJson != null) {
            Files.writeString(dir.resolve(PackageValidator.MANIFEST), manifestJson, StandardCharsets.UTF_8);
        }
        return dir;
    }

    /** manifest.json с SHA-256 содержимого файлов, как в make_package.py. */
    private static String manifest(Map<String, int[]> resolutions, Map<String, String> contents) throws Exception {
        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, int[]> e : resolutions.entrySet()) {
            int[] r = e.getValue();
            sb.append("  \"").append(e.getKey()).append("\": {\n")
                    .append("    \"resolution\": [").append(r[0]).append(", ").append(r[1]).append("],\n")
                    .append("    \"sha256\": \"").append(sha256(contents.get(e.getKey()))).append("\"\n")
                    .append("  }");
            sb.append(++i < resolutions.size() ? ",\n" : "\n");
        }
        return sb.append("}\n").toString();
    }

    private static String sha256(String text) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }
}
