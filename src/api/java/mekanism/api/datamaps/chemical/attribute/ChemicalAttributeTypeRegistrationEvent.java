package mekanism.api.datamaps.chemical.attribute;

import java.util.List;
import mekanism.api.chemical.Chemical;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.ApiStatus.Internal;

/// Used to register new data maps as ones that handle chemical attributes.
///
/// @since 10.8.0
public class ChemicalAttributeTypeRegistrationEvent extends Event implements IModBusEvent {

    private final List<DataMapType<Chemical, ? extends IChemicalAttribute>> attributeTypes;

    @Internal
    public ChemicalAttributeTypeRegistrationEvent(List<DataMapType<Chemical, ? extends IChemicalAttribute>> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public void register(DataMapType<Chemical, ? extends IChemicalAttribute> type) {
        if (attributeTypes.contains(type)) {
            throw new IllegalStateException("Duplicate chemical attribute type registered: " + type.id());
        }
        attributeTypes.add(type);
    }
}