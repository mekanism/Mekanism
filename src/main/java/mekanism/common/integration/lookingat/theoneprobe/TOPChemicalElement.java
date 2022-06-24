package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TOPChemicalElement extends ChemicalElement implements IElement {

    private final ResourceLocation id;

    protected TOPChemicalElement(ResourceLocation id, @NotNull ChemicalStack<?> stored, long capacity) {
        super(stored, capacity);
        this.id = id;
    }

    @Nullable
    public static TOPChemicalElement create(ChemicalStack<?> stored, long capacity) {
        if (stored instanceof GasStack) {
            return new TOPChemicalElement(LookingAtUtils.GAS, stored, capacity);
        } else if (stored instanceof InfusionStack) {
            return new TOPChemicalElement(LookingAtUtils.INFUSE_TYPE, stored, capacity);
        } else if (stored instanceof PigmentStack) {
            return new TOPChemicalElement(LookingAtUtils.PIGMENT, stored, capacity);
        } else if (stored instanceof SlurryStack) {
            return new TOPChemicalElement(LookingAtUtils.SLURRY, stored, capacity);
        }
        return null;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        ChemicalUtils.writeChemicalStack(buf, stored);
        buf.writeVarLong(capacity);
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    public static class GasElementFactory implements IElementFactory {

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(getId(), ChemicalUtils.readGasStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.GAS;
        }
    }

    public static class InfuseTypeElementFactory implements IElementFactory {

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(getId(), ChemicalUtils.readInfusionStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.INFUSE_TYPE;
        }
    }

    public static class PigmentElementFactory implements IElementFactory {

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(getId(), ChemicalUtils.readPigmentStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.PIGMENT;
        }
    }

    public static class SlurryElementFactory implements IElementFactory {

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(getId(), ChemicalUtils.readSlurryStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.SLURRY;
        }
    }
}