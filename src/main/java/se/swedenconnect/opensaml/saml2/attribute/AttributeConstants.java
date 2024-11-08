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
package se.swedenconnect.opensaml.saml2.attribute;

/**
 * Attribute constants.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class AttributeConstants {

  /**
   * The attribute name for the assurance certification attribute stored as an attribute in the entity attributes
   * extension.
   */
  public static final String ASSURANCE_CERTIFICATION_ATTRIBUTE_NAME =
      "urn:oasis:names:tc:SAML:attribute:assurance-certification";

  /**
   * The attribute template for the assurance certification attribute stored as an attribute in the entity attributes
   * extension.
   */
  public static final AttributeTemplate ASSURANCE_CERTIFICATION_ATTRIBUTE_TEMPLATE = new AttributeTemplate(
      ASSURANCE_CERTIFICATION_ATTRIBUTE_NAME, null);

  /** The attribute name for the entity category attribute stored as an attribute in the entity attributes extension. */
  public static final String ENTITY_CATEGORY_ATTRIBUTE_NAME = "http://macedir.org/entity-category";

  /**
   * The attribute template for the entity category attribute stored as an attribute in the entity attributes
   * extension.
   */
  public static final AttributeTemplate ENTITY_CATEGORY_TEMPLATE =
      new AttributeTemplate(ENTITY_CATEGORY_ATTRIBUTE_NAME, null);

  // Hidden constructor.
  private AttributeConstants() {
  }

}
