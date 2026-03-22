package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.BusyDayInfo;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Извежда обобщение на заетите часове по ден от седмицата.
 */
public class BusyDaysCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public BusyDaysCommand() {
    }

    /**
     * Изпълнява командата busydays.
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
            throw new InvalidCommandException("Употреба: busydays <from> <to>");
        }

        LocalDate from = LocalDate.parse(args[1]);
        LocalDate to = LocalDate.parse(args[2]);

        List<BusyDayInfo> busyDays = service.calculateBusyDays(context, from, to);
        for (BusyDayInfo info : busyDays) {
            System.out.println(info);
        }
    }
}
