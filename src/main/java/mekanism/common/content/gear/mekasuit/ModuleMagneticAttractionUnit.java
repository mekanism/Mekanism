package mekanism.common.content.gear.mekasuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@ParametersAreNotNullByDefault
public record ModuleMagneticAttractionUnit(Range range) implements ICustomModule<ModuleMagneticAttractionUnit> {

    public static final Identifier RANGE = Mekanism.rl("range");

    public ModuleMagneticAttractionUnit(IModule<ModuleMagneticAttractionUnit> module) {
        this(module.<Range>getConfigOrThrow(RANGE).get());
    }

    @Override
    public void tickServer(IModule<ModuleMagneticAttractionUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        if (range != Range.OFF) {
            float size = 4 + range.getRange();
            int usage = Mth.ceil(MekanismConfig.gear.mekaSuitEnergyUsageItemAttraction.get() * range.getRange());
            try (Transaction simulation = Transaction.openRoot()) {
                if (module.useEnergy(player, stack, usage, simulation) < usage) {
                    //Not enough energy, just exit
                    return;
                }
            }
            //If the energy cost is free, or we have enough energy for at least one pull grab all the items that can be picked up.
            //Note: We check distance afterwards so that we aren't having to calculate a bunch of distances when we may run out
            // of energy, and calculating distance is a bit more expensive than just checking if it can be picked up
            for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(size, size, size), item -> !item.hasPickUpDelay())) {
                if (item.distanceTo(player) > 0.001) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        if (module.useEnergy(player, stack, usage, transaction) < usage) {
                            //If we aren't able to extract enough energy, stop trying to pull any further items
                            break;
                        }
                        pullItem(player, item);
                        transaction.commit();
                    }
                }
            }
        }
    }

    private void pullItem(Player player, ItemEntity item) {
        Vec3 diff = player.position().subtract(item.position());
        Vec3 motionNeeded = new Vec3(Math.min(diff.x, 1), Math.min(diff.y, 1), Math.min(diff.z, 1));
        Vec3 motionDiff = motionNeeded.subtract(player.getDeltaMovement());
        item.setDeltaMovement(motionDiff.scale(0.2));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PacketLightningRender(LightningPreset.MAGNETIC_ATTRACTION, 31 * player.getUUID().hashCode() + item.hashCode(),
              player.position().add(0, 0.2, 0), item.position(), (int) (diff.length() * 4)));
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleMagneticAttractionUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleMagneticAttractionUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(moduleContainer, stack, player, MekanismLang.MODULE_MAGNETIC_ATTRACTION.translate());
    }

    @NothingNullByDefault
    public enum Range implements IHasTextComponent, StringRepresentable {
        OFF(0),
        LOW(1F),
        MED(3F),
        HIGH(5),
        ULTRA(10);

        public static final Codec<Range> CODEC = StringRepresentable.fromEnum(Range::values);
        public static final IntFunction<Range> BY_ID = ByIdMap.continuous(Range::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, Range> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Range::ordinal);

        private final String serializedName;
        private final float range;
        private final Component label;

        Range(float boost) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.range = boost;
            this.label = TextComponentUtil.getString(Float.toString(boost));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public float getRange() {
            return range;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}