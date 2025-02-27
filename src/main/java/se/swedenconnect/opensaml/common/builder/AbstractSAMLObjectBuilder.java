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
package se.swedenconnect.opensaml.common.builder;

import net.shibboleth.shared.xml.XMLParserException;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLRuntimeException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.InputStream;

/**
 * Abstract base class for the builder pattern.
 *
 * @param <T> the type
 * @author Martin Lindström (martin@idsec.se)
 */
public abstract class AbstractSAMLObjectBuilder<T extends XMLObject> implements SAMLObjectBuilder<T> {

  /** The object that is being built. */
  private final T object;

  /**
   * Constructor setting up the object to build.
   */
  public AbstractSAMLObjectBuilder() {
    this.object = this.getObjectType().cast(XMLObjectSupport.buildXMLObject(this.getDefaultElementName()));
  }

  /**
   * Constructor setting up the builder with a template object. Users of the instance may now change, add or delete, the
   * elements and attributes of the template object using the assignment methods of the builder.
   * <p>
   * Maps to {@link #AbstractSAMLObjectBuilder(XMLObject, boolean)} with the {@code clone} parameter set to
   * {@code true}.
   * </p>
   *
   * @param template the template object
   * @throws SAMLObjectBuilderRuntimeException for marshalling/unmarshalling errors
   */
  public AbstractSAMLObjectBuilder(final T template) throws SAMLObjectBuilderRuntimeException {
    this(template, true);
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
   * @throws SAMLObjectBuilderRuntimeException for marshalling/unmarshalling errors
   */
  public AbstractSAMLObjectBuilder(final T template, final boolean clone) throws SAMLObjectBuilderRuntimeException {
    try {
      this.object = clone ? XMLObjectSupport.cloneXMLObject(template) : template;
    }
    catch (final MarshallingException | UnmarshallingException e) {
      throw new SAMLObjectBuilderRuntimeException(e);
    }
  }

  /**
   * Constructor setting up the builder with a template object that is read from an input stream. Users of the instance
   * may now change, add or delete, the elements and attributes of the template object using the assignment methods of
   * the builder.
   *
   * @param resource the template resource
   * @throws UnmarshallingException for unmarshalling errors
   * @throws XMLParserException for XML parsing errors
   */
  public AbstractSAMLObjectBuilder(final InputStream resource) throws XMLParserException, UnmarshallingException {
    final Element elm = XMLObjectProviderRegistrySupport.getParserPool().parse(resource).getDocumentElement();
    this.object = this.getObjectType().cast(XMLObjectSupport.getUnmarshaller(elm).unmarshall(elm));
  }

  /**
   * The default implementation of this method assumes that the object has been built during assignment of its
   * attributes and elements, so it simply returns the object.
   * <p>
   * Implementations that need to perform additional processing during the build step should override this method.
   * </p>
   */
  @Override
  public T build() {
    return this.object();
  }

  /**
   * Returns the object type.
   *
   * @return the object type
   */
  protected abstract Class<T> getObjectType();

  /**
   * Returns the object being built.
   *
   * @return the object
   */
  public final T object() {
    return this.object;
  }

  /**
   * Gets the default element name for the object.
   *
   * @return a QName
   */
  protected QName getDefaultElementName() {
    try {
      return (QName) this.getObjectType().getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
    }
    catch (final NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
      throw new XMLRuntimeException(e);
    }
  }

}
