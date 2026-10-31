package com.sentineldesk.sla;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Service
public class BusinessHoursService {

    private static final Logger log = LoggerFactory.getLogger(BusinessHoursService.class);

    public static final LocalTime WORK_START = LocalTime.of(9, 0);
    public static final LocalTime WORK_END = LocalTime.of(18, 0);

    // Standard fixed public holidays (MM-dd)
    private static final Set<String> FIXED_HOLIDAYS = Set.of(
            "01-01", // New Year's Day
            "01-26", // Republic Day
            "07-04", // Independence Day (US)
            "08-15", // Independence Day (IN)
            "12-25"  // Christmas Day
    );

    /**
     * Determines whether a given calendar date is a recognized working business day.
     */
    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }
        String mmDd = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
        return !FIXED_HOLIDAYS.contains(mmDd);
    }

    /**
     * Accurately calculates an SLA deadline given a starting timestamp and SLA hour allotment.
     * When isBusinessHours is true, clock only runs Mon-Fri 09:00-18:00, pausing over nights, weekends, and holidays.
     * When isBusinessHours is false (e.g. P1 Critical 24/7 Outages), continuous calendar duration is applied.
     */
    public LocalDateTime calculateDeadline(LocalDateTime start, BigDecimal hours, boolean isBusinessHours) {
        if (start == null) {
            start = LocalDateTime.now();
        }
        if (hours == null || hours.compareTo(BigDecimal.ZERO) <= 0) {
            return start;
        }

        long totalMinutes = (long) Math.round(hours.doubleValue() * 60);

        if (!isBusinessHours) {
            return start.plusMinutes(totalMinutes);
        }

        LocalDateTime current = advanceToWorkingTime(start);
        long remainingMinutes = totalMinutes;

        while (remainingMinutes > 0) {
            LocalDateTime endOfWorkDay = current.toLocalDate().atTime(WORK_END);
            long availableMinutesToday = Duration.between(current, endOfWorkDay).toMinutes();

            if (remainingMinutes <= availableMinutesToday) {
                current = current.plusMinutes(remainingMinutes);
                remainingMinutes = 0;
            } else {
                remainingMinutes -= availableMinutesToday;
                current = moveToNextWorkDayStart(current.toLocalDate().plusDays(1));
            }
        }

        log.debug("Calculated business hours deadline from {} for {}h -> {}", start, hours, current);
        return current;
    }

    private LocalDateTime advanceToWorkingTime(LocalDateTime time) {
        LocalDate date = time.toLocalDate();
        LocalTime t = time.toLocalTime();

        while (!isWorkingDay(date)) {
            date = date.plusDays(1);
            t = WORK_START;
        }

        if (t.isBefore(WORK_START)) {
            return date.atTime(WORK_START);
        } else if (!t.isBefore(WORK_END)) {
            return moveToNextWorkDayStart(date.plusDays(1));
        } else {
            return date.atTime(t);
        }
    }

    private LocalDateTime moveToNextWorkDayStart(LocalDate date) {
        while (!isWorkingDay(date)) {
            date = date.plusDays(1);
        }
        return date.atTime(WORK_START);
    }
}
