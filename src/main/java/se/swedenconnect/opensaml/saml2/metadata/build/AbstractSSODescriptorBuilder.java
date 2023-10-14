/*
 * Copyright 2021-2023 Sweden Connect
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

import java.util.Arrays;
import java.util.List;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * Abstract base class for building a {@link SSODescriptor}.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class AbstractSSODescriptorBuilder<T extends SSODescriptor, B extends AbstractSAMLObjectBuilder<T>>
    extends AbstractSAMLObjectBuilder<T> {

  /**
   * Default constructor.
   */
  public AbstractSSODescriptorBuilder() {
    super();
    this.object().addSupportedProtocol(SAMLConstants.SAML20P_NS);
  }

  /**
   * Constructor setting up the builder with a template object. Users of the instance may now change, add or delete, the
   * elements and attributes of the template object using the assignment methods of the builder.
   * <p>
   * The {@code clone} parameter tells whether the object should be cloned or not. If set to {@code true}, any
   * modifications will have no effect on the passed object.
   * </p>
   *
   * @param template the template object
   * @param clone whether the template object should be cloned
   */
  public AbstractSSODescriptorBuilder(final T template, final boolean clone) {
    super(template, clone);
    this.object().addSupportedProtocol(SAMLConstants.SAML20P_NS);
  }

  /**
   * Adds the key descriptor elements.
   *
   * @param keyDescriptors the key descriptors
   * @return the builder
   */
  public B keyDescriptors(final List<KeyDescriptor> keyDescriptors) {
    this.object().getKeyDescriptors().clear();
    if (keyDescriptors == null || keyDescriptors.isEmpty()) {
      return this.getThis();
    }
    for (final KeyDescriptor kd : keyDescriptors) {
      try {
        if (kd != null) {
          this.object().getKeyDescriptors().add(XMLObjectSupport.cloneXMLObject(kd));
        }
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this.getThis();
  }

  /**
   * See {@link #keyDescriptors(List)}.
   *
   * @param keyDescriptors the key descriptors
   * @return the builder
   */
  public B keyDescriptors(final KeyDescriptor... keyDescriptors) {
    return this.keyDescriptors(keyDescriptors != null ? Arrays.asList(keyDescriptors) : null);
  }

  /**
   * Assigns metadata extensions.
   *
   * @param extensions the metadata extensions.
   * @return the builder
   */
  public B extensions(final Extensions extensions) {
    this.object().setExtensions(extensions);
    return this.getThis();
  }

  /**
   * Based on the contents of this object, an {@link ExtensionsBuilder} is returned. If the object holds an
   * {@link Extensions} object, this is fed to the builder (but not cloned).
   *
   * @return an {@link ExtensionsBuilder}
   */
  public ExtensionsBuilder getExtensionsBuilder() {
    if (this.object().getExtensions() != null) {
      return new ExtensionsBuilder(this.object().getExtensions(), false);
    }
    else {
      return new ExtensionsBuilder();
    }
  }

  /**
   * Assigns the {@code md:NameIDFormat} elements.
   *
   * @param nameIDFormats the nameID format strings
   * @return the builder
   */
  public B nameIDFormats(final List<String> nameIDFormats) {
    this.object().getNameIDFormats().clear();
    if (nameIDFormats == null || nameIDFormats.isEmpty()) {
      return this.getThis();
    }
    for (final String id : nameIDFormats) {
      final NameIDFormat name = (NameIDFormat) XMLObjectSupport.buildXMLObject(NameIDFormat.DEFAULT_ELEMENT_NAME);
      name.setURI(id);
      this.object().getNameIDFormats().add(name);
    }
    return this.getThis();
  }

  /**
   * See {@link #nameIDFormats(List)}.
   *
   * @param nameIDFormats the nameID format strings
   * @return the builder
   */
  public B nameIDFormats(final String... nameIDFormats) {
    return this.nameIDFormats(nameIDFormats != null ? Arrays.asList(nameIDFormats) : null);
  }

  /**
   * Adds {@code md:SingleLogoutService} elements to the {@code SSODescriptor}.
   *
   * @param singleLogoutServices single logout service objects (cloned before assignment)
   * @return the builder
   */
  public B singleLogoutServices(final List<SingleLogoutService> singleLogoutServices) {
    this.object().getSingleLogoutServices().clear();
    if (singleLogoutServices == null || singleLogoutServices.isEmpty()) {
      return this.getThis();
    }
    for (final SingleLogoutService slo : singleLogoutServices) {
      try {
        if (slo != null) {
          this.object().getSingleLogoutServices().add(XMLObjectSupport.cloneXMLObject(slo));
        }
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this.getThis();
  }

  /**
   * See {@link #singleLogoutServices(List)}.
   *
   * @param singleLogoutServices single logout service objects (cloned before assignment)
   * @return the builder
   */
  public B singleLogoutServices(final SingleLogoutService... singleLogoutServices) {
    return this.singleLogoutServices(singleLogoutServices != null ? Arrays.asList(singleLogoutServices) : null);
  }

  /**
   * In order for us to be able to make chaining calls we need to return the concrete type of the builder.
   *
   * @return the concrete type of the builder
   */
  protected abstract B getThis();

}
