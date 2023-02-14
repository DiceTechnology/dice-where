package technology.dice.dicewhere.building.mmdb;

public interface CityResult {
    String country();

  String city();

  String postal();

  String mostSpecificDivision();

  String leastSpecificDivision();

  String geoNameId();
}
