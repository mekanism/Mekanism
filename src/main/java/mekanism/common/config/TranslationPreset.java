package mekanism.common.config;

import java.util.Locale;
import java.util.function.UnaryOperator;

public enum TranslationPreset {
    ENERGY_STORAGE(
          type -> "storage." + type + ".energy",
          type -> type + " Energy Storage",
          type -> "Base energy storage in Joules of: " + type
    ),
    ENERGY_USAGE(
          type -> "usage." + type + ".energy",
          type -> type + " Energy Usage",
          type -> "Energy per operation in Joules of: " + type
    );

    private final UnaryOperator<String> pathCreator;
    private final UnaryOperator<String> titleCreator;
    private final UnaryOperator<String> tooltipCreator;

    TranslationPreset(UnaryOperator<String> pathCreator, UnaryOperator<String> titleCreator, UnaryOperator<String> tooltipCreator) {
        this.pathCreator = pathCreator;
        this.titleCreator = titleCreator;
        this.tooltipCreator = tooltipCreator;
    }

    public String path(String type) {
        return pathCreator.apply(type.toLowerCase(Locale.ROOT)
              .replace(' ', '_')
              .replace('-', '_')
        );
    }

    public String title(String type) {
        return titleCreator.apply(type);
    }

    public String tooltip(String type) {
        return tooltipCreator.apply(type);
    }
}