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
package se.swedenconnect.opensaml.common.validation;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.assertion.ValidationContext;
import org.opensaml.saml.common.assertion.ValidationResult;
import org.opensaml.saml.saml2.assertion.SAML2AssertionValidationParameters;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.CollectionCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.signature.support.SignaturePrevalidator;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.core.io.ClassPathResource;

import net.shibboleth.shared.resolver.CriteriaSet;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Test cases for {@code AbstractSignableObjectValidatorTest}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class AbstractSignableObjectValidatorTest extends OpenSAMLTestBase {

  public static final String ISSUER_ENTITYID = "https://idp.svelegtest.se/idp";

  private SignatureTrustEngine signatureTrustEngine;

  private SignaturePrevalidator signatureProfileValidator = new SAMLSignatureProfileValidator();

  public AbstractSignableObjectValidatorTest() throws Exception {
    X509Certificate cert = decodeCertificate(new ClassPathResource("/signed/signer.crt").getInputStream());
    BasicX509Credential cred = new BasicX509Credential(cert);
    cred.setEntityId(ISSUER_ENTITYID);

    CollectionCredentialResolver credentialResolver = new CollectionCredentialResolver(Arrays.asList(cred));

    this.signatureTrustEngine = new ExplicitKeySignatureTrustEngine(credentialResolver,
      DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
  }

  @Test
  public void testResponseSignatureValidation() throws Exception {
    TestResponseValidator validator = new TestResponseValidator(this.signatureTrustEngine, this.signatureProfileValidator);

    Map<String, Object> staticPars = new HashMap<String, Object>();
    staticPars.put(SAML2AssertionValidationParameters.SIGNATURE_REQUIRED, Boolean.TRUE);
    staticPars.put(SAML2AssertionValidationParameters.SIGNATURE_VALIDATION_CRITERIA_SET,
      new CriteriaSet(new EntityIdCriterion(ISSUER_ENTITYID), new UsageCriterion(UsageType.SIGNING)));

    ValidationContext context = new ValidationContext(staticPars);

    Response response = unmarshall(new ClassPathResource("signed/signed-response.xml").getInputStream(), Response.class);

    ValidationResult result = validator.validate(response, context);
    Assertions.assertEquals(ValidationResult.VALID, result);
  }

  @Test
  public void testResponseSignatureValidationFailureBadDigest() throws Exception {
    TestResponseValidator validator = new TestResponseValidator(this.signatureTrustEngine, this.signatureProfileValidator);

    Map<String, Object> staticPars = new HashMap<String, Object>();
    staticPars.put(SAML2AssertionValidationParameters.SIGNATURE_REQUIRED, Boolean.TRUE);
    staticPars.put(SAML2AssertionValidationParameters.SIGNATURE_VALIDATION_CRITERIA_SET,
      new CriteriaSet(new EntityIdCriterion(ISSUER_ENTITYID), new UsageCriterion(UsageType.SIGNING)));

    ValidationContext context = new ValidationContext(staticPars);

    Response response = unmarshall(new ClassPathResource("signed/signed-baddigest-response.xml").getInputStream(), Response.class);

    ValidationResult result = validator.validate(response, context);
    Assertions.assertEquals(ValidationResult.INVALID, result);
  }

  private static class TestResponseValidator extends AbstractSignableObjectValidator<Response> {

    public TestResponseValidator(SignatureTrustEngine trustEngine, SignaturePrevalidator signaturePrevalidator) {
      super(trustEngine, signaturePrevalidator);
    }

    @Override
    public ValidationResult validate(Response object, ValidationContext context) {
      return this.validateSignature(object, context);
    }

    @Override
    protected String getIssuer(Response signableObject) {
      return signableObject.getIssuer().getValue();
    }

    @Override
    protected String getID(Response signableObject) {
      return signableObject.getID();
    }

    @Override
    protected String getObjectName() {
      return "Response";
    }

  }

}
