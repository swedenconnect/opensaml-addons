/*
 * Copyright 2016-2026 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.response.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.common.validation.CoreValidatorParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test cases for {@link ResponseValidator}.
 *
 * @author Martin Lindström
 */
class ResponseValidatorTest extends OpenSAMLTestBase {

  @Test
  void testIndeterminateDoesNotStopValidation() {
    final StepValidator validator = new StepValidator(ValidationResult.INDETERMINATE, ValidationResult.VALID);
    final ValidationResult result = validator.validate(createResponse(), new ValidationContext(Collections.emptyMap()));
    Assertions.assertEquals(ValidationResult.INDETERMINATE, result);
    Assertions.assertTrue(validator.steps.contains("signature"), "Signature validation was skipped");
    Assertions.assertTrue(validator.steps.contains("extensions"), "Extensions validation was skipped");
  }

  @Test
  void testInvalidStopsValidation() {
    final StepValidator validator = new StepValidator(ValidationResult.INDETERMINATE, ValidationResult.INVALID);
    final ValidationResult result = validator.validate(createResponse(), new ValidationContext(Collections.emptyMap()));
    Assertions.assertEquals(ValidationResult.INVALID, result);
    Assertions.assertFalse(validator.steps.contains("assertions"));
  }

  @Test
  void testIssueInstant() {
    final ResponseValidator validator = new ResponseValidator(null, new SAMLSignatureProfileValidator());
    final Instant receiveInstant = Instant.now();
    final ValidationContext context = new ValidationContext(Map.of(
        CoreValidatorParameters.RECEIVE_INSTANT, receiveInstant,
        CoreValidatorParameters.MAX_AGE_MESSAGE, Duration.ofMinutes(3),
        SAML2AssertionValidationParameters.CLOCK_SKEW, Duration.ofSeconds(30)));

    final Response response = createResponse();

    // Issued 60 seconds before it was received - OK
    response.setIssueInstant(receiveInstant.minusSeconds(60));
    Assertions.assertEquals(ValidationResult.VALID, validator.validateIssueInstant(response, context));

    // Issued 10 seconds after it was received - within clock skew
    response.setIssueInstant(receiveInstant.plusSeconds(10));
    Assertions.assertEquals(ValidationResult.VALID, validator.validateIssueInstant(response, context));

    // Too old
    response.setIssueInstant(receiveInstant.minusSeconds(240));
    Assertions.assertEquals(ValidationResult.INVALID, validator.validateIssueInstant(response, context));

    // Issued in the future
    response.setIssueInstant(receiveInstant.plusSeconds(120));
    Assertions.assertEquals(ValidationResult.INVALID, validator.validateIssueInstant(response, context));
  }

  private static Response createResponse() {
    return (Response) XMLObjectSupport.buildXMLObject(Response.DEFAULT_ELEMENT_NAME);
  }

  /**
   * Validator that records the steps that are run. The Destination step gives one result and the signature step
   * another, all other steps are VALID.
   */
  private static class StepValidator extends ResponseValidator {

    private final List<String> steps = new ArrayList<>();
    private final ValidationResult destinationResult;
    private final ValidationResult signatureResult;

    StepValidator(final ValidationResult destinationResult, final ValidationResult signatureResult) {
      super(null, new SAMLSignatureProfileValidator());
      this.destinationResult = destinationResult;
      this.signatureResult = signatureResult;
    }

    @Override
    protected ValidationResult validateID(final Response response, final ValidationContext context) {
      return this.step("id", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateVersion(final Response response, final ValidationContext context) {
      return this.step("version", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateStatus(final Response response, final ValidationContext context) {
      return this.step("status", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateIssueInstant(final Response response, final ValidationContext context) {
      return this.step("issue-instant", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateInResponseTo(final Response response, final ValidationContext context) {
      return this.step("in-response-to", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateDestination(final Response response, final ValidationContext context) {
      return this.step("destination", this.destinationResult);
    }

    @Override
    protected ValidationResult validateConsent(final Response response, final ValidationContext context) {
      return this.step("consent", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateIssuer(final Response response, final ValidationContext context) {
      return this.step("issuer", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateSignature(final Response token, final ValidationContext context) {
      return this.step("signature", this.signatureResult);
    }

    @Override
    protected ValidationResult validateAssertions(final Response response, final ValidationContext context) {
      return this.step("assertions", ValidationResult.VALID);
    }

    @Override
    protected ValidationResult validateExtensions(final Response response, final ValidationContext context) {
      return this.step("extensions", ValidationResult.VALID);
    }

    private ValidationResult step(final String name, final ValidationResult result) {
      this.steps.add(name);
      return result;
    }
  }

}
