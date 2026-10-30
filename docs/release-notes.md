![Logo](https://docs.swedenconnect.se/technical-framework/img/sweden-connect.png)

# opensaml-addons

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) ![Maven Central](https://img.shields.io/maven-central/v/se.swedenconnect.opensaml/opensaml-addons.svg)

---

## Release Notes

### 2.2.2

**Date:** _not yet released_

- An INDETERMINATE validation result stopped the validation of a response or assertion, and was then accepted by [ResponseProcessorImpl](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/ResponseProcessorImpl.java). For example, a missing receive URL meant that the signature of the response was never checked. All validation steps are now run, and an INDETERMINATE result makes the response processing fail.

- The check of the `IssueInstant` of a response in [ResponseValidator](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/validation/ResponseValidator.java) was inverted. A response received more than 30 seconds (the allowed clock skew) after it was issued was rejected, while a response with an issue time up to 3.5 minutes in the future was accepted.

- The maximum SSO session age (`maxSessionAge`) of [ResponseValidationSettings](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/validation/ResponseValidationSettings.java) was never applied by [ResponseProcessorImpl](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/ResponseProcessorImpl.java), so assertions from IdP sessions of any age were accepted.

---

Copyright &copy; 2016-2026, [Sweden Connect](https://swedenconnect.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
