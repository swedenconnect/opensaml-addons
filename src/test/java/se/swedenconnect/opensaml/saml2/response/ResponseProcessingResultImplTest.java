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
package se.swedenconnect.opensaml.saml2.response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Test cases for {@link ResponseProcessingResultImpl}.
 *
 * @author Martin Lindström
 */
class ResponseProcessingResultImplTest extends OpenSAMLTestBase {

  @Test
  void testNoAuthnContextClassRef() {
    final Assertion assertion = (Assertion) XMLObjectSupport.buildXMLObject(Assertion.DEFAULT_ELEMENT_NAME);
    final AuthnStatement statement =
        (AuthnStatement) XMLObjectSupport.buildXMLObject(AuthnStatement.DEFAULT_ELEMENT_NAME);
    statement.setAuthnContext((AuthnContext) XMLObjectSupport.buildXMLObject(AuthnContext.DEFAULT_ELEMENT_NAME));
    assertion.getAuthnStatements().add(statement);

    final ResponseProcessingResultImpl result = new ResponseProcessingResultImpl(
        (Response) XMLObjectSupport.buildXMLObject(Response.DEFAULT_ELEMENT_NAME), assertion);
    Assertions.assertNull(result.getAuthnContextClassUri());
  }

}
