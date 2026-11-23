package planetarium;

import java.util.ArrayList;
import java.util.List;

/** Журнал проверки или применения профиля: строки по порядку и число ошибок. */
public final class Journal {

    private final List<String> lines = new ArrayList<>();
    private int errors;

    public void info(String text) {
        lines.add(text);
    }

    public void error(String text) {
        lines.add(text);
        errors++;
    }

    public int errorCount() {
        return errors;
    }

    public List<String> lines() {
        return List.copyOf(lines);
    }
}
