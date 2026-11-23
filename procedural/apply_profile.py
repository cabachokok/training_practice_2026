import time

PROFILES_DB = {
    "1": {
        "name": "Детский сеанс",
        "brightness": 80,
        "volume": 60,
        "scene_order": "1,2,3"
    },
    "2": {
        "name": "Научный семинар",
        "brightness": 40,
        "volume": 40,
        "scene_order": "3,1,2"
    }
}

def ping_equipment():
    print("Проверка связи с программно-аппаратным комплексом")
    time.sleep(1)
    return True

def apply_profile(profile_id):
    print(f"Запуск применения профиля ID: {profile_id}")
    
    if profile_id not in PROFILES_DB:
        print("[ОШИБКА] Профиль с таким ID не найден в базе данных.")
        return

    profile = PROFILES_DB[profile_id]
    print(f"Выбран профиль: {profile['name']}")
    
    if not ping_equipment():
        print("[ОШИБКА] Оборудование недоступно. Профиль не применен.")
        return

    params = ["brightness", "volume", "scene_order"]
    errors_count = 0
    
    for param in params:
        value = profile[param]
        print(f"Отправка команды на оборудование: установить {param} = {value}...")
        time.sleep(0.5)
        
        if param == "volume" and value > 90:
            print(f"[ОШИБКА] Оборудование не приняло команду для {param}.")
            errors_count += 1
        else:
            print(f"[OK] Команда для {param} успешно выполнена.")

    if errors_count == 0:
        print("СТАТУС: Профиль успешно применен.")
        print("Кнопка «Пуск» для оператора активирована.")
    else:
        print(f"СТАТУС: ОШИБКА. Не выполнено команд: {errors_count}")
        print("Кнопка «Пуск» заблокирована до устранения неполадок.")

# Точка входа
if __name__ == "__main__":
    apply_profile("1")
    
    print("\n" + "="*40 + "\n")
    
    PROFILES_DB["3"] = {"name": "Тестовый сбой", "brightness": 50, "volume": 100, "scene_order": "1"}
    apply_profile("3")
