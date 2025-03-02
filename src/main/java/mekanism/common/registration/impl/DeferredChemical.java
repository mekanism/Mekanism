package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class DeferredChemical<TYPE extends Chemical> extends MekanismDeferredHolder<Chemical, TYPE> implements IHasTextComponent, IHasTranslationKey {

    public DeferredChemical(ResourceKey<Chemical> key) {
        super(key);
    }

    @NotNull
    @Override
    public Component getTextComponent() {
        return get().getTextComponent();
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return get().getTranslationKey();
    }

    public ChemicalStack asStack(long size) {
        return new ChemicalStack(this, size);
    }
}