![Logo](https://docs.swedenconnect.se/technical-framework/img/sweden-connect.png)

# opensaml-addons

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) 

---

## Release Notes

### 2.2.2

**Date:** _not yet released_

- An INDETERMINATE validation result stopped the validation of a response or assertion, and was then accepted by [ResponseProcessorImpl](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/ResponseProcessorImpl.java). For example, a missing receive URL meant that the signature of the response was never checked. All validation steps are now run, and an INDETERMINATE result makes the response processing fail.

- The check of the `IssueInstant` of a response in [ResponseValidator](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/validation/ResponseValidator.java) was inverted. A response received more than 30 seconds (the allowed clock skew) after it was issued was rejected, while a response with an issue time up to 3.5 minutes in the future was accepted.

- The maximum SSO session age (`maxSessionAge`) of [ResponseValidationSettings](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/validation/ResponseValidationSettings.java) was never applied by [ResponseProcessorImpl](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/ResponseProcessorImpl.java), so assertions from IdP sessions of any age were accepted.

- The SP metadata cache of [ResponseProcessorImpl](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/response/ResponseProcessorImpl.java) was not thread safe. Also, if the SP metadata could not be found the first time, this was remembered for the lifetime of the processor.

- [ProxyMetadataProvider](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/metadata/provider/ProxyMetadataProvider.java) did not work when the wrapped resolver already had a metadata filter. Initialization either failed, or the provider's own filters (including signature validation) were not installed.

- The default `HttpClient` created by [HTTPMetadataProvider](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/metadata/provider/HTTPMetadataProvider.java) and [MDQMetadataProvider](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/metadata/provider/MDQMetadataProvider.java) was never closed, so each destroyed provider leaked a connection pool. The default client does not validate the TLS server certificate, and a warning is now also logged when metadata is downloaded using HTTPS with this client without signature verification.

- `getMetadata()` of [MDQMetadataProvider](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/metadata/provider/MDQMetadataProvider.java) failed with an `IllegalArgumentException` when the in-memory cache was used and new entities had been fetched since the last call. It also returned metadata that had not been processed by the configured filters, and could miss newly fetched entities. The metadata is now built from copies of the resolved (and filtered) entities.

- `getMetadata()` of [CompositeMetadataProvider](https://github.com/swedenconnect/opensaml-addons/blob/main/src/main/java/se/swedenconnect/opensaml/saml2/metadata/provider/CompositeMetadataProvider.java) failed with a `NullPointerException` when one of the underlying providers had no metadata (for example after a failed download). Also, the composite metadata was not rebuilt when an MDQ or static provider got new metadata.

---

Copyright &copy; 2016-2026, [Sweden Connect](https://swedenconnect.se). Licensed under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).
