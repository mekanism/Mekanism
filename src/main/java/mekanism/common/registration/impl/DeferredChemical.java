package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public class DeferredChemical<TYPE extends Chemical> extends MekanismDeferredHolder<Chemical, TYPE> implements IHasTextComponent, IHasTranslationKey {

    public DeferredChemical(ResourceKey<Chemical> key) {
        super(key);
    }

    @Override
    public Component getTextComponent() {
        return get().getTextComponent();
    }

    @Override
    public String getTranslationKey() {
        return get().getTranslationKey();
    }

    public ChemicalStackTemplate asTemplate(int size) {
        return new ChemicalStackTemplate(this, size);
    }

    public ChemicalResource asResource() {
        return ChemicalResource.of(this);
    }
}