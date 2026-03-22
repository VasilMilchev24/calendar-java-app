package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.Event;
import bg.tu_varna.sit.services.CalendarService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Променя съществуващо събитие.
 */
public class ChangeCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public ChangeCommand() {
    }

    /**
     * Изпълнява командата change.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или неуспешна валидация
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 9) {
            throw new InvalidCommandException("Употреба: change <date> <start> \"name\" <newDate> <newStart> <newEnd> \"newName\" \"newNote\"");
        }

        LocalDate oldDate = LocalDate.parse(args[1]);
        LocalTime oldStart = LocalTime.parse(args[2]);
        String oldName = args[3];

        LocalDate newDate = LocalDate.parse(args[4]);
        if (isNonWorkingDay(context, newDate)) {
            if (!confirmHolidayProceed(consoleScanner)) {
                System.out.println("Промяната на събитието е отказана.");
                return;
            }
        }
        LocalTime newStart = LocalTime.parse(args[5]);
        LocalTime newEnd = LocalTime.parse(args[6]);
        String newName = args[7];
        String newNote = args[8];

        Event newEvent = new Event(newDate, newStart, newEnd, newName, newNote);
        service.changeEvent(context, oldDate, oldStart, oldName, newEvent);
        System.out.println("Събитието е променено.");
    }

    /**
     * Пита потребителя дали да продължи запис в почивен ден.
     *
     * @param consoleScanner споделен четец от конзолата
     * @return true при избор yes, иначе false
     */
    private boolean confirmHolidayProceed(Scanner consoleScanner) {
        while (true) {
            System.out.println("Опитвате се да добавите събитие в почивен ден/празник. Желаете ли да продължите?");
            System.out.println("Опции:");
            System.out.println("- yes : записва събитието в почивен ден");
            System.out.println("- no  : отказва записването на събитието");
            System.out.print("Въведете yes или no: ");
            String answer = consoleScanner.nextLine().trim();
            if ("yes".equalsIgnoreCase(answer)) {
                return true;
            }
            if ("no".equalsIgnoreCase(answer)) {
                return false;
            }
            System.out.println("Моля въведете \"yes\" или \"no\".");
        }
    }

    /**
     * Проверява дали датата е почивна в текущия календар.
     *
     * @param context текущ календарен контекст
     * @param date дата за проверка
     * @return true, когато е събота, неделя или локален празник
     */
    private boolean isNonWorkingDay(CalendarContext context, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return true;
        }
        return context.isHoliday(date);
    }
}
