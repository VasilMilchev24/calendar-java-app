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
 * Записва ново календарно събитие.
 */
public class BookCommand implements Command {

    /**
     * Създава инстанция на командата.
     */
    public BookCommand() {
    }

    /**
     * Изпълнява командата book.
     *
     * @param args парсирани аргументи
     * @param context контекст на календара
     * @param service календарна услуга
     * @param consoleScanner споделен четец от конзолата
     * @throws Exception при невалиден синтаксис или конфликт при валидация
     */
    @Override
    public void execute(String[] args, CalendarContext context, CalendarService service, Scanner consoleScanner) throws Exception {
        if (args.length != 6) {
            throw new InvalidCommandException("Употреба: book <yyyy-MM-dd> <HH:mm> <HH:mm> \"name\" \"note\"");
        }
        LocalDate date = LocalDate.parse(args[1]);
        if (isNonWorkingDay(context, date)) {
            if (!confirmHolidayProceed(consoleScanner)) {
                System.out.println("Записването на събитието е отказано.");
                return;
            }
        }
        LocalTime start = LocalTime.parse(args[2]);
        LocalTime end = LocalTime.parse(args[3]);
        Event event = new Event(date, start, end, args[4], args[5]);
        service.addEvent(context, event);
        System.out.println("Записано събитие: " + event);
    }

    /**
     * Пита потребителя дали да продължи добавяне на събитие в почивен ден.
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
