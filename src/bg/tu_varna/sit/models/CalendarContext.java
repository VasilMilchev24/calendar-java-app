package bg.tu_varna.sit.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Агрегатен корен, който съхранява всички календарни данни в паметта.
 */
public class CalendarContext {

    /**
     * Списък със записани събития.
     */
    private final List<Event> events;

    /**
     * Списък с дати на неработни дни.
     */
    private final List<LocalDate> holidays;

    /**
     * Показва дали цикълът на ядрото трябва да спре.
     */
    private boolean exitRequested;

    /**
     * Създава празен контекст.
     */
    public CalendarContext() {
        this.events = new ArrayList<Event>();
        this.holidays = new ArrayList<LocalDate>();
        this.exitRequested = false;
    }

    /**
     * Връща само за четене изглед към събитията.
     *
     * @return непроменяем списък със събития
     */
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Връща само за четене изглед към неработните дни.
     *
     * @return непроменяем списък с неработни дни
     */
    public List<LocalDate> getHolidays() {
        return Collections.unmodifiableList(holidays);
    }

    /**
     * Добавя събитие.
     *
     * @param event събитие за добавяне
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    /**
     * Премахва събитие.
     *
     * @param event събитие за премахване
     */
    public void removeEvent(Event event) {
        events.remove(event);
    }

    /**
     * Добавя неработен ден, ако още не съществува.
     *
     * @param holiday дата на неработен ден
     */
    public void addHoliday(LocalDate holiday) {
        if (!holidays.contains(holiday)) {
            holidays.add(holiday);
        }
    }

    /**
     * Премахва неработен ден.
     *
     * @param holiday дата на неработен ден
     */
    public void removeHoliday(LocalDate holiday) {
        holidays.remove(holiday);
    }

    /**
     * Проверява дали денят е неработен.
     *
     * @param date дата за проверка
     * @return true, когато датата е неработен ден
     */
    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }

    /**
     * Заменя всички данни с данните от друг контекст.
     *
     * @param other изходен контекст
     */
    public void replaceWith(CalendarContext other) {
        this.events.clear();
        this.events.addAll(other.getEvents());
        this.holidays.clear();
        this.holidays.addAll(other.getHolidays());
    }

    /**
     * Изчиства всички събития и неработни дни.
     */
    public void clear() {
        events.clear();
        holidays.clear();
    }

    /**
     * Заявява изход от приложението.
     */
    public void requestExit() {
        this.exitRequested = true;
    }

    /**
     * Връща дали е заявено излизане.
     *
     * @return true, когато цикълът на ядрото трябва да спре
     */
    public boolean isExitRequested() {
        return exitRequested;
    }
}

