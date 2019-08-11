package mekanism.common.util;

import java.util.UUID;
import mekanism.api.EnumColor;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.MekanismClient;
import mekanism.common.SideData;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.tile.TileEntityLaserAmplifier.RedstoneOutput;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.UnitDisplayUtils.EnergyType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraft.block.Block;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

public class TextComponentUtil {

    //TODO: Replace a lot of the checks for enum types with an interface for IHasTranslationKey
    public static ITextComponent build(Object... components) {
        //TODO: Verify that just appending them to the first text component works properly.
        // My suspicion is we will need to chain downwards and append it that way so that the formatting matches
        // from call to call without resetting back to
        ITextComponent result = null;
        TextFormatting cachedFormat = null;
        for (Object component : components) {
            ITextComponent current = null;
            if (component instanceof Translation) {
                current = ((Translation) component).getTextComponent();
            } else if (component instanceof String) {
                current = getStringComponent((String) component);
            } else if (component instanceof EnumColor) {
                cachedFormat = ((EnumColor) component).textFormatting;
            } else if (component instanceof ITextComponent) {
                //Just append if a text component is being passed
                current = (ITextComponent) component;
            } else if (component instanceof TextFormatting) {
                cachedFormat = (TextFormatting) component;
            } else if (component instanceof InputMappings.Input) {
                //Helper for key bindings to not have to get the translation key directly and then pass that as a Translation object
                current = getTranslationComponent(((InputMappings.Input) component).getTranslationKey());
            } else if (component instanceof EnergyDisplay) {
                current = ((EnergyDisplay) component).getTextComponent();
            } else if (component instanceof OwnerDisplay) {
                current = ((OwnerDisplay) component).getTextComponent();
            } else if (component instanceof UpgradeDisplay) {
                current = ((UpgradeDisplay) component).getTextComponent();
            } else if (component instanceof SecurityMode) {
                current = ((SecurityMode) component).getTextComponent();
            } else if (component instanceof ConfiguratorMode) {
                current = ((ConfiguratorMode) component).getTextComponent();
            } else if (component instanceof BooleanStateDisplay) {
                current = ((BooleanStateDisplay) component).getTextComponent();
            } else if (component instanceof GasStack) {
                current = getTranslationComponent(((GasStack) component).getGas());
            } else if (component instanceof Gas) {
                current = getTranslationComponent((Gas) component);
            } else if (component instanceof SideData) {
                current = getTranslationComponent(((SideData) component).getTranslationKey());
            } else if (component instanceof TransmissionType) {
                current = getTranslationComponent(((TransmissionType) component).getTranslationKey());
            } else if (component instanceof RedstoneControl) {
                current = getTranslationComponent(((RedstoneControl) component).getTranslationKey());
            } else if (component instanceof RedstoneOutput) {
                current = getTranslationComponent(((RedstoneOutput) component).getTranslationKey());
            } else if (component instanceof ContainerEditMode) {
                current = getTranslationComponent(((ContainerEditMode) component).getTranslationKey());
            } else if (component instanceof ConnectionType) {
                current = getTranslationComponent(((ConnectionType) component).getTranslationKey());
            } else if (component instanceof Block) {
                current = ((Block) component).getNameTextComponent();
            } else if (component instanceof Item) {
                current = getTranslationComponent(((Item) component).getTranslationKey());
            } else if (component instanceof FluidStack) {
                current = getTranslationComponent(((FluidStack) component).getUnlocalizedName());
            } else if (component instanceof EnergyType) {
                current = getStringComponent(((EnergyType) component).name());
            } else if (component instanceof TempType) {
                current = getStringComponent(((TempType) component).name());
            } else if (component instanceof Boolean || component instanceof Number) {
                //Put actual boolean or integer/double, etc value
                current = getStringComponent(component.toString());
            } else {
                //TODO: Warning when unexpected type?
                //TODO: Add support wrappers for following types
            }
            if (current == null) {
                //If we don't have a component to add, don't
                continue;
            }
            if (cachedFormat != null) {
                //Apply the formatting
                current.applyTextStyle(cachedFormat);
                cachedFormat = null;
            }
            if (result == null) {
                result = current;
            } else {
                result.appendSibling(current);
            }
        }
        //Ignores any trailing formatting
        return result;
    }

    public static StringTextComponent getStringComponent(String component) {
        return new StringTextComponent(component);
    }

    public static TranslationTextComponent getTranslationComponent(String component, Object... args) {
        return new TranslationTextComponent(component, args);
    }

    public static TranslationTextComponent getTranslationComponent(Gas gas) {
        return getTranslationComponent(gas.getTranslationKey());
    }


    public static class Translation {

        private final String key;
        private final Object[] args;

        private Translation(String key, Object... args) {
            this.key = key;
            this.args = args;
        }

