package technology.dice.dicewhere.building.mmdb;

public interface AnonymousResult {
  boolean hostingProvider();

  boolean vpn();

  boolean torExitNode();

  boolean publicProxy();

  boolean residentialProxy();
}
