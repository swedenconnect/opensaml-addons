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

import org.opensaml.saml.saml2.core.Attribute;
import se.swedenconnect.opensaml.common.LibraryVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * An attribute template is a template of a SAML attribute, i.e., it represents the name, friendly name and name format
 * but not the value of the attribute.
 * <p>
 * A template may be useful when defining attribute sets and/or attribute release policies.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class AttributeTemplate implements Serializable {

  @Serial
  private static final long serialVersionUID = LibraryVersion.SERIAL_VERSION_UID;

  /** The attribute name. */
  private final String name;

  /** The attribute friendly name. */
  private final String friendlyName;

  /**
   * The name format of this attribute. The default is {@code urn:oasis:names:tc:SAML:2.0:attrname-format:uri}
   * ({@link Attribute#URI_REFERENCE}).
   */
  private final String nameFormat;

  /**
   * Creates an attribute template with the given name and friendly name, the default name format
   * {@code urn:oasis:names:tc:SAML:2.0:attrname-format:uri} ({@link Attribute#URI_REFERENCE}) and not multivalued.
   *
   * @param name the attribute name
   * @param friendlyName the attribute friendly name (optional)
   */
  public AttributeTemplate(final String name, final String friendlyName) {
    this(name, friendlyName, Attribute.URI_REFERENCE);
  }

  /**
   * Creates an attribute template with the given name, friendly name and name format.
   *
   * @param name the attribute name
   * @param friendlyName the attribute friendly name
   * @param nameFormat the name format
   */
  public AttributeTemplate(final String name, final String friendlyName, final String nameFormat) {
    this.name = Optional.ofNullable(name).orElseThrow(() -> new IllegalArgumentException("'name' must not be null"));
    this.friendlyName = friendlyName;
    this.nameFormat = Optional.ofNullable(nameFormat).orElse(Attribute.URI_REFERENCE);
  }

  /**
   * Get the name of this attribute template.
   *
   * @return the name of this attribute template
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the friendly name of this attribute template.
   *
   * @return the friendly name of this attribute template
   */
  public String getFriendlyName() {
    return this.friendlyName;
  }

  /**
   * Get the name format of this attribute template.
   *
   * @return the name format of this attribute template
   */
  public String getNameFormat() {
    return this.nameFormat;
  }

  /**
   * Based on the attribute template an {@link AttributeBuilder} object is created.
   *
   * @return a builder
   */
  public AttributeBuilder createBuilder() {
    final AttributeBuilder builder = new AttributeBuilder(this.name);
    return builder.friendlyName(this.friendlyName).nameFormat(this.nameFormat);
  }

}
