# Личен календар (CLI) - Java OOP1

Проектът представлява конзолно приложение за управление на личен календар, реализирано на Java със стриктна ООП архитектура.

## Компилация

```powershell
Set-Location "C:\Programing\UNI\OOP1\Calendar-java-app"
if (Test-Path out) { Remove-Item out -Recurse -Force }
New-Item -ItemType Directory -Path out | Out-Null
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
```

## Стартиране

```powershell
Set-Location "C:\Programing\UNI\OOP1\Calendar-java-app"
java -cp out bg.tu_varna.sit.Main
```

## Бързи примери за команди

- `open calendar.xml`
- `book 2026-03-23 09:00 10:00 "Лекция по ООП" "Зала 301"`
- `agenda 2026-03-23`
- `findslot 2026-03-23 2`
- `save`
- `exit`

## Примерни XML файлове за тест

В папка `samples` са добавени минимални файлове за директно тестване:

- `samples/primary.xml` и `samples/secondary.xml` - за `findslotwith`
- `samples/merge-main.xml` и `samples/merge-other.xml` - за `merge`

Пълни сценарии за тестване ще намерите в `test_scenarios.txt`.

