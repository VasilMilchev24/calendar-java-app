package bg.tu_varna.sit.models;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Представя резултат за наличен свободен интервал.
 */
public class TimeSlot {

    /**
     * Дата на интервала.
     */
    private final LocalDate date;

    /**
     * Начален час на интервала.
     */
    private final LocalTime startTime;

    /**
     * Краен час на интервала.
     */
    private final LocalTime endTime;

    /**
     * Създава времеви интервал.
     *
     * @param date дата на интервала
     * @param startTime начален час
     * @param endTime краен час
     */
    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Връща датата на интервала.
     *
     * @return дата
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Връща началния час на интервала.
     *
     * @return начален час
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Връща крайния час на интервала.
     *
     * @return краен час
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Връща текст за визуализация.
     *
     * @return текст на интервала
     */
    @Override
    public String toString() {
        return date + " " + startTime + "-" + endTime;
    }
}

