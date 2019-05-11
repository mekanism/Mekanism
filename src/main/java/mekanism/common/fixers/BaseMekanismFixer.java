package mekanism.common.fixers;

import mekanism.common.fixers.MekanismDataFixers.MekFixers;
import net.minecraft.util.datafix.IFixableData;

/**
 * Created by Thiakil on 5/05/2019.
 */
public abstract class BaseMekanismFixer implements IFixableData {

    private final MekFixers fixer;

    protected BaseMekanismFixer(MekFixers fixer) {
        this.fixer = fixer;
    }

    @Override
    public int getFixVersion() {
        return this.fixer.getFixVersion();
    }
}
