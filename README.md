spring-boot-tus
======

[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)

Spring-boot-tus (sb-tus) is a standalone server side implementation of the [TUS protocol](http://tus.io/protocols/resumable-upload.html "Official TUS protocol website") with [Spring Boot](http://projects.spring.io/spring-boot/ "Official Spring-Boot website").

This application provides following features:

* A plugable architecture of core functionality for easy customization, like
  * persistence of asset (meta data for the upload)
  * persistence of binary data (the actual upload)
  * locking mechanism
  * location resolver
  * expiration strategy
  * error handling
* Download of binary data
  * at the original upload location out-of-the-box
  * custom, optional location for providing Browser/Client friendly URLs
* An event-driven API that can be used as an Service Providing Interface (SPI) for custom actions, like
  * Asset created
  * Asset terminated
  * etc.

This application is 100% compatible with the official library [TUS Java Client](https://github.com/tus/tus-java-client "Official TUS client Java based implementation")

## Getting Started

TODO


## Build
In order to build sb-tus you need to have JAVA 1.8 or higher in your `PATH`.

```shell
./gradlew clean build
```
