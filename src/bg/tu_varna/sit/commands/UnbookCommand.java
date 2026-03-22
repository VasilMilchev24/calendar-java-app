package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Премахва съществуващо събитие.
 */
public class UnbookCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public UnbookCommand() {
    }

    /**
     * Изпълнява командата unbook.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 4) {
            throw new InvalidCommandException("Употреба: unbook <yyyy-MM-dd> <HH:mm> \"name\"");
        }
        LocalDate date = LocalDate.parse(args[1]);
        LocalTime start = LocalTime.parse(args[2]);
        String name = args[3];
        service.removeEvent(context, date, start, name);
        System.out.println("Събитието е премахнато.");
    }
}
