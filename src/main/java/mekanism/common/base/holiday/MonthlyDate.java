package mekanism.common.base.holiday;

import java.time.Month;

record MonthlyDate(Month month) implements KnownDate {

    @Override
    public boolean isToday(YearlyDate today) {
        return month == today.month();
    }
}