package mekanism.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/// Expose this as a capability on your block to expose it to Mekanism's Configuration card for purposes of saving data to the card and then loading it on another block.
public interface IConfigCardAccess {

    /// The translation key for the name to display as the "type" of data/tile in a configured configuration card.
    ///
    /// @return The translation key for the name to display.
    default String getConfigCardName() {
        return getConfigurationDataType().getDescriptionId();
    }

    /// Gets the type of the block this config card access exposes.
    ///
    /// @return The type of the block.
    ///
    /// @apiNote The reason this exists rather than being gotten directly from the tile the capability is accessed from is for purposes of if a block is proxying a
    /// capability such as Mekanism's bounding blocks.
    Block getConfigurationDataType();

    /// Checks if this config card access can handle the configuration data from another type of tile. This is used in Mekanism for things like allowing factories to
    /// accept data from different tiers of the same factory.
    ///
    /// @param type Type of the block the saved configuration data is.
    ///
    /// @return `true` if the data is compatible.
    default boolean isConfigurationDataCompatible(Block type) {
        return type == getConfigurationDataType();
    }

    /// Collects configuration data for this capability onto the passed in [`output`][ValueOutput].
    ///
    /// @param output Output to write the pertinent configuration data to.
    /// @param player Player who is using the configuration card.
    ///
    /// @since 10.8.0, Replaces the old getConfigurationData
    void writeConfigurationData(ValueOutput output, Player player);

    /// Sets the configuration data for the tile this capability represents from the given [`input`][ValueInput] that contains the previously stored configuration data.
    ///
    /// @param input  The configuration data stored on the configuration card ItemStack.
    /// @param player Player who is using the configuration card.
    void setConfigurationData(ValueInput input, Player player);

    /// This is called after [#setConfigurationData(ValueInput, Player)] to allow for easily doing any post-processing such as invalidating capabilities while
    /// ensuring that the proper data can be set first if a hierarchy is used so there may be multiple layers of [#setConfigurationData(ValueInput, Player)] and
    /// ensuring the post-processing doesn't happen until afterwards would lead to a bunch of duplicate code.
    void configurationDataSet();
}