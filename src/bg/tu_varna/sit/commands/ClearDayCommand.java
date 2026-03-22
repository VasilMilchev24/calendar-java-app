package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Изтрива всички събития за конкретна дата.
 */
public class ClearDayCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public ClearDayCommand() {
    }

    /**
     * Изпълнява командата clearday.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 3) {
            throw new InvalidCommandException("Употреба: clearday <name of file of calendar> <yyyy-MM-dd>");
        }

        String filePath = args[1];
        LocalDate date = LocalDate.parse(args[2]);
        int removed = service.clearDay(filePath, date);
        System.out.println("Изтрити събития за " + date + " във файл " + filePath + ": " + removed);
    }
}

