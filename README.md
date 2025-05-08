![Logo](img/sweden-connect-logo.png)

# opensaml-addons

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) ![Maven Central](https://img.shields.io/maven-central/v/se.swedenconnect.opensaml/opensaml-addons.svg)



Utility extensions for OpenSAML.

---

This open source package is an extension to OpenSAML that offers utility classes and interfaces such as:

* Utility methods for creating OpenSAML objects that does not require using the builder classes directly.
* Utility methods for marshalling and unmarshalling.
* Simplified support for signing XML objects.
* An abstraction for metadata handling making it easier to download and use SAML metadata.
* A builder pattern for some commonly used objects, such as creating SAML attribute objects, entity descriptors (metadata) or authentication requests.
* A framework for validation of responses and assertions.

Java API documentation of the opensaml-addons library is found at [https://docs.swedenconnect.se/opensaml-addons/apidoc](https://docs.swedenconnect.se/opensaml-addons/apidoc/).

### Maven and opensaml-addons

The opensaml-addons project artifacts are published to Maven central.

Include the following snippet in your Maven POM to add opensaml-addons as a dependency for your project.

```
<dependency>
  <groupId>se.swedenconnect.opensaml</groupId>
  <artifactId>opensaml-addons</artifactId>
  <version>${opensaml-addons.version}</version>
</dependency>
```
### Initializing the OpenSAML library

See <https://github.com/swedenconnect/opensaml-security-ext>.
        
---

Copyright &copy; 2016-2025, [Sweden Connect](https://swedenconnect.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
