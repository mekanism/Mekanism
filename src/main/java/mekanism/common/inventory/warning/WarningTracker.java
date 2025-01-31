package mekanism.common.inventory.warning;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WarningTracker implements IWarningTracker {

    private final Map<WarningType, List<BooleanSupplier>> warnings = new EnumMap<>(WarningType.class);

    @Override
    public BooleanSupplier trackWarning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        Objects.requireNonNull(type, "Warning type cannot be null.");
        Objects.requireNonNull(warningSupplier, "Warning check cannot be null.");
        warnings.computeIfAbsent(type, t -> new ArrayList<>(t.expectedWarnings)).add(warningSupplier);
        return warningSupplier;
    }

    @Override
    public void clearTrackedWarnings() {
        warnings.clear();
    }

    @Override
    public boolean hasWarning() {
        for (List<BooleanSupplier> warningSuppliers : warnings.values()) {
            for (BooleanSupplier warningSupplier : warningSuppliers) {
                if (warningSupplier.getAsBoolean()) {
                    //Exit as soon as we run into a warning as that means we have at least one and the warning tab should be displayed
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<Component> getWarnings() {
        List<Component> warningMessages = new ArrayList<>();
        for (Map.Entry<WarningType, List<BooleanSupplier>> entry : warnings.entrySet()) {
            for (BooleanSupplier warningSupplier : entry.getValue()) {
                if (warningSupplier.getAsBoolean()) {
                    WarningType warningType = entry.getKey();
                    warningMessages.add(warningType.langEntry.translate());
                    if (warningType == WarningType.NOT_ENOUGH_ENERGY) {
                        //If we are adding a not enough energy warning, don't bother adding one about running at a reduced rate
                        // Note: This relies on the energy warnings being the last two warnings in WarningType and would ideally
                        // have a better pairing of what warnings are mutually exclusive of each other, but this is the simplest
                        // low overhead way of implementing it for now for the one case that we care about
                        return warningMessages;
                    }
                    break;
                }
            }
        }
        return warningMessages;
    }

    //Note: Order of this enum is important as it determines the order that the warnings show up if there are multiple warnings
    // by virtue of how EnumMaps iterate
    public enum WarningType {
        INPUT_DOESNT_PRODUCE_OUTPUT(MekanismLang.ISSUE_INPUT_DOESNT_PRODUCE_OUTPUT),
        //TODO - 1.18: Should this be renamed to not enough input? We don't really have a no matching recipe
        NO_MATCHING_RECIPE(MekanismLang.ISSUE_NO_MATCHING_RECIPE),
        NO_SPACE_IN_OUTPUT(MekanismLang.ISSUE_NO_SPACE_IN_OUTPUT),
        NO_SPACE_IN_OUTPUT_OVERFLOW(MekanismLang.ISSUE_NO_SPACE_IN_OUTPUT_OVERFLOW),
        NOT_ENOUGH_ENERGY(MekanismLang.ISSUE_NOT_ENOUGH_ENERGY),
        NOT_ENOUGH_ENERGY_REDUCED_RATE(MekanismLang.ISSUE_NOT_ENOUGH_ENERGY_REDUCED_RATE),
        INVALID_OREDICTIONIFICATOR_FILTER(MekanismLang.ISSUE_INVALID_OREDICTIONIFICATOR_FILTER, 4),
        FILTER_HAS_BLACKLISTED_ELEMENT(MekanismLang.ISSUE_FILTER_HAS_BLACKLISTED_ELEMENT, 5),
        REDSTONE_SIGNAL_ABSENT(MekanismLang.ISSUE_REDSTONE_SIGNAL_ABSENT),
        REDSTONE_SIGNAL_PRESENT(MekanismLang.ISSUE_REDSTONE_SIGNAL_PRESENT),
        REDSTONE_PULSE_REQUIRED(MekanismLang.ISSUE_REDSTONE_PULSE_REQUIRED),
        ;

        private final ILangEntry langEntry;
        private final int expectedWarnings;

        WarningType(ILangEntry langEntry) {
            //Note: We use a default size of one as in most cases we will only have one of any given type of warning except for things
            // with multiple outputs or for factories
            //TODO: Eventually we may want to define a default capacity in WarningType as some things may make more sense to have slightly higher?
            this(langEntry, 1);
        }

        WarningType(ILangEntry langEntry, int expectedWarnings) {
            this.langEntry = langEntry;
            this.expectedWarnings = expectedWarnings;
        }
    }
}