package mekanism.common.base.holiday;

import java.time.LocalDate;
import java.time.Month;
import org.jetbrains.annotations.Range;

record YearlyDate(Month month, @Range(from = 1, to = 31) int day) implements KnownDate {

    @Override
    public boolean isToday(YearlyDate today) {
        return equals(today);
    }

    public static YearlyDate now() {
        LocalDate time = LocalDate.now();
        return new YearlyDate(time.getMonth(), time.getDayOfMonth());
    }
}