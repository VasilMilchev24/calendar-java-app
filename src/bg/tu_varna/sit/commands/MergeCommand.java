package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.Event;
import bg.tu_varna.sit.models.MergeConflict;
import bg.tu_varna.sit.services.CalendarService;

import java.util.List;
import java.util.Scanner;

/**
 * Слива събития от втори календар с интерактивен избор при конфликт.
 */
public class MergeCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public MergeCommand() {
    }

    /**
     * Изпълнява командата merge.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или грешка при вход/изход
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 2) {
            throw new InvalidCommandException("Употреба: merge <filePath>");
        }

        CalendarContext incoming = service.loadTemporaryCalendar(args[1]);
        List<MergeConflict> conflicts = service.findMergeConflicts(context, incoming);

        int mergedWithoutConflict = 0;
        for (Event incomingEvent : incoming.getEvents()) {
            Event conflict = service.findConflictingEvent(context, incomingEvent, null);
            if (conflict == null) {
                context.addEvent(incomingEvent);
                mergedWithoutConflict++;
            }
        }

        int resolvedConflicts = 0;
        for (MergeConflict conflict : conflicts) {
            System.out.println("Открит е конфликт:");
            System.out.println("  Текущо събитие : " + conflict.getExistingEvent());
            System.out.println("  Входящо събитие: " + conflict.getIncomingEvent());
            System.out.print("Запази текущото (1) или замени с входящото (2)? ");

            String choice = consoleScanner.nextLine().trim();
            if ("2".equals(choice)) {
                context.removeEvent(conflict.getExistingEvent());
                context.addEvent(conflict.getIncomingEvent());
                resolvedConflicts++;
            }
        }

        System.out.println("Сливането завърши. Добавени без конфликт: " + mergedWithoutConflict
                + ", обработени конфликти: " + conflicts.size()
                + ", направени замени: " + resolvedConflicts);
    }
}
