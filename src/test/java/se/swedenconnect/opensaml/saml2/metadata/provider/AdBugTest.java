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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testing that our workaround to only save SP:s and IDP:s avoids the bug somewhere deep down in OpenSAML when unknown
 * role descriptors are present.
 *
 * @author Martin Lindström
 */
class AdBugTest extends OpenSAMLTestBase {

  @Test
  void testKeepOnlySsoDescriptors() throws Exception {
    final Resource resource = new ClassPathResource("federationmetadata.xml");
    final XMLObject object = XMLObjectSupport.unmarshallFromInputStream(
        XMLObjectProviderRegistrySupport.getParserPool(), resource.getInputStream());
    final StaticMetadataProvider p = new StaticMetadataProvider((EntityDescriptor) object);
    p.initialize();

    assertEquals(1, p.getServiceProviders().size());
    assertEquals(1, p.getIdentityProviders().size());

    final EntityDescriptor sp = p.getEntityDescriptor("http://fstest.example.se/adfs/services/trust",
        SPSSODescriptor.DEFAULT_ELEMENT_NAME);
    assertDoesNotThrow(() -> XMLObjectSupport.cloneXMLObject(sp));
  }

  @Test
  void testKeepAllDescriptors() throws Exception {
    final Resource resource = new ClassPathResource("federationmetadata.xml");
    final XMLObject object = XMLObjectSupport.unmarshallFromInputStream(
        XMLObjectProviderRegistrySupport.getParserPool(), resource.getInputStream());
    final StaticMetadataProvider p = new StaticMetadataProvider((EntityDescriptor) object);
    p.setKeepOnlySpAndIdps(false);
    p.initialize();

    final EntityDescriptor sp = p.getEntityDescriptor("http://fstest.example.se/adfs/services/trust",
        SPSSODescriptor.DEFAULT_ELEMENT_NAME);

    // Clone fails -> OpenSAML bug ...
    assertThrows(ClassCastException.class, () -> XMLObjectSupport.cloneXMLObject(sp));
  }
}
