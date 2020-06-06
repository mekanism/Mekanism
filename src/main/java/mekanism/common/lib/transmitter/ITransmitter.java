package mekanism.common.lib.transmitter;

//TODO: Merge with IGridTransmitter?
public interface ITransmitter {

    /**
     * Get the transmitter's transmission type
     *
     * @return TransmissionType this transmitter uses
     */
    TransmissionType getTransmissionType();
}