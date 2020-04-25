package mekanism.common.block.interfaces;

import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;

public interface IHasDescription {

    @Nonnull
    ILangEntry getDescription();
}