package mekanism.common.base;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.FactoryType;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

/**
 * Internal interface for managing various Factory types.
 *
 * @author AidanBrady
 */
public interface IFactory {

    /**
     * Gets the recipe type this Smelting Factory currently has.
     *
     * @param itemStack - stack to check
     *
     * @return RecipeType ordinal
     */
    int getRecipeType(ItemStack itemStack);

    /**
     * Gets the recipe type this Factory currently has.
     *
     * @param itemStack - stack to check
     *
     * @return RecipeType or null if it has invalid NBT
     */
    @Nullable
    RecipeType getRecipeTypeOrNull(ItemStack itemStack);

    /**
     * Sets the recipe type of this Smelting Factory to a new value.
     *
     * @param type      - RecipeType ordinal
     * @param itemStack - stack to set
     */
    void setRecipeType(int type, ItemStack itemStack);

    enum MachineFuelType {
        BASIC,
        ADVANCED,
        DOUBLE,
        CHANCE
    }

    enum RecipeType implements IStringSerializable, IHasTranslationKey {
        SMELTING(MekanismLang.SMELTING, MekanismBlocks.ENERGIZED_SMELTER, MachineFuelType.BASIC, false),
        ENRICHING(MekanismLang.ENRICHING, MekanismBlocks.ENRICHMENT_CHAMBER, MachineFuelType.BASIC, false),
        CRUSHING(MekanismLang.CRUSHING, MekanismBlocks.CRUSHER, MachineFuelType.BASIC, false),
        COMPRESSING(MekanismLang.COMPRESSING, MekanismBlocks.OSMIUM_COMPRESSOR, MachineFuelType.ADVANCED, false),
        COMBINING(MekanismLang.COMBINING, MekanismBlocks.COMBINER, MachineFuelType.DOUBLE, false),
        PURIFYING(MekanismLang.PURIFYING, MekanismBlocks.PURIFICATION_CHAMBER, MachineFuelType.ADVANCED, true),
        INJECTING(MekanismLang.INJECTING, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER, MachineFuelType.ADVANCED, true),
        INFUSING(MekanismLang.INFUSING, MekanismBlocks.METALLURGIC_INFUSER, MachineFuelType.BASIC, false),
        SAWING(MekanismLang.SAWING, MekanismBlocks.PRECISION_SAWMILL, MachineFuelType.CHANCE, false);

        private final ILangEntry langEntry;
        private final IBlockProvider type;
        private final MachineFuelType fuelType;
        private final boolean fuelSpeed;

        RecipeType(ILangEntry langEntry, IBlockProvider type, MachineFuelType fuelType, boolean fuelSpeed) {
            this.langEntry = langEntry;
            this.type = type;
            this.fuelType = fuelType;
            this.fuelSpeed = fuelSpeed;
        }

        public static RecipeType getFromFactoryType(@Nonnull FactoryType factoryType) {
            switch (factoryType) {
                case SMELTING:
                    return SMELTING;
                case ENRICHING:
                    return ENRICHING;
                case CRUSHING:
                    return CRUSHING;
                case COMPRESSING:
                    return COMPRESSING;
                case COMBINING:
                    return COMBINING;
                case PURIFYING:
                    return PURIFYING;
                case INJECTING:
                    return INJECTING;
                case INFUSING:
                    return INFUSING;
                case SAWING:
                    return SAWING;
            }
            return null;
        }

        public int getSecondaryEnergyPerTick() {
            if (fuelType == MachineFuelType.ADVANCED) {
                //TODO: Verify this works properly/move it into the factory code itself
                return TileEntityAdvancedElectricMachine.BASE_GAS_PER_TICK;
            }
            return 0;
        }

        public double getEnergyUsage() {
            //TODO: Get from block
            return 0;//type.getUsage();
        }

        public double getEnergyStorage() {
            //TODO: Get from block
            return 0;//type.getStorage();
        }

        public ItemStack getStack() {
            //TODO: Get from block
            return ItemStack.EMPTY;//type.getStack();
        }

        @Override
        public String getTranslationKey() {
            return langEntry.getTranslationKey();
        }

        public MachineFuelType getFuelType() {
            return fuelType;
        }

        public boolean fuelEnergyUpgrades() {
            return fuelSpeed;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public IBlockProvider getType() {
            //TODO: Make this be part of the block's info
            return type;
        }
    }
}