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

import net.shibboleth.shared.component.ComponentInitializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.TestWebServer;
import se.swedenconnect.security.credential.utils.X509Utils;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Additional test cases for HTTPMetadataProvider.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class HTTPMetadataProvider2Test extends OpenSAMLTestBase {

  @Test
  public void testHttp() throws Exception {
    final TestWebServer server =
        new TestWebServer(() -> new ClassPathResource("/metadata/sveleg-fedtest.xml"), null, null);
    server.start();

    HTTPMetadataProvider provider = null;
    try {
      provider = new HTTPMetadataProvider(server.getUrl(), null);
      provider.setFailFastInitialization(true);
      provider.setRequireValidMetadata(true);
      provider.initialize();

      final EntityDescriptor ed = provider.getEntityDescriptor(BaseMetadataProviderTest.TEST_IDP);
      Assertions.assertNotNull(ed,
          String.format("EntityDescriptor for '%s' was not found", BaseMetadataProviderTest.TEST_IDP));
    }
    finally {
      provider.destroy();
      server.stop();
    }
  }

  @Test
  public void testSwedenConnect() throws Exception {
    final X509Certificate signingCert = decodeCertificate(new ClassPathResource("sweden-connect-prod.crt"));
    HTTPMetadataProvider provider = null;

    try {
      provider = new HTTPMetadataProvider("https://md.swedenconnect.se/role/idp.xml", null,
          HTTPMetadataProvider.createDefaultHttpClient(null, null));
      provider.setFailFastInitialization(true);
      provider.setRequireValidMetadata(true);
      provider.setSignatureVerificationCertificate(signingCert);
      provider.initialize();

      final List<EntityDescriptor> idps = provider.getIdentityProviders();
      Assertions.assertTrue(idps.size() > 1);
    }
    finally {
      provider.destroy();
    }
  }

  @Test
  public void testSwedenConnectNotTrusted() throws Exception {
    final X509Certificate signingCert = decodeCertificate(new ClassPathResource("sweden-connect-prod.crt"));
    Assertions.assertThrows(ComponentInitializationException.class, () -> {
      HTTPMetadataProvider provider = null;
      try {
        provider = new HTTPMetadataProvider("https://md.swedenconnect.se/role/idp.xml", null,
            HTTPMetadataProvider.createDefaultHttpClient(loadKeyStore("src/test/resources/trust.jks", "secret", null),
                null));
        provider.setFailFastInitialization(true);
        provider.setRequireValidMetadata(true);
        provider.setSignatureVerificationCertificate(signingCert);
        provider.initialize();
      }
      finally {
        provider.destroy();
      }
    });
  }

  @Test
  public void testSwedenConnectFailedSignatureValidation() throws Exception {
    final X509Certificate signingCert = decodeCertificate(new ClassPathResource("testca.crt"));

    Assertions.assertThrows(ComponentInitializationException.class, () -> {
      HTTPMetadataProvider provider = null;
      try {
        provider = new HTTPMetadataProvider("https://md.swedenconnect.se/role/idp.xml", null,
            HTTPMetadataProvider.createDefaultHttpClient(null, null));
        provider.setFailFastInitialization(true);
        provider.setRequireValidMetadata(true);
        provider.setSignatureVerificationCertificate(signingCert);
        provider.initialize();

        provider.getIdentityProviders();
      }
      finally {
        provider.destroy();
      }
    });
  }

  private static X509Certificate decodeCertificate(final Resource certResource) throws Exception {
    try (final InputStream is = certResource.getInputStream()) {
      return X509Utils.decodeCertificate(is);
    }
  }

}
