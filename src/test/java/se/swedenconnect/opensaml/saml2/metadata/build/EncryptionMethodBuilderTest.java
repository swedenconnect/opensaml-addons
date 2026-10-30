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
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

/**
 * Test cases for {@link EncryptionMethodBuilder}.
 *
 * @author Martin Lindström
 */
class EncryptionMethodBuilderTest extends OpenSAMLTestBase {

  @Test
  void testKeySizeAndOAEPparams() {
    final EncryptionMethod method = EncryptionMethodBuilder.builder()
        .algorithm("http://www.w3.org/2009/xmlenc11#aes256-gcm")
        .keySize(256)
        .oAEPparams("AQID")
        .build();
    Assertions.assertEquals(256, method.getKeySize().getValue());
    Assertions.assertEquals("AQID", method.getOAEPparams().getValue());

    final EncryptionMethod method2 = EncryptionMethodBuilder.builder()
        .algorithm("http://www.w3.org/2009/xmlenc11#aes256-gcm")
        .keySize(null)
        .oAEPparams(null)
        .build();
    Assertions.assertNull(method2.getKeySize());
    Assertions.assertNull(method2.getOAEPparams());
  }

}
