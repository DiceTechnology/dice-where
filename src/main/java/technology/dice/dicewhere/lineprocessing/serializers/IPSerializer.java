package technology.dice.dicewhere.lineprocessing.serializers;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;
import technology.dice.dicewhere.api.api.IP;

import java.io.IOException;

/**
 * Created by gluiz on 10/07/2018.
 */
public class IPSerializer extends GroupSerializerObjectArray<IP> {

	@Override
	public void serialize(@NotNull DataOutput2 dataOutput2, @NotNull IP ip) throws IOException {
		dataOutput2.packInt(ip.getBytes().length);
		dataOutput2.write(ip.getBytes());

	}

	@Override
	public IP deserialize(@NotNull DataInput2 dataInput2, int i) throws IOException {
		int size = dataInput2.unpackInt();
		byte[] ret = new byte[size];
		dataInput2.readFully(ret);
		return new IP(ret);
	}
}
