package bg.tu_varna.sit.services;

import bg.tu_varna.sit.exceptions.CalendarFileException;
import bg.tu_varna.sit.exceptions.EventConflictException;
import bg.tu_varna.sit.exceptions.InvalidCommandException;
import bg.tu_varna.sit.models.BusyDayInfo;
import bg.tu_varna.sit.models.CalendarContext;
import bg.tu_varna.sit.models.Event;
import bg.tu_varna.sit.models.MergeConflict;
import bg.tu_varna.sit.models.RescheduleResult;
import bg.tu_varna.sit.models.TimeSlot;
import bg.tu_varna.sit.repository.CalendarRepository;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * Капсулира бизнес логиката и правилата за оркестрация на календара.
 */
public class CalendarService {

    /**
     * Начало на работния ден.
     */
    public static final LocalTime WORK_START = LocalTime.of(8, 0);

    /**
     * Край на работния ден.
     */
    public static final LocalTime WORK_END = LocalTime.of(17, 0);

    /**
     * Реализация на календарното хранилище.
     */
    private final CalendarRepository repository;

    /**
     * Път на текущо отворения файл.
     */
    private String currentFilePath;

    /**
     * Създава инстанция на услугата.
     *
     * @param repository зависимост към хранилище
     */
    public CalendarService(CalendarRepository repository) {
        this.repository = repository;
        this.currentFilePath = null;
    }

    /**
     * Отваря календарен файл и заменя съдържанието на контекста.
     *
     * @param context целеви контекст в паметта
     * @param filePath път до изходния файл
     * @throws InvalidCommandException когато вече има отворен файл
     * @throws CalendarFileException при грешка при работа с файл
     */
    public void open(CalendarContext context, String filePath)
            throws CalendarFileException, InvalidCommandException {
        if (currentFilePath != null) {
            throw new InvalidCommandException("За да отвориш нов файл първо затвори този с \"close\" или запази с \"save/saveas\".");
        }
        CalendarContext loaded = repository.load(filePath);
        context.replaceWith(loaded);
        currentFilePath = filePath;
    }

    /**
     * Записва контекста в текущо отворения файл.
     *
     * @param context изходен контекст
     * @throws CalendarFileException при неуспешен запис
     * @throws InvalidCommandException когато няма отворен файл
     */
    public void save(CalendarContext context) throws CalendarFileException, InvalidCommandException {
        if (currentFilePath == null) {
            throw new InvalidCommandException("Няма отворен файл. Използвайте първо open <filePath>.");
        }
        repository.save(currentFilePath, context);
    }

    /**
     * Записва контекста в нов път и го маркира като текущ.
     *
     * @param context изходен контекст
     * @param filePath път до целевия файл
     * @throws CalendarFileException при неуспешен запис
     */
    public void saveAs(CalendarContext context, String filePath) throws CalendarFileException {
        repository.save(filePath, context);
        currentFilePath = filePath;
    }

    /**
     * Затваря текущия файл и изчиства контекста.
     *
     * @param context целеви контекст
     */
    public void close(CalendarContext context) {
        context.clear();
        currentFilePath = null;
    }

    /**
     * Връща пътя на текущо отворения файл.
     *
     * @return текущ път до файл или null
     */
    public String getCurrentFilePath() {
        return currentFilePath;
    }

    /**
     * Добавя събитие след проверка за припокриване.
     *
     * @param context целеви контекст
     * @param event събитие за добавяне
     * @throws EventConflictException при засечено припокриване
     * @throws InvalidCommandException при невалиден интервал на събитие
     */
    public void addEvent(CalendarContext context, Event event) throws EventConflictException, InvalidCommandException {
        validateEventInterval(event.getStartTime(), event.getEndTime());
        Event conflict = findConflictingEvent(context, event, null);
        if (conflict != null) {
            throw new EventConflictException("Събитието се припокрива със съществуващо събитие: " + conflict);
        }
        context.addEvent(event);
    }

