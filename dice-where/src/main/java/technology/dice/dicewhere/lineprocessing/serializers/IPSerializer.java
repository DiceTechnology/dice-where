/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.lineprocessing.serializers;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;
import technology.dice.dicewhere.api.api.IP;

public class IPSerializer extends GroupSerializerObjectArray<IP> {

  @Override
  public void serialize(@Nonnull DataOutput2 dataOutput2, @Nonnull IP ip) throws IOException {
    dataOutput2.packInt(ip.getBytes().length);
    dataOutput2.write(ip.getBytes());
  }

  @Override
  public IP deserialize(@Nonnull DataInput2 dataInput2, int i) throws IOException {
    int size = dataInput2.unpackInt();
    byte[] ret = new byte[size];
    dataInput2.readFully(ret);
    return new IP(ret);
  }
}
