package mekanism.common.integration.lookingat.wthit;

import java.util.ArrayList;
import java.util.List;
import mcp.mobius.waila.api.IData;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.neoforge.fluids.FluidStack;

public class WTHITLookingAtHelper implements LookingAtHelper, IData {

    public static final IData.Serializer<WTHITLookingAtHelper> SERIALIZER = buf -> {
        WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
        int count = buf.readVarInt();
        //TODO - 1.20.5: When WTHIT updates this cast will probably not be necessary
        RegistryFriendlyByteBuf buffer = (RegistryFriendlyByteBuf) buf;
        for (int i = 0; i < count; i++) {
            LookingAtTypes type = buffer.readEnum(LookingAtTypes.class);
            Object element = switch (type) {
                case UNKNOWN -> null;
                case ENERGY -> new EnergyElement(FloatingLong.readFromBuffer(buffer), FloatingLong.readFromBuffer(buffer));
                case FLUID -> new FluidElement(FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readVarInt());
                case GAS -> new ChemicalElement(GasStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readVarLong());
                case INFUSION -> new ChemicalElement(InfusionStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readVarLong());
                case PIGMENT -> new ChemicalElement(PigmentStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readVarLong());
                case SLURRY -> new ChemicalElement(SlurryStack.OPTIONAL_STREAM_CODEC.decode(buffer), buffer.readVarLong());
                case COMPONENT -> ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
            };
            if (element != null) {
                helper.elements.add(element);
            }
        }
        return helper;
    };

    final List<Object> elements = new ArrayList<>();

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(elements, (buf, object) -> {
            LookingAtTypes type = LookingAtTypes.getType(object);
            buf.writeEnum(type);
            switch (type) {
                case ENERGY -> {
                    EnergyElement energyElement = (EnergyElement) object;
                    energyElement.getEnergy().writeToBuffer(buf);
                    energyElement.getMaxEnergy().writeToBuffer(buf);
                }
                case FLUID -> {
                    FluidElement fluidElement = (FluidElement) object;
                    //TODO - 1.20.5: When WTHIT updates this cast will probably not be necessary
                    FluidStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, fluidElement.getStored());
                    buf.writeVarInt(fluidElement.getCapacity());
                }
                case GAS, INFUSION, PIGMENT, SLURRY -> {
                    ChemicalElement chemicalElement = (ChemicalElement) object;
                    if (chemicalElement.getStored().isEmpty()) {
                        buffer.writeBoolean(false);
                    } else {
                        buffer.writeBoolean(true);
                        chemicalElement.getStored().writeToPacket(buffer);
                    }
                    buf.writeVarLong(chemicalElement.getCapacity());
                }
                //TODO - 1.20.5: When WTHIT updates this cast will probably not be necessary
                case COMPONENT -> ComponentSerialization.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, (Component) object);
            }
        });
    }

    @Override
    public void addText(Component text) {
        elements.add(text);
    }

    @Override
    public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        elements.add(new EnergyElement(energy, maxEnergy));
    }

    @Override
    public void addFluidElement(FluidStack stored, int capacity) {
        elements.add(new FluidElement(stored, capacity));
    }

    @Override
    public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
        elements.add(new ChemicalElement(stored, capacity));
    }

    private enum LookingAtTypes {
        UNKNOWN,
        ENERGY,
        FLUID,
        GAS,
        INFUSION,
        PIGMENT,
        SLURRY,
        COMPONENT;

        public static LookingAtTypes getType(Object element) {
            if (element instanceof Component) {
                return COMPONENT;
            } else if (element instanceof EnergyElement) {
                return ENERGY;
            } else if (element instanceof FluidElement) {
                return FLUID;
            } else if (element instanceof ChemicalElement chemicalElement) {
                return switch (chemicalElement.getChemicalType()) {
                    case GAS -> GAS;
                    case INFUSION -> INFUSION;
                    case PIGMENT -> PIGMENT;
                    case SLURRY -> SLURRY;
                };
            }
            return UNKNOWN;
        }
    }
}