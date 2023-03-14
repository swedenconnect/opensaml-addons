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

import java.util.Arrays;
import java.util.List;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;

/**
 * Builder for {@link IDPSSODescriptor} objects.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class IDPSSODescriptorBuilder extends AbstractSSODescriptorBuilder<IDPSSODescriptor, IDPSSODescriptorBuilder> {

  /**
   * Default constructor.
   */
  public IDPSSODescriptorBuilder() {
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
  public IDPSSODescriptorBuilder(final IDPSSODescriptor template, final boolean clone) {
    super(template, clone);
  }  

  /**
   * Utility method that creates a {@code IDPSSODescriptorBuilder} instance.
   * 
   * @return a IDPSSODescriptorBuilder instance
   */
  public static IDPSSODescriptorBuilder builder() {
    return new IDPSSODescriptorBuilder();
  }

  /**
   * Assigns the {@code WantAuthnRequestsSigned} attribute of the {@code md:IDPSSODescriptor} element.
   * 
   * @param b
   *          boolean
   * @return the builder
   */
  public IDPSSODescriptorBuilder wantAuthnRequestsSigned(final Boolean b) {
    this.object().setWantAuthnRequestsSigned(b);
    return this;
  }

  /**
   * Adds {@code md:SingleSignOnService} elements to the {@code IDPSSODescriptor}.
   * 
   * @param singleSignOnServices
   *          single sign on service objects (cloned before assignment)
   * @return the builder
   */
  public IDPSSODescriptorBuilder singleSignOnServices(final List<SingleSignOnService> singleSignOnServices) {
    this.object().getSingleSignOnServices().clear();
    if (singleSignOnServices == null || singleSignOnServices.isEmpty()) {
      return this;
    }
    for (final SingleSignOnService sso : singleSignOnServices) {
      try {
        this.object().getSingleSignOnServices().add(XMLObjectSupport.cloneXMLObject(sso));
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  /**
   * See {@link #singleSignOnServices(List)}.
   * 
   * @param singleSignOnServices
   *          single sign on service objects (cloned before assignment)
   * @return the builder
   */
  public IDPSSODescriptorBuilder singleSignOnServices(final SingleSignOnService... singleSignOnServices) {
    return this.singleSignOnServices(singleSignOnServices != null ? Arrays.asList(singleSignOnServices) : null);
  }

  /**
   * Adds {@code Attribute} elements to the {@code IDPSSODescriptor}.
   * 
   * @param attributes
   *          the attributes to add
   * @return the builder
   */
  public IDPSSODescriptorBuilder attributes(final List<Attribute> attributes) {
    this.object().getAttributes().clear();
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    for (final Attribute a : attributes) {
      try {
        this.object().getAttributes().add(XMLObjectSupport.cloneXMLObject(a));
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  /**
   * See {@link #attributes(List)}.
   * 
   * @param attributes
   *          the attributes to add
   * @return the builder
   */
  public IDPSSODescriptorBuilder attributes(final Attribute... attributes) {
    return this.attributes(attributes != null ? Arrays.asList(attributes) : null);
  }

  /** {@inheritDoc} */
  @Override
  protected IDPSSODescriptorBuilder getThis() {
    return this;
  }

  /** {@inheritDoc} */
  @Override
  protected Class<IDPSSODescriptor> getObjectType() {
    return IDPSSODescriptor.class;
  }

}
