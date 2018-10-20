package technology.dice.dicewhere.decorator;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

import java.io.*;

public class DecoratorInformationSerializer<T extends DecoratorInformation>
    extends GroupSerializerObjectArray<T> {

  @Override
  public void serialize(
      @NotNull DataOutput2 dataOutput2, @NotNull T decoratorInformation)
      throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(decoratorInformation);
    oos.flush();
    byte[] data = bos.toByteArray();
    dataOutput2.packInt(data.length);
    dataOutput2.write(data);
  }

  @Override
  public T deserialize(@NotNull DataInput2 dataInput2, int i)
      throws IOException {
    int size = dataInput2.unpackInt();
    byte[] ret = new byte[size];
    dataInput2.readFully(ret);

    ByteArrayInputStream bis = new ByteArrayInputStream(ret);
    ObjectInput in = null;
    try {
      in = new ObjectInputStream(bis);
      return (T) in.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }
}
