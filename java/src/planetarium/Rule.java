package planetarium;

import java.util.Optional;
import java.util.Set;

/**
 * Правило проверки файла пакета. Один тип проверки — один объект этого интерфейса.
 * Новый тип проверки добавляется новой реализацией; класс PackageValidator не меняется.
 */
@FunctionalInterface
public interface Rule {

    /** Текст ошибки или пустой Optional, если файл правило прошёл. */
    Optional<String> check(PackageItem item);

    static Rule allowedFormats(Set<String> extensions) {
        return item -> extensions.contains(item.extension())
                ? Optional.empty()
                : Optional.of("недопустимый формат " + item.extension());
    }

    static Rule described() {
        return item -> item.entry() == null
                ? Optional.of("файл не описан в manifest.json")
                : Optional.empty();
    }

    static Rule minResolution(int minWidth, int minHeight) {
        return item -> {
            if (item.entry() == null) {
                return Optional.empty();
            }
            int w = item.entry().width();
            int h = item.entry().height();
            return (w < minWidth || h < minHeight)
                    ? Optional.of("разрешение " + w + "x" + h + " ниже требуемого " + minWidth + "x" + minHeight)
                    : Optional.empty();
        };
    }

    static Rule checksum() {
        return item -> {
            if (item.entry() == null) {
                return Optional.empty();
            }
            return item.sha256().equals(item.entry().sha256())
                    ? Optional.empty()
                    : Optional.of("контрольная сумма не совпадает (файл повреждён)");
        };
    }
}
