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
package se.swedenconnect.opensaml.saml2.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.X509Credential;
import org.springframework.core.io.ClassPathResource;
import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.saml2.metadata.build.KeyDescriptorBuilder;
import se.swedenconnect.opensaml.saml2.metadata.build.SPSSODescriptorBuilder;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

/**
 * Test cases for {@link EntityDescriptorUtils}.
 *
 * @author Martin Lindström
 */
class EntityDescriptorUtilsTest extends OpenSAMLTestBase {

  @Test
  void testGetMetadataCertificatesLineWrapped() throws Exception {
    final X509Certificate certificate =
        decodeCertificate(new ClassPathResource("sweden-connect-prod.crt").getInputStream());

    // Base64 with line breaks, as is common in metadata
    final String encoding = "\n" + Base64.getMimeEncoder().encodeToString(certificate.getEncoded()) + "\n";

    final SPSSODescriptor descriptor = SPSSODescriptorBuilder.builder()
        .keyDescriptors(KeyDescriptorBuilder.builder().use(UsageType.SIGNING).certificate(encoding).build())
        .build();

    final List<X509Credential> credentials =
        EntityDescriptorUtils.getMetadataCertificates(descriptor, UsageType.SIGNING);
    Assertions.assertEquals(1, credentials.size());
    Assertions.assertEquals(certificate, credentials.get(0).getEntityCertificate());
  }

}
