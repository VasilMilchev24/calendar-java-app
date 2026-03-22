package bg.tu_varna.sit.commands;

import bg.tu_varna.sit.exceptions.CalendarFileException;
import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.services.CalendarService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Добавя дата на неработен ден.
 */
public class AddHolidayCommand implements Command {

    /**
     * Информация за единичен конфликт.
     */
    private static class ConflictItem {
        /**
         * Пореден номер на конфликта в списъка.
         */
        private int id;

        /**
         * Път до файла на календара.
         */
        private final String filePath;

        /**
         * Показва дали конфликтът е в текущия календар.
         */
        private final boolean inCurrentCalendar;

        /**
         * Показва дали файлът е само за четене или без права за запис.
         */
        private final boolean readOnly;

        /**
         * Име на събитието.
         */
        private final String eventName;

        /**
         * Дата на събитието.
         */
        private final LocalDate date;

        /**
         * Начален час на събитието.
         */
        private final LocalTime start;

        /**
         * Краен час на събитието.
         */
        private final LocalTime end;

        /**
         * Маркер дали редът описва holiday конфликт.
         */
        private final boolean holidayMarker;

        /**
         * Създава обект за описание на конфликт.
         *
         * @param filePath път до файла на календара
         * @param inCurrentCalendar true, ако конфликтът е в текущия календар
         * @param readOnly true, ако файлът е само за четене или без права за запис
         * @param eventName име на събитието
         * @param date дата на събитието
         * @param start начален час на събитието
         * @param end краен час на събитието
         * @param holidayMarker true, ако редът описва holiday конфликт
         */
        private ConflictItem(String filePath,
                             boolean inCurrentCalendar,
                             boolean readOnly,
                             String eventName,
                             LocalDate date,
                             LocalTime start,
                             LocalTime end,
                             boolean holidayMarker) {
            this.filePath = filePath;
            this.inCurrentCalendar = inCurrentCalendar;
            this.readOnly = readOnly;
            this.eventName = eventName;
            this.date = date;
            this.start = start;
            this.end = end;
            this.holidayMarker = holidayMarker;
        }
    }

    /**
     * Ред за предварителен преглед на промяна.
     */
    private static class ChangePreview {
        /**
         * Файл, в който се прилага промяната.
         */
        private final String filePath;

        /**
         * Състояние преди промяната.
         */
        private final String before;

        /**
         * Състояние след промяната.
         */
        private final String after;

        /**
         * Създава ред за предварителен преглед.
         *
         * @param filePath файл на промяната
         * @param before текст преди промяната
         * @param after текст след промяната
         */
        private ChangePreview(String filePath, String before, String after) {
            this.filePath = filePath;
            this.before = before;
            this.after = after;
        }
    }

    /**
     * Създава инстанция на командата.
     */
    public AddHolidayCommand() {
    }

