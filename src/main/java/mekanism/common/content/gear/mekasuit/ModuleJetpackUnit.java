package mekanism.common.content.gear.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.gear.IClientModuleHelper;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public record ModuleJetpackUnit(JetpackMode mode, ThrustMultiplier thrustMultiplier, ThrustMultiplier hoverThrustMultiplier) implements ICustomModule<ModuleJetpackUnit> {

    public static final Identifier JETPACK_MODE = Mekanism.rl("jetpack_mode");
    public static final Identifier JETPACK_MULT = Mekanism.rl("jetpack_mult");
    public static final Identifier JETPACK_HOVER_MULT = Mekanism.rl("jetpack_mult.hover");

    public ModuleJetpackUnit(IModule<ModuleJetpackUnit> module) {
        this(module.<JetpackMode>getConfigOrThrow(JETPACK_MODE).get(), module.<ThrustMultiplier>getConfigOrThrow(JETPACK_MULT).get(), module.<ThrustMultiplier>getConfigOrThrow(JETPACK_HOVER_MULT).get());
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<ModuleJetpackUnit> module, IModuleContainer moduleContainer, ITEM instance,
          Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            ResourceHandler<ChemicalResource> chemicalHandler = Capabilities.CHEMICAL.getCapability(ItemAccessUtils.sideEffectFreeAccess(instance));
            if (chemicalHandler == null) {
                hudElementAdder.accept(IClientModuleHelper.INSTANCE.hudElementPercent(mode.getHUDIcon(), 1));
            } else {
                long stored = StorageUtils.getContainedChemical(chemicalHandler, MekanismChemicals.HYDROGEN);
                double ratio = MathUtils.divideToLevel(stored, chemicalHandler.getCapacityAsLong(0, chemicalHandler.getResource(0)));
                hudElementAdder.accept(IClientModuleHelper.INSTANCE.hudElementPercent(mode.getHUDIcon(), ratio));
            }
        }
    }

    @Override
    public void changeMode(IModule<ModuleJetpackUnit> module, Player player, ItemAccess itemAccess, int shift, boolean displayChangeMessage,
          @Nullable TransactionContext transaction) {
        JetpackMode newMode = mode.adjust(shift);
        if (mode != newMode) {
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_JETPACK_MODE.translate(), newMode);
            }
            module.replaceModuleConfig(player.registryAccess(), itemAccess, transaction, module.<JetpackMode>getConfigOrThrow(JETPACK_MODE).with(newMode));
        }
    }

    @Override
    public void onRemoved(IModule<ModuleJetpackUnit> module, ItemAccess itemAccess, boolean last, TransactionContext transaction) {
        //Vent the excess hydrogen from the jetpack
        if (Capabilities.CHEMICAL.getCapability(itemAccess) instanceof IMekanismResourceHandler<ChemicalResource, ?> handler) {
            //Note: Just directly interact with the containers as we want to change the entire access and don't care about
            // splitting between multiple items if for some reason the player has an oversized stack of the MekaSuit
            for (IResourceContainer<ChemicalResource> container : handler.getContainers()) {
                ContainerType.CHEMICAL.clampContents(container, transaction);
            }
        }
    }

    public float getThrustMultiplier() {
        if (mode == JetpackMode.HOVER) {
            return hoverThrustMultiplier.getMultiplier();
        }
        return thrustMultiplier.getMultiplier();
    }

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
