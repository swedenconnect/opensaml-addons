/*
 * Copyright 2016-2024 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.core.build;

import java.util.Arrays;
import java.util.List;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Extensions;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * Builder for {@link Extensions} objects.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ExtensionsBuilder extends AbstractSAMLObjectBuilder<Extensions> {

  /**
   * Default constructor.
   */
  public ExtensionsBuilder() {
    super();
  }

  /**
   * Creates a builder instance.
   *
   * @return a builder instance
   */
  public static ExtensionsBuilder builder() {
    return new ExtensionsBuilder();
  }

  /**
   * Adds the extensions (overwrites any previous extensions).
   *
   * @param extensions the extension objects
   * @return the builder
   */
  public ExtensionsBuilder extensions(final List<XMLObject> extensions) {
    // First clear all previous values ...
    this.object().getUnknownXMLObjects().clear();

    for (final XMLObject obj : extensions) {
      try {
        if (obj != null) {
          this.object().getUnknownXMLObjects().add(XMLObjectSupport.cloneXMLObject(obj));
        }
      }
      catch (final MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }

    return this;
  }

  /**
   * See {@link #extensions(List)}.
   *
   * @param extensions the extension objects
   * @return the builder
   */
  public ExtensionsBuilder extensions(final XMLObject... extensions) {
    return this.extensions(extensions != null ? Arrays.asList(extensions) : null);
  }

  /**
   * Adds an extension to this {@code Extensions} object.
   *
   * @param extension the extension to add
   * @return the builder
   */
  public ExtensionsBuilder extension(final XMLObject extension) {
    if (extension == null) {
      return this;
    }
    try {
      this.object().getUnknownXMLObjects().add(XMLObjectSupport.cloneXMLObject(extension));
    }
    catch (final MarshallingException | UnmarshallingException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected Class<Extensions> getObjectType() {
    return Extensions.class;
  }

}
