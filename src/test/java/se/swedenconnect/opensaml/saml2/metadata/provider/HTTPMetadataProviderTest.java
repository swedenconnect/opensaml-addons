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

import java.security.KeyStore;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.core.io.Resource;

import se.swedenconnect.opensaml.TestWebServer;

/**
 * Test cases for the {@code HTTPMetadataProvider} class.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class HTTPMetadataProviderTest extends BaseMetadataProviderTest {

  /** Holds the metadata that is serviced by the web server. */
  private static MetadataResourceProvider resourceProvider = new MetadataResourceProvider();

  /** The TLS trust. */
  private static KeyStore trustStore;

  /** The web server that serves the metadata. */
  private static TestWebServer server;

  static {
    try {
      trustStore = loadKeyStore("src/test/resources/trust.jks", "secret", null);
      server = new TestWebServer(resourceProvider, "src/test/resources/localhost.jks", "secret");
    }
    catch (Exception e) {
    }
  }

  /**
   * Starts the "remote" metadata service.
   *
   * @throws Exception for errors
   */
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

  /** {@inheritDoc} */
  @Override
  protected AbstractMetadataProvider createMetadataProvider(final Resource resource) throws Exception {

    resourceProvider.setResource(resource);
    return new HTTPMetadataProvider(server.getUrl(), null,
        HTTPMetadataProvider.createDefaultHttpClient(trustStore, null));
  }

  /**
   * Simple class holding the metadata (accessed by the web server).
   */
  private static class MetadataResourceProvider implements TestWebServer.ResourceProvider {

    private Resource resource;

    public void setResource(Resource resource) {
      this.resource = resource;
    }

    @Override
    public Resource getResource() {
      return this.resource;
    }
  }

}
