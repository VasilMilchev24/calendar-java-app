package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.TimeSlot;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Намира първия свободен интервал спрямо текущия и импортнат календар.
 */
public class FindSlotWithCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public FindSlotWithCommand() {
    }

    /**
     * Изпълнява командата findslotwith.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или грешка при вход/изход
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 4) {
            throw new InvalidCommandException("Употреба: findslotwith <yyyy-MM-dd> <durationHours> <filePath>");
        }
        LocalDate fromDate = LocalDate.parse(args[1]);
        int durationHours = Integer.parseInt(args[2]);
        String filePath = args[3];

        TimeSlot slot = service.findSlotWith(context, filePath, fromDate, durationHours);
        if (slot == null) {
            System.out.println("Не е намерен общ свободен интервал.");
        } else {
            System.out.println("Първи общ свободен интервал: " + slot);
        }
    }
}