    /**
     * Изпълнява командата holiday.
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
            throw new InvalidCommandException("Употреба: holiday <yyyy-MM-dd>");
        }

        LocalDate date = LocalDate.parse(args[1]);

        String currentFilePath = service.getCurrentFilePath();
        if (currentFilePath == null) {
            throw new InvalidCommandException("Няма отворен файл. Използвайте първо open <filePath>.");
        }

        String normalizedCurrentPath = normalizePath(currentFilePath);
        Map<String, CalendarContext> allCalendars = loadAllCalendars(context, service, normalizedCurrentPath);
        boolean localHolidayConflict = allCalendars.get(normalizedCurrentPath).isHoliday(date);
        boolean otherHolidayConflict = hasHolidayInOtherCalendars(allCalendars, normalizedCurrentPath, date);

        List<ConflictItem> localConflicts = collectConflictsForDate(allCalendars, normalizedCurrentPath, date, true);
        List<ConflictItem> otherConflicts = collectConflictsForDate(allCalendars, normalizedCurrentPath, date, false);
        boolean hasLocalConflict = localHolidayConflict || !localConflicts.isEmpty();
        boolean hasOtherConflict = otherHolidayConflict || !otherConflicts.isEmpty();

        if (!hasLocalConflict && !hasOtherConflict) {
            context.addHoliday(date);
            System.out.println("Добавен е неработен ден: " + date);
            return;
        }

        if (hasLocalConflict && !hasOtherConflict) {
            handleLocalConflict(context, service, consoleScanner, date, normalizedCurrentPath, localConflicts);
            return;
        }

        if (!hasLocalConflict) {
            handleOnlyOtherConflicts(context, service, consoleScanner, date, normalizedCurrentPath, allCalendars, otherConflicts);
            return;
        }

        handleBothLocalAndOtherConflicts(context,
                service,
                consoleScanner,
                date,
                normalizedCurrentPath,
                allCalendars,
                localConflicts,
                otherConflicts);
    }

    /**
     * Обработва конфликт само в текущия календар.
     *
     * @param context текущ контекст
     * @param service календарна услуга
     * @param consoleScanner четец от конзолата
     * @param date целева дата
     * @param currentFilePath текущ файл
     * @param localConflicts локални конфликти
     * @throws Exception при грешка
     */
    private void handleLocalConflict(CalendarContext context,
                                     CalendarService service,
                                     Scanner consoleScanner,
                                     LocalDate date,
                                     String currentFilePath,
                                     List<ConflictItem> localConflicts) throws Exception {
        System.out.println("Конфликт в този календар");
        System.out.println("Дата: " + date);
        printConflictList(localConflicts);

        while (true) {
            System.out.println("Опции:");
            System.out.println("- clearday   : изтрива конфликтните събития за тази дата в текущия календар");
            System.out.println("- reschedule : премества конфликтните събития към първия възможен работен ден в текущия календар");
            System.out.println("- cancel     : отказва операцията без промени");
            System.out.print("Въведете команда: ");
            String choice = consoleScanner.nextLine().trim();
            if ("cancel".equalsIgnoreCase(choice)) {
                System.out.println("Операцията е отказана.");
                return;
            }
            if ("clearday".equalsIgnoreCase(choice)) {
                int removed = service.clearDay(context, date);
                context.addHoliday(date);
                System.out.println("Изтрити локални събития: " + removed + ". Денят е добавен като holiday: " + date);
                return;
            }

            if ("reschedule".equalsIgnoreCase(choice)) {
                service.rescheduleDay(context, date);
                context.addHoliday(date);
                System.out.println("Събитията са пренасрочени. Денят е добавен като holiday: " + date);
                return;
            }

            System.out.println("Невалидна опция. Въведете clearday, reschedule или cancel.");
        }
    }

    /**
     * Обработва конфликт само в други календари.
     *
     * @param context текущ контекст
     * @param service календарна услуга
     * @param consoleScanner четец от конзолата
     * @param date целева дата
     * @param currentFilePath текущ файл
     * @param allCalendars всички календари
     * @param otherConflicts конфликти в други календари
     * @throws Exception при грешка
     */
    private void handleOnlyOtherConflicts(CalendarContext context,
                                          CalendarService service,
                                          Scanner consoleScanner,
                                          LocalDate date,
                                          String currentFilePath,
                                          Map<String, CalendarContext> allCalendars,
                                          List<ConflictItem> otherConflicts) throws Exception {
        System.out.println("Конфлик в друг календар");
        printConflictList(otherConflicts);

        while (true) {
            System.out.println("Опции:");
            System.out.println("- clearday    : стартира глобално изчистване на избрани конфликтни събития");
            System.out.println("- reschedule  : стартира глобално пренасрочване на избрани конфликтни събития");
            System.out.println("- nonrelevant : игнорира конфликтите в други календари и добавя holiday само тук");
            System.out.println("- cancel      : отказва операцията без промени");
            System.out.print("Въведете команда: ");
            String choice = consoleScanner.nextLine().trim();
            if ("cancel".equalsIgnoreCase(choice)) {
                System.out.println("Операцията е отказана.");
                return;
            }
            if ("nonrelevant".equalsIgnoreCase(choice)) {
                context.addHoliday(date);
                System.out.println("Добавен е неработен ден само в текущия календар: " + date);
                return;
            }
            if ("clearday".equalsIgnoreCase(choice) || "reschedule".equalsIgnoreCase(choice)) {
                runGlobalResolveFlow(context, service, consoleScanner, date, currentFilePath, allCalendars, false);
                return;
            }
            System.out.println("Невалидна опция. Въведете clearday, reschedule, cancel или nonrelevant.");
        }
    }

