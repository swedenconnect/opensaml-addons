/*
 * Copyright 2021 Sweden Connect
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

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.idpdisco.DiscoveryResponse;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

public class DiscoveryResponseBuilder extends AbstractSAMLObjectBuilder<DiscoveryResponse> {

  /**
   * Default constructor.
   */
  public DiscoveryResponseBuilder() {
    super();
    this.object().setBinding(SAMLConstants.SAML_IDP_DISCO_NS);
  }

  /**
   * Constructor setting the location and the index.
   * 
   * @param location
   *          the location
   * @param index
   *          the index
   */
  public DiscoveryResponseBuilder(final String location, final Integer index) {
    this();
    this.location(location);
    this.index(index);
  }

  /**
   * Creates a builder.
   * 
   * @return a builder
   */
  public static DiscoveryResponseBuilder builder() {
    return new DiscoveryResponseBuilder();
  }

  /**
   * Creates a builder.
   * 
   * @param location
   *          the location
   * @param index
   *          the index
   * @return
   */
  public static DiscoveryResponseBuilder builder(final String location, final Integer index) {
    return new DiscoveryResponseBuilder(location, index);
  }

  /**
   * Adds discovery response location.
   * 
   * @param location
   *          URL for discovery responses
   * @return the builder
   */
  public DiscoveryResponseBuilder location(final String location) {
    this.object().setLocation(location);
    return this;
  }

  /**
   * Adds discovery index.
   * 
   * @param index
   *          the index
   * @return the builder
   */
  public DiscoveryResponseBuilder index(final Integer index) {
    this.object().setIndex(index);
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected Class<DiscoveryResponse> getObjectType() {
    return DiscoveryResponse.class;
  }

}