    /**
     * Премахва събитие по ключови полета.
     *
     * @param context изходен контекст
     * @param date дата на събитието
     * @param start начален час
     * @param name име на събитието
     * @throws InvalidCommandException когато не съществува съвпадащо събитие
     */
    public void removeEvent(CalendarContext context, LocalDate date, LocalTime start, String name)
            throws InvalidCommandException {
        Event target = findExactEvent(context, date, start, name);
        if (target == null) {
            throw new InvalidCommandException("Не е намерено събитие за премахване с посочените данни.");
        }
        context.removeEvent(target);
    }

    /**
     * Променя съществуващо събитие с проверка за конфликт.
     *
     * @param context изходен контекст
     * @param oldDate текуща дата
     * @param oldStart текущ начален час
     * @param oldName текущо име
     * @param newEvent данни за замяна
     * @throws InvalidCommandException когато липсва изходно събитие или новият интервал е невалиден
     * @throws EventConflictException когато замяната се припокрива с друго събитие
     */
    public void changeEvent(CalendarContext context,
                            LocalDate oldDate,
                            LocalTime oldStart,
                            String oldName,
                            Event newEvent) throws InvalidCommandException, EventConflictException {
        validateEventInterval(newEvent.getStartTime(), newEvent.getEndTime());
        Event target = findExactEvent(context, oldDate, oldStart, oldName);
        if (target == null) {
            throw new InvalidCommandException("Не е намерено събитие за промяна с посочените данни.");
        }

        Event conflict = findConflictingEvent(context, newEvent, target);
        if (conflict != null) {
            throw new EventConflictException("Промененото събитие ще се припокрие със: " + conflict);
        }

        target.setDate(newEvent.getDate());
        target.setStartTime(newEvent.getStartTime());
        target.setEndTime(newEvent.getEndTime());
        target.setName(newEvent.getName());
        target.setNote(newEvent.getNote());
    }

