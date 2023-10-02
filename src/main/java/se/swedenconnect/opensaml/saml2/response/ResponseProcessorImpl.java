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
package se.swedenconnect.opensaml.saml2.response;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.assertion.impl.AudienceRestrictionConditionValidator;
import org.opensaml.saml.saml2.assertion.impl.BearerSubjectConfirmationValidator;
import org.opensaml.saml.saml2.assertion.impl.HolderOfKeySubjectConfirmationValidator;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.signature.support.SignaturePrevalidator;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import net.shibboleth.shared.codec.Base64Support;
import net.shibboleth.shared.codec.DecodingException;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.component.InitializableComponent;
import net.shibboleth.shared.component.UnmodifiableComponentException;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.XMLParserException;
import se.swedenconnect.opensaml.common.validation.CoreValidatorParameters;
import se.swedenconnect.opensaml.saml2.assertion.validation.AbstractAssertionValidationParametersBuilder;
import se.swedenconnect.opensaml.saml2.assertion.validation.AssertionValidationParametersBuilder;
import se.swedenconnect.opensaml.saml2.assertion.validation.AssertionValidator;
import se.swedenconnect.opensaml.saml2.assertion.validation.AuthnStatementValidator;
import se.swedenconnect.opensaml.saml2.response.replay.MessageReplayChecker;
import se.swedenconnect.opensaml.saml2.response.replay.MessageReplayException;
import se.swedenconnect.opensaml.saml2.response.validation.ResponseValidationException;
import se.swedenconnect.opensaml.saml2.response.validation.ResponseValidationParametersBuilder;
import se.swedenconnect.opensaml.saml2.response.validation.ResponseValidationSettings;
import se.swedenconnect.opensaml.saml2.response.validation.ResponseValidator;
import se.swedenconnect.opensaml.xmlsec.encryption.support.SAMLObjectDecrypter;

