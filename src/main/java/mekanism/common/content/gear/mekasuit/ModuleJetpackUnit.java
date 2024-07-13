package mekanism.common.content.gear.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.StorageUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public record ModuleJetpackUnit(JetpackMode mode, ThrustMultiplier thrustMultiplier, ThrustMultiplier hoverThrustMultiplier) implements ICustomModule<ModuleJetpackUnit> {

    public static final ResourceLocation JETPACK_MODE = Mekanism.rl("jetpack_mode");
    public static final ResourceLocation JETPACK_MULT = Mekanism.rl("jetpack_mult");
    public static final ResourceLocation JETPACK_HOVER_MULT = Mekanism.rl("jetpack_mult.hover");

    public ModuleJetpackUnit(IModule<ModuleJetpackUnit> module) {
        this(module.<JetpackMode>getConfigOrThrow(JETPACK_MODE).get(), module.<ThrustMultiplier>getConfigOrThrow(JETPACK_MULT).get(), module.<ThrustMultiplier>getConfigOrThrow(JETPACK_HOVER_MULT).get());
    }

    @Override
    public void addHUDElements(IModule<ModuleJetpackUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            IGasHandler gasHandler = Capabilities.GAS.getCapability(stack);
            if (gasHandler == null) {
                hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(mode.getHUDIcon(), 1));
            } else {
                GasStack stored = StorageUtils.getContainedGas(gasHandler, MekanismGases.HYDROGEN);
                double ratio = StorageUtils.getRatio(stored.getAmount(), gasHandler.getTankCapacity(0));
                hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(mode.getHUDIcon(), ratio));
            }
        }
    }

    @Override
    public void changeMode(IModule<ModuleJetpackUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_JETPACK_MODE.translate(), newMode);
            }
            moduleContainer.replaceModuleConfig(player.level().registryAccess(), stack, module.getData(), module.<JetpackMode>getConfigOrThrow(JETPACK_MODE).with(newMode));
        }
    }

    @Override
    public void onRemoved(IModule<ModuleJetpackUnit> module, IModuleContainer moduleContainer, ItemStack stack, boolean last) {
        //Vent the excess hydrogen from the jetpack
        IGasHandler gasHandler = Capabilities.GAS.getCapability(stack);
        if (gasHandler != null) {
            for (int tank = 0, tanks = gasHandler.getTanks(); tank < tanks; tank++) {
                GasStack stored = gasHandler.getChemicalInTank(tank);
                if (!stored.isEmpty()) {
                    long capacity = gasHandler.getTankCapacity(tank);
                    if (stored.getAmount() > capacity) {
                        gasHandler.setChemicalInTank(tank, stored.copyWithAmount(capacity));
                    }
                }
            }
        }
    }

    public float getThrustMultiplier() {
        if (mode == JetpackMode.HOVER) {
            return hoverThrustMultiplier.getMultiplier();
        }
        return thrustMultiplier.getMultiplier();
    }

    @NothingNullByDefault
    public enum ThrustMultiplier implements IHasTextComponent, StringRepresentable {
        HALF(.5f),
        NORMAL(1f),
        FAST(2f),
        FASTER(3f),
        FASTEST(4f);

        public static final Codec<ThrustMultiplier> CODEC = StringRepresentable.fromEnum(ThrustMultiplier::values);
        public static final IntFunction<ThrustMultiplier> BY_ID = ByIdMap.continuous(ThrustMultiplier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ThrustMultiplier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ThrustMultiplier::ordinal);

        private final String serializedName;
        private final float mult;
        private final Component label;

        ThrustMultiplier(float mult) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.mult = mult;
            this.label = TextComponentUtil.getString(Float.toString(mult));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getMultiplier() {
            return mult;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
