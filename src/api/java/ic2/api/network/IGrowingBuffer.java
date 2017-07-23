package ic2.api.network;

import java.io.DataInput;
import java.io.DataOutput;

public interface IGrowingBuffer extends DataInput, DataOutput {
	/**
	 * Write a number of variable size to the buffer
	 *
	 * @param i The number to write
	 */
	void writeVarInt(int i);

	/**
	 * Write a string directly to the buffer,
	 * opposed to {@link DataOutput#writeUTF(String)} that properly encodes Unicode characters first
	 *
	 * @param s The string to write
	 */
	void writeString(String s);

	/**
	 * Read a number of variable size to the buffer
	 *
	 * @return The variably sized number stored on the buffer
	 */
	int readVarInt();

	/**
	 * Read a string directly from the buffer,
	 * opposed to {@link DataInput#readUTF()} that properly decodes Unicode characters first
	 *
	 * @return The string stored on the buffer
	 */
	String readString();
}