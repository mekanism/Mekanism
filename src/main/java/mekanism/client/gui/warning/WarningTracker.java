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

public class WarningTracker implements IWarningTracker {

    private final Map<WarningType, List<BooleanSupplier>> warnings = new EnumMap<>(WarningType.class);

    @Override
    public BooleanSupplier trackWarning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier) {
        //Note: We use a default size of one as in most cases we will only have one of any given type of warning except for things
        // with multiple outputs or for factories
        //TODO - 10.1: Should we maybe define default capacity in WarningType as some things may make more sense to have slightly higher?
        warnings.computeIfAbsent(Objects.requireNonNull(type, "Warning type cannot be null."), t -> new ArrayList<>(1))
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
        ;

        private final ILangEntry langEntry;

        WarningType(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }
    }
}