package mekanism.chemistry.common;

public class ChemistryTags {

    private ChemistryTags() {
    }

    public static void init() {
        Fluids.init();
        Gases.init();
    }

    public static class Fluids {
        private static void init() {}

        private Fluids() {}
    }

    public static class Gases {
        private static void init() {}

        private Gases() {}
    }
}