    /**
     * Обработва конфликт едновременно в текущия и в други календари.
     *
     * @param context текущ контекст
     * @param service календарна услуга
     * @param consoleScanner четец от конзолата
     * @param date целева дата
     * @param currentFilePath текущ файл
     * @param allCalendars всички календари
     * @param localConflicts локални конфликти
     * @param otherConflicts други конфликти
     * @throws Exception при грешка
     */
    private void handleBothLocalAndOtherConflicts(CalendarContext context,
                                                  CalendarService service,
                                                  Scanner consoleScanner,
                                                  LocalDate date,
                                                  String currentFilePath,
                                                  Map<String, CalendarContext> allCalendars,
                                                  List<ConflictItem> localConflicts,
                                                  List<ConflictItem> otherConflicts) throws Exception {
        System.out.println("Има конфликт в този календар и в друг календар.");
        System.out.println("Локални конфликти:");
        printConflictList(localConflicts);
        System.out.println("Конфликти в други календари:");
        printConflictList(otherConflicts);

        while (true) {
            System.out.println("Избери:");
            System.out.println("1. Искам да разреша конфликта само в този календар (локални действия clearday/reschedule)");
            System.out.println("2. Искам да разреша конфликата във всички файлове (глобален процес с избор по ID)");
            System.out.println("3. cancel (отказ без промени)");
            System.out.print("Въведете 1, 2 или 3: ");
            String choice = consoleScanner.nextLine().trim();

            if ("1".equals(choice)) {
                handleLocalConflict(context, service, consoleScanner, date, currentFilePath, localConflicts);
                return;
            }
            if ("2".equals(choice)) {
                runGlobalResolveFlow(context, service, consoleScanner, date, currentFilePath, allCalendars, true);
                return;
            }
            if ("3".equals(choice) || "cancel".equalsIgnoreCase(choice)) {
                System.out.println("Операцията е отказана.");
                return;
            }
            System.out.println("Невалидна опция. Изберете 1, 2 или 3.");
        }
    }

