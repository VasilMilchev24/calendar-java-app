package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * Отваря XML файл на календар.
 */
public class OpenCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public OpenCommand() {
    }

    /**
     * Изпълнява командата open.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при неуспешна валидация или грешка при вход/изход
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 2) {
            throw new InvalidCommandException("Употреба: open <filePath>");
        }
        service.open(context, args[1]);
        System.out.println("Отворен файл: " + args[1]);
    }
}
