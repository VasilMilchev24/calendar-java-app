package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.RescheduleResult;
import bg.tu_varna.sit.services.CalendarService;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Пренасрочва събитията за ден към първия възможен работен ден.
 */
public class RescheduleCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public RescheduleCommand() {
    }

    /**
     * Изпълнява командата reschedule.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или липса на възможно пренасрочване
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 3) {
            throw new InvalidCommandException("Употреба: reschedule <name of file of calendar> <yyyy-MM-dd>");
        }

        String filePath = args[1];
        LocalDate date = LocalDate.parse(args[2]);
        RescheduleResult result = service.rescheduleDay(filePath, date);
        System.out.println("Пренасрочени събития във файл " + filePath + ": "
                + result.getMovedCount() + ". Нова дата: " + result.getTargetDate());
    }
}

