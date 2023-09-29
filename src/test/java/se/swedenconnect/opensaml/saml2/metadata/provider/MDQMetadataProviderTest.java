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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.swedenconnect.opensaml.OpenSAMLTestBase;
import se.swedenconnect.opensaml.TestWebServer;

public class MDQMetadataProviderTest extends OpenSAMLTestBase {

  private static final File cacheDir = new File("target/cache");

//  private X509Certificate cert;

  /** The TLS trust. */
  private static KeyStore trustStore;

  /** The web server that serves the metadata. */
  private static TestWebServer server;

  static {
    try {
      trustStore = loadKeyStore("src/test/resources/trust.jks", "secret", null);
      server = new TestWebServer(new MDQHandler(
          new MDQProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"))),
          "src/test/resources/localhost.jks", "secret");
    }
    catch (Exception e) {
    }
  }

  @BeforeAll
  static public void startServer() throws Exception {

    // Since our test CA cert is missing BasicConstraints extension ...
    System.setProperty("jdk.security.allowNonCaAnchor", "true");

    server.start();
  }

  /**
   * Stops the "remote" metadata service.
   *
   * @throws Exception for errors
   */
  @AfterAll
  static public void stopServer() throws Exception {
    server.stop();

    System.clearProperty("jdk.security.allowNonCaAnchor");
  }

  @BeforeEach
  public void init() throws Exception {
    try {
      FileUtils.forceDelete(cacheDir);
    }
    catch (Exception e) {
    }
//    this.cert = decodeCertificate(new ClassPathResource("nordunet.crt").getInputStream());
  }

  @Test
  public void testGet() throws Exception {

    final MDQMetadataProvider provider = new MDQMetadataProvider(server.getUrl(),
        HTTPMetadataProvider.createDefaultHttpClient(trustStore, null),
        cacheDir.getAbsolutePath());

//    provider.setSignatureVerificationCertificate(this.cert);
    provider.initialize();

    Assertions.assertTrue(provider.getIdentityProviders().isEmpty());
    Assertions.assertTrue(provider.getServiceProviders().isEmpty());

    // Now, get some entities using MDQ ...
    final EntityDescriptor ed = provider.getEntityDescriptor("https://sickelstatest.transportstyrelsen.se/extweb/");
    Assertions.assertNotNull(ed);

    final EntityDescriptor ed2 = provider.getEntityDescriptor("https://idp.svelegtest.se/idp");
    Assertions.assertNotNull(ed2);

    Assertions.assertTrue(provider.getIdentityProviders().size() == 1);
    Assertions.assertTrue(provider.getServiceProviders().size() == 1);

    EntitiesDescriptor metadata = (EntitiesDescriptor) provider.getMetadata();
    Assertions.assertNotNull(metadata);
    Assertions.assertEquals(2, metadata.getEntityDescriptors().size());

    // Not found
    final EntityDescriptor ed3 = provider.getEntityDescriptor("https://not.found.com");
    Assertions.assertNull(ed3);
  }

//  @Test
//  public void testList() throws Exception {
//    final MDQMetadataProvider provider = new MDQMetadataProvider("https://md.nordu.net",
//        HTTPMetadataProvider.createDefaultHttpClient(), cacheDir.getAbsolutePath());
//    provider.setSignatureVerificationCertificate(this.cert);
//    provider.initialize();
//
//    Assert.assertTrue(provider.getIdentityProviders().isEmpty());
//    Assert.assertTrue(provider.getServiceProviders().isEmpty());
//
//    // Now, get some entities using MDQ ...
//    final EntityDescriptor ed = provider.getEntityDescriptor("http://adfs.hv.se/adfs/services/trust");
//    Assert.assertNotNull(ed);
//
//    final EntityDescriptor edb = provider.getEntityDescriptor("http://adfs.hv.se/adfs/services/trust2");
//    Assert.assertNull(edb);
//
//
//    // Both a SP and IdP
//    final EntityDescriptor ed2 = provider.getEntityDescriptor("http://adfs.helb-prigogine.be/adfs/services/trust");
//    Assert.assertNotNull(ed2);
//
//    Assert.assertTrue(provider.getIdentityProviders().size() == 2);
//    Assert.assertTrue(provider.getServiceProviders().size() == 1);
//
//    EntitiesDescriptor metadata = (EntitiesDescriptor) provider.getMetadata();
//    Assert.assertNotNull(metadata);
//    Assert.assertEquals(2, metadata.getEntityDescriptors().size());
//  }
//

  private static class MDQProvider implements Function<String, EntityDescriptor> {

    private final EntitiesDescriptor metadata;

    public MDQProvider(final Resource metadataResource) {
      try {
        this.metadata = OpenSAMLTestBase.unmarshall(metadataResource.getInputStream(), EntitiesDescriptor.class);
      }
      catch (Exception e) {
        throw new SecurityException(e);
      }
    }

    @Override
    public EntityDescriptor apply(final String id) {
      for (final EntityDescriptor ed : this.metadata.getEntityDescriptors()) {
        if (ed.getEntityID().equals(id)) {
          return ed;
        }
      }
      return null;
    }

  }

  public static class MDQHandler extends AbstractHandler {

    private final Function<String, EntityDescriptor> resourceProvider;

    public MDQHandler(final Function<String, EntityDescriptor> resourceProvider) {
      this.resourceProvider = resourceProvider;
    }

    @Override
    public void handle(final String target, final Request baseRequest,
        final jakarta.servlet.http.HttpServletRequest request,
        final jakarta.servlet.http.HttpServletResponse response) throws IOException, jakarta.servlet.ServletException {

      final int pos = request.getRequestURI().indexOf("/entities/");
      if (pos == -1) {
        response.sendError(404, "Not found");
      }
      else {
        final String id = request.getRequestURI().substring(pos + "/entities/".length());
        final String entityId = URLDecoder.decode(id, StandardCharsets.UTF_8);
        final EntityDescriptor ed = this.resourceProvider.apply(entityId);
        if (ed == null) {
          response.sendError(404, "Not found");
        }
        else {
          try {
            XMLObjectSupport.marshallToOutputStream(ed, response.getOutputStream());
          }
          catch (final MarshallingException | IOException e) {
            response.sendError(500, e.getMessage());
          }
        }
      }
      baseRequest.setHandled(true);
    }
  }

}