/**
 * Response processor for SAML Response messages.
 * <p>
 * Note that {@link #initialize()} must be invoked before the bean can be used.
 * </p>
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class ResponseProcessorImpl implements ResponseProcessor, InitializableComponent {

  /** Logging instance. */
  private final Logger log = LoggerFactory.getLogger(ResponseProcessorImpl.class);

  /** Metadata resolver for finding IdP and SP metadata. */
  protected MetadataResolver metadataResolver;

  /** The decrypter instance. */
  protected SAMLObjectDecrypter decrypter;

  /** The replay checker. */
  protected MessageReplayChecker messageReplayChecker;

  /** Used to locate certificates from the IdP metadata. */
  protected MetadataCredentialResolver metadataCredentialResolver;

  /** The signature trust engine to be used when validating signatures. */
  protected SignatureTrustEngine signatureTrustEngine;

  /** Validator for checking the a Signature is correct with respect to the standards. */
  protected SignaturePrevalidator signatureProfileValidator = new SAMLSignatureProfileValidator();

  /** The response validator. */
  protected ResponseValidator responseValidator;

  /** The assertion validator. */
  protected AssertionValidator assertionValidator;

  /** Static response validation settings. */
  protected ResponseValidationSettings responseValidationSettings;

  /** Do we require assertions to be encrypted? The default is {@code true}. */
  protected boolean requireEncryptedAssertions = true;

  /** Is this component initialized? */
  private boolean isInitialized = false;

  /** A cache for the SP metadata. */
  private Map<String, EntityDescriptor> spMetadataCache;

  /** {@inheritDoc} */
  @Override
  public ResponseProcessingResult processSamlResponse(final String samlResponse, final String relayState,
      final ResponseProcessingInput input, final ValidationContext validationContext)
      throws ResponseStatusErrorException, ResponseProcessingException {

    try {
      // Step 1: Decode the SAML response message.
      //
      final Response response = this.decodeResponse(samlResponse);

      if (log.isTraceEnabled()) {
        log.trace("[{}] Decoded Response: {}", logId(response), toString(response));
      }

      // The IdP metadata is required for all steps below ...
      //
      final String issuer = Optional.ofNullable(response.getIssuer()).map(Issuer::getValue).orElse(null);
      final EntityDescriptor idpMetadata = issuer != null
          ? this.getMetadata(issuer, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)
          : null;

      // Step 2: Validate the Response (including its signature).
      //
      this.validateResponse(response, relayState, input, idpMetadata, validationContext);

      // Step 3: Make sure this isn't a replay attack
      //
      this.messageReplayChecker.checkReplay(response);

      // Step 4. Check Status
      //
      if (!StatusCode.SUCCESS.equals(response.getStatus().getStatusCode().getValue())) {
        log.info("Authentication failed with status '{}' [{}]",
            ResponseStatusErrorException.statusToString(response.getStatus()), logId(response));
        throw new ResponseStatusErrorException(response.getStatus(), response.getID(), issuer);
      }

      // Step 5. Verify that the relay state matches the request.
      //
      this.validateRelayState(response, relayState, input);

      // Step 6. Decrypt assertion (if needed)
      //
      Assertion assertion = null;
      if (!response.getEncryptedAssertions().isEmpty()) {
        assertion = this.decrypter.decrypt(response.getEncryptedAssertions().get(0), Assertion.class);
        if (log.isTraceEnabled()) {
          log.trace("[{}] Decrypted Assertion: {}", logId(response, assertion), toString(assertion));
        }
      }
      else if (this.requireEncryptedAssertions) {
        throw new ResponseProcessingException("Assertion in response message is not encrypted - this is required");
      }
      else {
        assertion = response.getAssertions().get(0);
        if (log.isTraceEnabled()) {
          log.trace("[{}] Assertion: {}", logId(response, assertion), toString(assertion));
        }
      }

      // Step 7. Validate the assertion
      //
      this.validateAssertion(assertion, response, input, idpMetadata, validationContext);

      // And finally, build the result.
      //
      return new ResponseProcessingResultImpl(response, assertion);
    }
    catch (MessageReplayException e) {
      throw new ResponseProcessingException("Message replay: " + e.getMessage(), e);
    }
    catch (DecryptionException e) {
      throw new ResponseProcessingException("Failed to decrypt assertion: " + e.getMessage(), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void initialize() throws ComponentInitializationException {
    if (this.metadataResolver == null) {
      throw new ComponentInitializationException("Property 'metadataResolver' must be assigned");
    }
    if (this.requireEncryptedAssertions && this.decrypter == null) {
      throw new ComponentInitializationException("Property 'decrypter' must be assigned");
    }
    else if (this.decrypter == null) {
      log.warn("Property 'decrypter' is not assigned - the processor will not be able to decrypt assertions");
    }
    if (this.messageReplayChecker == null) {
      throw new ComponentInitializationException("Property 'messageReplayChecker' must be assigned");
    }

    if (this.responseValidationSettings == null) {
      this.responseValidationSettings = new ResponseValidationSettings();
      log.info("Using default responseValidationSettings [{}]", this.responseValidationSettings);
    }

    if (!this.isInitialized) {

      this.metadataCredentialResolver = new MetadataCredentialResolver();
      this.metadataCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap
          .buildBasicInlineKeyInfoCredentialResolver());
      this.metadataCredentialResolver.initialize();

      this.signatureTrustEngine = new ExplicitKeySignatureTrustEngine(this.metadataCredentialResolver,
          DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());

      this.responseValidator = this.createResponseValidator(signatureTrustEngine, signatureProfileValidator);
      if (this.responseValidator == null) {
        throw new ComponentInitializationException("createResponseValidator must not return null");
      }
      this.assertionValidator = this.createAssertionValidator(signatureTrustEngine, signatureProfileValidator);
      if (this.assertionValidator == null) {
        throw new ComponentInitializationException("createAssertionValidator must not return null");
      }

      this.isInitialized = true;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isInitialized() {
    return this.isInitialized;
  }

  /**
   * Sets up the response validator.
   * <p>
   * The default implementation creates a {@link ResponseValidator} instance. For use within the Swedish eID framework
   * subclasses should create a {@code SwedishEidResponseValidator} instance, see the swedish-eid-opensaml library
   * (https://github.com/litsec/swedish-eid-opensaml).
   * </p>
   *
   * @param signatureTrustEngine the signature trust engine to be used when validating signatures
   * @param signatureProfileValidator validator for checking the a Signature is correct with respect to the standards
   * @return the created response validator
   */
  protected ResponseValidator createResponseValidator(final SignatureTrustEngine signatureTrustEngine,
      final SignaturePrevalidator signatureProfileValidator) {
    return new ResponseValidator(signatureTrustEngine, signatureProfileValidator);
  }

  /**
   * Sets up the assertion validator.
   * <p>
   * The default implementation creates a {@link AssertionValidator} instance. For use within the Swedish eID framework
   * subclasses should create a {@code SwedishEidAssertionValidator} instance, see the opensaml-swedish-eid library
   * (https://github.com/swedenconnect/opensaml-swedish-eid).
   * </p>
   *
   * @param signatureTrustEngine the signature trust engine to be used when validating signatures
   * @param signatureProfileValidator validator for checking the a Signature is correct with respect to the standards
   * @return the created assertion validator
   */
  protected AssertionValidator createAssertionValidator(
      final SignatureTrustEngine signatureTrustEngine, final SignaturePrevalidator signatureProfileValidator) {

    return new AssertionValidator(signatureTrustEngine, signatureProfileValidator,
        Arrays.asList(new BearerSubjectConfirmationValidator(), new HolderOfKeySubjectConfirmationValidator()),
        Arrays.asList(new AudienceRestrictionConditionValidator()),
        Arrays.asList(new AuthnStatementValidator()));
  }

  protected AbstractAssertionValidationParametersBuilder<?> getAssertionValidationParametersBuilder() {
    return AssertionValidationParametersBuilder.builder();
  }

  /**
   * Decodes the received SAML response message into a {@link Response} object.
   *
   * @param samlResponse the Base64 encoded SAML response
   * @return a {@code Response} object
   * @throws ResponseProcessingException for decoding errors
   */
  protected Response decodeResponse(final String samlResponse) throws ResponseProcessingException {
    try {
      final byte[] decodedBytes = Base64Support.decode(samlResponse);
      if (decodedBytes == null) {
        log.info("Unable to Base64 decode SAML response message");
        throw new MessageDecodingException("Unable to Base64 decode SAML response message");
      }
      return Response.class.cast(
          XMLObjectSupport.unmarshallFromInputStream(
              XMLObjectProviderRegistrySupport.getParserPool(), new ByteArrayInputStream(decodedBytes)));
    }
    catch (MessageDecodingException | XMLParserException | UnmarshallingException | DecodingException e) {
      throw new ResponseProcessingException("Failed to decode message", e);
    }
  }

  /**
   * Validates the response including its signature.
   *
   * @param response the response to verify
   * @param relayState the relay state that was received
   * @param input the processing input
   * @param idpMetadata the IdP metadata
   * @param validationContext optional validation context
   * @throws ResponseValidationException for validation errors
   */
  protected void validateResponse(final Response response, final String relayState, final ResponseProcessingInput input,
      final EntityDescriptor idpMetadata, final ValidationContext validationContext)
      throws ResponseValidationException {

    final AuthnRequest authnRequest = input.getAuthnRequest(response.getInResponseTo());
    if (authnRequest == null) {
      final String msg = String.format("No AuthnRequest available when processing Response [%s]", logId(response));
      log.info("{}", msg);
      throw new ResponseValidationException(msg);
    }

    final IDPSSODescriptor descriptor =
        idpMetadata != null ? idpMetadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS) : null;
    if (descriptor == null) {
      throw new ResponseValidationException("Invalid/missing IdP metadata - cannot verify Response signature");
    }

    final ResponseValidationParametersBuilder b = ResponseValidationParametersBuilder.builder()
        .strictValidation(this.responseValidationSettings.isStrictValidation())
        .allowedClockSkew(this.responseValidationSettings.getAllowedClockSkew())
        .maxAgeReceivedMessage(this.responseValidationSettings.getMaxAgeResponse())
        .signatureRequired(Boolean.TRUE)
        .signatureValidationCriteriaSet(
            new CriteriaSet(new RoleDescriptorCriterion(descriptor), new UsageCriterion(UsageType.SIGNING)))
        .expectedIssuer(idpMetadata.getEntityID())
        .receiveInstant(input.getReceiveInstant())
        .receiveUrl(input.getReceiveURL())
        .authnRequest(authnRequest);

    if (validationContext != null) {
      b.addStaticParameters(validationContext.getStaticParameters());
      b.addDynamicParameters(validationContext.getDynamicParameters());
    }
    final ValidationContext context = b.build();

    final ValidationResult result = this.responseValidator.validate(response, context);
    if (validationContext != null) {
      validationContext.getDynamicParameters().putAll(context.getDynamicParameters());
    }
    switch (result) {
    case VALID:
      log.debug("Response was successfully validated [{}]", logId(response));
      break;
    case INDETERMINATE:
      log.info("Validation of Response was indeterminate - {} [{}]", context.getValidationFailureMessages(),
          logId(response));
      break;
    case INVALID:
      log.info("Validation of Response failed - {} [{}]", context.getValidationFailureMessages(), logId(response));
      throw new ResponseValidationException(String.join(" - ", context.getValidationFailureMessages()));
    }
  }

  /**
   * Validates the received relay state matches what we sent.
   *
   * @param response the response
   * @param relayState the received relay state
   * @param input the response processing input
   * @throws ResponseValidationException for validation errors
   */
  protected void validateRelayState(final Response response, final String relayState,
      final ResponseProcessingInput input)
      throws ResponseValidationException {

    final String requestRelayState = Optional.ofNullable(input.getRequestRelayState(response.getInResponseTo()))
        .map(String::trim).filter(r -> !r.isEmpty()).orElse(null);
    final String _relayState = Optional.ofNullable(relayState).map(String::trim).filter(r -> !r.isEmpty()).orElse(null);
    final boolean relayStateMatch = Objects.equal(requestRelayState, _relayState);

    if (!relayStateMatch) {
      final String msg =
          String.format("RelayState variable received with response (%s) does not match the sent one (%s)",
              relayState, requestRelayState);
      log.info("{} [{}]", msg, logId(response));
      throw new ResponseValidationException(msg);
    }
  }

  /**
   * Validates the assertion.
   *
   * @param assertion the assertion to validate
   * @param response the response that contained the assertion
   * @param input the processing input
   * @param idpMetadata the IdP metadat
   * @param validationContext optional validation context
   * @throws ResponseValidationException for validation errors
   */
  protected void validateAssertion(final Assertion assertion, final Response response,
      final ResponseProcessingInput input,
      final EntityDescriptor idpMetadata, final ValidationContext validationContext)
      throws ResponseValidationException {

    final IDPSSODescriptor descriptor =
        idpMetadata != null ? idpMetadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS) : null;
    if (descriptor == null) {
      throw new ResponseValidationException("Invalid/missing IdP metadata - cannot verify Assertion");
    }

    final AuthnRequest authnRequest = input.getAuthnRequest(response.getInResponseTo());
    if (authnRequest == null) {
      log.info("No AuthnRequest available for ID: {}", response.getInResponseTo());
    }
    final String entityID =
        Optional.ofNullable(authnRequest).map(AuthnRequest::getIssuer).map(Issuer::getValue).orElse(null);

    final AbstractAssertionValidationParametersBuilder<?> builder = this.getAssertionValidationParametersBuilder();

    if (validationContext != null) {
      builder.addStaticParameters(validationContext.getStaticParameters());
      builder.addDynamicParameters(validationContext.getDynamicParameters());
    }

    builder
        .strictValidation(this.responseValidationSettings.isStrictValidation())
        .allowedClockSkew(this.responseValidationSettings.getAllowedClockSkew())
        .maxAgeReceivedMessage(this.responseValidationSettings.getMaxAgeResponse())
        .signatureRequired(this.responseValidationSettings.isRequireSignedAssertions())
        .signatureValidationCriteriaSet(
            new CriteriaSet(new RoleDescriptorCriterion(descriptor), new UsageCriterion(UsageType.SIGNING)))
        .idpMetadata(idpMetadata)
        .receiveInstant(input.getReceiveInstant())
        .receiveUrl(input.getReceiveURL())
        .authnRequest(authnRequest)
        .expectedIssuer(idpMetadata.getEntityID())
        .responseIssueInstant(response.getIssueInstant().toEpochMilli())
        .validAudiences(entityID)
        .validRecipients(input.getReceiveURL(), entityID)
        .validAddresses(input.getClientIpAddress())
        .clientCertificate(input.getClientCertificate());

    // TODO: We should really make sure that we honor all passed in validation context settings
    if (validationContext == null
        || validationContext.getStaticParameters().get(CoreValidatorParameters.SP_METADATA) == null) {
      builder.spMetadata(this.getSpMetadata(entityID));
    }

    final ValidationContext context = builder.build();

    final ValidationResult result = this.assertionValidator.validate(assertion, context);
    if (validationContext != null) {
      validationContext.getDynamicParameters().putAll(context.getDynamicParameters());
    }
    switch (result) {
    case VALID:
      log.debug("Assertion with ID '{}' was successfully validated", assertion.getID());
      break;
    case INDETERMINATE:
      log.info("Validation of Assertion with ID '{}' was indeterminate - {}", assertion.getID(),
          context.getValidationFailureMessages());
      break;
    case INVALID:
      log.info("Validation of Assertion failed - {}", context.getValidationFailureMessages());
      throw new ResponseValidationException(String.join(" - ", context.getValidationFailureMessages()));
    }
  }

  /**
   * Gets the metadata for the given entityID and role (type).
   *
   * @param entityID the entity ID
   * @param role the role
   * @return the entity descriptor or null if no metadata is found
   */
  protected EntityDescriptor getMetadata(final String entityID, final QName role) {
    if (entityID == null) {
      return null;
    }
    try {
      final CriteriaSet criteria = new CriteriaSet();
      criteria.add(new EntityIdCriterion(entityID));
      final EntityDescriptor ed = this.metadataResolver.resolveSingle(criteria);
      if (role != null && ed != null) {
        if (ed.getRoleDescriptors(role).isEmpty()) {
          return null;
        }
      }
      return ed;
    }
    catch (final ResolverException e) {
      log.error("Failure when trying to obtain metadata for '{}'", entityID, e);
      return null;
    }
  }

  /**
   * Gets the SAML metadata for a given SP.
   *
   * @param entityID the SP entityID
   * @return the SP metadata or null if none is found
   */
  protected EntityDescriptor getSpMetadata(final String entityID) {
    if (entityID == null) {
      return null;
    }
    if (this.spMetadataCache != null && this.spMetadataCache.containsKey(entityID)) {
      return this.spMetadataCache.get(entityID);
    }
    final EntityDescriptor spMetadata = this.getMetadata(entityID, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    if (this.spMetadataCache == null) {
      this.spMetadataCache = new HashMap<>();
    }
    this.spMetadataCache.put(entityID, spMetadata);
    return spMetadata;
  }

  /**
   * Assigns the metadata resolver to use.
   *
   * @param metadataResolver the metadata resolver
   */
  public void setMetadataResolver(final MetadataResolver metadataResolver) {
    this.checkSetterPreconditions();
    this.metadataResolver = metadataResolver;
  }

  /**
   * Assigns the decrypter instance.
   *
   * @param decrypter the decrypter
   */
  public void setDecrypter(final SAMLObjectDecrypter decrypter) {
    this.checkSetterPreconditions();
    this.decrypter = decrypter;
  }

  /**
   * Assigns the message replay checker to use.
   *
   * @param messageReplayChecker message replay checker
   */
  public void setMessageReplayChecker(final MessageReplayChecker messageReplayChecker) {
    this.checkSetterPreconditions();
    this.messageReplayChecker = messageReplayChecker;
  }

  /**
   * Assigns the response validation settings.
   *
   * @param responseValidationSettings validation settings
   */
  public void setResponseValidationSettings(final ResponseValidationSettings responseValidationSettings) {
    this.checkSetterPreconditions();
    this.responseValidationSettings = responseValidationSettings;
  }

  /**
   * Assigns whether require assertions to be encrypted? The default is {@code true}.
   *
   * @param requireEncryptedAssertions boolean
   */
  public void setRequireEncryptedAssertions(boolean requireEncryptedAssertions) {
    this.checkSetterPreconditions();
    this.requireEncryptedAssertions = requireEncryptedAssertions;
  }

  /**
   * Helper for a setter method to check the standard preconditions.
   */
  protected final void checkSetterPreconditions() {
    if (this.isInitialized()) {
      throw new UnmodifiableComponentException(
          "Unidentified Component has already been initialized and cannot be changed");
    }
  }

  private static String logId(final Response response) {
    return String.format("response-id:'%s'", Optional.ofNullable(response.getID()).orElse("<empty>"));
  }

  private static String logId(final Response response, final Assertion assertion) {
    return String.format("response-id:'%s',assertion-id:'%s'",
        Optional.ofNullable(response.getID()).orElse("<empty>"),
        Optional.ofNullable(assertion.getID()).orElse("<empty>"));
  }

  /**
   * Returns the given SAML object in its "pretty print" XML string form.
   *
   * @param <T> the type of object to "print"
   * @param object the object to display as a string
   * @return the XML as a string
   */
  private static <T extends SAMLObject> String toString(final T object) {
    try {
      return SerializeSupport.prettyPrintXML(XMLObjectSupport.marshall(object));
    }
    catch (Exception e) {
      return "";
    }
  }

}
