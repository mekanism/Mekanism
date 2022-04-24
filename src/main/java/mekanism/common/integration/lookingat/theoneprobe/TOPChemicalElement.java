package mekanism.common.integration.lookingat.theoneprobe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.ChemicalElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TOPChemicalElement extends ChemicalElement implements IElement {

    private final ResourceLocation id;

    protected TOPChemicalElement(ResourceLocation id, @Nonnull ChemicalStack<?> stored, long capacity) {
        super(stored, capacity);
        this.id = id;
    }

    @Nullable
    public static TOPChemicalElement create(ChemicalStack<?> stored, long capacity) {
        if (stored instanceof GasStack) {
            return new TOPChemicalElement(GasElementFactory.ID, stored, capacity);
        } else if (stored instanceof InfusionStack) {
            return new TOPChemicalElement(InfuseTypeElementFactory.ID, stored, capacity);
        } else if (stored instanceof PigmentStack) {
            return new TOPChemicalElement(PigmentElementFactory.ID, stored, capacity);
        } else if (stored instanceof SlurryStack) {
            return new TOPChemicalElement(SlurryElementFactory.ID, stored, capacity);
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

        private static final ResourceLocation ID = Mekanism.rl("gas");

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(ID, ChemicalUtils.readGasStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }

    public static class InfuseTypeElementFactory implements IElementFactory {

        private static final ResourceLocation ID = Mekanism.rl("infuse_type");

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(ID, ChemicalUtils.readInfusionStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }

    public static class PigmentElementFactory implements IElementFactory {

        private static final ResourceLocation ID = Mekanism.rl("pigment");

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(ID, ChemicalUtils.readPigmentStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }

    public static class SlurryElementFactory implements IElementFactory {

        private static final ResourceLocation ID = Mekanism.rl("slurry");

        @Override
        public TOPChemicalElement createElement(FriendlyByteBuf buf) {
            return new TOPChemicalElement(ID, ChemicalUtils.readSlurryStack(buf), buf.readVarLong());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}