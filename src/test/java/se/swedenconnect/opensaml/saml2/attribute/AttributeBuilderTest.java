/*
 * Copyright 2016-2024 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.attribute;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.schema.XSBase64Binary;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;

import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Tests for the {@link AttributeBuilder}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class AttributeBuilderTest extends OpenSAMLTestBase {

  public static final String ATTRIBUTE_NAME_SN = "urn:oid:2.5.4.4";

  public static final String ATTRIBUTE_FRIENDLY_NAME_SN = "sn";

  public static final String ATTRIBUTE_NAME_MAIL = "urn:oid:2.5.4.10";

  public static final String ATTRIBUTE_FRIENDLY_NAME_MAIL = "mail";

  @Test
  public void testCreateStringValueAttribute() {
    final Attribute attribute = AttributeBuilder.builder(ATTRIBUTE_NAME_SN)
        .friendlyName(ATTRIBUTE_FRIENDLY_NAME_SN)
        .nameFormat(Attribute.URI_REFERENCE)
        .value("Eriksson")
        .build();

    Assertions.assertEquals(ATTRIBUTE_NAME_SN, attribute.getName());
    Assertions.assertEquals(Attribute.URI_REFERENCE, attribute.getNameFormat());
    Assertions.assertEquals(ATTRIBUTE_FRIENDLY_NAME_SN, attribute.getFriendlyName());
    Assertions.assertTrue(attribute.getAttributeValues().size() == 1);
    Assertions.assertEquals("Eriksson", AttributeUtils.getAttributeStringValue(attribute));
  }

  @Test
  public void testCreateMultipleStringValuesAttribute() {
    final Attribute attribute = AttributeBuilder.builder(ATTRIBUTE_NAME_MAIL)
        .friendlyName(ATTRIBUTE_FRIENDLY_NAME_MAIL)
        .nameFormat(Attribute.URI_REFERENCE)
        .value("martin@litsec.se")
        .value("martin.lindstrom@litsec.se")
        .build();

    Assertions.assertEquals(ATTRIBUTE_NAME_MAIL, attribute.getName());
    Assertions.assertEquals(Attribute.URI_REFERENCE, attribute.getNameFormat());
    Assertions.assertEquals(ATTRIBUTE_FRIENDLY_NAME_MAIL, attribute.getFriendlyName());
    Assertions.assertEquals(Arrays.asList("martin@litsec.se", "martin.lindstrom@litsec.se"), AttributeUtils.getAttributeStringValues(attribute));
  }

  @Test
  public void testCreateNonStringAttribute() {

    // We pretend that there is a attribute that holds a boolean ...
    final XSBoolean value = AttributeBuilder.createValueObject(XSBoolean.class);
    value.setValue(XSBooleanValue.valueOf("true"));

    final Attribute attribute = AttributeBuilder.builder("http://eid.litsec.se/types/boolean")
        .friendlyName("booleanAttribute")
        .nameFormat(Attribute.URI_REFERENCE)
        .value(value)
        .build();

    Assertions.assertEquals("http://eid.litsec.se/types/boolean", attribute.getName());
    Assertions.assertEquals(Attribute.URI_REFERENCE, attribute.getNameFormat());
    Assertions.assertEquals("booleanAttribute", attribute.getFriendlyName());
    Assertions.assertTrue(AttributeUtils.getAttributeValues(attribute, XSBoolean.class).size() == 1);
    Assertions.assertEquals(AttributeUtils.getAttributeValue(attribute, XSBoolean.class).getValue().getValue(), Boolean.TRUE);
  }

  @Test
  public void testDefaultNameFormat() {
    final Attribute attribute = AttributeBuilder.builder(ATTRIBUTE_NAME_SN)
        .value("Eriksson")
        .build();

    Assertions.assertEquals(ATTRIBUTE_NAME_SN, attribute.getName());
    Assertions.assertEquals(Attribute.URI_REFERENCE, attribute.getNameFormat());
    Assertions.assertEquals("Eriksson", AttributeUtils.getAttributeStringValue(attribute));
  }

  @Test
  public void testCreateValueObject() {
    final XSBase64Binary value = AttributeBuilder.createValueObject(XSBase64Binary.class);
    Assertions.assertEquals(XSBase64Binary.TYPE_NAME, value.getSchemaType());
    Assertions.assertEquals(AttributeValue.DEFAULT_ELEMENT_NAME, value.getElementQName());
  }

  @Test
  public void testRequiredName() {

    try {
      new AttributeBuilder((String)null);
      Assertions.fail("Expected IllegalArgumentException");
    }
    catch (final IllegalArgumentException e) {
    }

    final AttributeBuilder builder = AttributeBuilder.builder(ATTRIBUTE_NAME_SN)
        .friendlyName(ATTRIBUTE_FRIENDLY_NAME_SN)
        .nameFormat(Attribute.URI_REFERENCE)
        .value("Eriksson");

    builder.name(null);

    try {
      builder.build();
      Assertions.fail("Expected RuntimeException");
    }
    catch (final RuntimeException e) {
    }
  }

}
