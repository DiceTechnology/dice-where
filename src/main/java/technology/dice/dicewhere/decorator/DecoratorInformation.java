package technology.dice.dicewhere.decorator;

import technology.dice.dicewhere.api.api.IP;

public interface DecoratorInformation {

  IP getRangeStart();

  IP getRangeEnd();

  int getNumberOfMatches();

  <T extends DecoratorInformation> T withNewRange(IP start, IP end);

  <T extends DecoratorInformation> T withNumberOfMatches(int i);

}
