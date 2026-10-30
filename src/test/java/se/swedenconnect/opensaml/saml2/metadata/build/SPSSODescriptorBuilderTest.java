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
package se.swedenconnect.opensaml.saml2.metadata.build;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

import java.util.List;

/**
 * Test cases for {@link SPSSODescriptorBuilder} and {@link AttributeConsumingServiceBuilder}.
 *
 * @author Martin Lindström
 */
class SPSSODescriptorBuilderTest extends OpenSAMLTestBase {

  @Test
  void testClearLists() {
    final AttributeConsumingService service = AttributeConsumingServiceBuilder.builder()
        .requestedAttributes((List<RequestedAttribute>) null)
        .index(0)
        .build();
    Assertions.assertTrue(service.getRequestedAttributes().isEmpty());

    final SPSSODescriptor descriptor = SPSSODescriptorBuilder.builder()
        .attributeConsumingServices((List<AttributeConsumingService>) null)
        .wantAssertionsSigned(true)
        .build();
    Assertions.assertTrue(descriptor.getAttributeConsumingServices().isEmpty());
  }

}
