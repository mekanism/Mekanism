package mekanism.common.integration.crafttweaker.jeitweaker;

import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientConverter;
import com.blamejared.jeitweaker.common.api.ingredient.JeiIngredientCreator;
import java.util.function.Function;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

class JeiChemicalIngredientConverter<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> implements JeiIngredientConverter<STACK, CRT_STACK> {

    private final Function<STACK, CRT_STACK> converter;

    JeiChemicalIngredientConverter(Function<STACK, CRT_STACK> converter) {
        this.converter = converter;
    }

    @Override
    public JeiIngredientCreator.Creator<STACK, CRT_STACK> toFullIngredientFromJei(JeiIngredientCreator.FromJei creator, STACK jeiType) {
        return creator.of(jeiType, ChemicalUtil::copy);
    }

    @Override
    public JeiIngredientCreator.Creator<STACK, CRT_STACK> toFullIngredientFromZen(JeiIngredientCreator.FromZen creator, CRT_STACK zenType) {
        return creator.of(zenType.asImmutable());
    }

    @Override
    public JeiIngredientCreator.Creator<STACK, CRT_STACK> toFullIngredientFromBoth(JeiIngredientCreator.FromBoth creator, STACK jeiType, CRT_STACK zenType) {
        return creator.of(jeiType, ChemicalUtil::copy, zenType.asImmutable());
    }

    @Override
    public STACK toJeiFromZen(CRT_STACK zenType) {
        return zenType.getInternal();
    }

    @Override
    public CRT_STACK toZenFromJei(STACK jeiType) {
        return converter.apply(jeiType);
    }

    @Override
    public String toCommandStringFromZen(CRT_STACK zenType) {
        return zenType.getCommandString();
    }

    @Override
    public ResourceLocation toRegistryNameFromJei(STACK jeiType) {
        return jeiType.getTypeRegistryName();
    }
}