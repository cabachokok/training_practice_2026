# Учебная практика: цифровой купол планетария

Программные модули к отчёту по учебной практике (ИКБО-30-24, Солдатова П.С.).
Модуль проверяет медиапакет для купола и применяет профиль оборудования.

## Структура

| Папка / файл | Что внутри |
|---|---|
| `procedural/` | Версия на Python: проверка пакета (алгоритм 1) и применение профиля (алгоритм 2) |
| `procedural/make_package.py` | Создаёт тестовый пакет `media_package/` |
| `procedural/validator.py` | Алгоритм 1: проверка пакета |
| `procedural/apply_profile.py` | Алгоритм 2: применение профиля |
| `java/src/planetarium/` | Версия на Java 21: те же алгоритмы, классы модуля |
| `java/test/planetarium/SelfTest.java` | Самопроверка Java-модуля (18 проверок) |

## Запуск Python

Нужен Python 3.

```bash
cd procedural
python make_package.py      # создаст папку media_package/
python validator.py         # алгоритм 1
python apply_profile.py     # алгоритм 2
```

Пример результата: `procedural/demo_output_python.txt`.

## Запуск Java

Нужен JDK 21.

```bash
cd java
javac -d out $(find src test -name "*.java")
java -cp out planetarium.SelfTest
```

Ожидаемый результат самопроверки записан в `java/selftest_output.txt`,
пример работы программы — в `java/demo_output.txt`.
Папка `java/out/` не хранится в репозитории (см. `java/.gitignore`).

## Что проверено

- Вывод Java-модуля совпадает с выводом Python-версии построчно (`demo_output.txt` и `demo_output_python.txt`).
- Самопроверка Java: 18 проверок, 0 неудачных.

## Тестовые данные

Папка `media_package/` создаётся скриптом `make_package.py`.
Она содержит четыре сцены, из которых одна имеет неподдерживаемый формат, одна — низкое разрешение и одна повреждена: это намеренно, чтобы проверка их находила.
