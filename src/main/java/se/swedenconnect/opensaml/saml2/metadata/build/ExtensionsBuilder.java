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

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.Extensions;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * Builder for metadata {@link Extensions} objects.
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
  public ExtensionsBuilder(final Extensions template, final boolean clone) {
    super(template, clone);
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
        this.object().getUnknownXMLObjects().add(XMLObjectSupport.cloneXMLObject(obj));
      }
      catch (MarshallingException | UnmarshallingException e) {
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
   * Adds one, or more, extensions to this {@code Extensions} object.
   * 
   * @param extension the extension(s) to add
   * @return the builder
   */
  public ExtensionsBuilder extension(final XMLObject... extension) {
    if (extension == null) {
      return this;
    }
    for (final XMLObject obj : extension) {
      try {
        this.object().getUnknownXMLObjects().add(XMLObjectSupport.cloneXMLObject(obj));
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected Class<Extensions> getObjectType() {
    return Extensions.class;
  }

}
