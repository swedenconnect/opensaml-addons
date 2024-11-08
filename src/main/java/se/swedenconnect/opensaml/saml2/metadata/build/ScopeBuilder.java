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
package se.swedenconnect.opensaml.saml2.metadata.build;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLRuntimeException;
import org.opensaml.saml.common.SAMLObject;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;
import se.swedenconnect.opensaml.saml2.metadata.scope.Scope;

/**
 * Builder for {@code Scope} elements.
 * <p>
 * Since this class is also defined in Shibboleth's idp-saml-api and idp-saml-impl libraries we create only a
 * {@link SAMLObject} and depending on if the user has Shibboleth in the classpath an object of the
 * {@code net.shibboleth.idp.saml.xmlobject.Scope} or {@code se.swedenconnect.opensaml.saml2.metadata.scope.Scope} is
 * created.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class ScopeBuilder extends AbstractSAMLObjectBuilder<XMLObject> {

  /**
   * Creates a new {@code ScopeBuilder} instance.
   *
   * @return a ScopeBuilder instance
   */
  public static ScopeBuilder builder() {
    return new ScopeBuilder();
  }

  /**
   * Assigns the {@code regexp} attribute.
   *
   * @param regexp the regexp attribute
   * @return the builder
   */
  public ScopeBuilder regexp(final Boolean regexp) {
    if (this.object() instanceof Scope) {
      ((Scope) this.object()).setRegexp(regexp);
    }
    else {
      try {
        final Method method = this.object().getClass().getDeclaredMethod("setRegexp", Boolean.class);
        method.invoke(this.object(), regexp);
      }
      catch (final Exception e) {
        throw new XMLRuntimeException(e);
      }
    }
    return this;
  }

  /**
   * Assigns the value.
   *
   * @param value the value
   * @return the builder
   */
  public ScopeBuilder value(final String value) {
    if (this.object() instanceof Scope) {
      ((Scope) this.object()).setValue(value);
    }
    else {
      try {
        final Method method = this.object().getClass().getDeclaredMethod("setValue", String.class);
        method.invoke(this.object(), value);
      }
      catch (final Exception e) {
        throw new XMLRuntimeException(e);
      }
    }
    return this;
  }

  /**
   * Builds and casts to the correct Scope type.
   *
   * @param <T> the Scope type to cast to
   * @param clazz the Scope type to cast to
   * @return a Scope object
   */
  public <T> T build(final Class<T> clazz) {
    return clazz.cast(this.build());
  }

  /** {@inheritDoc} */
  @Override
  protected Class<XMLObject> getObjectType() {
    return XMLObject.class;
  }

  /** {@inheritDoc} */
  @Override
  protected QName getDefaultElementName() {
    return Scope.DEFAULT_ELEMENT_NAME;
  }

}
