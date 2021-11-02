/*
 * Copyright 2016-2021 Sweden Connect
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

import java.util.Optional;
import java.util.function.Predicate;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.swedenconnect.opensaml.saml2.metadata.HolderOfKeyMetadataSupport;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorInput.HokRequirement;

/**
 * Abstract base class for generating AuthnRequest messages.
 * 
 * @param <I>
 *          the type of the input required by this generator
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class AbstractAuthnRequestGenerator<I extends AuthnRequestGeneratorInput> extends AbstractRequestGenerator<AuthnRequest, I>
    implements AuthnRequestGenerator<I> {

  /** Logging instance. */
  private final Logger log = LoggerFactory.getLogger(AbstractAuthnRequestGenerator.class);

  /** Function for checking if a binding is valid. */
  protected static Predicate<String> isValidBinding = b -> SAMLConstants.SAML2_POST_BINDING_URI.equals(b)
      || SAMLConstants.SAML2_REDIRECT_BINDING_URI.equals(b);

  /** Function for checking if a metadata SingleSignOnService element has a binding that we support. */
  protected static Predicate<SingleSignOnService> hasSupportedBinding = s -> isValidBinding.test(s.getBinding());

  /**
   * Constructor.
   * 
   * @param entityID
   *          the entityID
   */
  public AbstractAuthnRequestGenerator(final String entityID) {
    super(entityID);
  }

  /**
   * Utility method that, given a {@link SingleSignOnService}, gets the binding URI (redirect/post).
   * 
   * @param sso
   *          the SingleSignOnService
   * @return the binding URI
   */
  protected String getBinding(final SingleSignOnService sso) {
    if (HolderOfKeyMetadataSupport.HOK_WEBSSO_PROFILE_URI.equals(sso.getBinding())) {
      return sso.getUnknownAttributes().get(HolderOfKeyMetadataSupport.HOK_PROTOCOL_BINDING_ATTRIBUTE);
    }
    else {
      return sso.getBinding();
    }
  }

  /**
   * Returns the {@code SingleSignOnService} element to use when sending the request to the IdP. The preferred binding
   * will be searched for first, and if this is not found, another binding that we support will be used.
   * 
   * @param idp
   *          the IdP metadata
   * @param input
   *          input for generating
   * @return a SingleSignOnService object
   * @throws RequestGenerationException
   *           if not valid endpoint can be found
   */
  protected SingleSignOnService getSingleSignOnService(final EntityDescriptor idp, final I input)
      throws RequestGenerationException {

    final String preferBinding = input.getPreferredBinding() != null ? input.getPreferredBinding() : this.getDefaultBinding();

    final IDPSSODescriptor descriptor = Optional.ofNullable(idp.getIDPSSODescriptor(SAMLConstants.SAML20P_NS))
      .orElseThrow(() -> new RequestGenerationException("Invalid IdP metadata - missing IDPSSODescriptor"));

    if (HokRequirement.REQUIRED.equals(input.getHokRequirement()) || HokRequirement.IF_AVAILABLE.equals(input.getHokRequirement())) {
      SingleSignOnService ssoService = null;
      for (final SingleSignOnService sso : HolderOfKeyMetadataSupport.getHokSingleSignOnServices(descriptor)) {
        final String protocolBinding = sso.getUnknownAttributes().get(HolderOfKeyMetadataSupport.HOK_PROTOCOL_BINDING_ATTRIBUTE);
        if (preferBinding.equals(protocolBinding)) {
          return sso;
        }
        else {
          ssoService = sso;
        }
      }
      if (ssoService != null) {
        return ssoService;
      }
      else if (HokRequirement.REQUIRED.equals(input.getHokRequirement())) {
        String msg = String.format("IdP '%s' does not specify endpoints for Holder-of-key - cannot send request", idp.getEntityID());
        log.error(msg);
        throw new RequestGenerationException(msg);
      }
      else {
        // HokRequirement.IF_AVAILABLE
        log.info("IdP '%s' does not specify endpoints for Holder-of-key - using normal WebSSO Profile", idp.getEntityID());
      }
    }

    SingleSignOnService ssoService = descriptor.getSingleSignOnServices()
      .stream()
      .filter(s -> preferBinding.equals(s.getBinding()))
      .findFirst()
      .orElse(null);

    if (ssoService == null) {
      ssoService = descriptor.getSingleSignOnServices().stream().filter(hasSupportedBinding).findFirst().orElse(null);
    }
    if (ssoService == null) {
      String msg = String.format("IdP '%s' does not specify endpoints for POST or Redirect - cannot send request", idp.getEntityID());
      log.error(msg);
      throw new RequestGenerationException(msg);
    }
    return ssoService;
  }

}
