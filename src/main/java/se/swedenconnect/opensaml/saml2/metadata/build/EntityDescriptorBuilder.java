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
package se.swedenconnect.opensaml.saml2.metadata.build;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.ContactPerson;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;

import net.shibboleth.utilities.java.support.xml.XMLParserException;
import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

/**
 * A builder for creating {@link EntityDescriptor} objects.
 * 
 * @author Martin Lindström (martin@idsec.se)
 */
public class EntityDescriptorBuilder extends AbstractSAMLObjectBuilder<EntityDescriptor> {

  /**
   * Constructor setting up the builder with no template. This means that the entire {@code EntityDescriptor} object is
   * created from data assigned using the builder.
   */
  public EntityDescriptorBuilder() {
    super();
  }

  /**
   * Constructor setting up the builder with a template {@code EntityDescriptor} that is read from a resource. Users of
   * the bean may now change, add or delete, the elements and attributes of the template object using the assignment
   * methods of the builder.
   * 
   * @param resource
   *          the template resource
   * @throws IOException
   *           if the resource can not be read
   * @throws UnmarshallingException
   *           for unmarshalling errors
   * @throws XMLParserException
   *           for XML parsing errors
   */
  public EntityDescriptorBuilder(final InputStream resource) throws XMLParserException, UnmarshallingException, IOException {
    super(resource);

    // Remove signature
    this.object().setSignature(null);
  }

  /**
   * Constructor setting up the builder with a template {@code EntityDescriptor}. Users of the bean may now change, add
   * or delete, the elements and attributes of the template object using the assignment methods of the builder.
   * 
   * @param template
   *          the template
   * @throws UnmarshallingException
   *           for unmarshalling errors
   * @throws MarshallingException
   *           for marshalling errors
   */
  public EntityDescriptorBuilder(final EntityDescriptor template) throws UnmarshallingException, MarshallingException {
    super(template);

    // Remove signature
    this.object().setSignature(null);
  }

  /**
   * Assigns the entityID for the {@code EntityDescriptor}.
   * 
   * @param entityID
   *          the entityID
   * @return the builder
   */
  public EntityDescriptorBuilder entityID(final String entityID) {
    this.object().setEntityID(entityID);
    return this;
  }

  /**
   * Utility method that creates an {@code EntityDescriptorBuilder} instance.
   * 
   * @return an EntityDescriptorBuilder instance
   */
  public static EntityDescriptorBuilder builder() {
    return new EntityDescriptorBuilder();
  }

  /**
   * Utility method that creates an {@code EntityDescriptorBuilder} instance from a supplied input stream.
   * 
   * @param resource
   *          the template resource
   * @return an EntityDescriptorBuilder instance
   * @throws IOException
   *           if the resource can not be read
   * @throws UnmarshallingException
   *           for unmarshalling errors
   * @throws XMLParserException
   *           for XML parsing errors
   */
  public static EntityDescriptorBuilder builder(final InputStream resource) throws XMLParserException, UnmarshallingException, IOException {
    return new EntityDescriptorBuilder(resource);
  }

  /**
   * Utility method that creates an {@code EntityDescriptorBuilder} instance from a supplied template.
   * 
   * @param template
   *          the template
   * @return an EntityDescriptorBuilder instance
   * @throws UnmarshallingException
   *           for unmarshalling errors
   * @throws MarshallingException
   *           for marshalling errors
   */
  public static EntityDescriptorBuilder builder(final EntityDescriptor template) throws UnmarshallingException, MarshallingException {
    return new EntityDescriptorBuilder(template);
  }

  /**
   * Assigns the ID attribute for the {@code EntityDescriptor}.
   * 
   * @param id
   *          the ID
   * @return the builder
   */
  public EntityDescriptorBuilder id(final String id) {
    this.object().setID(id);
    return this;
  }

