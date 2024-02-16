package mekanism.common.base.holiday;

import java.time.Month;

class AprilFools extends Holiday {

    public static final AprilFools INSTANCE = new AprilFools();

    private AprilFools() {
        super(new YearlyDate(Month.APRIL, 1));
    }
}