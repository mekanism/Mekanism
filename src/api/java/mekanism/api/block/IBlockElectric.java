package mekanism.api.block;

/**
 * Implement this if the block is electric
 */
public interface IBlockElectric {

    default double getUsage() {
        return 0;
    }

    default double getConfigStorage() {
        return 400 * getUsage();
    }

    default double getStorage() {
        return Math.max(getConfigStorage(), getUsage());
    }
}