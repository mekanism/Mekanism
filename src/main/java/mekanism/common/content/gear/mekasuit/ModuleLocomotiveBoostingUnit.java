package mekanism.common.content.gear.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.IIncrementalEnum;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public record ModuleLocomotiveBoostingUnit(SprintBoost sprintBoost) implements ICustomModule<ModuleLocomotiveBoostingUnit> {

    public static final Identifier SPRINT_BOOST = Mekanism.rl("sprint_boost");

    public ModuleLocomotiveBoostingUnit(IModule<ModuleLocomotiveBoostingUnit> module) {
        this(module.<SprintBoost>getConfigOrThrow(SPRINT_BOOST).get());
    }

    @Override
    public void changeMode(IModule<ModuleLocomotiveBoostingUnit> module, Player player, ItemAccess itemAccess, int shift, boolean displayChangeMessage,
          @Nullable TransactionContext transaction) {
        SprintBoost newMode = sprintBoost.adjust(shift, v -> v.ordinal() < module.getInstalledCount() + 1);
        if (sprintBoost != newMode) {
            if (displayChangeMessage) {
                module.displayModeChange(player, MekanismLang.MODULE_SPRINT_BOOST.translate(), newMode);
            }
            module.replaceModuleConfig(player.registryAccess(), itemAccess, transaction, module.<SprintBoost>getConfigOrThrow(SPRINT_BOOST).with(newMode));
        }
    }

    @Override
    public void tickServer(IModule<ModuleLocomotiveBoostingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        try (Transaction subTransaction = Transaction.open(transaction)) {
            if (tick(module, itemAccess, player, subTransaction)) {
                subTransaction.commit();
            }
        }
    }

    @Override
    public void tickClient(IModule<ModuleLocomotiveBoostingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        // leave energy usage up to server
        try (Transaction simulation = Transaction.open(transaction)) {
            tick(module, itemAccess, player, simulation);
        }
    }

    private boolean tick(IModule<ModuleLocomotiveBoostingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        if (canFunction(module, itemAccess, player, transaction)) {
            float boost = sprintBoost.getBoost();
            if (!player.onGround()) {
                boost /= 5F; // throttle if we're in the air
            }
            if (player.isInWater()) {
                boost /= 5F; // throttle if we're in the water
            }
            player.moveRelative(boost, new Vec3(0, 0, 1));
            return true;
        }
        return false;
    }

    public boolean canFunction(IModule<ModuleLocomotiveBoostingUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        //Don't allow boosting unit to work when flying with the elytra, a jetpack should be used instead
        if (!player.isFallFlying() && player.isSprinting()) {
            int energyRequired = MathUtils.clampToInt(MekanismConfig.gear.mekaSuitEnergyUsageSprintBoost.get() * sprintBoost.getBoost() / 0.1D);
            return module.useAllEnergy(player, itemAccess, energyRequired, transaction);
        }
        return false;
    }

    public enum SprintBoost implements IHasTextComponent, IIncrementalEnum<SprintBoost>, StringRepresentable {
        OFF(0),
        LOW(0.05F),
        MED(0.1F),
        HIGH(0.25F),
        ULTRA(0.5F);

        public static final Codec<SprintBoost> CODEC = StringRepresentable.fromEnum(SprintBoost::values);
        public static final IntFunction<SprintBoost> BY_ID = ByIdMap.continuous(SprintBoost::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, SprintBoost> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SprintBoost::ordinal);

        private final String serializedName;
        private final float boost;
        private final Component label;

        SprintBoost(float boost) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.boost = boost;
            this.label = TextComponentUtil.getString(Float.toString(boost));
        }

        @Override
        public SprintBoost byIndex(int index) {
            return BY_ID.apply(index);
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getBoost() {
            return boost;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}