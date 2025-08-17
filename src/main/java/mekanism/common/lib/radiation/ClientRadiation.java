package mekanism.common.lib.radiation;

import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class ClientRadiation {

    private static RadiationScale clientRadiationScale = RadiationScale.NONE;
    private static double clientEnvironmentalRadiation = RadiationManager.get().baselineRadiation();
    private static double clientMaxMagnitude = RadiationManager.get().baselineRadiation();

    public static void setClientEnvironmentalRadiation(double radiation, double maxMagnitude) {
        clientEnvironmentalRadiation = radiation;
        clientMaxMagnitude = maxMagnitude;
        clientRadiationScale = RadiationScale.get(clientEnvironmentalRadiation);
    }

    public static double getClientEnvironmentalRadiation() {
        return RadiationManager.isGlobalRadiationEnabled() ? clientEnvironmentalRadiation : RadiationManager.get().baselineRadiation();
    }

    public static double getClientMaxMagnitude() {
        return RadiationManager.isGlobalRadiationEnabled() ? clientMaxMagnitude : RadiationManager.get().baselineRadiation();
    }

    public static RadiationScale getClientScale() {
        return RadiationManager.isGlobalRadiationEnabled() ? clientRadiationScale : RadiationScale.NONE;
    }

    public static void tickClient(Player player) {
        // terminate early if we're disabled
        if (!RadiationManager.isGlobalRadiationEnabled()) {
            return;
        }
        // perhaps also play Geiger counter sound effect, even when not using item (similar to fallout)
        RandomSource randomSource = player.level().getRandom();
        if (clientRadiationScale != RadiationScale.NONE && MekanismConfig.client.radiationParticleCount.get() != 0 && randomSource.nextInt(2) == 0) {
            int count = randomSource.nextInt(clientRadiationScale.ordinal() * MekanismConfig.client.radiationParticleCount.get());
            int radius = MekanismConfig.client.radiationParticleRadius.get();
            for (int i = 0; i < count; i++) {
                double x = player.getX() + randomSource.nextDouble() * radius * 2 - radius;
                double y = player.getY() + randomSource.nextDouble() * radius * 2 - radius;
                double z = player.getZ() + randomSource.nextDouble() * radius * 2 - radius;
                player.level().addParticle(MekanismParticleTypes.RADIATION.get(), x, y, z, 0, 0, 0);
            }
        }
    }

    public static void resetClient() {
        setClientEnvironmentalRadiation(RadiationManager.get().baselineRadiation(), RadiationManager.get().baselineRadiation());
    }
}
