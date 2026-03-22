# Cheat Sheet: Добавяне на нова команда за 5 минути

## 1) Създайте клас в `commands`
- Създайте `YourCommand.java` в `src/bg/tu_varna/sit/commands`.
- Имплементирайте интерфейса `Command`.

## 2) Добавете валидация на аргументите и действие
- Проверете първо `args.length`.
- При грешен синтаксис хвърляйте `InvalidCommandException`.
- Дръжте бизнес логиката в `CalendarService`, а не в класа на командата.

## 3) Свържете командата в `Engine`
- Добавете `commands.put("yourcommand", new YourCommand());` в `registerCommands()`.
- Добавете ред за командата в `buildHelpText()`.

## 4) Тествайте ръчно в конзолата
- Стартирайте приложението и извикайте командата с валидни аргументи.
- Повторете с невалидни аргументи, за да проверите обработката на грешки.

## 5) Записвайте във файл само при нужда
- Използвайте `save`/`saveas` за запис на данните.
- Всички промени по събитията се правят първо в паметта.

## Универсален шаблон за команда

```java
package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * TODO: Опишете предназначението на командата.
 */
public class YourCommand implements Command {

    /**
     * Изпълнява командата.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен scanner за конзолата
     * @throws Exception при невалидни аргументи или грешка при изпълнение
     */
    @Override
    public void execute(String[] args,
                        CalendarContext context,
                        CalendarService service,
                        Scanner consoleScanner) throws Exception {
        if (args.length != 2) {
            throw new InvalidCommandException("Употреба: yourcommand <arg>");
        }

        String value = args[1];
        // TODO: извикайте метод от service слоя.

        System.out.println("Командата yourcommand е изпълнена със стойност: " + value);
    }
}
```

