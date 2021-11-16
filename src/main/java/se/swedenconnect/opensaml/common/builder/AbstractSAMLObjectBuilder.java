/*
 * Copyright 2016-2021 Sweden Connect
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

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLRuntimeException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.w3c.dom.Element;

import net.shibboleth.utilities.java.support.xml.XMLParserException;

/**
 * Abstract base class for the builder pattern.
 * 
 * @author Martin Lindström (martin@idsec.se)
 *
 * @param <T>
 *          the type
 */
public abstract class AbstractSAMLObjectBuilder<T extends XMLObject> implements SAMLObjectBuilder<T> {

  /** The object that is being built. */
  private T object;

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
   * Note that the supplied object is cloned, so any modifications have no effect on the passed object.
   * </p>
   * 
   * @param template
   *          the template object
   * @throws UnmarshallingException
   *           for unmarshalling errors
   * @throws MarshallingException
   *           for marshalling errors
   */
  public AbstractSAMLObjectBuilder(final T template) throws MarshallingException, UnmarshallingException {
    this.object = XMLObjectSupport.cloneXMLObject(template);
  }

  /**
   * Constructor setting up the builder with a template object that is read from an input stream. Users of the instance
   * may now change, add or delete, the elements and attributes of the template object using the assignment methods of
   * the builder.
   * 
   * @param resource
   *          the template resource
   * @throws UnmarshallingException
   *           for unmarshalling errors
   * @throws XMLParserException
   *           for XML parsing errors
   */
  public AbstractSAMLObjectBuilder(final InputStream resource) throws XMLParserException, UnmarshallingException {
    final Element elm = XMLObjectProviderRegistrySupport.getParserPool().parse(resource).getDocumentElement();
    this.object = this.getObjectType().cast(XMLObjectSupport.getUnmarshaller(elm).unmarshall(elm));
  }

  /**
   * The default implementation of this method assumes that the object has been built during assignment of its
   * attributes and elements so it simply returns the object.
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
    catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
      throw new XMLRuntimeException(e);
    }
  }

}