    /**
     * Изпълнява глобалния процес за разрешаване на конфликти във всички файлове.
     *
     * @param currentContext контекстът на текущо отворения календар
     * @param service календарна услуга
     * @param consoleScanner четец от конзолата
     * @param holidayDate дата, която се добавя като неработен ден
     * @param currentFilePath път до текущо отворения календар
     * @param sourceCalendars изходни календарни контексти по файлове
     * @param includeCurrentConflicts true, когато трябва да се включат и конфликтите от текущия календар
     * @throws Exception при грешка по време на интерактивната обработка
     */
    private void runGlobalResolveFlow(CalendarContext currentContext,
                                      CalendarService service,
                                      Scanner consoleScanner,
                                      LocalDate holidayDate,
                                      String currentFilePath,
                                      Map<String, CalendarContext> sourceCalendars,
                                      boolean includeCurrentConflicts) throws Exception {
        Map<String, CalendarContext> stagedCalendars = cloneCalendars(sourceCalendars);
        List<ConflictItem> conflicts = buildNumberedConflictList(stagedCalendars, currentFilePath, holidayDate, includeCurrentConflicts);

        printGlobalConflicts(conflicts, includeCurrentConflicts);
        if (conflicts.isEmpty()) {
            System.out.println("Няма конфликти за обработка.");
            return;
        }

        Set<Integer> idsRescheduled;
        Set<Integer> idsCleared;

        while (true) {
            System.out.println("За да разрешим всички конфликти изберете кои дни как да се променят, ако не изброите опция за конкретен конфликт се счита, че не искате промяна там:");
            System.out.println("Лист с конфликти:");
            printNumberedConflictList(conflicts);
            System.out.println("Формат: days_rescheduled <номер> <номер> ...");
            System.out.println("Пример: days_rescheduled 1");
            System.out.println("След изпълнение конфликт №1 ще бъде маркиран за rescheduled.");
            idsRescheduled = readIdsCommand(consoleScanner, "days_rescheduled", conflicts.size());

            System.out.println("Формат: days_cleared <номер> <номер> ...");
            System.out.println("Пример: days_cleared 2 3");
            System.out.println("След изпълнение конфликт №2 и №3 ще бъдат маркирани за cleared.");
            idsCleared = readIdsCommand(consoleScanner, "days_cleared", conflicts.size());

            Set<Integer> overlap = new HashSet<Integer>(idsRescheduled);
            overlap.retainAll(idsCleared);
            if (overlap.isEmpty()) {
                break;
            }

            System.out.println("Следният/ следните конфликти са избрани и за двете дейстивя days_rescheduled ... и days_cleared ..  моля изберете само едно :");
            System.out.println("Лист с оставащи конфликти:");
            for (Integer id : overlap) {
                ConflictItem item = conflicts.get(id.intValue() - 1);
                System.out.println(id + ". " + formatConflictLine(item));
            }
        }

        List<ChangePreview> preview = new ArrayList<ChangePreview>();
        Set<String> changedFiles = new HashSet<String>();

        for (Integer id : idsRescheduled) {
            ConflictItem item = conflicts.get(id.intValue() - 1);
            if (item.holidayMarker) {
                continue;
            }
            CalendarContext fileContext = stagedCalendars.get(item.filePath);
            bg.tu_varna.sit.models.Event event = findMatchingEvent(fileContext, item);
            if (event == null) {
                continue;
            }
            String before = formatEventLine(item.filePath, event);
            service.moveEventToFirstAvailableWorkingDay(fileContext, event, holidayDate.plusDays(1));
            String after = formatEventLine(item.filePath, event) + " *преместен*";
            preview.add(new ChangePreview(item.filePath, before, after));
            changedFiles.add(item.filePath);
        }

        for (Integer id : idsCleared) {
            ConflictItem item = conflicts.get(id.intValue() - 1);
            if (item.holidayMarker) {
                continue;
            }
            CalendarContext fileContext = stagedCalendars.get(item.filePath);
            bg.tu_varna.sit.models.Event event = findMatchingEvent(fileContext, item);
            if (event == null) {
                continue;
            }
            String before = formatEventLine(item.filePath, event);
            fileContext.removeEvent(event);
            preview.add(new ChangePreview(item.filePath, before, "*изтрит*"));
            changedFiles.add(item.filePath);
        }

        for (String changedFile : changedFiles) {
            CalendarContext changedContext = stagedCalendars.get(changedFile);
            changedContext.addHoliday(holidayDate);
        }

        System.out.println("Промени по конфликти:");
        if (preview.isEmpty()) {
            System.out.println("Няма избрани промени.");
        } else {
            for (ChangePreview row : preview) {
                if ("*изтрит*".equals(row.after)) {
                    System.out.println(row.before + " " + row.after);
                } else {
                    System.out.println(row.before + " променен на -> " + row.after);
                }
            }
        }

        while (true) {
            System.out.println("Запази промените?");
            System.out.println("- yes : запазва всички приложени промени във файловете (където е възможно)");
            System.out.println("- no  : отказва всички промени от тази операция");
            System.out.print("Въведете yes или no: ");
            String answer = consoleScanner.nextLine().trim();
            if ("no".equalsIgnoreCase(answer)) {
                System.out.println("Промените са отказани.");
                return;
            }
            if ("yes".equalsIgnoreCase(answer)) {
                applyGlobalChanges(currentContext, service, currentFilePath, stagedCalendars, changedFiles);
                return;
            }
            System.out.println("Моля въведете yes или no.");
        }
    }

