/*
 * Copyright 2016-2025 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.opensaml.saml2.response;

import org.opensaml.saml.saml2.core.AuthnRequest;

import java.security.cert.X509Certificate;
import java.time.Instant;

/**
 * Represents the input passed along with a SAML Response to the {@link ResponseProcessor}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public interface ResponseProcessingInput {

  /**
   * Returns the authentication request message that corresponds to the response message being processed.
   *
   * @param id the ID of the authentication request
   * @return the AuthnRequest message or null if no message is available
   */
  AuthnRequest getAuthnRequest(final String id);

  /**
   * Returns the RelayState that was included in the request (or {@code null} if none was sent).
   *
   * @param id the ID of the authentication request
   * @return the RelayState variable or null
   */
  String getRequestRelayState(final String id);

  /**
   * Returns the URL on which the response message was received.
   *
   * @return the reception URL
   */
  String getReceiveURL();

  /**
   * Returns the timestamp when the response was received.
   *
   * @return the reception timestamp
   */
  Instant getReceiveInstant();

  /**
   * If the validation should perform a check of the Address(es) found in the assertion, this method should return the
   * address of the client, otherwise return {@code null}.
   *
   * @return the client IP address of null if no check should be made
   */
  String getClientIpAddress();

  /**
   * If the Holder-of-key WebSSO profile is in use, the client presented certificate is required.
   *
   * @return the client certificate, or null if none is available
   */
  X509Certificate getClientCertificate();

}
