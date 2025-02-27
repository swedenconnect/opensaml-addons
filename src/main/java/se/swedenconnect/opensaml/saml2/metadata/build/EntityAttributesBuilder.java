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
package se.swedenconnect.opensaml.saml2.metadata.build;

import java.util.Arrays;
import java.util.List;

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;

import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;
import se.swedenconnect.opensaml.saml2.attribute.AttributeConstants;

/**
 * A builder for {@link EntityAttributes} objects.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class EntityAttributesBuilder extends AbstractSAMLObjectBuilder<EntityAttributes> {

  /**
   * Default constructor.
   */
  public EntityAttributesBuilder() {
    super();
  }

  /**
   * Creates a builder instance.
   *
   * @return a builder instance
   */
  public static EntityAttributesBuilder builder() {
    return new EntityAttributesBuilder();
  }

  /**
   * Creates (or replaces) the {@code mdattr:EntityAttributes} element and adds the supplied attributes.
   * <p>
   * If {@code null} is supplied, the attributes are cleared.
   * </p>
   *
   * @param attributes the attributes to add to the entity attributes object
   * @return the builder
   */
  public EntityAttributesBuilder attributes(final List<Attribute> attributes) {
    this.object().getAttributes().clear();
    if (attributes == null) {
      return this;
    }
    for (final Attribute a : attributes) {
      this.attribute(a);
    }
    return this;
  }

  /**
   * Adds an attribute to this {@code mdattr:EntityAttributes}.
   *
   * @param attribute the attribute to add
   * @return the builder
   */
  public EntityAttributesBuilder attribute(final Attribute attribute) {
    if (attribute != null) {
      try {
        this.object().getAttributes().add(XMLObjectSupport.cloneXMLObject(attribute));
      }
      catch (final MarshallingException | UnmarshallingException e) {
        throw new RuntimeException(e);
      }
    }
    return this;
  }

  /**
   * Adds an {@value AttributeConstants#ASSURANCE_CERTIFICATION_ATTRIBUTE_NAME} attribute containing the supplied URIs
   * as attribute values.
   * <p>
   * If the attribute already exists its value will be updated.
   * </p>
   * <p>
   * If {@code null} is supplied an already existing attribute is removed.
   * </p>
   *
   * @param uris the assurance URI values that should be added
   * @return the builder
   */
  public EntityAttributesBuilder assuranceCertificationAttribute(final List<String> uris) {
    Attribute assuranceCertification = this.object().getAttributes().stream()
        .filter(a -> AttributeConstants.ASSURANCE_CERTIFICATION_ATTRIBUTE_NAME.equals(a.getName()))
        .findFirst()
        .orElse(null);

    if (assuranceCertification == null) {
      if (uris != null && !uris.isEmpty()) {
        assuranceCertification = AttributeConstants.ASSURANCE_CERTIFICATION_ATTRIBUTE_TEMPLATE
            .createBuilder().value(uris).build();
        return this.attribute(assuranceCertification);
      }
    }
    else {
      if (uris != null && !uris.isEmpty()) {
        assuranceCertification.getAttributeValues().clear();
        for (final String u : uris) {
          final XSString sv = (XSString) XMLObjectSupport.getBuilder(XSString.TYPE_NAME)
              .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                  XSString.TYPE_NAME);
          sv.setValue(u);
          assuranceCertification.getAttributeValues().add(sv);
        }
      }
      else {
        this.object().getAttributes().remove(assuranceCertification);
      }
    }

    return this;
  }

  /**
   * See {@link #assuranceCertificationAttribute(List)}.
   *
   * @param uris the assurance URI values that should be added
   * @return the builder
   */
  public EntityAttributesBuilder assuranceCertificationAttribute(final String... uris) {
    return this.assuranceCertificationAttribute(uris != null ? Arrays.asList(uris) : null);
  }

  /**
   * Adds an {@value AttributeConstants#ENTITY_CATEGORY_ATTRIBUTE_NAME} attribute containing the supplied URIs as
   * attribute values.
   * <p>
   * If the attribute already exists its value will be updated.
   * </p>
   * <p>
   * If {@code null} is supplied an already existing attribute is removed.
   * </p>
   *
   * @param uris the entity category URI values that should be added
   * @return the builder
   */
  public EntityAttributesBuilder entityCategoriesAttribute(final List<String> uris) {
    Attribute entityCategories = this.object().getAttributes().stream()
        .filter(a -> AttributeConstants.ENTITY_CATEGORY_ATTRIBUTE_NAME.equals(a.getName()))
        .findFirst()
        .orElse(null);

    if (entityCategories == null) {
      if (uris != null && !uris.isEmpty()) {
        entityCategories = AttributeConstants.ENTITY_CATEGORY_TEMPLATE.createBuilder().value(uris).build();
        return this.attribute(entityCategories);
      }
    }
    else {
      if (uris != null && !uris.isEmpty()) {
        entityCategories.getAttributeValues().clear();
        for (final String u : uris) {
          final XSString sv = (XSString) XMLObjectSupport.getBuilder(XSString.TYPE_NAME)
              .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,
                  XSString.TYPE_NAME);
          sv.setValue(u);
          entityCategories.getAttributeValues().add(sv);
        }
      }
      else {
        this.object().getAttributes().remove(entityCategories);
      }
    }

    return this;
  }

  /**
   * See {@link #entityCategoriesAttribute(List)}.
   *
   * @param uris the entity category URI values that should be added
   * @return the builder
   */
  public EntityAttributesBuilder entityCategoriesAttribute(final String... uris) {
    return this.entityCategoriesAttribute(uris != null ? Arrays.asList(uris) : null);
  }

  /** {@inheritDoc} */
  @Override
  protected Class<EntityAttributes> getObjectType() {
    return EntityAttributes.class;
  }

}
