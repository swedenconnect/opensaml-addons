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
package se.swedenconnect.opensaml.saml2.assertion.validation;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.assertion.AssertionValidationException;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.assertion.ConditionValidator;
import org.opensaml.saml.saml2.assertion.SAML20AssertionValidator;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.opensaml.saml.saml2.assertion.StatementValidator;
import org.opensaml.saml.saml2.assertion.SubjectConfirmationValidator;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignaturePrevalidator;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.swedenconnect.opensaml.common.validation.AbstractObjectValidator;
import se.swedenconnect.opensaml.common.validation.AbstractSignableObjectValidator;
import se.swedenconnect.opensaml.common.validation.CoreValidatorParameters;
import se.swedenconnect.opensaml.common.validation.ValidationSupport;
import se.swedenconnect.opensaml.common.validation.ValidationSupport.ValidationResultException;
import se.swedenconnect.opensaml.saml2.metadata.HolderOfKeyMetadataSupport;

/**
 * A validator for {@code Assertion} objects.
 * 
 * <p>
 * Supports the following {@link ValidationContext} static parameters:
 * </p>
 * <ul>
 * <li>The static parameters defined for {@link AbstractSignableObjectValidator}.</li>
 * <li>{@link CoreValidatorParameters#SP_METADATA}: Required. The SP metadata.</li>
 * <li>{@link CoreValidatorParameters#IDP_METADATA}: Required. The IdP metadata.</li>
 * <li>{@link CoreValidatorParameters#STRICT_VALIDATION}: Optional. If not supplied, defaults to 'false'. Tells whether
 * strict validation should be performed.</li>
 * <li>{@link SAML2AssertionValidationParameters#CLOCK_SKEW}: Optional. Gives the number of milliseconds that is the
 * maximum allowed clock skew. If not given {@link SAML20AssertionValidator#DEFAULT_CLOCK_SKEW} is used.</li>
 * <li>{@link CoreValidatorParameters#MAX_AGE_MESSAGE}: Optional. Gives the maximum age (difference between issuance
 * time and the validation time). If not given, the {@link AbstractObjectValidator#DEFAULT_MAX_AGE_RECEIVED_MESSAGE} is
 * used.</li>
 * <li>{@link CoreValidatorParameters#RECEIVE_INSTANT}: Optional. Gives the timestamp (Instant) for when the response
 * message was received. If not given the current time is used.</li>
 * <li>{@link CoreValidatorParameters#AUTHN_REQUEST}: Required. Will be used in a number of validations when information
 * from the corresponding {@code AuthnRequest} is needed.</li>
 * <li>{@link CoreValidatorParameters#AUTHN_REQUEST_ID}: Required if the {@link CoreValidatorParameters#AUTHN_REQUEST}
 * is not assigned. Is used when validating the {@code InResponseTo} attribute of the response.</li>
 * <li>{@link CoreValidatorParameters#RECEIVE_URL}: Required. A String holding the URL on which we received the response
 * message. Is used when the {@code Destination} attribute is validated.</li>
 * <li>{@link CoreValidatorParameters#EXPECTED_ISSUER}: Optional. If set, is used when the issuer of the response is
 * validated. If not set, the issuer from the {@link CoreValidatorParameters#AUTHN_REQUEST} is used (if available).</li>
 * <li>{@link #RESPONSE_ISSUE_INSTANT}: Optional. If set, the IssueInstant of the Assertion being validated is compared
 * with the corresponding response issue instant.</li>
 * </ul>
 * 
 * <p>
 * Supports the following {@link ValidationContext} dynamic parameters:
 * </p>
 * <ul>
 * <li>{@link SAML2AssertionValidationParameters#CONFIRMED_SUBJECT_CONFIRMATION}: Optional. Will be present after
 * validation if subject confirmation was successfully performed.</li>
 * <li>{@link #HOK_PROFILE_ACTIVE}: Is set to indicate whether the holder-of-key WebSSO profile is active.</li>
 * </ul>
 * 
 * <p>
 * <b>Note:</b> Also check the validation context parameters defined by the {@code SubjectConfirmationValidator} and
 * {@code ConditionValidator} instances that are installed.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class AssertionValidator extends AbstractSignableObjectValidator<Assertion> {

  /**
   * Carries a {@link Instant} holding the issue instant of the Response that contained the assertion being validated.
   */
  public static final String RESPONSE_ISSUE_INSTANT = CoreValidatorParameters.STD_PREFIX + ".ResponseIssueInstant";

  /**
   * Tells whether the AuthnRequest corresponding to this assertion was sent to the IdP's holder of key-endpoints, i.e.,
   * whether the Holder-of-key profile is in use. Carries a {@link Boolean}.
   */
  public static final String HOK_PROFILE_ACTIVE = CoreValidatorParameters.STD_PREFIX + ".HokProfileActive";

  /** Class logger. */
  private final Logger log = LoggerFactory.getLogger(AssertionValidator.class);

  /** Registered {@link SubjectConfirmation} validators. */
  protected Map<String, SubjectConfirmationValidator> subjectConfirmationValidators;

  /** Registered {@link Condition} validators. */
  protected Map<QName, ConditionValidator> conditionValidators;

  /** Registered {@link Statement} validators. */
  private Map<QName, StatementValidator> statementValidators;

  /**
   * Constructor.
   * 
   * @param trustEngine
   *          the trust used to validate the object's signature
   * @param signaturePrevalidator
   *          the signature pre-validator used to pre-validate the object's signature
   * @param confirmationValidators
   *          validators used to validate {@link SubjectConfirmation} methods within the assertion
   * @param conditionValidators
   *          validators used to validate the {@link Condition} elements within the assertion
   * @param statementValidators
   *          validators used to validate {@link Statement}s within the assertion
   */
  public AssertionValidator(final SignatureTrustEngine trustEngine,
      final SignaturePrevalidator signaturePrevalidator,
      final Collection<SubjectConfirmationValidator> confirmationValidators,
      final Collection<ConditionValidator> conditionValidators,
      final Collection<StatementValidator> statementValidators) {
    super(trustEngine, signaturePrevalidator);

    this.subjectConfirmationValidators = new HashMap<>();
    if (confirmationValidators != null) {
      for (SubjectConfirmationValidator validator : confirmationValidators) {
        if (validator != null) {
          this.subjectConfirmationValidators.put(validator.getServicedMethod(), validator);
        }
      }
    }

    this.conditionValidators = new HashMap<>();
    if (conditionValidators != null) {
      for (ConditionValidator validator : conditionValidators) {
        if (validator != null) {
          this.conditionValidators.put(validator.getServicedCondition(), validator);
        }
      }
    }

    this.statementValidators = new HashMap<>();
    if (statementValidators != null) {
      for (StatementValidator validator : statementValidators) {
        if (validator != null) {
          this.statementValidators.put(validator.getServicedStatement(), validator);
        }
      }
    }
  }

  /**
   * Validates the assertion.
   */
  @Override
  public ValidationResult validate(final Assertion assertion, final ValidationContext context) {
    try {
      ValidationSupport.check(this.validateID(assertion, context));
      ValidationSupport.check(this.validateVersion(assertion, context));
      ValidationSupport.check(this.validateIssueInstant(assertion, context));
      ValidationSupport.check(this.validateIssuer(assertion, context));
      ValidationSupport.check(this.validateHolderOfKeyRequirement(assertion, context));
      ValidationSupport.check(this.validateSignature(assertion, context));
      ValidationSupport.check(this.validateSubject(assertion, context));
      ValidationSupport.check(this.validateConditions(assertion, context));
      ValidationSupport.check(this.validateStatements(assertion, context));
    }
    catch (ValidationResultException e) {
      return e.getResult();
    }
    return ValidationResult.VALID;
  }

  /**
   * Validates that the {@code Assertion} object has an ID attribute.
   * 
   * @param assertion
   *          the assertion
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateID(final Assertion assertion, final ValidationContext context) {
    if (assertion.getID() == null || assertion.getID().isBlank()) {
      context.setValidationFailureMessage("Missing ID attribute in Assertion");
      return ValidationResult.INVALID;
    }
    return ValidationResult.VALID;
  }

  /**
   * Validates that the {@code Response} object has a valid Version attribute.
   * 
   * @param assertion
   *          the assertion
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateVersion(final Assertion assertion, final ValidationContext context) {
    if (assertion.getVersion() == null || !assertion.getVersion().toString().equals(SAMLVersion.VERSION_20.toString())) {
      context.setValidationFailureMessage("Invalid SAML version in Assertion");
      return ValidationResult.INVALID;
    }
    return ValidationResult.VALID;
  }

  /**
   * Validates that the {@code Assertion} object has a IssueInstant attribute and checks that its value is OK. If the
   * response that contained the assertion was previously validated the static context parameter
   * {@link #RESPONSE_ISSUE_INSTANT} should be passed. If so, the method checks that the assertion issue instant is not
   * after the response issue instant. Otherwise the method checks that the IssueInstant is not too old given the
   * {@link CoreValidatorParameters#MAX_AGE_MESSAGE} and {@link CoreValidatorParameters#RECEIVE_INSTANT} context
   * parameters.
   * 
   * @param assertion
   *          the response
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateIssueInstant(final Assertion assertion, final ValidationContext context) {
    if (assertion.getIssueInstant() == null) {
      context.setValidationFailureMessage("Missing IssueInstant attribute in Assertion");
      return ValidationResult.INVALID;
    }

    // Is the response issue instance specified? If so, we only check that the assertion issue instant
    // is before the response issue instant. In these cases we assume that the response issue instant
    // has been verified.
    //
    Instant responseIssueInstant = this.getResponseIssueInstant(context);
    if (responseIssueInstant != null) {
      if (assertion.getIssueInstant().isAfter(responseIssueInstant)) {
        final String msg = String.format("Invalid Assertion - Its issue-instant (%s) is after the response message issue-instant (%s)",
          assertion.getIssueInstant(), responseIssueInstant);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }
    }
    else {
      // Otherwise, we have to make more checks.

      final Instant receiveInstant = getReceiveInstant(context);
      final long receiveInstantMillis = receiveInstant.toEpochMilli();
      final Instant issueInstant = assertion.getIssueInstant();
      final long issueInstantMillis = issueInstant.toEpochMilli();

      final Duration maxAgeResponse = getMaxAgeReceivedMessage(context);
      final Duration allowedClockSkew = getAllowedClockSkew(context);

      // Too old?
      //
      if ((receiveInstantMillis - issueInstantMillis) > (maxAgeResponse.toMillis() + allowedClockSkew.toMillis())) {
        final String msg = String.format("Received Assertion is too old - issue-instant: %s - receive-time: %s",
          assertion.getIssueInstant(), receiveInstant);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }

      // Not yet valid? -> Clock skew is unacceptable.
      //
      if ((issueInstantMillis - receiveInstantMillis) > allowedClockSkew.toMillis()) {
        final String msg = String.format("Issue-instant of Assertion (%s) is newer than receive time (%s) - Non accepted clock skew",
          assertion.getIssueInstant(), receiveInstant);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }
    }

    return ValidationResult.VALID;
  }

  /**
   * Gets the {@link #RESPONSE_ISSUE_INSTANT} setting.
   * 
   * @param context
   *          the context
   * @return the response issue instant, or null if it is not set
   */
  protected Instant getResponseIssueInstant(final ValidationContext context) {
    Object object = context.getStaticParameters().get(RESPONSE_ISSUE_INSTANT);
    if (object != null) {
      if (Instant.class.isInstance(object)) {
        return Instant.class.cast(object);
      }
      else if (Long.class.isInstance(object)) {
        return Instant.ofEpochMilli(Long.class.cast(object));
      }
    }
    return null;
  }

  /**
   * Ensures that the {@code Issuer} element is present and matches the expected issuer (if set in the context under the
   * {@link CoreValidatorParameters#EXPECTED_ISSUER} key).
   * 
   * @param assertion
   *          the assertion
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateIssuer(final Assertion assertion, final ValidationContext context) {
    if (assertion.getIssuer() == null || assertion.getIssuer().getValue() == null) {
      context.setValidationFailureMessage("Missing Issuer element in Assertion");
      return ValidationResult.INVALID;
    }
    String expectedIssuer = (String) context.getStaticParameters().get(CoreValidatorParameters.EXPECTED_ISSUER);
    if (expectedIssuer != null) {
      if (!assertion.getIssuer().getValue().equals(expectedIssuer)) {
        final String msg = String.format("Issuer of Assertion (%s) did not match expected issuer (%s)",
          assertion.getIssuer().getValue(), expectedIssuer);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }
    }
    else {
      log.warn("EXPECTED_ISSUER key not set - will not check issuer of Assertion");
    }

    return ValidationResult.VALID;
  }

  /**
   * Performs initial validation concerning the Holder-of-key WebSSO Profile. The method checks that if the request was
   * sent to an IdP HoK-endpoint, we verify that the SP received the response on an endpoint dedicated for HoK.
   * <p>
   * The method also sets the dynamic validation parameter {@link #HOK_PROFILE_ACTIVE}.
   * </p>
   * 
   * @param assertion
   *          the assertion
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateHolderOfKeyRequirement(final Assertion assertion, final ValidationContext context) {

    Boolean hokActive = Boolean.FALSE;

    try {
      final String receiveUrl = (String) context.getStaticParameters().get(CoreValidatorParameters.RECEIVE_URL);
      if (receiveUrl == null) {
        final String msg = String.format("Could not determine if Holder-of-key profile is active. '%s' parameter is missing",
          CoreValidatorParameters.RECEIVE_URL);
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INDETERMINATE;
      }

      final AuthnRequest authnRequest = (AuthnRequest) context.getStaticParameters().get(CoreValidatorParameters.AUTHN_REQUEST);
      if (authnRequest == null || authnRequest.getDestination() == null) {
        final String msg = String.format("Could not determine if Holder-of-key profile is active."
            + "'%s' parameter is missing or no Destination was set in AuthnRequest",
          CoreValidatorParameters.AUTHN_REQUEST);
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INDETERMINATE;
      }
      final String destination = authnRequest.getDestination();

      final EntityDescriptor idpMetadata =
          (EntityDescriptor) context.getStaticParameters().get(CoreValidatorParameters.IDP_METADATA);
      if (idpMetadata == null) {
        final String msg = String.format("Could not determine if Holder-of-key profile is active. '%s' parameter is missing",
          CoreValidatorParameters.IDP_METADATA);
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INDETERMINATE;
      }

      hokActive = idpMetadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getSingleSignOnServices().stream()
        .filter(s -> s.getLocation() != null && s.getLocation().equals(destination))
        .filter(HolderOfKeyMetadataSupport::isHoKSingleSignOnService)
        .findFirst()
        .isPresent();

      if (!hokActive) {
        // HoK is not active.
        return ValidationResult.VALID;
      }

      final EntityDescriptor spMetadata =
          (EntityDescriptor) context.getStaticParameters().get(CoreValidatorParameters.SP_METADATA);
      if (spMetadata == null) {
        final String msg = String.format("Could not determine if Holder-of-key profile is active. '%s' parameter is missing",
          CoreValidatorParameters.SP_METADATA);
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INDETERMINATE;
      }

      if (spMetadata.getSPSSODescriptor(SAMLConstants.SAML20P_NS).getAssertionConsumerServices().stream()
        .filter(s -> receiveUrl.equals(s.getLocation()))
        .filter(HolderOfKeyMetadataSupport::isHoKAssertionConsumerService)
        .findFirst().isEmpty()) {
        
        final String msg = "Expected response to be delivered to a Holder-of-key AssertionConsumerService endpoint";
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }

      return ValidationResult.VALID;
    }
    finally {
      context.getDynamicParameters().put(HOK_PROFILE_ACTIVE, hokActive);
    }
  }

  /**
   * Validates the {@code Subject} element of the assertion. The default implementation returns
   * {@link ValidationResult#VALID} if there is no {@code Subject} element since it is optional according to the SAML
   * 2.0 Core specifications.
   * 
   * @param assertion
   *          the assertion
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateSubject(final Assertion assertion, final ValidationContext context) {
    if (assertion.getSubject() == null) {

      // Assertions containing AuthnStatements must contain a Subject.
      //
      if (assertion.getAuthnStatements() != null && !assertion.getAuthnStatements().isEmpty()) {
        context.setValidationFailureMessage("Assertion contains AuthnStatement but no Subject - invalid");
        return ValidationResult.INVALID;
      }

      log.debug("Assertion does not contain a Subject element - allowed by default assertion validator");
      return ValidationResult.VALID;
    }
    final Subject subject = assertion.getSubject();
    List<SubjectConfirmation> confirmations = subject.getSubjectConfirmations();
    if (confirmations == null || confirmations.isEmpty()) {
      final boolean hokProfileActive = Optional.ofNullable(context.getDynamicParameters().get(HOK_PROFILE_ACTIVE))
        .map(Boolean.class::cast).orElse(Boolean.FALSE);
      if (hokProfileActive) {
        String msg = String.format("No subject confirmation for method '%s' found for assertion with ID '%s'",
          SubjectConfirmation.METHOD_HOLDER_OF_KEY, assertion.getID());
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }
      else {
        log.debug("Assertion contains no SubjectConfirmations, default assertion validator skips subject confirmation");
        return ValidationResult.VALID;
      }
    }

    return this.validateSubjectConfirmations(assertion, confirmations, context);
  }

  /**
   * Validates the subject confirmations and for the one that is confirmed, it is saved in the validation context under
   * the {@link SAML2AssertionValidationParameters#CONFIRMED_SUBJECT_CONFIRMATION} key.
   * 
   * @param assertion
   *          the assertion
   * @param subjectConfirmations
   *          the subject confirmations
   * @param context
   *          the validation context
   * @return a validation result
   */
  protected ValidationResult validateSubjectConfirmations(final Assertion assertion, final List<SubjectConfirmation> subjectConfirmations,
      final ValidationContext context) {

    final boolean hokProfileActive = Optional.ofNullable(context.getDynamicParameters().get(HOK_PROFILE_ACTIVE))
      .map(Boolean.class::cast).orElse(Boolean.FALSE);

    for (final SubjectConfirmation confirmation : subjectConfirmations) {
      if (hokProfileActive && !SubjectConfirmation.METHOD_HOLDER_OF_KEY.equals(confirmation.getMethod())) {
        log.info("Holder-of-key profile is active - Ignoring SubjectConfirmation with method '{}'", confirmation.getMethod());
        continue;
      }
      final SubjectConfirmationValidator validator = subjectConfirmationValidators.get(confirmation.getMethod());
      if (validator != null) {
        try {
          final ValidationResult r = validator.validate(confirmation, assertion, context);
          if (r == ValidationResult.VALID) {
            context.getDynamicParameters().put(
              SAML2AssertionValidationParameters.CONFIRMED_SUBJECT_CONFIRMATION, confirmation);
            return ValidationResult.VALID;
          }
          else {
            log.info("Validation of SubjectConfirmation with method '{}' failed - {}", confirmation.getMethod(),
              context.getValidationFailureMessage());
            if (hokProfileActive) {
              return ValidationResult.INVALID;
            }
          }
        }
        catch (AssertionValidationException e) {
          log.warn("Error while executing subject confirmation validation " + validator.getClass().getName(), e);
        }
      }
      else {
        log.info("No validator installed for SubjectConfirmation method '{}'", confirmation.getMethod());
      }
    }

    String msg = String.format("No subject confirmation methods were met for assertion with ID '%s'", assertion.getID());
    log.debug(msg);
    context.setValidationFailureMessage(msg);
    return ValidationResult.INVALID;
  }

  /**
   * Validates the {@code Conditions} elements of the assertion.
   * 
   * @param assertion
   *          the assertion
   * @param context
   *          the validation context
   * @return the validation result
   */
  protected ValidationResult validateConditions(final Assertion assertion, final ValidationContext context) {

    final Conditions conditions = assertion.getConditions();
    if (conditions == null) {
      log.debug("Assertion contained no Conditions element - allowed by default assertion validator");
      return ValidationResult.VALID;
    }

    ValidationResult timeboundsResult = this.validateConditionsTimeBounds(assertion, context);
    if (timeboundsResult != ValidationResult.VALID) {
      return timeboundsResult;
    }

    for (Condition condition : conditions.getConditions()) {
      ConditionValidator validator = conditionValidators.get(condition.getElementQName());
      if (validator == null && condition.getSchemaType() != null) {
        validator = conditionValidators.get(condition.getSchemaType());
      }

      if (validator == null) {
        final String msg = String.format("Unknown Condition '%s' of type '%s' in assertion '%s'",
          condition.getElementQName(), condition.getSchemaType(), assertion.getID());
        log.warn(msg);
        if (isStrictValidation(context)) {
          context.setValidationFailureMessage(msg);
          return ValidationResult.INDETERMINATE;
        }
        else {
          continue;
        }
      }

      ValidationResult r;
      try {
        r = validator.validate(condition, assertion, context);
      }
      catch (AssertionValidationException e) {
        log.error("Failed Conditions validation - {}", e.getMessage());
        log.debug("", e);
        context.setValidationFailureMessage(e.getMessage());
        r = ValidationResult.INVALID;
      }

      if (r != ValidationResult.VALID) {
        String msg = String.format("Condition '%s' of type '%s' in assertion '%s' was not valid - %s.",
          condition.getElementQName(), condition.getSchemaType(), assertion.getID(), context.getValidationFailureMessage());
        if (context.getValidationFailureMessage() != null) {
          msg = msg + ": " + context.getValidationFailureMessage();
        }
        log.debug(msg);
        context.setValidationFailureMessage(msg);
        return ValidationResult.INVALID;
      }
    }

    return ValidationResult.VALID;
  }

  /**
   * Validates the NotBefore and NotOnOrAfter Conditions constraints on the assertion.
   * 
   * @param assertion
   *          the assertion whose conditions will be validated
   * @param context
   *          current validation context
   * @return the result of the validation evaluation
   */
  protected ValidationResult validateConditionsTimeBounds(final Assertion assertion, final ValidationContext context) {

    Conditions conditions = assertion.getConditions();
    if (conditions == null) {
      return ValidationResult.VALID;
    }

    final Duration clockSkew = getAllowedClockSkew(context);
    final Instant receiveInstant = getReceiveInstant(context);

    final Instant notBefore = conditions.getNotBefore();
    log.debug("Evaluating Conditions NotBefore '{}' against 'skewed now' time '{}'", notBefore, receiveInstant.plus(clockSkew));
    if (notBefore != null && notBefore.isAfter(receiveInstant.plus(clockSkew))) {
      context.setValidationFailureMessage(String.format(
        "Assertion '%s' with NotBefore condition of '%s' is not yet valid", assertion.getID(), notBefore));
      return ValidationResult.INVALID;
    }

    final Instant notOnOrAfter = conditions.getNotOnOrAfter();
    log.debug("Evaluating Conditions NotOnOrAfter '{}' against 'skewed now' time '{}'", notOnOrAfter, receiveInstant.minus(clockSkew));
    if (notOnOrAfter != null && notOnOrAfter.isBefore(receiveInstant.minus(clockSkew))) {
      context.setValidationFailureMessage(String.format(
        "Assertion '%s' with NotOnOrAfter condition of '%s' is no longer valid", assertion.getID(), notOnOrAfter));
      return ValidationResult.INVALID;
    }

    return ValidationResult.VALID;
  }

  /**
   * Validates the statements of the assertion using the registered {@link StatementValidator} instance.
   * 
   * @param assertion
   *          the assertion to validate
   * @param context
   *          the validation context
   * @return validation result
   */
  protected ValidationResult validateStatements(final Assertion assertion, final ValidationContext context) {

    List<Statement> statements = assertion.getStatements();
    if (statements == null || statements.isEmpty()) {
      return ValidationResult.VALID;
    }

    StatementValidator validator;
    for (Statement statement : statements) {
      validator = statementValidators.get(statement.getElementQName());
      if (validator == null && statement.getSchemaType() != null) {
        validator = statementValidators.get(statement.getSchemaType());
      }

      if (validator != null) {
        ValidationResult result;
        try {
          result = validator.validate(statement, assertion, context);
        }
        catch (AssertionValidationException e) {
          log.error("Failed Statement validation - {}", e.getMessage());
          log.debug("", e);
          context.setValidationFailureMessage(e.getMessage());
          result = ValidationResult.INVALID;
        }
        if (result != ValidationResult.VALID) {
          return result;
        }
      }
    }

    return ValidationResult.VALID;
  }

  /**
   * Returns the Assertion issuer.
   */
  @Override
  protected String getIssuer(final Assertion signableObject) {
    return signableObject.getIssuer() != null ? signableObject.getIssuer().getValue() : null;
  }

  /**
   * Returns the Assertion ID.
   */
  @Override
  protected String getID(final Assertion signableObject) {
    return signableObject.getID();
  }

  /** {@inheritDoc} */
  @Override
  protected String getObjectName() {
    return "Assertion";
  }

}
