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
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.springframework.core.io.ClassPathResource;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Test cases for {@link KeyDescriptorBuilder}.
 *
 * @author Martin Lindström
 */
class KeyDescriptorBuilderTest extends OpenSAMLTestBase {

  @Test
  void testRemoveKeyNameAndCertificate() throws Exception {
    final KeyDescriptorBuilder builder = KeyDescriptorBuilder.builder()
        .keyName("name")
        .certificate(new ClassPathResource("sweden-connect-prod.crt").getInputStream());
    Assertions.assertEquals(1, builder.object().getKeyInfo().getKeyNames().size());
    Assertions.assertEquals(1, builder.object().getKeyInfo().getX509Datas().size());

    final KeyDescriptor descriptor = builder
        .keyName(null)
        .certificate((String) null)
        .build();
    Assertions.assertTrue(descriptor.getKeyInfo().getKeyNames().isEmpty());
    Assertions.assertTrue(descriptor.getKeyInfo().getX509Datas().isEmpty());
  }

}