    /**
     * Прилага промените по всички избрани файлове с частичен commit и summary.
     *
     * @param currentContext контекстът на текущо отворения календар
     * @param service календарна услуга
     * @param currentFilePath път до текущия календарен файл
     * @param stagedCalendars временни контексти с подготвени промени
     * @param changedFiles списък с файлове, които трябва да се запишат
     */
    private void applyGlobalChanges(CalendarContext currentContext,
                                    CalendarService service,
                                    String currentFilePath,
                                    Map<String, CalendarContext> stagedCalendars,
                                    Set<String> changedFiles) {
        List<String> success = new ArrayList<String>();
        List<String> fail = new ArrayList<String>();

        for (String filePath : changedFiles) {
            try {
                service.saveCalendarToFile(filePath, stagedCalendars.get(filePath));
                if (filePath.equals(currentFilePath)) {
                    currentContext.replaceWith(stagedCalendars.get(filePath));
                }
                success.add(filePath);
            } catch (Exception e) {
                StringBuilder reason = new StringBuilder();
                reason.append(filePath).append(" -> ").append(e.getMessage());
                if (isReadOnlyOrNoRights(filePath)) {
                    reason.append(" <- read-only/no rights");
                }
                fail.add(reason.toString());
            }
        }

        System.out.println("Summary:");
        if (success.isEmpty()) {
            System.out.println("Успешни промени: няма");
        } else {
            System.out.println("Успешни промени:");
            for (String file : success) {
                System.out.println("- " + file);
            }
        }

        if (fail.isEmpty()) {
            System.out.println("Неуспешни промени: няма");
        } else {
            System.out.println("Неуспешни промени:");
            for (String row : fail) {
                System.out.println("- " + row);
            }
        }
    }

    /**
     * Зарежда всички валидни XML календари в workspace (без top-level папка samples).
     *
     * @param currentContext контекстът на текущо отворения календар
     * @param service календарна услуга
     * @param currentFilePath път до текущо отворения календар
     * @return карта с всички заредени календари по път до файл
     * @throws CalendarFileException при грешка при достъп до календарен файл
     */
    private Map<String, CalendarContext> loadAllCalendars(CalendarContext currentContext,
                                                          CalendarService service,
                                                          String currentFilePath) throws CalendarFileException {
        Map<String, CalendarContext> calendars = new LinkedHashMap<String, CalendarContext>();
        calendars.put(currentFilePath, cloneContext(currentContext));

        File workspaceRoot = new File(System.getProperty("user.dir"));
        File samplesDir = new File(workspaceRoot, "samples");

        List<File> xmlFiles = new ArrayList<File>();
        collectXmlFiles(workspaceRoot, samplesDir, xmlFiles);

        for (File xmlFile : xmlFiles) {
            String filePath = normalizePath(xmlFile.getAbsolutePath());
            if (filePath.equals(currentFilePath)) {
                continue;
            }
            try {
                CalendarContext loaded = service.loadCalendarFromFile(filePath);
                calendars.put(filePath, loaded);
            } catch (CalendarFileException e) {
                // Прескача невалидни или несвързани XML файлове.
            }
        }
        return calendars;
    }

