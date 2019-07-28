package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import mekanism.common.base.IBlockType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tier.BaseTier;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateMachine extends ExtendedBlockState {

    public static final PropertyBool activeProperty = PropertyBool.create("active");
    public static final PropertyEnum<BaseTier> tierProperty = PropertyEnum.create("tier", BaseTier.class);
    public static final PropertyEnum<RecipeType> recipeProperty = PropertyEnum.create("recipe", RecipeType.class);

    public BlockStateMachine(BlockMekanismContainer block) {
        //TODO: Should tier stay part of blockstate or be extracted into its own block
        super(block, new IProperty[]{BlockStateFacing.facingProperty, activeProperty, tierProperty, recipeProperty}, new IUnlistedProperty[]{});
    }

    public enum MachineType implements IStringSerializable, IBlockType {
        ENRICHMENT_CHAMBER,
        OSMIUM_COMPRESSOR,
        COMBINER,
        CRUSHER,
        DIGITAL_MINER,
        BASIC_FACTORY,
        ADVANCED_FACTORY,
        ELITE_FACTORY,
        METALLURGIC_INFUSER,
        PURIFICATION_CHAMBER,
        ENERGIZED_SMELTER,
        TELEPORTER,
        ELECTRIC_PUMP,
        PERSONAL_CHEST,
        CHARGEPAD,
        LOGISTICAL_SORTER,
        ROTARY_CONDENSENTRATOR,
        CHEMICAL_OXIDIZER,
        CHEMICAL_INFUSER,
        CHEMICAL_INJECTION_CHAMBER,
        ELECTROLYTIC_SEPARATOR,
        PRECISION_SAWMILL,
        CHEMICAL_DISSOLUTION_CHAMBER,
        CHEMICAL_WASHER,
        CHEMICAL_CRYSTALLIZER,
        SEISMIC_VIBRATOR,
        PRESSURIZED_REACTION_CHAMBER,
        FLUID_TANK,
        FLUIDIC_PLENISHER,
        LASER,
        LASER_AMPLIFIER,
        LASER_TRACTOR_BEAM,
        QUANTUM_ENTANGLOPORTER,
        SOLAR_NEUTRON_ACTIVATOR,
        OREDICTIONIFICATOR,
        RESISTIVE_HEATER,
        FORMULAIC_ASSEMBLICATOR,
        FUELWOOD_HEATER;

        private static final List<MachineType> VALID_MACHINES = new ArrayList<>();

        static {
            VALID_MACHINES.addAll(Arrays.asList(MachineType.values()));
        }

        public static List<MachineType> getValidMachines() {
            return VALID_MACHINES;
        }

        public static MachineType get(Block block, int meta) {
            return null;
        }

        public static MachineType get(ItemStack stack) {
            return null;
        }

        @Override
        public String getBlockName() {
            return "";
        }

        @Override
        public boolean isEnabled() {
            return MekanismConfig.current().general.machinesManager.isEnabled(this);
        }

        //TODO: Put this as part of IBlockElectric?
        public double getUsage() {
            //TODO
            return 0;
        }

        public double getStorage() {
            //TODO
            return 0;
        }

        public ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}