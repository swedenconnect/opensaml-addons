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
package se.swedenconnect.opensaml.config;

import jakarta.annotation.Nonnull;
import org.opensaml.core.xml.config.AbstractXMLObjectProviderInitializer;

/**
 * XMLObject provider initializer for this module.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class XMLObjectProviderInitializer extends AbstractXMLObjectProviderInitializer {

  /** Config resources. */
  private static final String[] configs = {
      "/shib-scope-config.xml"
  };

  /** {@inheritDoc} */
  @Override
  @Nonnull
  protected String[] getConfigResources() {
    try {
      // If the Shibboleth implementation of the Scope element can be found
      // in the classpath it means that the Shibboleth version will be loaded,
      // and we don't have to register ours.
      //
      Class.forName("net.shibboleth.idp.saml.xmlobject.impl.ScopeImpl", false, this.getClass().getClassLoader());
      return new String[0];
    }
    catch (final Exception e) {
      // Shibboleth is not available, register our Scope object.
      return configs;
    }
  }
}
