/*
 * Copyright 2016-2023 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.request;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.x509.X509Credential;

/**
 * Interface for generating {@code AuthnRequest} messages.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public interface AuthnRequestGenerator {

  /**
   * Generates a SAML authentication request message.
   * 
   * @param idpEntityID the entityID of the IdP that we should send the request to
   * @param relayState the RelayState to include (may be null)
   * @param context the generator context (may be null)
   * @return a request object
   * @throws RequestGenerationException for errors during request generation
   */
  RequestHttpObject<AuthnRequest> generateAuthnRequest(final String idpEntityID, final String relayState,
      final AuthnRequestGeneratorContext context) throws RequestGenerationException;
  
  /**
   * Generates a SAML authentication request message.
   * 
   * @param idp the metadata for the IdP that we should send the request to
   * @param relayState the RelayState to include (may be null)
   * @param context the generator context (may be null)
   * @return a request object
   * @throws RequestGenerationException for errors during request generation
   */
  RequestHttpObject<AuthnRequest> generateAuthnRequest(final EntityDescriptor idp, final String relayState,
      final AuthnRequestGeneratorContext context) throws RequestGenerationException;  

  /**
   * Gets the entityID for the service provider that this generator services.
   * 
   * @return the SP entityID
   */
  String getSpEntityID();

  /**
   * Gets the signing credential to be used when signing the {@link AuthnRequest} messages.
   * 
   * @return the signing credential, or null if no signing should be performed
   */
  X509Credential getSignCredential();

}
