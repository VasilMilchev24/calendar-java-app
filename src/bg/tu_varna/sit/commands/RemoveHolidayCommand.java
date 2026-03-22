package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Премахва дата на неработен ден.
 */
public class RemoveHolidayCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public RemoveHolidayCommand() {
    }

    /**
     * Изпълнява командата removeholiday.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 2) {
            throw new InvalidCommandException("Употреба: removeholiday <yyyy-MM-dd>");
        }
        LocalDate date = LocalDate.parse(args[1]);
        service.removeHoliday(context, date);
        System.out.println("Премахнат е неработен ден: " + date);
    }
}
