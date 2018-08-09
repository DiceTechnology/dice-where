[![Build Status](https://travis-ci.com/IMGGaming/dice-where.svg?token=F6ktiNWbNbvGRbN5NmqA&branch=master)](https://travis-ci.com/IMGGaming/dice-where)
# DICE-WHERE
dice-where is a low memory footprint, highly efficient Geo IP lookup library that relies on locally available data. 
The library pre-processes all the data from an list of databases and allows the client application to lookup one or all of them in a blocking or non-blocking way.
It has been designed to load *csv* datasources but can be extended to load data from any format. This library is able to load *csv* files directly from within a *gzip* or *zip* file.

## API
### IPResolver
wip
### LineReader
wip
###  Listeners
* Line reader  Listener
* Line processor Listener
* Database builder listener

## Databases
This library contains out-of-the-box parsers for the following databases:
* DB-IP (https://db-ip.com)
* Maxmind (https://www.maxmind.com)

### DB-IP
Country or city
wip
### Maxmind
Maximind distributes their databases spread across three main files: 
* An IPV4 database *csv*
* An IPV6 database *csv*
* A localised location name *csv*
dice-where requires the client application to initialise the Maxmind database reader by providing the location of those three files. In it's most simple form, a Maxmind reader can be created as follows:

```java
new MaxmindDbReader(
    Paths.get("<localHD>/GeoIP2-City-CSV_20180703/GeoIP2-City-Locations-en.csv.zip"),
	Paths.get("<localHD>/GeoIP2-City-CSV_20180703/GeoIP2-City-Blocks-IPv4.csv.zip"),
	Paths.get("<localHD>/GeoIP2-City-CSV_20180703/GeoIP2-City-Blocks-IPv6.csv")
)
```

Country or city
bla

## Listeners

## Usage Example

### Single database (Maxmind)

