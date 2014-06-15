package cofh.api.transport;

interface IEnderAttuned {

	public String getOwnerString();

	public int getFrequency();

	public boolean setFrequency(int frequency);

	public boolean clearFrequency();

}
