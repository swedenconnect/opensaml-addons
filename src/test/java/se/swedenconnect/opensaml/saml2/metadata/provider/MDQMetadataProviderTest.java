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

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.function.Function;

class MDQMetadataProviderTest extends OpenSAMLTestBase {

  private static final File cacheDir = new File("target/cache");

  //  private X509Certificate cert;

  /** The TLS trust. */
  private static final KeyStore trustStore;

  /** The web server that serves the metadata. */
  private static final TestWebServer server;

  static {
    try {
      trustStore = loadKeyStore("src/test/resources/trust.jks", "secret", null);
      server = new TestWebServer(new MDQHandler(
          new MDQProvider(new ClassPathResource("/metadata/sveleg-fedtest.xml"))),
          "src/test/resources/localhost.jks", "secret");
    }
    catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeAll
  static void startServer() throws Exception {

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
  static void stopServer() throws Exception {
    server.stop();

    System.clearProperty("jdk.security.allowNonCaAnchor");
  }

  @BeforeEach
  void init() {
    try {
      FileUtils.forceDelete(cacheDir);
    }
    catch (final Exception ignored) {
    }
  }

  @Test
  void testGet() throws Exception {

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

    final EntitiesDescriptor metadata = (EntitiesDescriptor) provider.getMetadata();
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

    private final Resource metadataResource;
    private EntitiesDescriptor metadata;

    public MDQProvider(final Resource metadataResource) {
      this.metadataResource = metadataResource;
    }

    @Override
    public EntityDescriptor apply(final String id) {
      if (this.metadata == null) {
        try {
          this.metadata = OpenSAMLTestBase.unmarshall(this.metadataResource.getInputStream(), EntitiesDescriptor.class);
        }
        catch (final Exception e) {
          throw new SecurityException(e);
        }
      }
      for (final EntityDescriptor ed : this.metadata.getEntityDescriptors()) {
        if (ed.getEntityID().equals(id)) {
          return ed;
        }
      }
      return null;
    }

  }

  public static class MDQHandler extends Handler.Abstract {

    private final Function<String, EntityDescriptor> resourceProvider;

    public MDQHandler(final Function<String, EntityDescriptor> resourceProvider) {
      this.resourceProvider = resourceProvider;
    }

    @Override
    public boolean handle(final Request request, final Response response, final Callback callback) {
      final int pos = request.getHttpURI().asString().indexOf("/entities/");
      if (pos == -1) {
        Response.writeError(request, response, callback, HttpStatus.NOT_FOUND_404);
      }
      else {
        final String id = request.getHttpURI().asString().substring(pos + "/entities/".length());
        final String entityId = URLDecoder.decode(id, StandardCharsets.UTF_8);
        final EntityDescriptor ed = this.resourceProvider.apply(entityId);
        if (ed == null) {
          Response.writeError(request, response, callback, HttpStatus.NOT_FOUND_404);
        }
        else {
          try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLObjectSupport.marshallToOutputStream(ed, baos);
            response.write(true, ByteBuffer.wrap(baos.toByteArray()), callback);
          }
          catch (final MarshallingException e) {
            Response.writeError(request, response, callback, HttpStatus.INTERNAL_SERVER_ERROR_500);
          }
        }
      }
      return true;
    }
  }

}
