/*
 * Copyright (C) 2018 - present by Dice Technology Ltd.
 *
 * Please see distribution for license.
 */

package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;

import java.io.Serializable;

/**
 * Minimum set of information for each decorating object.
 */
public interface DecoratorInformation extends Serializable {

  IP getRangeStart();

  IP getRangeEnd();

  int getNumberOfMatches();

  <T extends DecoratorInformation> T withNewRange(IP start, IP end);

  <T extends DecoratorInformation> T withNumberOfMatches(int i);

}
