package mekanism.common.block.interfaces;

import javax.annotation.Nonnull;
import mekanism.common.base.ILangEntry;

public interface IHasDescription {

    @Nonnull
    ILangEntry getDescription();
}