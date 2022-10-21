package mekanism.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * Expose this as a capability on your TileEntity to expose it to Mekanism's Configuration card for purposes of saving data to the card and then loading it on another
 * tile.
 */
@AutoRegisterCapability
public interface IConfigCardAccess {

    /**
     * The translation key for the name to display as the "type" of data/tile in a configured configuration card.
     *
     * @return The translation key for the name to display.
     */
    String getConfigCardName();

    /**
     * Gets the type of the tile this config card access exposes.
     *
     * @return The type of the tile.
     *
     * @apiNote The reason this exists rather than being gotten directly from the tile the capability is accessed from is for purposes of if a block is proxying a
     * capability such as Mekanism's bounding blocks.
     */
    BlockEntityType<?> getConfigurationDataType();

    /**
     * Checks if this config card access can handle the configuration data from another type of tile. This is used in Mekanism for things like allowing factories to
     * accept data from different tiers of the same factory.
     *
     * @param type Type of the tile the saved configuration data is.
     *
     * @return {@code true} if the data is compatible.
     */
    default boolean isConfigurationDataCompatible(BlockEntityType<?> type) {
        return type == getConfigurationDataType();
    }

    /**
     * Collects configuration data for this capability into a new {@link CompoundTag}.
     * <br><br>
     * Mekanism additionally adds two extra pieces of data to this {@link CompoundTag} afterwards corresponding to the following two constants: {@link
     * NBTConstants#DATA_NAME} and {@link NBTConstants#DATA_TYPE} so it is recommended to ensure you don't put any data in a matching entry, or it will be overwritten.
     *
     * @param player - Player who is using the configuration card.
     *
     * @return A new {@link CompoundTag} containing all pertinent configuration data.
     */
    CompoundTag getConfigurationData(Player player);

    /**
     * Sets the configuration data for the tile this capability represents from the given {@link CompoundTag} that contains the previously stored configuration data.
     *
     * @param player - Player who is using the configuration card.
     * @param data   - {@link CompoundTag} of the configuration data stored on the configuration card ItemStack.
     */
    void setConfigurationData(Player player, CompoundTag data);

    /**
     * This is called after {@link #setConfigurationData(Player, CompoundTag)} to allow for easily doing any post-processing such as invalidating capabilities while
     * ensuring that the proper data can be set first if a hierarchy is used so there may be multiple layers of {@link #setConfigurationData(Player, CompoundTag)} and
     * ensuring the post-processing doesn't happen until afterwards would lead to a bunch of duplicate code.
     */
    void configurationDataSet();
}