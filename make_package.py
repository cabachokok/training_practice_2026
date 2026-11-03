"""Создаёт тестовый пакет media_package: 4 файла + manifest.json с описанием."""
import hashlib, json, os

PKG = "media_package"
os.makedirs(PKG, exist_ok=True)

def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

files = {
    "scene_01.mp4":        (b"scene one data",   [4096, 4096]),   # всё верно
    "scene_02.avi":        (b"scene two data",   [4096, 4096]),   # неверный формат
    "scene_03_lowres.mp4": (b"scene three data", [2048, 2048]),   # низкое разрешение
    "scene_04.mp4":        (b"scene four data",  [4096, 4096]),   # будет испорчен (хэш)
}
manifest = {}
for name, (data, res) in files.items():
    with open(os.path.join(PKG, name), "wb") as f:
        f.write(data)
    manifest[name] = {"resolution": res, "sha256": sha256(data)}

# портим scene_04 уже после записи эталонного хэша в manifest
with open(os.path.join(PKG, "scene_04.mp4"), "wb") as f:
    f.write(b"scene four data CORRUPTED")

with open(os.path.join(PKG, "manifest.json"), "w", encoding="utf-8") as f:
    json.dump(manifest, f, indent=2)
print("Тестовый пакет создан:", PKG)
