package mekanism

enum PerModule {
    NONE,
    MODULE_ONLY,
    ALL;

    boolean enabled() {
        return this != NONE
    }

    boolean datagen() {
        return this == ALL
    }
}