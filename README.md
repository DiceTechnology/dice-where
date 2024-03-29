[![Java CI](https://github.com/DiceTechnology/dice-where/actions/workflows/main.yml/badge.svg?branch=master&event=push)](https://github.com/DiceTechnology/dice-where/actions/workflows/main.yml)[ ![Download](https://maven-badges.herokuapp.com/maven-central/technology.dice.open/dice-where/badge.svg) ](https://maven-badges.herokuapp.com/maven-central/technology.dice.open/dice-where)[![codecov](https://codecov.io/gh/DiceTechnology/dice-where/branch/master/graph/badge.svg)](https://codecov.io/gh/DiceTechnology/dice-where)

dice-where is a low memory footprint, highly efficient Geo IP lookup library that relies on locally available data. 
The library pre-processes all the data from a list of databases and allows the client application to lookup one or all of them in a blocking or non-blocking way.
It has been designed to load *csv* datasources but can be extended to load data from any format. This library is also able to load *csv* files directly from within a *gzip* or *zip* file.

# Installation

dice-where is available from [Maven Central](https://search.maven.org/artifact/technology.dice.open/dice-where), with the coordinates `technology.dice.open:dice-where`.

 If you are using Maven, just add the following dependency to your `pom.xml`:
```xml
<dependency>
  <groupId>technology.dice.open</groupId>
  <artifactId>dice-where</artifactId>
  <version>VERSION</version>
  <type>pom</type>
</dependency>
```

# Usage Examples
TL/DR section for quickly getting up and running. The code snippets below assume there is a `print()` method to print out the results of the lookups, printing the fields in the following order: country, least specific division, most specific division, city, and postcode.
## Single database

```java
IPResolver resolver = new IPResolver.Builder()
      .withProvider(
        new MaxmindLineParser(
            Paths.get("<localHD>/GeoLite2-Country-CSV_20180703/GeoLite2-Country-Locations-en.csv"),
            Paths.get("<localHD>/GeoLite2-Country-CSV_20180703/GeoLite2-Country-Blocks-IPv4.csv"),
            Paths.get("<localHD>/GeoLite2-Country-CSV_20180703/GeoLite2-Country-Blocks-IPv6.csv")
        ))
        build();      	

        print(resolver.resolve("31.185.196.84"));
        print(resolver.resolve("43.14.124.2"));
        print(resolver.resolve("d3b6:3068:9496:934c:16a:fcfc:23c0:807a"));
        print(resolver.resolve("2c0f:feb1::"));		

```
output
```
31.185.196.84 -> [GB,Optional.empty,Optional.empty,Optional.empty,Optional.empty].
43.14.124.2   -> [JP,Optional.empty,Optional.empty,Optional.empty,Optional.empty].
d3b6:3068:9496:934c:16a:fcfc:23c0:807a -> IP not found
2c0f:feb1::   ->[MU,Optional.empty,Optional.empty,Optional.empty,Optional.empty].
```

## Two databases
```java
IPResolver resolver = new IPResolver.Builder()
        .withProvider(
          new MaxmindLineParser(
              Paths.get("<localHD>/GeoLite2-Country-CSV_20180703/GeoLite2-Country-Locations-en.csv"),
              Paths.get("<localHD>/GeoLite2-Country-CSV_20180703/GeoLite2-Country-Blocks-IPv4.csv"),
              Paths.get("<localHD>/GeoLite2-Country-CSV_20180703/GeoLite2-Country-Blocks-IPv6.csv")
          ))
          .withProvider(new DbIpIpToLocationAndIspCSVLineParser(Paths.get("<localHD>/dbip-full-2018-07.csv")))
        build();      			

        print(resolver.resolve("31.185.196.84"));
        print(resolver.resolve("43.14.124.2"));
        print(resolver.resolve("d3b6:3068:9496:934c:16a:fcfc:23c0:807a"));
        print(resolver.resolve("2c0f:feb1::"));
```

output:
```
31.185.196.84
   Maxmind -> [GB,Optional.empty,Optional.empty,Optional.empty,Optional.empty].
   DB-IP   -> [GB,Optional[England],Optional[Devon],Optional[Whitestone],Optional.empty].
43.14.124.2
   Maxmind -> [JP,Optional.empty,Optional.empty,Optional.empty,Optional.empty].
   DB-IP   -> [JP,Optional[Okayama],Optional[Kurashiki Shi],Optional[Kurashiki (Kanda)],Optional[683-0051]].
d3b6:3068:9496:934c:16a:fcfc:23c0:807a
   Maxmind -> IP not found
   DB-IP   -> [US,Optional[New York],Optional[New York County],Optional[New York],Optional[10123]].
2c0f:feb1::
   Maxmind  ->[MU,Optional.empty,Optional.empty,Optional.empty,Optional.empty].
   DB-IP    ->[TZ,Optional[Dar es Salaam],Optional[Ilala District],Optional[Dar es Salaam],Optional.empty].
```

## Listeners
wip

# API
There are two main classes client applications will typically use. Those are detailed below.

## IPResolver
### Building
The `IPResolver` is the main entry class for dice-where. It needs at least one `LineReader` (see Databases below) but has numerous options that define its behaviour once in use. An `IPResolver` is build throw it's builder class `IPResolver.Builder` and has the following build options:
* `withProvider` - an arbitrary number of `LineReaders`. Accepts at most one of each `DatabaseProvider` (see below for supported database providers)
* `withReaderListener` - a listener that is notified of events occurring during the line reading stage
* `withProcessorListener` - a listener that is notified of events occurring during the line processing stage
* `withBuilderListener` - a listener that is notified of events occurring during the in-memory database building stage
* `retainOriginalLine` - whether to make the original file line available on query results

An instance of `IPResolver`can be obtained by calling `build()` on the `IPResolver.Builder`instance and the result.
This method will trigger the processing of all the configured databases and can take some time, depending on the number 
of lines to be processed (typically a function of the database granularity). See the benchmark section below for more details.

###Decorators
A `Decorator` mechanism has been baked into the library to offer some flexibility with various tasks and enrich
`IpInformation` objects - based on data from a given `DecoratorDbReader` implementation. A good example are the `VpnDecorator`
and `MaxmindVpnDecoratorDbReader` implementations. The `VpnDecorator` is responsible for marking all `IpInformation` ranges
as VPN if certain criteria are met. The `MaxmindVpnDecoratorDbReader` reads the Maxmind 'anonymous' database and identifies
all the VPN entries. Those decorators can de extended if need be, and new decorators can be created as the user sees fit
to aid with a specific challenge. Example details below:

#####Inbound VPN/Proxy traffic
At the moment, there is logic in place to determine whether an IP originates from a VPN. That logic lives in the `parseDbLine` method
in the `MaxmindVpnDecoratorDbReader` class. The decision is made based on the result value of the 
`is_anonymous_vpn` database field. The `is_anonymous` field is ignored.
 
#####`is_anonymous`: Whether the IP address belongs to any sort of anonymous network
#####`is_anonymous_vpn`: Whether the IP address belongs to an anonymous VPN system

There might be a scenario where the IP is coming from a Proxy, in which case that IP would, rightly so, not be deemed a VPN,
but it is still anonymous. You can choose to handle this scenario by overriding the implementation of the `parseDbLine`
method in your own implementation of `DecoratorDbReader` and utilize the unused field in a way you see fit.

### Querying
Once created, it contains methods to query a location by IP.
There are two main query methods:
* `CompletionStage<Optional<IPInformation>> resolveAsync(String ip, DatabaseProvider provider, ExecutorService executorService)`
* `Map<DatabaseProvider, CompletionStage<Optional<IPInformation>>> resolveAsync(String ip, ExecutorService executorService)`

The main difference is passing, or not, the specific `DatabaseProvider` we want to query against, or instead perform a query against all the loaded databases obtaining a `Map` indexed by the `DatabaseProvider` that produced each result.
These methods are overloaded to accept different representation of the IPs, to omit the `ExecutorService` to use (and therefore use the system default one, typically `ForkJoinPool`), or lastly to perform a blocking lookup. For more details see the class `IPResolver`

### IPInformation
The IPInformation class is the representation of a location in dice-where. It contains the following accessors:
* `String getCountryCodeAlpha2()` - the two character representation of the country
* `Optional<String> getCity()` - the city the IP
* `Optional<String> getLeastSpecificDivision()` - the least specific administrative division of this location
* `Optional<String> getMostSpecificDivision()` - mostSpecificDivision  the most specific administrative division of this location
* `Optional<String> getPostcode()`- the post code of this location
* `IP getStartOfRange()` - the first IP of the range of IPs located in this location
* `IP getEndOfRange()`- the last IP of the range of IPs located in this location
* `Optional<String> getOriginalLine()` - the database line that got processed into this location object

The original line field will be populated only if the database reader was initialised with the option to retain them.
The remaining `Optional<String>` fields will be filled depending on the granularity of the provided database.

### Listeners
wip

## Internals
wip: stages of processing, threading etc
### Line reader

`Line reader` aka. `Provider` processes the raw data into easy to look up format, to achieve optimal performance. Depends on characteristic of your application you can choose one of many storage types:
* StorageMode.HEAP
* StorageMode.HEAP_BYTE_ARRAY
* StorageMode.OFF_HEAP
* StorageMode.FILE

Those are directly linked to mapdb modes described [here](http://www.mapdb.org/book/performance/). Default one is `StorageMode.FILE`
### Line processor
wip
### Database builder
wip

## Databases
This library contains out-of-the-box parsers for the following databases:
* DB-IP (https://db-ip.com)
* Maxmind (https://www.maxmind.com)

### DB-IP
DB-IP distributes their database in a single file, containing the IPV4 and IPV6 ranges and their locations. In it's simplest form, a DB-IP reader can be created as follows:

```java
new DbIpIpToLocationAndIspCSVLineParser(Paths.get("<localHD>/dbip-country-2018-07.csv.gz"))
```

### Maxmind
Maximind distributes their databases spread across three main files:
* An IPV4 database *csv*
* An IPV6 database *csv*
* A localised location name *csv*
dice-where requires the client application to initialise the Maxmind database reader by providing the location of those three files. In its most simple form, a Maxmind reader can be created as follows:

```java
new MaxmindLineParser(
    Paths.get("<localHD>/GeoIP2-City-CSV_20180703/GeoIP2-City-Locations-en.csv.zip"),
	Paths.get("<localHD>/GeoIP2-City-CSV_20180703/GeoIP2-City-Blocks-IPv4.csv.zip"),
	Paths.get("<localHD>/GeoIP2-City-CSV_20180703/GeoIP2-City-Blocks-IPv6.csv")
)
```

The Maxmind reader can load a database with any precision (for example City or Country) and from both the Lite and commercial versions.

# Benchmark
Performance of the library depends on a number of variables including:
- CPU
- type of local disk
- OS

However, on a 2017 MBP with SSD, MacOS and 16Gb RAM we observed the following performance for
when loading the full Maxmind and DbIp databases (9.4M ip ranges):

- Initial load from ip data files: 35s
- Single threaded lookup of 1000 distinct ip addresses: 100ms

Benchmarking on other machine - WIP
Benchmarking heap and off-heap memory usage - WIP
