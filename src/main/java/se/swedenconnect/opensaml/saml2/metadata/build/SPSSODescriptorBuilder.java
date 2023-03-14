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
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

/**
 * A builder for {@link SPSSODescriptor} objects.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class SPSSODescriptorBuilder extends AbstractSSODescriptorBuilder<SPSSODescriptor, SPSSODescriptorBuilder> {

  /**
   * Default constructor.
   */
  public SPSSODescriptorBuilder() {
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
  public SPSSODescriptorBuilder(final SPSSODescriptor template, final boolean clone) {
    super(template, clone);
  }  

  /**
   * Utility method that creates a {@code SPSSODescriptorBuilder} instance.
   * 
   * @return a SPSSODescriptorBuilder instance
   */
  public static SPSSODescriptorBuilder builder() {
    return new SPSSODescriptorBuilder();
  }

  /**
   * Assigns the {@code AuthnRequestsSigned} attribute of the {@code md:SPSSODescriptor} element.
   * 
   * @param b
   *          boolean (if null, the attribute is not set)
   * @return the builder
   */
  public SPSSODescriptorBuilder authnRequestsSigned(final Boolean b) {
    this.object().setAuthnRequestsSigned(b);
    return this;
  }

  /**
   * Assigns the {@code WantAssertionsSigned} attribute of the {@code md:SPSSODescriptor} element.
   * 
   * @param b
   *          whether assertions should be signed
   * @return the builder
   */
  public SPSSODescriptorBuilder wantAssertionsSigned(final Boolean b) {
    this.object().setWantAssertionsSigned(b);
    return this;
  }

  /**
   * Adds {@code md:AssertionConsumerService} elements to the {@code SPSSODescriptor}.
   * 
   * @param assertionConsumerServices
   *          assertion consumer service objects (cloned before assignment)
   * @return the builder
   */
  public SPSSODescriptorBuilder assertionConsumerServices(final List<AssertionConsumerService> assertionConsumerServices) {
    this.object().getAssertionConsumerServices().clear();
    if (assertionConsumerServices == null || assertionConsumerServices.isEmpty()) {
      return this;
    }
    for (final AssertionConsumerService a : assertionConsumerServices) {
      try {
        this.object().getAssertionConsumerServices().add(XMLObjectSupport.cloneXMLObject(a));
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  /**
   * See {@link #assertionConsumerServices(List)}.
   * 
   * @param assertionConsumerServices
   *          assertion consumer service objects (cloned before assignment)
   * @return the builder
   */
  public SPSSODescriptorBuilder assertionConsumerServices(final AssertionConsumerService... assertionConsumerServices) {
    return this.assertionConsumerServices(assertionConsumerServices != null ? Arrays.asList(assertionConsumerServices) : null);
  }
  
  /**
   * Adds {@code md:AttributeConsumingService} elements to the {@code SPSSODescriptor}.
   * 
   * @param attributeConsumingServices
   *          attribute consumer service objects (cloned before assignment)
   * @return the builder
   */
  public SPSSODescriptorBuilder attributeConsumingServices(final List<AttributeConsumingService> attributeConsumingServices) {
    this.object().getAttributeConsumingServices().clear();    
    if (attributeConsumingServices == null || attributeConsumingServices.isEmpty()) {
      return null;
    }
    for (final AttributeConsumingService a : attributeConsumingServices) {
      try {
        this.object().getAttributeConsumingServices().add(XMLObjectSupport.cloneXMLObject(a));
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  /**
   * See {@link #attributeConsumingServices(List)}.
   * 
   * @param attributeConsumingServices
   *          attribute consumer service objects (cloned before assignment)
   * @return the builder
   */
  public SPSSODescriptorBuilder attributeConsumingServices(final AttributeConsumingService... attributeConsumingServices) {
    return this.attributeConsumingServices(attributeConsumingServices != null ? Arrays.asList(attributeConsumingServices) : null);
  }  

  /** {@inheritDoc} */
  @Override
  protected SPSSODescriptorBuilder getThis() {
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected Class<SPSSODescriptor> getObjectType() {
    return SPSSODescriptor.class;
  }

}
