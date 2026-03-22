package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.Event;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Извежда програмата за една дата.
 */
public class AgendaCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public AgendaCommand() {
    }

    /**
     * Изпълнява командата agenda.
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
            throw new InvalidCommandException("Употреба: agenda <yyyy-MM-dd>");
        }
        LocalDate date = LocalDate.parse(args[1]);
        if (context.isHoliday(date)) {
            System.out.println("Този ден е почивен/празник");
        }
        List<Event> events = service.getAgendaForDate(context, date);
        if (events.isEmpty()) {
            System.out.println("Няма събития за " + date);
            return;
        }
        for (Event event : events) {
            System.out.println(event);
        }
    }
}
