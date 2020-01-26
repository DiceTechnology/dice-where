/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */
package technology.dice.dicewhere.utils;

import technology.dice.dicewhere.lineprocessing.serializers.protobuf.ThreeStateValueProto;

import java.util.Objects;
import java.util.Optional;

/** Created by stan on 14/09/2018 */
public class ProtoValueConverter {
  public static Optional<Boolean> parseThreeStateProto(
      ThreeStateValueProto.ThreeStateValue stateValue) {
    if (stateValue == ThreeStateValueProto.ThreeStateValue.NOT_SPECIFIED) {
      return Optional.empty();
    } else {
      return Optional.of(stateValue == ThreeStateValueProto.ThreeStateValue.IS_TRUE);
    }
  }

  public static ThreeStateValueProto.ThreeStateValue toThreeStateValue(Boolean input) {
    if (Objects.isNull(input)) {
      return ThreeStateValueProto.ThreeStateValue.NOT_SPECIFIED;
    } else {
      return input
          ? ThreeStateValueProto.ThreeStateValue.IS_TRUE
          : ThreeStateValueProto.ThreeStateValue.IS_FALSE;
    }
  }
}
