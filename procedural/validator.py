import hashlib
import json
import os

PROFILE_RULES = {
    "allowed_formats": [".mp4", ".mov"],
    "min_resolution": (4000, 4000),
}
MANIFEST_NAME = "manifest.json"  


def file_sha256(path):
    """Считает SHA-256 файла, читая его частями."""
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(8192), b""):
            h.update(chunk)
    return h.hexdigest()


def validate_package(package_path, rules):
    """Проверяет все файлы пакета. Возвращает (список ошибок, число проверенных файлов)."""
    errors = []

    if not os.path.isdir(package_path):
        return ["Папка с пакетом не найдена"], 0

    manifest_path = os.path.join(package_path, MANIFEST_NAME)
    if not os.path.isfile(manifest_path):
        return [f"В пакете нет файла {MANIFEST_NAME}"], 0
    with open(manifest_path, encoding="utf-8") as f:
        manifest = json.load(f)

    checked = 0
    for filename in sorted(os.listdir(package_path)):
        filepath = os.path.join(package_path, filename)
        if filename == MANIFEST_NAME or not os.path.isfile(filepath):
            continue
        checked += 1
        file_errors = []

        ext = os.path.splitext(filename)[1].lower()
        if ext not in rules["allowed_formats"]:
            file_errors.append(f"недопустимый формат {ext}")

        info = manifest.get(filename)
        if info is None:
            file_errors.append(f"файл не описан в {MANIFEST_NAME}")
        else:
            width, height = info["resolution"]
            min_w, min_h = rules["min_resolution"]
            if width < min_w or height < min_h:
                file_errors.append(
                    f"разрешение {width}x{height} ниже требуемого {min_w}x{min_h}")
            if file_sha256(filepath) != info["sha256"]:
                file_errors.append("контрольная сумма не совпадает (файл повреждён)")

        if file_errors:
            for e in file_errors:
                errors.append(f"[ОШИБКА] Файл {filename}: {e}")
        else:
            print(f"[OK] Файл {filename}: прошёл проверку")

    return errors, checked


if __name__ == "__main__":
    package = "./media_package"
    print(f"Проверка пакета: {package}")
    errors, checked = validate_package(package, PROFILE_RULES)
    if errors:
        print(f"СТАТУС: ОШИБКА. Проверено файлов: {checked}. Найдено проблем: {len(errors)}")
        for e in errors:
            print(e)
    else:
        print(f"СТАТУС: УСПЕШНО. Проверено файлов: {checked}, все валидны.")
