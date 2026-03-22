package bg.tu_varna.sit.models;

import java.time.LocalDate;

/**
 * Представя резултат от пренасрочване на събития за даден ден.
 */
public class RescheduleResult {

    /**
     * Дата, към която са преместени събитията.
     */
    private final LocalDate targetDate;

    /**
     * Брой преместени събития.
     */
    private final int movedCount;

    /**
     * Създава резултат от пренасрочване.
     *
     * @param targetDate целева дата
     * @param movedCount брой преместени събития
     */
    public RescheduleResult(LocalDate targetDate, int movedCount) {
        this.targetDate = targetDate;
        this.movedCount = movedCount;
    }

    /**
     * Връща целевата дата.
     *
     * @return целева дата
     */
    public LocalDate getTargetDate() {
        return targetDate;
    }

    /**
     * Връща броя преместени събития.
     *
     * @return брой преместени събития
     */
    public int getMovedCount() {
        return movedCount;
    }
}

