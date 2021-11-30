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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.security.impl.RandomIdentifierGenerationStrategy;
import se.swedenconnect.opensaml.common.utils.SamlLog;
import se.swedenconnect.opensaml.saml2.core.build.AuthnRequestBuilder;
import se.swedenconnect.opensaml.saml2.metadata.EntityDescriptorUtils;
import se.swedenconnect.opensaml.saml2.metadata.HolderOfKeyMetadataSupport;
import se.swedenconnect.opensaml.saml2.request.AuthnRequestGeneratorContext.HokRequirement;

/**
 * Abstract base class for generating AuthnRequest messages.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class AbstractAuthnRequestGenerator extends AbstractInitializableComponent implements AuthnRequestGenerator {

  /** Logging instance. */
  private final Logger log = LoggerFactory.getLogger(AbstractAuthnRequestGenerator.class);

  /** The SP entityID. */
  private final String spEntityID;

  /** The SP signing credential. */
  private final X509Credential signCredential;

  /** The SP metadata. */
  private EntityDescriptor cachedSpMetadata;

  /** Generates ID. */
  private final RandomIdentifierGenerationStrategy idGenerator = new RandomIdentifierGenerationStrategy(20);

  /**
   * Constructor.
   * 
   * @param spEntityID
   *          the SP entityID
   * @param signCredential
   *          the signing credential
   */
  public AbstractAuthnRequestGenerator(final String spEntityID, final X509Credential signCredential) {
    this.spEntityID = Optional.ofNullable(spEntityID)
      .filter(e -> !StringUtils.isBlank(e))
      .orElseThrow(() -> new IllegalArgumentException("spEntityID must be set"));
    this.signCredential = signCredential;
    if (this.signCredential == null) {
      log.warn("No signing credential supplied - Generation will fail if the IdP requires signed requests");
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void doInitialize() throws ComponentInitializationException {
    this.cachedSpMetadata = this.getSpMetadata();
    if (this.cachedSpMetadata == null) {
      throw new ComponentInitializationException("No SP metadata is available");
    }
  }

  /** {@inheritDoc} */
  @Override
  public RequestHttpObject<AuthnRequest> generateAuthnRequest(final String idpEntityID,
      final String relayState, final AuthnRequestGeneratorContext context) throws RequestGenerationException {

    log.debug("Request to generate an AuthnRequest for {} ...", idpEntityID);

    final AuthnRequestGeneratorContext generatorContext = context != null
        ? context
        : new AuthnRequestGeneratorContext() {
        };

    final SPSSODescriptor spDescriptor = this.getSpMetadata().getSPSSODescriptor(SAMLConstants.SAML20P_NS);

    // First get hold of the IdP metadata ...
    //
    final EntityDescriptor idpMetadata = this.getIdpMetadata(idpEntityID);
    if (idpMetadata == null) {
      throw new RequestGenerationException("No metadata could be found for IdP " + idpEntityID);
    }

    // Make some checks about the holder of key requirement ...
    // If the hok-requirement states that HoK must be used, we ensure that the SP
    // has an AssertionConsumerService endpoint dedicated for this.
    //
    if (HokRequirement.REQUIRED.equals(context.getHokRequirement())) {
      if (HolderOfKeyMetadataSupport.getHokAssertionConsumerServices(spDescriptor).isEmpty()) {
        throw new RequestGenerationException(
          "Context Holder-of-key requirement states that HoK must be used, but SP does not "
              + "have a dedicated AssertionConsumerService endpoint for this");
      }
    }

    // Find out where to send the request and with which binding ...
    //
    final SingleSignOnService ssoService = this.getSingleSignOnService(idpMetadata, generatorContext);

    // Is this a HoK endpoint?
    boolean hokActive = HolderOfKeyMetadataSupport.HOK_WEBSSO_PROFILE_URI.equals(ssoService.getBinding());

    // OK, let's start building the AuthnRequest ...
    //
    AuthnRequestBuilder builder = AuthnRequestBuilder.builder();
    builder
      .id(this.idGenerator.generateIdentifier())
      .issuer(this.getSpEntityID())
      .issueInstant(Instant.now())
      .forceAuthn(generatorContext.getForceAuthnAttribute())
      .isPassive(generatorContext.getIsPassiveAttribute())
      .destination(ssoService.getLocation());

    // Ask the context callback about which AssertionConsumerService to use ...
    //
    final Object assertionConsumerService =
        generatorContext.getAssertionConsumerServiceResolver().apply(this.getPossibleAssertionConsumerServices(hokActive));
    if (assertionConsumerService != null) {
      if (assertionConsumerService instanceof String) {
        builder.assertionConsumerServiceURL((String) assertionConsumerService);
      }
      else if (assertionConsumerService instanceof Integer) {
        builder.assertionConsumerServiceIndex((Integer) assertionConsumerService);
      }
      else {
        throw new RequestGenerationException("Illegal return value from AssertionConsumerServiceResolver");
      }
    }

    // Ask the context callback about whether to include a AttributeConsumingServiceIndex, and if so, which.
    //
    if (!spDescriptor.getAttributeConsumingServices().isEmpty()) {
      builder.attributeConsumerServiceIndex(
        generatorContext.getAttributeConsumingServiceIndexResolver().apply(spDescriptor.getAttributeConsumingServices()));
    }

    // Get the intersection concerning NameID:s between the SP and IdP metadata
    // and invoke the builder for getting a NameIDPolicy.
    //
    final List<NameIDFormat> idpFormats = idpMetadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getNameIDFormats();
    final List<NameIDFormat> formats = spDescriptor.getNameIDFormats().stream()
      .filter(f -> idpFormats.stream().filter(idpf -> idpf.getURI().equals(f.getURI())).findFirst().isPresent())
      .collect(Collectors.toList());
    builder.nameIDPolicy(generatorContext.getNameIDPolicyBuilderFunction().apply(formats));

    // OK, time to build the RequestedAuthnContext element ...
    // We need to get hold of the assurance certifications of the IdP.
    //
    builder.requestedAuthnContext(generatorContext.getRequestedAuthnContextBuilderFunction()
      .apply(this.getAssuranceCertificationUris(idpMetadata, generatorContext), hokActive));

    // Add Scoping element (if implemented)
    this.addScoping(builder, generatorContext, idpMetadata);

    // Add Extensions element (if implemented)
    this.addExtensions(builder, generatorContext, idpMetadata);

    // OK, we are almost done. Build the AuthnRequest and let's invoke the customizer to give the
    // caller the possibility to add extensions and such ...
    //
    final AuthnRequest authnRequest = builder.build();
    generatorContext.getAuthnRequestCustomizer().accept(authnRequest);

    if (log.isTraceEnabled()) {
      log.trace("Created AuthnRequest: {}", SamlLog.toStringSafe(authnRequest));
    }

    return this.buildRequestHttpObject(
      authnRequest, relayState, generatorContext, this.getBinding(ssoService), ssoService.getLocation(), idpMetadata);
  }

  /**
   * Method that adds the {@code Scoping} element to the {@code AuthnRequest} being built.
   * <p>
   * The default implementation does nothing.
   * </p>
   * 
   * @param builder
   *          the builder
   * @param context
   *          the generator context
   * @param idpMetadata
   *          the IdP metadata
   * @throws RequestGenerationException
   *           for generation errors
   */
  protected void addScoping(final AuthnRequestBuilder builder, final AuthnRequestGeneratorContext context,
      final EntityDescriptor idpMetadata) throws RequestGenerationException {
    // NO-OP
  }

  /**
   * Method that adds the {@code Extensions} element to the {@code AuthnRequest} being built.
   * <p>
   * The default implementation does nothing.
   * </p>
   * 
   * @param builder
   *          the builder
   * @param context
   *          the generator context
   * @param idpMetadata
   *          the IdP metadata
   * @throws RequestGenerationException
   *           for generation errors
   */
  protected void addExtensions(final AuthnRequestBuilder builder, final AuthnRequestGeneratorContext context,
      final EntityDescriptor idpMetadata) throws RequestGenerationException {
    // NO-OP
  }

  /**
   * Gets the assurance certification URI:s for the IdP metadata.
   * <p>
   * The default implementation returns all URI:s found in the metadata.
   * </p>
   * 
   * @param idpMetadata
   *          the IdP metadata
   * @param context
   *          the context
   * @return a list of URI:s
   * @throws RequestGenerationException
   *           for errors
   */
  protected List<String> getAssuranceCertificationUris(final EntityDescriptor idpMetadata,
      final AuthnRequestGeneratorContext context) throws RequestGenerationException {
    return EntityDescriptorUtils.getAssuranceCertificationUris(idpMetadata);
  }

  /** {@inheritDoc} */
  @Override
  public String getSpEntityID() {
    return this.spEntityID;
  }

  /** {@inheritDoc} */
  @Override
  public X509Credential getSignCredential() {
    return this.signCredential;
  }

  /**
   * Gets the metadata for the SP that this generator services.
   * 
   * @return the SP metadata, or null if no metadata is found
   */
  protected abstract EntityDescriptor getSpMetadata();

  /**
   * Gets the IdP metadata for the given entityID.
   * 
   * @param idpEntityID
   *          the entityID for the IdP
   * @return the metadata or null if no metadata could be found
   */
  protected abstract EntityDescriptor getIdpMetadata(final String idpEntityID);

  /**
   * Builds a request HTTP object (including signing).
   * 
   * @param request
   *          the actual request
   * @param relayState
   *          the RelayState (may be null)
   * @param context
   *          the request generation context
   * @param binding
   *          the binding to use
   * @param destination
   *          the destination URL
   * @param recipientMetadata
   *          the recipient metadata
   * @return a request HTTP object
   * @throws RequestGenerationException
   *           for errors during signing or encoding
   */
  protected RequestHttpObject<AuthnRequest> buildRequestHttpObject(final AuthnRequest request,
      final String relayState, final AuthnRequestGeneratorContext context, final String binding,
      final String destination, final EntityDescriptor recipientMetadata)
      throws RequestGenerationException {

    final X509Credential signCred = Optional.ofNullable(context.getOverrideSignCredential()).orElse(this.signCredential);

    try {
      if (SAMLConstants.SAML2_REDIRECT_BINDING_URI.equals(binding)) {
        // Redirect binding
        return new RedirectRequestHttpObject<>(request, relayState, signCred, destination,
          recipientMetadata, context.getSignatureSigningConfiguration());
      }
      else if (SAMLConstants.SAML2_POST_BINDING_URI.equals(binding)) {
        // POST binding
        return new PostRequestHttpObject<>(request, relayState, signCred, destination, recipientMetadata,
          context.getSignatureSigningConfiguration());
      }
      else {
        throw new RequestGenerationException("Unsupported binding: " + binding);
      }
    }
    catch (MessageEncodingException | SignatureException e) {
      String msg = "Failed to encode/sign request for transport";
      log.error(msg, e);
      throw new RequestGenerationException(msg);
    }
  }

  /**
   * Extracts all possible SP AssertionConsumerService endpoints.
   * 
   * @param hokActive
   *          a flag that tells whether HoK is active or not
   * @return a list of possible endpoints
   */
  protected List<AssertionConsumerService> getPossibleAssertionConsumerServices(final boolean hokActive) {
    final SPSSODescriptor descriptor = this.getSpMetadata().getSPSSODescriptor(SAMLConstants.SAML20P_NS);
    if (hokActive) {
      return HolderOfKeyMetadataSupport.getHokAssertionConsumerServices(descriptor);
    }
    else {
      return descriptor.getAssertionConsumerServices().stream()
        .filter(a -> SAMLConstants.SAML2_POST_BINDING_URI.equals(a.getBinding()))
        .collect(Collectors.toList());
    }
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
   * @param context
   *          context for generating
   * @return a SingleSignOnService object
   * @throws RequestGenerationException
   *           if not valid endpoint can be found
   */
  protected SingleSignOnService getSingleSignOnService(final EntityDescriptor idp, final AuthnRequestGeneratorContext context)
      throws RequestGenerationException {

    final IDPSSODescriptor descriptor = Optional.ofNullable(idp.getIDPSSODescriptor(SAMLConstants.SAML20P_NS))
      .orElseThrow(() -> new RequestGenerationException("Invalid IdP metadata - missing IDPSSODescriptor"));

    if (HokRequirement.REQUIRED.equals(context.getHokRequirement()) || HokRequirement.IF_AVAILABLE.equals(context.getHokRequirement())) {
      SingleSignOnService ssoService = null;
      for (final SingleSignOnService sso : HolderOfKeyMetadataSupport.getHokSingleSignOnServices(descriptor)) {
        final String protocolBinding = sso.getUnknownAttributes().get(HolderOfKeyMetadataSupport.HOK_PROTOCOL_BINDING_ATTRIBUTE);
        if (context.getPreferredBinding().equals(protocolBinding)) {
          return sso;
        }
        else {
          ssoService = sso;
        }
      }
      if (ssoService != null) {
        return ssoService;
      }
      else if (HokRequirement.REQUIRED.equals(context.getHokRequirement())) {
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
      .filter(s -> context.getPreferredBinding().equals(s.getBinding()))
      .findFirst()
      .orElse(null);

    if (ssoService == null) {
      ssoService = descriptor.getSingleSignOnServices().stream()
        .filter(s -> SAMLConstants.SAML2_POST_BINDING_URI.equals(s.getBinding())
            || SAMLConstants.SAML2_REDIRECT_BINDING_URI.equals(s.getBinding()))
        .findFirst()
        .orElse(null);
    }
    if (ssoService == null) {
      String msg = String.format(
        "IdP '%s' does not specify endpoints for POST or Redirect - cannot send request", idp.getEntityID());
      log.error(msg);
      throw new RequestGenerationException(msg);
    }
    return ssoService;
  }

}
