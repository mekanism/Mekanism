package mekanism.common.block.states;

import java.util.Locale;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Tier.BaseTier;
import mekanism.common.block.BlockTransmitter;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.property.PropertyConnection;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateTransmitter extends ExtendedBlockState {

    public static final PropertyEnum<TransmitterType> typeProperty = PropertyEnum.create("type", TransmitterType.class);
    public static final PropertyEnum<BaseTier> tierProperty = PropertyEnum.create("tier", BaseTier.class);

    public BlockStateTransmitter(BlockTransmitter block) {
        super(block, new IProperty[]{typeProperty, tierProperty},
              new IUnlistedProperty[]{OBJProperty.INSTANCE, PropertyColor.INSTANCE, PropertyConnection.INSTANCE});
    }

    public enum TransmitterType implements IStringSerializable {
        UNIVERSAL_CABLE("UniversalCable", Size.SMALL, TransmissionType.ENERGY, false, true),
        MECHANICAL_PIPE("MechanicalPipe", Size.LARGE, TransmissionType.FLUID, false, true),
        PRESSURIZED_TUBE("PressurizedTube", Size.SMALL, TransmissionType.GAS, false, true),
        LOGISTICAL_TRANSPORTER("LogisticalTransporter", Size.LARGE, TransmissionType.ITEM, true, true),
        RESTRICTIVE_TRANSPORTER("RestrictiveTransporter", Size.LARGE, TransmissionType.ITEM, false, false),
        DIVERSION_TRANSPORTER("DiversionTransporter", Size.LARGE, TransmissionType.ITEM, true, false),
        THERMODYNAMIC_CONDUCTOR("ThermodynamicConductor", Size.SMALL, TransmissionType.HEAT, false, true);

        private String unlocalizedName;
        private Size size;
        private TransmissionType transmissionType;
        private boolean transparencyRender;
        private boolean tiers;

        TransmitterType(String name, Size s, TransmissionType type, boolean transparency, boolean b) {
            unlocalizedName = name;
            size = s;
            transmissionType = type;
            transparencyRender = transparency;
            tiers = b;
        }

        public static TransmitterType get(int meta) {
            return TransmitterType.values()[meta];
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String getUnlocalizedName() {
            return unlocalizedName;
        }

        public Size getSize() {
            return size;
        }

        public boolean hasTransparency() {
            return transparencyRender;
        }

        public TransmissionType getTransmission() {
            return transmissionType;
        }

        public boolean hasTiers() {
            return tiers;
        }

        public enum Size {
            SMALL(6),
            LARGE(8);

            public int centerSize;

            Size(int size) {
                centerSize = size;
            }
        }
    }

    public static class TransmitterStateMapper extends StateMapperBase {

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            BlockTransmitter block = (BlockTransmitter) state.getBlock();
            TransmitterType type = state.getValue(typeProperty);
            StringBuilder builder = new StringBuilder();
            String nameOverride = null;

            if (type.tiers) {
                BaseTier tier = state.getValue(tierProperty);

                if (tier == BaseTier.CREATIVE) {
                    tier = BaseTier.ULTIMATE;
                }

                nameOverride = type.getName() + "_" + tier.getName();
            }

            if (builder.length() == 0) {
                builder.append("normal");
            }

            ResourceLocation baseLocation = new ResourceLocation("mekanism",
                  nameOverride != null ? nameOverride : type.getName());

            return new ModelResourceLocation(baseLocation, builder.toString());
        }
    }
}