  /**
   * Assigns the cacheDuration attribute for the {@code EntityDescriptor}.
   * 
   * @param cacheDuration
   *          the cache duration (in milliseconds)
   * @return the builder
   */
  public EntityDescriptorBuilder cacheDuration(final Long cacheDuration) {
    this.object().setCacheDuration(cacheDuration != null ? Duration.ofMillis(cacheDuration) : null);
    return this;
  }

  /**
   * Assigns the cacheDuration attribute for the {@code EntityDescriptor}.
   * 
   * @param cacheDuration
   *          the cache duration
   * @return the builder
   */
  public EntityDescriptorBuilder cacheDuration(final Duration cacheDuration) {
    this.object().setCacheDuration(cacheDuration);
    return this;
  }

  /**
   * Assigns the valid until time.
   * 
   * @param time
   *          valid until
   * @return the builder
   */
  public EntityDescriptorBuilder validUntil(final Instant time) {
    this.object().setValidUntil(time);
    return this;
  }

  /**
   * Assigns metadata extensions.
   * 
   * @param extensions
   *          the metadata extensions.
   * @return the builder
   */
  public EntityDescriptorBuilder extensions(final Extensions extensions) {
    this.object().setExtensions(extensions);
    return this;
  }

  /**
   * Adds the supplied SSO descriptors.
   * 
   * @param roleDescriptors
   *          the SSO descriptors to add
   * @return the builder
   */
  public EntityDescriptorBuilder roleDescriptors(final List<RoleDescriptor> roleDescriptors) {
    if (roleDescriptors == null || roleDescriptors.isEmpty()) {
      this.object().getRoleDescriptors().clear();
      return this;
    }
    for (final RoleDescriptor rd : roleDescriptors) {
      try {
        this.object().getRoleDescriptors().add(XMLObjectSupport.cloneXMLObject(rd));
      }
      catch (MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }  
    }    
    return this;
  }

  /**
   * See {@link #roleDescriptors(List)}.
   * 
   * @param roleDescriptors
   *          the SSO descriptors to add
   * @return the builder
   */
  public EntityDescriptorBuilder roleDescriptors(final RoleDescriptor... roleDescriptors) {
    return this.roleDescriptors(roleDescriptors != null ? Arrays.asList(roleDescriptors) : null);
  }

  /**
   * Adds one SSO descriptor (which is the most common case).
   * 
   * @param ssoDescriptor
   *          the descriptor to add
   * @return the builder
   */
  public EntityDescriptorBuilder ssoDescriptor(final SSODescriptor ssoDescriptor) {
    return this.roleDescriptors(ssoDescriptor);
  }

  /**
   * Assigns the {@code Organization} element to the entity descriptor.
   * 
   * @param organization
   *          the organization (will be cloned before assignment)
   * @return the builder
   */
  public EntityDescriptorBuilder organization(final Organization organization) {
    try {
      this.object().setOrganization(XMLObjectSupport.cloneXMLObject(organization));
    }
    catch (MarshallingException | UnmarshallingException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Assigns the {@code ContactPerson} elements to the entity descriptor.
   * 
   * @param contactPersons
   *          the contact person elements (will be cloned before assignment)
   * @return the builder
   */
  public EntityDescriptorBuilder contactPersons(final List<ContactPerson> contactPersons) {
    this.object().getContactPersons().clear();
    if (contactPersons != null) {
      for (ContactPerson cp : contactPersons) {
        try {
          this.object().getContactPersons().add(XMLObjectSupport.cloneXMLObject(cp));
        }
        catch (MarshallingException | UnmarshallingException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return this;
  }

  /**
   * @see #contactPersons(List)
   * 
   * @param contactPersons
   *          the contact person elements (will be cloned before assignment)
   * @return the builder
   */
  public EntityDescriptorBuilder contactPersons(final ContactPerson... contactPersons) {
    return this.contactPersons(contactPersons != null ? Arrays.asList(contactPersons) : null);
  }

  /** {@inheritDoc} */
  @Override
  protected Class<EntityDescriptor> getObjectType() {
    return EntityDescriptor.class;
  }

}
