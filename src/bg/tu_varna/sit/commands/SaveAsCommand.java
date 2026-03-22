package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.util.Scanner;

/**
 * Записва текущия контекст в зададен път до файл.
 */
public class SaveAsCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public SaveAsCommand() {
    }

    /**
     * Изпълнява командата saveas.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или неуспешен запис
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 2) {
            throw new InvalidCommandException("Употреба: saveas <filePath>");
        }
        service.saveAs(context, args[1]);
        System.out.println("Записано като: " + args[1]);
    }
}