    /**
     * Връща сортирана програма за конкретна дата.
     *
     * @param context изходен контекст
     * @param date заявена дата
     * @return сортиран списък със събития за датата
     */
    public List<Event> getAgendaForDate(CalendarContext context, LocalDate date) {
        List<Event> result = new ArrayList<Event>();
        for (Event event : context.getEvents()) {
            if (event.getDate().equals(date)) {
                result.add(event);
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Намира първия свободен интервал в работното време на основния календар.
     *
     * @param context изходен контекст
     * @param fromDate начална дата за търсене
     * @param durationHours продължителност на интервала в часове
     * @return първи свободен интервал или null
     * @throws InvalidCommandException при невалидна продължителност
     */
    public TimeSlot findSlot(CalendarContext context, LocalDate fromDate, int durationHours)
            throws InvalidCommandException {
        return findSlotWithSecondary(context, null, fromDate, durationHours);
    }

    /**
     * Намира първия свободен интервал едновременно в основния и вторичния календар.
     *
     * @param context изходен контекст
     * @param otherFile път до вторичния календар
     * @param fromDate начална дата за търсене
     * @param durationHours продължителност на интервала в часове
     * @return първи свободен интервал или null
     * @throws InvalidCommandException при невалидна продължителност
     * @throws CalendarFileException при неуспешно зареждане на вторичния календар
     */
    public TimeSlot findSlotWith(CalendarContext context, String otherFile, LocalDate fromDate, int durationHours)
            throws InvalidCommandException, CalendarFileException {
        CalendarContext secondary = repository.load(otherFile);
        return findSlotWithSecondary(context, secondary, fromDate, durationHours);
    }

    /**
     * Агрегира заетите часове по ден от седмицата в низходящ ред за зададен период.
     *
     * @param context изходен контекст
     * @param from начална дата на периода
     * @param to крайна дата на периода
     * @return сортирани обобщения за заети дни
     * @throws InvalidCommandException при невалиден период
     */
    public List<BusyDayInfo> calculateBusyDays(CalendarContext context, LocalDate from, LocalDate to)
            throws InvalidCommandException {
        if (from.isAfter(to)) {
            throw new InvalidCommandException("Началната дата трябва да е преди или равна на крайната дата.");
        }

        EnumMap<DayOfWeek, Long> map = new EnumMap<DayOfWeek, Long>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            map.put(day, Long.valueOf(0));
        }

        for (Event event : context.getEvents()) {
            if (event.getDate().isBefore(from) || event.getDate().isAfter(to)) {
                continue;
            }
            long minutes = Duration.between(event.getStartTime(), event.getEndTime()).toMinutes();
            DayOfWeek day = event.getDate().getDayOfWeek();
            long existing = map.get(day).longValue();
            map.put(day, Long.valueOf(existing + minutes));
        }

        List<BusyDayInfo> result = new ArrayList<BusyDayInfo>();
        for (DayOfWeek day : DayOfWeek.values()) {
            result.add(new BusyDayInfo(day, map.get(day).longValue()));
        }

        Collections.sort(result, new Comparator<BusyDayInfo>() {
            @Override
            public int compare(BusyDayInfo a, BusyDayInfo b) {
                if (a.getBusyMinutes() == b.getBusyMinutes()) {
                    return a.getDayOfWeek().compareTo(b.getDayOfWeek());
                }
                return Long.compare(b.getBusyMinutes(), a.getBusyMinutes());
            }
        });

        return result;
    }

    /**
     * Зарежда външен календар във временен контекст.
     *
     * @param filePath изходен XML файл
     * @return зареден временен контекст
     * @throws CalendarFileException при грешка при работа с файл
     */
    public CalendarContext loadTemporaryCalendar(String filePath) throws CalendarFileException {
        return repository.load(filePath);
    }

    /**
     * Изчислява конфликтите при сливане между текущи и импортнати събития.
     *
     * @param current текущ контекст
     * @param incoming входящ контекст
     * @return списък с конфликти
     */
    public List<MergeConflict> findMergeConflicts(CalendarContext current, CalendarContext incoming) {
        List<MergeConflict> conflicts = new ArrayList<MergeConflict>();
        for (Event incomingEvent : incoming.getEvents()) {
            Event conflict = findConflictingEvent(current, incomingEvent, null);
            if (conflict != null) {
                conflicts.add(new MergeConflict(conflict, incomingEvent));
            }
        }
        return conflicts;
    }

    /**
     * Намира конфликтно събитие в целевия контекст.
     *
     * @param context изходен контекст
     * @param candidate събитие за проверка
     * @param ignore събитие за игнориране при проверката
     * @return първо конфликтно събитие или null
     */
    public Event findConflictingEvent(CalendarContext context, Event candidate, Event ignore) {
        for (Event existing : context.getEvents()) {
            if (ignore != null && existing == ignore) {
                continue;
            }
            if (!existing.getDate().equals(candidate.getDate())) {
                continue;
            }
            if (isOverlap(existing.getStartTime(), existing.getEndTime(), candidate.getStartTime(), candidate.getEndTime())) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Добавя дата на неработен ден.
     *
     * @param context целеви контекст
     * @param holiday дата на неработен ден
     * @throws InvalidCommandException когато датата вече съдържа събития
     */
    public void addHoliday(CalendarContext context, LocalDate holiday) throws InvalidCommandException {
        addHoliday(context, holiday, true);
    }

    /**
     * Добавя дата на неработен ден с избор дали да се проверяват всички календари.
     *
     * @param context целеви контекст
     * @param holiday дата на неработен ден
     * @param checkAllCalendars true за проверка във всички валидни XML календари
     * @throws InvalidCommandException когато има конфликт
     */
    public void addHoliday(CalendarContext context, LocalDate holiday, boolean checkAllCalendars)
            throws InvalidCommandException {
        boolean localHoliday = context.isHoliday(holiday);
        boolean localEvents = hasEventsOnDate(context, holiday);

        boolean globalHoliday = false;
        boolean globalEvents = false;
        if (checkAllCalendars) {
            List<CalendarContext> otherCalendars = loadOtherCalendarContexts();
            for (CalendarContext otherContext : otherCalendars) {
                if (otherContext.isHoliday(holiday)) {
                    globalHoliday = true;
                }
                if (hasEventsOnDate(otherContext, holiday)) {
                    globalEvents = true;
                }
            }
        }

        if (localHoliday || localEvents || globalHoliday || globalEvents) {
            StringBuilder warning = new StringBuilder();
            warning.append("ВНИМАНИЕ: Открит е конфликт за тази дата:\n");
            if (localHoliday) {
                warning.append("- този ден вече е обявен за почивен в текущия календар.\n");
            }
            if (localEvents) {
                warning.append("- този ден вече има записани събития в текущия календар.\n");
            }
            if (globalHoliday) {
                warning.append("- този ден вече е обявен за почивен в друг календар.\n");
            }
            if (globalEvents) {
                warning.append("- този ден вече има записани събития в друг календар.\n");
            }
            warning.append("- ако искате да изтриете тези събитията за този ден напишете \"clearday <date>\". ");
            warning.append("После може да използвате \"holiday <date>\"\n");
            warning.append("- ако искате да презапишете събитията за първият възможен ден, за да можете да изчистите графика си за денят, ");
            warning.append("напишете \"reschedule <date>\". И после използвайте \"holiday <date>\"\n");
            warning.append("- ако искате да прекратите тази операция, напишете \"cancel\"");
            if (checkAllCalendars) {
                warning.append("\n- ако ви интересува само текущият календар, напишете \"nonrelevant\"");
            }
            throw new InvalidCommandException(warning.toString());
        }
        context.addHoliday(holiday);
    }

    /**
     * Проверява дали дата е неработен ден в текущия или в някой друг валиден календарен файл.
     *
     * @param context текущ контекст
     * @param date дата за проверка
     * @return true, когато датата е неработен ден поне в един календар
     */
    public boolean isHolidayAcrossAllCalendars(CalendarContext context, LocalDate date) {
        if (context.isHoliday(date)) {
            return true;
        }

        List<CalendarContext> otherCalendars = loadOtherCalendarContexts();
        for (CalendarContext otherContext : otherCalendars) {
            if (otherContext.isHoliday(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Изтрива всички събития за конкретна дата.
     *
     * @param context целеви контекст
     * @param date дата за изчистване
     * @return брой изтрити събития
     */
    public int clearDay(CalendarContext context, LocalDate date) {
        List<Event> eventsToRemove = getEventsByDate(context, date);
        for (Event event : eventsToRemove) {
            context.removeEvent(event);
        }
        return eventsToRemove.size();
    }

    /**
     * Изтрива всички събития за дата в конкретен календарен файл и записва промените.
     *
     * @param filePath път до календарен файл
     * @param date дата за изчистване
     * @return брой изтрити събития
     * @throws CalendarFileException при грешка при работа с файл
     */
    public int clearDay(String filePath, LocalDate date) throws CalendarFileException {
        CalendarContext fileContext = repository.load(filePath);
        int removed = clearDay(fileContext, date);
        repository.save(filePath, fileContext);
        return removed;
    }

    /**
     * Пренасрочва всички събития за конкретна дата към първия възможен работен ден.
     *
     * @param context целеви контекст
     * @param date дата, от която се преместват събитията
     * @return резултат от пренасрочването
     * @throws InvalidCommandException когато няма събития за пренасрочване или липсва възможна дата
     */
    public RescheduleResult rescheduleDay(CalendarContext context, LocalDate date) throws InvalidCommandException {
        List<Event> eventsToMove = getEventsByDate(context, date);
        if (eventsToMove.isEmpty()) {
            throw new InvalidCommandException("Няма събития за пренасрочване на дата: " + date);
        }

        LocalDate candidate = date.plusDays(1);
        int checkedDays = 0;
        while (checkedDays < 3650) {
            if (isWorkingDay(context, candidate) && canMoveAllEventsToDate(context, eventsToMove, candidate)) {
                for (Event event : eventsToMove) {
                    event.setDate(candidate);
                }
                return new RescheduleResult(candidate, eventsToMove.size());
            }
            candidate = candidate.plusDays(1);
            checkedDays++;
        }

        throw new InvalidCommandException("Не е намерен подходящ работен ден за пренасрочване.");
    }

    /**
     * Пренасрочва всички събития за дата в конкретен календарен файл и записва промените.
     *
     * @param filePath път до календарен файл
     * @param date дата за пренасрочване
     * @return резултат от пренасрочването
     * @throws InvalidCommandException при невалидно пренасрочване
     * @throws CalendarFileException при грешка при работа с файл
     */
    public RescheduleResult rescheduleDay(String filePath, LocalDate date)
            throws InvalidCommandException, CalendarFileException {
        CalendarContext fileContext = repository.load(filePath);
        RescheduleResult result = rescheduleDay(fileContext, date);
        repository.save(filePath, fileContext);
        return result;
    }

    /**
     * Зарежда календарен контекст от файл без промяна на текущо отворения файл.
     *
     * @param filePath път до календарен файл
     * @return зареден контекст
     * @throws CalendarFileException при грешка при работа с файл
     */
    public CalendarContext loadCalendarFromFile(String filePath) throws CalendarFileException {
        return repository.load(filePath);
    }

    /**
     * Записва календарен контекст във файл без промяна на текущо отворения файл.
     *
     * @param filePath път до календарен файл
     * @param context контекст за запис
     * @throws CalendarFileException при грешка при запис
     */
    public void saveCalendarToFile(String filePath, CalendarContext context) throws CalendarFileException {
        repository.save(filePath, context);
    }

    /**
     * Проверява дали ден е неработен в текущия календар.
     *
     * @param context контекст на календар
     * @param date дата за проверка
     * @return true, когато денят е събота, неделя или локален празник
     */
    public boolean isNonWorkingDayLocal(CalendarContext context, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return true;
        }
        return context.isHoliday(date);
    }

    /**
     * Връща копие на събитията за конкретна дата.
     *
     * @param context контекст на календар
     * @param date дата за филтриране
     * @return списък със събития за датата
     */
    public List<Event> getEventsByDateSnapshot(CalendarContext context, LocalDate date) {
        List<Event> source = getEventsByDate(context, date);
        List<Event> copy = new ArrayList<Event>();
        for (Event event : source) {
            copy.add(new Event(event.getDate(), event.getStartTime(), event.getEndTime(), event.getName(), event.getNote()));
        }
        return copy;
    }

    /**
     * Премества конкретно събитие към първия възможен работен ден в същия календар.
     *
     * @param context контекст на календар
     * @param event събитие за преместване
     * @param fromDate начална дата за търсене (включително)
     * @return новата дата на събитието
     * @throws InvalidCommandException при липса на възможна дата
     */
    public LocalDate moveEventToFirstAvailableWorkingDay(CalendarContext context, Event event, LocalDate fromDate)
            throws InvalidCommandException {
        LocalDate candidate = fromDate;
        int checkedDays = 0;
        while (checkedDays < 3650) {
            if (!isNonWorkingDayLocal(context, candidate)
                    && isIntervalFreeOnDate(context, candidate, event.getStartTime(), event.getEndTime(), event)) {
                event.setDate(candidate);
                return candidate;
            }
            candidate = candidate.plusDays(1);
            checkedDays++;
        }
        throw new InvalidCommandException("Не е намерен подходящ работен ден за преместване на събитието.");
    }

    /**
     * Премахва дата на неработен ден.
     *
     * @param context целеви контекст
     * @param holiday дата на неработен ден
     */
    public void removeHoliday(CalendarContext context, LocalDate holiday) {
        context.removeHoliday(holiday);
    }

    /**
     * Намира точно събитие по дата, начален час и име.
     *
     * @param context изходен контекст
     * @param date дата
     * @param start начален час
     * @param name име на събитието
     * @return съвпадащо събитие или null
     */
    private Event findExactEvent(CalendarContext context, LocalDate date, LocalTime start, String name) {
        for (Event event : context.getEvents()) {
            if (event.getDate().equals(date)
                    && event.getStartTime().equals(start)
                    && event.getName().equalsIgnoreCase(name)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Проверява дали има събития за конкретна дата.
     *
     * @param context изходен контекст
     * @param date дата за проверка
     * @return true, когато има поне едно събитие
     */
    private boolean hasEventsOnDate(CalendarContext context, LocalDate date) {
        for (Event event : context.getEvents()) {
            if (event.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Зарежда всички валидни XML календари от работната директория, различни от текущо отворения.
     * Невалидните или несвързани XML файлове се прескачат.
     *
     * @return списък с контексти от други календарни файлове
     */
    private List<CalendarContext> loadOtherCalendarContexts() {
        List<CalendarContext> contexts = new ArrayList<CalendarContext>();
        List<File> xmlFiles = new ArrayList<File>();
        collectXmlFiles(new File(System.getProperty("user.dir")), xmlFiles);

        for (File xmlFile : xmlFiles) {
            if (isCurrentOpenFile(xmlFile)) {
                continue;
            }

            try {
                CalendarContext loaded = repository.load(xmlFile.getAbsolutePath());
                contexts.add(loaded);
            } catch (CalendarFileException e) {
                // Прескача невалидни/несвързани XML файлове.
            }
        }
        return contexts;
    }

    /**
     * Събира рекурсивно всички XML файлове от директория.
     *
     * @param directory начална директория
     * @param xmlFiles списък с намерени XML файлове
     */
    private void collectXmlFiles(File directory, List<File> xmlFiles) {
        if (directory == null || !directory.exists()) {
            return;
        }

        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                collectXmlFiles(child, xmlFiles);
            } else if (child.isFile() && child.getName().toLowerCase().endsWith(".xml")) {
                xmlFiles.add(child);
            }
        }
    }

    /**
     * Проверява дали файлът е текущо отвореният календар.
     *
     * @param candidate файл за проверка
     * @return true, когато файлът е текущо отвореният
     */
    private boolean isCurrentOpenFile(File candidate) {
        if (currentFilePath == null) {
            return false;
        }

        try {
            String currentCanonical = new File(currentFilePath).getCanonicalPath();
            String candidateCanonical = candidate.getCanonicalPath();
            return currentCanonical.equals(candidateCanonical);
        } catch (IOException e) {
            return currentFilePath.equals(candidate.getAbsolutePath());
        }
    }

    /**
     * Връща списък със събития за конкретна дата, сортирани по час.
     *
     * @param context изходен контекст
     * @param date дата за филтриране
     * @return списък със събития за деня
     */
    private List<Event> getEventsByDate(CalendarContext context, LocalDate date) {
        List<Event> events = new ArrayList<Event>();
        for (Event event : context.getEvents()) {
            if (event.getDate().equals(date)) {
                events.add(event);
            }
        }
        Collections.sort(events);
        return events;
    }

    /**
     * Проверява дали всички събития могат да бъдат преместени на дадена дата без конфликт.
     *
     * @param context изходен контекст
     * @param eventsToMove събития за преместване
     * @param candidateDate дата кандидат
     * @return true, когато всички събития могат да се преместят
     */
    private boolean canMoveAllEventsToDate(CalendarContext context, List<Event> eventsToMove, LocalDate candidateDate) {
        for (Event eventToMove : eventsToMove) {
            if (!isFree(context, candidateDate, eventToMove.getStartTime(), eventToMove.getEndTime())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверява припокриването между два интервала.
     *
     * @param startA първи начален час
     * @param endA първи краен час
     * @param startB втори начален час
     * @param endB втори краен час
     * @return true, когато интервалите се припокриват
     */
    private boolean isOverlap(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    /**
     * Валидира интервала на събитието.
     *
     * @param start начален час
     * @param end краен час
     * @throws InvalidCommandException при невалиден интервал
     */
    private void validateEventInterval(LocalTime start, LocalTime end) throws InvalidCommandException {
        if (!start.isBefore(end)) {
            throw new InvalidCommandException("Началният час трябва да е преди крайния час.");
        }
    }

    /**
     * Намира интервал, като при нужда проверява и вторичен календар.
     *
     * @param primary основен контекст
     * @param secondary незадължителен вторичен контекст
     * @param fromDate начална дата
     * @param durationHours продължителност в часове
     * @return намерен интервал или null
     * @throws InvalidCommandException при невалидна продължителност
     */
    private TimeSlot findSlotWithSecondary(CalendarContext primary,
                                           CalendarContext secondary,
                                           LocalDate fromDate,
                                           int durationHours) throws InvalidCommandException {
        if (durationHours <= 0 || durationHours > 9) {
            throw new InvalidCommandException("Продължителността трябва да бъде между 1 и 9 часа.");
        }

        LocalDate day = fromDate;
        int checkedDays = 0;
        while (checkedDays < 3650) {
            if (isWorkingDay(primary, day)) {
                int maxStartHour = WORK_END.getHour() - durationHours;
                int hour = WORK_START.getHour();
                while (hour <= maxStartHour) {
                    LocalTime start = LocalTime.of(hour, 0);
                    LocalTime end = start.plusHours(durationHours);
                    if (isFree(primary, day, start, end)
                            && (secondary == null || isFree(secondary, day, start, end))) {
                        return new TimeSlot(day, start, end);
                    }
                    hour++;
                }
            }
            day = day.plusDays(1);
            checkedDays++;
        }
        return null;
    }

    /**
     * Проверява дали датата е работен ден.
     *
     * @param context изходен контекст
     * @param date дата за проверка
     * @return true, когато денят е работен
     */
    private boolean isWorkingDay(CalendarContext context, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        return !context.isHoliday(date);
    }

    /**
     * Проверява дали интервалът е свободен спрямо всички събития за датата.
     *
     * @param context изходен контекст
     * @param date целева дата
     * @param start начален час
     * @param end краен час
     * @return true, когато интервалът е свободен
     */
    private boolean isFree(CalendarContext context, LocalDate date, LocalTime start, LocalTime end) {
        for (Event event : context.getEvents()) {
            if (!event.getDate().equals(date)) {
                continue;
            }
            if (isOverlap(event.getStartTime(), event.getEndTime(), start, end)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверява дали интервал е свободен на дата, като игнорира конкретно събитие.
     *
     * @param context контекст на календар
     * @param date дата за проверка
     * @param start начален час
     * @param end краен час
     * @param ignoreEvent събитие за игнориране при проверката
     * @return true, когато интервалът е свободен
     */
    private boolean isIntervalFreeOnDate(CalendarContext context,
                                         LocalDate date,
                                         LocalTime start,
                                         LocalTime end,
                                         Event ignoreEvent) {
        for (Event existing : context.getEvents()) {
            if (ignoreEvent != null && existing == ignoreEvent) {
                continue;
            }
            if (!existing.getDate().equals(date)) {
                continue;
            }
            if (isOverlap(existing.getStartTime(), existing.getEndTime(), start, end)) {
                return false;
            }
        }
        return true;
    }
}

