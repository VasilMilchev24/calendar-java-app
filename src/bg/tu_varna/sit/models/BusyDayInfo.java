package bg.tu_varna.sit.models;

import java.time.DayOfWeek;

/**
 * Съхранява натрупаното заето време за конкретен ден от седмицата.
 */
public class BusyDayInfo {

    /**
     * Ден от седмицата.
     */
    private final DayOfWeek dayOfWeek;

    /**
     * Продължителност на заетостта в минути.
     */
    private final long busyMinutes;

    /**
     * Създава запис за заетост по ден.
     *
     * @param dayOfWeek ден от седмицата
     * @param busyMinutes заети минути
     */
    public BusyDayInfo(DayOfWeek dayOfWeek, long busyMinutes) {
        this.dayOfWeek = dayOfWeek;
        this.busyMinutes = busyMinutes;
    }

    /**
     * Връща деня от седмицата.
     *
     * @return ден от седмицата
     */
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Връща заетите минути.
     *
     * @return заети минути
     */
    public long getBusyMinutes() {
        return busyMinutes;
    }

    /**
     * Връща заетите пълни часове.
     *
     * @return заети часове
     */
    public long getBusyHours() {
        return busyMinutes / 60;
    }

    /**
     * Връща текст за извеждане.
     *
     * @return текст за извеждане
     */
    @Override
    public String toString() {
        return getBulgarianDayName(dayOfWeek) + ": " + getBusyHours() + " ч.";
    }

    /**
     * Връща име на деня на български език.
     *
     * @param day ден от седмицата
     * @return име на деня на български език
     */
    private String getBulgarianDayName(DayOfWeek day) {
        if (day == DayOfWeek.MONDAY) {
            return "Понеделник";
        }
        if (day == DayOfWeek.TUESDAY) {
            return "Вторник";
        }
        if (day == DayOfWeek.WEDNESDAY) {
            return "Сряда";
        }
        if (day == DayOfWeek.THURSDAY) {
            return "Четвъртък";
        }
        if (day == DayOfWeek.FRIDAY) {
            return "Петък";
        }
        if (day == DayOfWeek.SATURDAY) {
            return "Събота";
        }
        return "Неделя";
    }
}

