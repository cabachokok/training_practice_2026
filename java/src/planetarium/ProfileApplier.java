package planetarium;

import java.util.List;
import java.util.Map;

/** Применяет профиль сеанса: проверяет связь, отправляет параметры по одному и решает, активна ли кнопка «Пуск». */
public final class ProfileApplier {

    private static final List<String> PARAMETERS = List.of("brightness", "volume", "scene_order");

    private final Equipment equipment;

    public ProfileApplier(Equipment equipment) {
        this.equipment = equipment;
    }

    public Journal apply(String profileId, Map<String, Profile> catalog) {
        Journal journal = new Journal();
        journal.info("Запуск применения профиля ID: " + profileId);

        Profile profile = catalog.get(profileId);
        if (profile == null) {
            journal.error("[ОШИБКА] Профиль с таким ID не найден в базе данных.");
            return journal;
        }
        journal.info("Выбран профиль: " + profile.name());

        journal.info("Проверка связи с программно-аппаратным комплексом");
        if (!equipment.ping()) {
            journal.error("[ОШИБКА] Оборудование недоступно. Профиль не применен.");
            return journal;
        }

        for (String param : PARAMETERS) {
            DeviceCommand command = new DeviceCommand(param, profile.parameter(param));
            journal.info("Отправка команды на оборудование: установить " + param + " = " + command.value() + "...");
            if (equipment.send(command)) {
                journal.info("[OK] Команда для " + param + " успешно выполнена.");
            } else {
                journal.error("[ОШИБКА] Оборудование не приняло команду для " + param + ".");
            }
        }

        if (journal.errorCount() == 0) {
            journal.info("СТАТУС: Профиль успешно применен.");
            journal.info("Кнопка «Пуск» для оператора активирована.");
        } else {
            journal.info("СТАТУС: ОШИБКА. Не выполнено команд: " + journal.errorCount());
            journal.info("Кнопка «Пуск» заблокирована до устранения неполадок.");
        }
        return journal;
    }
}