        public static Translation of(String key, Object... args) {
            return new Translation(key, args);
        }

        public ITextComponent getTextComponent() {
            return getTranslationComponent(key, args);
        }
    }

    public static class EnergyDisplay {

        private final double energy;
        private final double max;

        private EnergyDisplay(double energy, double max) {
            this.energy = energy;
            this.max = max;
        }

        //TODO: Wrapper for getting this from itemstack
        public static EnergyDisplay of(double energy, double max) {
            return new EnergyDisplay(energy, max);
        }

        public static EnergyDisplay of(double energy) {
            return of(energy, 0);
        }

        public ITextComponent getTextComponent() {
            if (energy == Double.MAX_VALUE) {
                return getTranslationComponent("mekanism.gui.infinite");
            }
            if (max == 0) {
                return getStringComponent(MekanismUtils.getEnergyDisplayShort(energy));
            }
            //Pass max back as a new Energy Display so that if we have 0/infinite it shows that properly without us having to add extra handling
            return build(MekanismUtils.getEnergyDisplayShort(energy), "/", of(max));
        }
    }

    public static class OwnerDisplay {

        private final PlayerEntity player;
        private final UUID ownerUUID;
        private final String ownerName;

        private OwnerDisplay(PlayerEntity player, UUID ownerUUID, String ownerName) {
            this.player = player;
            this.ownerUUID = ownerUUID;
            this.ownerName = ownerName;
        }

        public static OwnerDisplay of(PlayerEntity player, UUID ownerUUID) {
            return of(player, ownerUUID, null);
        }

        public static OwnerDisplay of(PlayerEntity player, UUID ownerUUID, String ownerName) {
            return new OwnerDisplay(player, ownerUUID, ownerName);
        }

        public ITextComponent getTextComponent() {
            if (ownerUUID == null) {
                return build(EnumColor.RED, Translation.of("mekanism.gui.no_owner"));
            }
            //TODO: If the name is supposed to be gotten differently server side, then do so
            //Allows for the name to be overridden by a passed value
            String name = ownerName == null ? MekanismClient.clientUUIDMap.get(ownerUUID) : ownerName;
            return build(EnumColor.GREY, Translation.of("mekanism.gui.owner"), player.getUniqueID().equals(ownerUUID) ? EnumColor.BRIGHT_GREEN : EnumColor.RED, name);
        }
    }

    public static class UpgradeDisplay {

        private final Upgrade upgrade;
        private final int level;

        private UpgradeDisplay(Upgrade upgrade, int level) {
            this.upgrade = upgrade;
            this.level = level;
        }

        public static UpgradeDisplay of(Upgrade upgrade) {
            return of(upgrade, 0);
        }

        public static UpgradeDisplay of(Upgrade upgrade, int level) {
            return new UpgradeDisplay(upgrade, level);
        }

        public ITextComponent getTextComponent() {
            if (upgrade.canMultiply() && level > 0) {
                return build(upgrade.getColor(), "- ", upgrade.getName(), ": ", EnumColor.GREY, "x" + level);
            }
            return build(upgrade.getColor(), "- ", upgrade.getName());
        }
    }

    public static abstract class BooleanStateDisplay {

        protected final boolean value;
        protected final boolean colored;

        protected BooleanStateDisplay(boolean value, boolean colored) {
            this.value = value;
            this.colored = colored;
        }

        protected abstract String getKey();

        public ITextComponent getTextComponent() {
            ITextComponent translation = getTranslationComponent(getKey());
            if (colored) {
                translation.getStyle().setColor(value ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED);
            }
            return translation;
        }
    }

    public static class YesNo extends BooleanStateDisplay {

        private YesNo(boolean value, boolean colored) {
            super(value, colored);
        }

        public static YesNo of(boolean value) {
            return of(value, false);
        }

        public static YesNo of(boolean value, boolean colored) {
            return new YesNo(value, colored);
        }

        @Override
        protected String getKey() {
            return "mekanism.tooltip." + (value ? "yes" : "no");
        }
    }

    public static class OnOff extends BooleanStateDisplay {

        private OnOff(boolean value, boolean colored) {
            super(value, colored);
        }

        public static OnOff of(boolean value) {
            return of(value, false);
        }

        public static OnOff of(boolean value, boolean colored) {
            return new OnOff(value, colored);
        }

        @Override
        protected String getKey() {
            return "mekanism.tooltip." + (value ? "on" : "off");
        }
    }

    public static class OutputInput extends BooleanStateDisplay {

        private OutputInput(boolean value, boolean colored) {
            super(value, colored);
        }

        public static OutputInput of(boolean value) {
            return of(value, false);
        }

        public static OutputInput of(boolean value, boolean colored) {
            return new OutputInput(value, colored);
        }

        @Override
        protected String getKey() {
            return "mekanism.gui." + (value ? "output" : "input");
        }
    }
}