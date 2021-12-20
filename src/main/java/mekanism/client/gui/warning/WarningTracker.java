package mekanism.client.gui.warning;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

//TODO - 1.18: Look at TODOs related to warning system
public class WarningTracker implements IWarningTracker {

    private final Map<WarningType, List<BooleanSupplier>> warnings = new EnumMap<>(WarningType.class);

    @Override
    public BooleanSupplier trackWarning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier) {
        warnings.computeIfAbsent(Objects.requireNonNull(type, "Warning type cannot be null."), t -> new ArrayList<>(type.expectedWarnings))
              .add(Objects.requireNonNull(warningSupplier, "Warning check cannot be null."));
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
    public List<ITextComponent> getWarnings() {
        List<ITextComponent> warningMessages = new ArrayList<>();
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
        NO_MATCHING_RECIPE(MekanismLang.ISSUE_NO_MATCHING_RECIPE),
        NO_SPACE_IN_OUTPUT(MekanismLang.ISSUE_NO_SPACE_IN_OUTPUT),
        NOT_ENOUGH_ENERGY(MekanismLang.ISSUE_NOT_ENOUGH_ENERGY),
        NOT_ENOUGH_ENERGY_REDUCED_RATE(MekanismLang.ISSUE_NOT_ENOUGH_ENERGY_REDUCED_RATE),
        INVALID_OREDICTIONIFICATOR_FILTER(MekanismLang.ISSUE_INVALID_OREDICTIONIFICATOR_FILTER, 4),
        FILTER_HAS_BLACKLISTED_ELEMENT(MekanismLang.ISSUE_FILTER_HAS_BLACKLISTED_ELEMENT, 5),
        ;

        private final ILangEntry langEntry;
        private final int expectedWarnings;

        WarningType(ILangEntry langEntry) {
            //Note: We use a default size of one as in most cases we will only have one of any given type of warning except for things
            // with multiple outputs or for factories
            //TODO - WARNING SYSTEM: Should we maybe define default capacity in WarningType as some things may make more sense to have slightly higher?
            this(langEntry, 1);
        }

        WarningType(ILangEntry langEntry, int expectedWarnings) {
            this.langEntry = langEntry;
            this.expectedWarnings = expectedWarnings;
        }
    }
}