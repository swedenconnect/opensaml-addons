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
package se.swedenconnect.opensaml;

import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.core.io.Resource;

import java.nio.ByteBuffer;

/**
 * Class for supporting test cases that need a web server.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class TestWebServer {

  /** The web server. */
  private final Server server;

  /** The URL that is exposed by the web server. */
  private String url;

  /**
   * Constructor setting up the web server.
   *
   * @param handler the handler
   * @param keyStorePath the path to the keystore holding the private key for the web server (may be null)
   * @param keyStorePassword the password for the keystore
   */
  public TestWebServer(final Handler handler, final String keyStorePath, final String keyStorePassword) {
    final QueuedThreadPool serverThreads = new QueuedThreadPool();
    serverThreads.setName("server");
    this.server = new Server(serverThreads);

    SslContextFactory.Server contextFactory = null;
    if (keyStorePath != null) {
      contextFactory = new SslContextFactory.Server();
      contextFactory.setTrustAll(true);
      contextFactory.setKeyStorePath(keyStorePath);
      contextFactory.setKeyStorePassword(keyStorePassword);
      contextFactory.setMaxCertPathLength(-1);
      contextFactory.setProtocol("TLS");
    }

    final ServerConnector connector = new ServerConnector(this.server, contextFactory);
    connector.setHost("localhost");
    this.server.addConnector(connector);
    this.server.setHandler(handler);

    for (final Connector c : this.server.getConnectors()) {
      c.getConnectionFactories().stream()
          .filter(HttpConnectionFactory.class::isInstance)
          .forEach(factory -> {
            final HttpConfiguration httpConfig = ((HttpConnectionFactory) factory).getHttpConfiguration();
            httpConfig.setUriCompliance(UriCompliance.LEGACY);
          });
    }
  }

  /**
   * Constructor setting up the web server.
   *
   * @param resourceProvider the provider handling the data
   * @param keyStorePath the path to the keystore holding the private key for the web server (may be null)
   * @param keyStorePassword the password for the keystore
   */
  public TestWebServer(final ResourceProvider resourceProvider, final String keyStorePath,
      final String keyStorePassword) {
    this(new ResourceHandler(resourceProvider), keyStorePath, keyStorePassword);
  }

  /**
   * Starts the metadata service.
   *
   * @throws Exception if the server fails to start
   */
  public void start() throws Exception {
    this.server.start();
    this.url = this.server.getURI().toURL().toString();
  }

  /**
   * Stops the metadata service.
   *
   * @throws Exception if the service fails to stop
   */
  public void stop() throws Exception {
    if (this.server != null && this.server.isStarted() && !this.server.isStopped()) {
      this.server.stop();
    }
  }

  /**
   * Returns the URL for the server
   *
   * @return the URL
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Simple interface for a resource provider.
   */
  @FunctionalInterface
  public interface ResourceProvider {
    Resource getResource();
  }

  /**
   * The {@code ResourceHandler} that is used by the server.
   */
  public static class ResourceHandler extends Handler.Abstract {

    private final ResourceProvider resourceProvider;

    public ResourceHandler(final ResourceProvider resourceProvider) {
      this.resourceProvider = resourceProvider;
    }

    @Override
    public boolean handle(final Request request, final Response response, final Callback callback) throws Exception {
      response.write(
          true, ByteBuffer.wrap(this.resourceProvider.getResource().getInputStream().readAllBytes()), callback);
      return true;
    }
  }

}
