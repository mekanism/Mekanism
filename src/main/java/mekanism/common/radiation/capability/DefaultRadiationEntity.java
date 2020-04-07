package mekanism.common.radiation.capability;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketRadiationData;
import mekanism.common.radiation.RadiationManager;
import mekanism.common.radiation.RadiationManager.RadiationScale;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class DefaultRadiationEntity implements IRadiationEntity {

    private double radiation;
    private double clientSeverity = 0;

    @Override
    public double getRadiation() {
        return radiation;
    }

    @Override
    public void radiate(double magnitude) {
        radiation += magnitude;
    }

    @Override
    public void update(ServerPlayerEntity player) {
        if (clientSeverity != radiation) {
            clientSeverity = radiation;
            PacketRadiationData.sync(player);
        }

        if (!player.isCreative()) {
            Random rand = player.world.getRandom();
            double minSeverity = MekanismConfig.general.radiationNegativeEffectsMinSeverity.get();
            double severityScale = RadiationScale.getScaledDoseSeverity(radiation);
            // Add food exhaustion randomly
            double chance = minSeverity + rand.nextDouble() * (1 - minSeverity);
            if (severityScale > chance) {
                player.getFoodStats().addExhaustion(8F);
            }
            // Hurt player randomly
            chance = minSeverity + rand.nextDouble() * (1 - minSeverity);
            if (severityScale > chance && rand.nextInt() % 3 == 0) {
                player.attackEntityFrom(RadiationManager.RADIATION_DAMAGE, 1);
            }
        }
    }

    @Override
    public void set(double magnitude) {
        radiation = magnitude;
    }

    @Override
    public void decay() {
        radiation = Math.max(RadiationManager.BASELINE, radiation * MekanismConfig.general.radiationTargetDecayRate.get());
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT ret = new CompoundNBT();
        ret.putDouble(NBTConstants.RADIATION, radiation);
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        radiation = nbt.getDouble(NBTConstants.RADIATION);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IRadiationEntity.class, new Capability.IStorage<IRadiationEntity>() {
            @Override
            public CompoundNBT writeNBT(Capability<IRadiationEntity> capability, IRadiationEntity instance, Direction side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IRadiationEntity> capability, IRadiationEntity instance, Direction side, INBT nbt) {
                if (nbt instanceof CompoundNBT) {
                    instance.deserializeNBT((CompoundNBT) nbt);
                }
            }
        }, DefaultRadiationEntity::new);
    }

    public static class Provider implements ICapabilitySerializable<CompoundNBT> {

        public static final ResourceLocation NAME = new ResourceLocation(Mekanism.MODID, NBTConstants.RADIATION);
        private final IRadiationEntity defaultImpl = new DefaultRadiationEntity();
        private final LazyOptional<IRadiationEntity> provider = LazyOptional.of(() -> defaultImpl);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
            if (capability == Capabilities.RADIATION_ENTITY_CAPABILITY) {
                return provider.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundNBT serializeNBT() {
            return defaultImpl.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            defaultImpl.deserializeNBT(nbt);
        }
    }
}