    /**
     * Събира рекурсивно XML файлове, пропускайки top-level папка samples.
     *
     * @param directory директория за обход
     * @param samplesDir top-level директория samples за пропускане
     * @param xmlFiles списък, в който се добавят намерените XML файлове
     */
    private void collectXmlFiles(File directory, File samplesDir, List<File> xmlFiles) {
        if (directory == null || !directory.exists()) {
            return;
        }

        if (sameFile(directory, samplesDir)) {
            return;
        }

        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                collectXmlFiles(child, samplesDir, xmlFiles);
            } else if (child.isFile() && child.getName().toLowerCase().endsWith(".xml")) {
                xmlFiles.add(child);
            }
        }
    }

    /**
     * Създава списък с конфликти за конкретна дата.
     *
     * @param calendars карта с календарни контексти по файл
     * @param currentFilePath път до текущия календарен файл
     * @param date дата за проверка на конфликти
     * @param localOnly true, ако трябва да се проверява само текущият календар
     * @return списък с намерените конфликти
     */
    private List<ConflictItem> collectConflictsForDate(Map<String, CalendarContext> calendars,
                                                       String currentFilePath,
                                                       LocalDate date,
                                                       boolean localOnly) {
        List<ConflictItem> list = new ArrayList<ConflictItem>();
        for (Map.Entry<String, CalendarContext> entry : calendars.entrySet()) {
            boolean isCurrent = entry.getKey().equals(currentFilePath);
            if (localOnly && !isCurrent) {
                continue;
            }
            if (!localOnly && isCurrent) {
                continue;
            }

            CalendarContext fileContext = entry.getValue();
            boolean readOnly = isReadOnlyOrNoRights(entry.getKey());
            List<bg.tu_varna.sit.models.Event> dayEvents = serviceEventsForDate(fileContext, date);
            for (bg.tu_varna.sit.models.Event event : dayEvents) {
                list.add(new ConflictItem(entry.getKey(), isCurrent, readOnly,
                        event.getName(), event.getDate(), event.getStartTime(), event.getEndTime(), false));
            }
        }
        return list;
    }

    /**
     * Създава номериран списък с конфликти за глобално разрешаване.
     *
     * @param calendars карта с календарни контексти по файл
     * @param currentFilePath път до текущия календарен файл
     * @param date дата за проверка на конфликти
     * @param includeCurrent true, ако трябва да се включат конфликтите от текущия календар
     * @return номериран списък с конфликти
     */
    private List<ConflictItem> buildNumberedConflictList(Map<String, CalendarContext> calendars,
                                                         String currentFilePath,
                                                         LocalDate date,
                                                         boolean includeCurrent) {
        List<ConflictItem> result = new ArrayList<ConflictItem>();

        for (Map.Entry<String, CalendarContext> entry : calendars.entrySet()) {
            boolean isCurrent = entry.getKey().equals(currentFilePath);
            if (!includeCurrent && isCurrent) {
                continue;
            }

            CalendarContext fileContext = entry.getValue();
            boolean readOnly = isReadOnlyOrNoRights(entry.getKey());
            List<bg.tu_varna.sit.models.Event> dayEvents = serviceEventsForDate(fileContext, date);
            for (bg.tu_varna.sit.models.Event event : dayEvents) {
                result.add(new ConflictItem(entry.getKey(), isCurrent, readOnly,
                        event.getName(), event.getDate(), event.getStartTime(), event.getEndTime(), false));
            }
        }

        int id = 1;
        for (ConflictItem item : result) {
            item.id = id;
            id++;
        }
        return result;
    }

    /**
     * Отпечатва глобалния списък с конфликти.
     *
     * @param conflicts списък с конфликти
     * @param includeCurrent true, ако трябва да се показват и конфликтите от текущия календар
     */
    private void printGlobalConflicts(List<ConflictItem> conflicts, boolean includeCurrent) {
        System.out.println("Конфликти:");
        boolean hasCurrent = false;
        for (ConflictItem item : conflicts) {
            if (item.inCurrentCalendar) {
                hasCurrent = true;
                break;
            }
        }

        if (!includeCurrent || !hasCurrent) {
            System.out.println("Current calendar: no conflicts");
        } else {
            System.out.println("Current calendar:");
            for (ConflictItem item : conflicts) {
                if (item.inCurrentCalendar) {
                    System.out.println(item.id + ". " + formatConflictLine(item));
                }
            }
        }

        System.out.println("All other calendars:");
        for (ConflictItem item : conflicts) {
            if (!item.inCurrentCalendar) {
                System.out.println(item.id + ". " + formatConflictLine(item));
            }
        }
    }

    /**
     * Отпечатва локален/друг списък с конфликти.
     *
     * @param conflicts списък с конфликти за печат
     */
    private void printConflictList(List<ConflictItem> conflicts) {
        if (conflicts.isEmpty()) {
            System.out.println("Няма събития за конфликт.");
            return;
        }
        int index = 1;
        for (ConflictItem item : conflicts) {
            System.out.println(index + ". " + formatConflictLine(item));
            index++;
        }
    }

    /**
     * Чете IDs от контекстна команда days_rescheduled/days_cleared.
     *
     * @param scanner четец от конзолата
     * @param commandName очаквано име на контекстната команда
     * @param maxId максимално допустим ID номер
     * @return множество от избрани ID номера
     */
    private Set<Integer> readIdsCommand(Scanner scanner, String commandName, int maxId) {
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.length() == 0) {
                System.out.println("Невалиден формат. Опитайте отново.");
                continue;
            }

            StringTokenizer tokenizer = new StringTokenizer(line);
            if (!tokenizer.hasMoreTokens()) {
                System.out.println("Невалиден формат. Опитайте отново.");
                continue;
            }

            String command = tokenizer.nextToken();
            if (!commandName.equals(command)) {
                System.out.println("Невалиден формат. Използвайте точно: " + commandName + " <номер> <номер> ...");
                continue;
            }

            Set<Integer> ids = new HashSet<Integer>();
            boolean invalid = false;
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int id;
                try {
                    id = Integer.parseInt(token);
                } catch (NumberFormatException ex) {
                    invalid = true;
                    break;
                }
                if (id < 1 || id > maxId) {
                    invalid = true;
                    break;
                }
                ids.add(Integer.valueOf(id));
            }

            if (invalid) {
                System.out.println("Невалиден списък от ID. Опитайте отново.");
                continue;
            }

            return ids;
        }
    }

    /**
     * Форматира ред за конфликт.
     *
     * @param item елемент конфликт за форматиране
     * @return форматиран текст на конфликта
     */
    private String formatConflictLine(ConflictItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append(new File(item.filePath).getName()).append(" ")
                .append(item.eventName).append(" ")
                .append(item.date).append(" ")
                .append(item.start == null ? "-" : item.start).append(" ")
                .append(item.end == null ? "-" : item.end);
        if (item.readOnly) {
            builder.append(" <- read-only/no rights");
        }
        return builder.toString();
    }

    /**
     * Отпечатва номериран списък на конфликтите в кратък формат.
     *
     * @param conflicts списък с конфликти
     */
    private void printNumberedConflictList(List<ConflictItem> conflicts) {
        for (ConflictItem item : conflicts) {
            System.out.println(item.id + ". " + formatConflictLine(item));
        }
    }

    /**
     * Форматира събитие за preview/summary.
     *
     * @param filePath път до файла на календара
     * @param event събитие за форматиране
     * @return форматиран ред за събитието
     */
    private String formatEventLine(String filePath, bg.tu_varna.sit.models.Event event) {
        return new File(filePath).getName() + " " + event.getName() + " " + event.getDate() + " "
                + event.getStartTime() + " " + event.getEndTime();
    }

    /**
     * Намира събитие по ключови полета в даден контекст.
     *
     * @param context календарен контекст
     * @param item конфликтен елемент с търсените ключови полета
     * @return намерено събитие или null
     */
    private bg.tu_varna.sit.models.Event findMatchingEvent(CalendarContext context, ConflictItem item) {
        for (bg.tu_varna.sit.models.Event event : context.getEvents()) {
            if (!event.getDate().equals(item.date)) {
                continue;
            }
            if (item.start != null && !event.getStartTime().equals(item.start)) {
                continue;
            }
            if (item.end != null && !event.getEndTime().equals(item.end)) {
                continue;
            }
            if (!event.getName().equals(item.eventName)) {
                continue;
            }
            return event;
        }
        return null;
    }

    /**
     * Връща събития за ден от контекст.
     *
     * @param context календарен контекст
     * @param date дата за филтриране
     * @return списък със събития за датата
     */
    private List<bg.tu_varna.sit.models.Event> serviceEventsForDate(CalendarContext context, LocalDate date) {
        List<bg.tu_varna.sit.models.Event> events = new ArrayList<bg.tu_varna.sit.models.Event>();
        for (bg.tu_varna.sit.models.Event event : context.getEvents()) {
            if (event.getDate().equals(date)) {
                events.add(event);
            }
        }
        return events;
    }

    /**
     * Проверява дали в други календари датата е вече обявена за holiday.
     *
     * @param calendars карта с календари
     * @param currentFilePath път до текущия календарен файл
     * @param date дата за проверка
     * @return true, когато в поне един друг календар датата е holiday
     */
    private boolean hasHolidayInOtherCalendars(Map<String, CalendarContext> calendars,
                                               String currentFilePath,
                                               LocalDate date) {
        for (Map.Entry<String, CalendarContext> entry : calendars.entrySet()) {
            if (entry.getKey().equals(currentFilePath)) {
                continue;
            }
            if (entry.getValue().isHoliday(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверява права за запис във файл.
     *
     * @param filePath път до файла
     * @return true, когато файлът е само за четене или няма права за запис
     */
    private boolean isReadOnlyOrNoRights(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return !file.canWrite();
        }
        File parent = file.getParentFile();
        return parent == null || !parent.canWrite();
    }

    /**
     * Нормализира файлов път до canonical.
     *
     * @param path входен файлов път
     * @return нормализиран път
     */
    private String normalizePath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            return new File(path).getAbsolutePath();
        }
    }

    /**
     * Проверява дали два File обекта сочат към един и същи файл.
     *
     * @param a първи файлов обект
     * @param b втори файлов обект
     * @return true, когато сочат към един и същи файл
     */
    private boolean sameFile(File a, File b) {
        if (a == null || b == null) {
            return false;
        }
        try {
            return a.getCanonicalPath().equals(b.getCanonicalPath());
        } catch (IOException e) {
            return a.getAbsolutePath().equals(b.getAbsolutePath());
        }
    }

    /**
     * Клонира един календарен контекст.
     *
     * @param source изходен календарен контекст
     * @return копие на контекста
     */
    private CalendarContext cloneContext(CalendarContext source) {
        CalendarContext clone = new CalendarContext();
        for (LocalDate holiday : source.getHolidays()) {
            clone.addHoliday(holiday);
        }
        for (bg.tu_varna.sit.models.Event event : source.getEvents()) {
            clone.addEvent(new bg.tu_varna.sit.models.Event(
                    event.getDate(),
                    event.getStartTime(),
                    event.getEndTime(),
                    event.getName(),
                    event.getNote()
            ));
        }
        return clone;
    }

    /**
     * Клонира карта от календари.
     *
     * @param source изходна карта от календари
     * @return копие на картата с календари
     */
    private Map<String, CalendarContext> cloneCalendars(Map<String, CalendarContext> source) {
        Map<String, CalendarContext> clone = new HashMap<String, CalendarContext>();
        for (Map.Entry<String, CalendarContext> entry : source.entrySet()) {
            clone.put(entry.getKey(), cloneContext(entry.getValue()));
        }
        return clone;
    }
}
