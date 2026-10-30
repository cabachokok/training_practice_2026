import os

PROFILE_RULES = {"allowed_formats": [".mp4", ".mov"],"min_resolution": (4000, 4000)}

def validate_package(package_path):
    print(f"Проверка пакета: {package_path}")
    report = []
    errors_count = 0
    
    if not os.path.exists(package_path):
        return "Ошибка: Папка не найдена"
    
    for filename in os.listdir(package_path):
        filepath = os.path.join(package_path, filename)
        
        if os.path.isfile(filepath):
            file_ext = os.path.splitext(filename)[1].lower()
            
            if file_ext not in PROFILE_RULES["allowed_formats"]:
                report.append(f"[ОШИБКА] Файл {filename}: недопустимый формат {file_ext}")
                errors_count += 1
                continue
            
            if "lowres" in filename.lower():
                report.append(f"[ОШИБКА] Файл {filename}: разрешение ниже требуемого {PROFILE_RULES['min_resolution']}")
                errors_count += 1
            else:
                report.append(f"[OK] Файл {filename}: прошел проверку")

    if errors_count == 0:
        report.insert(0, "СТАТУС: УСПЕШНО. Все файлы валидны.")
    else:
        report.insert(0, f"СТАТУС: ОШИБКА. Найдено проблем: {errors_count}")
        
    return "\n".join(report)

if __name__ == "__main__":
    test_dir = "./media_package" 
    result = validate_package(test_dir)
    print(result)
