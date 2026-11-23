package planetarium;

import java.util.LinkedHashMap;
import java.util.Map;

/** Профиль сеанса: значения параметров, которые нужно выставить на оборудовании. */
public record Profile(String id, String name, int brightness, int volume, String sceneOrder) {

    /** Профили из PROFILES_DB программы, ключ — id профиля. */
    public static Map<String, Profile> standard() {
        Map<String, Profile> db = new LinkedHashMap<>();
        db.put("1", new Profile("1", "Детский сеанс", 80, 60, "1,2,3"));
        db.put("2", new Profile("2", "Научный семинар", 40, 40, "3,1,2"));
        return db;
    }

    /** Значение параметра в виде текста, как оно уходит в команду. */
    public String parameter(String param) {
        return switch (param) {
            case "brightness" -> String.valueOf(brightness);
            case "volume" -> String.valueOf(volume);
            case "scene_order" -> sceneOrder;
            default -> throw new IllegalArgumentException("нет параметра " + param);
        };
    }
}
