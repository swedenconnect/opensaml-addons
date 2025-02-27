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
package se.swedenconnect.opensaml.common.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.StatusCode;
import org.w3c.dom.Element;

import net.shibboleth.shared.xml.SerializeSupport;
import net.shibboleth.shared.xml.XMLParserException;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Test cases for {@link AbstractSAMLObjectBuilder}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class AbstractSAMLObjectBuilderTest extends OpenSAMLTestBase {

  @Test
  public void testBuild() throws Exception {
    final StatusCodeBuilder builder = new StatusCodeBuilder();
    final StatusCode status = builder
        .value(StatusCode.REQUESTER)
        .statusCode(
          (new StatusCodeBuilder())
            .value(StatusCode.AUTHN_FAILED)
            .statusCode(StatusCode.NO_SUPPORTED_IDP)
            .build())
        .build();

    Assertions.assertEquals(StatusCode.REQUESTER, status.getValue());
    Assertions.assertEquals(StatusCode.AUTHN_FAILED, status.getStatusCode().getValue());
    Assertions.assertEquals(StatusCode.NO_SUPPORTED_IDP, status.getStatusCode().getStatusCode().getValue());
    Assertions.assertNull(status.getStatusCode().getStatusCode().getStatusCode());
  }

  @Test
  public void testBuildFromTemplate() throws Exception {
    final StatusCode template = (StatusCode) XMLObjectSupport.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
    template.setValue(StatusCode.REQUESTER);
    final StatusCode subCode = (StatusCode) XMLObjectSupport.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
    subCode.setValue(StatusCode.AUTHN_FAILED);
    template.setStatusCode(subCode);

    final StatusCodeBuilder builder = new StatusCodeBuilder(template);
    final StatusCode status = builder.value(StatusCode.RESPONDER).build();

    Assertions.assertEquals(StatusCode.RESPONDER, status.getValue());
    Assertions.assertEquals(StatusCode.AUTHN_FAILED, status.getStatusCode().getValue());

    // Template should be untouched ...
    Assertions.assertEquals(StatusCode.REQUESTER, template.getValue());
    Assertions.assertEquals(StatusCode.AUTHN_FAILED, template.getStatusCode().getValue());
  }

  @Test
  public void testBuildFromResource() throws Exception {
    final StatusCode template = (StatusCode) XMLObjectSupport.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
    template.setValue(StatusCode.REQUESTER);
    final StatusCode subCode = (StatusCode) XMLObjectSupport.buildXMLObject(StatusCode.DEFAULT_ELEMENT_NAME);
    subCode.setValue(StatusCode.AUTHN_FAILED);
    template.setStatusCode(subCode);

    // Go to XML
    final Element element = XMLObjectSupport.marshall(template);
    final String xml = SerializeSupport.prettyPrintXML(element);

    final StatusCodeBuilder builder = new StatusCodeBuilder(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    final StatusCode status = builder.value(StatusCode.RESPONDER).build();

    Assertions.assertEquals(StatusCode.RESPONDER, status.getValue());
    Assertions.assertEquals(StatusCode.AUTHN_FAILED, status.getStatusCode().getValue());
  }


  // Dummy class used for testing abstract object builder.
  //
  private static class StatusCodeBuilder extends AbstractSAMLObjectBuilder<StatusCode> {

    public StatusCodeBuilder() {
      super();
    }

    public StatusCodeBuilder(final InputStream resource) throws XMLParserException, UnmarshallingException {
      super(resource);
    }

    public StatusCodeBuilder(final StatusCode template) throws MarshallingException, UnmarshallingException {
      super(template);
    }

    StatusCodeBuilder value(final String value) {
      this.object().setValue(value);
      return this;
    }

    StatusCodeBuilder statusCode(final StatusCode statusCode) {
      this.object().setStatusCode(statusCode);
      return this;
    }

    StatusCodeBuilder statusCode(final String statusCode) {
      this.object().setStatusCode((new StatusCodeBuilder()).value(statusCode).build());
      return this;
    }

    @Override
    protected Class<StatusCode> getObjectType() {
      return StatusCode.class;
    }

  }

}
