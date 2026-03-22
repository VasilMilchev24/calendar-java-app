package bg.tu_varna.sit.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Представя календарно събитие с дата, времеви интервал, заглавие и бележка.
 */
public class Event implements Comparable<Event> {

    /**
     * Дата на събитието.
     */
    private LocalDate date;

    /**
     * Начален час на събитието.
     */
    private LocalTime startTime;

    /**
     * Краен час на събитието.
     */
    private LocalTime endTime;

    /**
     * Заглавие на събитието.
     */
    private String name;

    /**
     * Бележка към събитието.
     */
    private String note;

    /**
     * Конструктор по подразбиране.
     */
    public Event() {
    }

    /**
     * Създава инстанция на събитие.
     *
     * @param date дата на събитието
     * @param startTime начален час
     * @param endTime краен час
     * @param name име на събитието
     * @param note бележка към събитието
     */
    public Event(LocalDate date, LocalTime startTime, LocalTime endTime, String name, String note) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.note = note;
    }

    /**
     * Връща датата на събитието.
     *
     * @return дата
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Задава датата на събитието.
     *
     * @param date дата на събитието
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Връща началния час.
     *
     * @return начален час
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Задава началния час.
     *
     * @param startTime начален час
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Връща крайния час.
     *
     * @return краен час
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Задава крайния час.
     *
     * @param endTime краен час
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Връща името на събитието.
     *
     * @return име на събитието
     */
    public String getName() {
        return name;
    }

    /**
     * Задава името на събитието.
     *
     * @param name име на събитието
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Връща бележката към събитието.
     *
     * @return бележка към събитието
     */
    public String getNote() {
        return note;
    }

    /**
     * Задава бележката към събитието.
     *
     * @param note бележка към събитието
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Сравнява събития по дата и след това по начален час.
     *
     * @param other друго събитие
     * @return отрицателна, нулева или положителна стойност
     */
    @Override
    public int compareTo(Event other) {
        int dateCompare = this.date.compareTo(other.date);
        if (dateCompare != 0) {
            return dateCompare;
        }
        return this.startTime.compareTo(other.startTime);
    }

    /**
     * Връща текст за визуализация на събитието.
     *
     * @return форматиран текст на събитието
     */
    @Override
    public String toString() {
        return date + " " + startTime + "-" + endTime + " | " + name + " | " + note;
    }

    /**
     * Проверява структурна еквивалентност.
     *
     * @param o друг обект
     * @return true, когато обектите са равни
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        Event event = (Event) o;
        return Objects.equals(date, event.date)
                && Objects.equals(startTime, event.startTime)
                && Objects.equals(endTime, event.endTime)
                && Objects.equals(name, event.name)
                && Objects.equals(note, event.note);
    }

    /**
     * Връща хеш код.
     *
     * @return хеш код
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, startTime, endTime, name, note);
    }
}

