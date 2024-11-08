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
package se.swedenconnect.opensaml.saml2.metadata;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2alg.DigestMethod;
import org.opensaml.saml.ext.saml2alg.SigningMethod;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.X509Data;
import se.swedenconnect.opensaml.saml2.attribute.AttributeConstants;
import se.swedenconnect.opensaml.saml2.attribute.AttributeUtils;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility methods for accessing metadata elements.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class EntityDescriptorUtils {

  /** Factory for creating certificates. */
  private static final CertificateFactory certFactory;

  static {
    try {
      certFactory = CertificateFactory.getInstance("X.509");
    }
    catch (final CertificateException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Finds the first extension matching the supplied type.
   *
   * @param extensions the Extensions to search
   * @param clazz the extension type
   * @param <T> the type of the extension
   * @return the matching extension or null
   */
  public static <T> T getMetadataExtension(final Extensions extensions, final Class<T> clazz) {
    if (extensions == null) {
      return null;
    }
    return extensions.getUnknownXMLObjects()
        .stream()
        .filter(e -> clazz.isAssignableFrom(e.getClass()))
        .map(clazz::cast)
        .findFirst()
        .orElse(null);
  }

  /**
   * Finds the first extension matching the supplied QName.
   *
   * @param extensions the Extensions to search
   * @param qname the QName to match
   * @return the matching extension or null
   */
  public static XMLObject getMetadataExtension(final Extensions extensions, final QName qname) {
    if (extensions == null) {
      return null;
    }
    return extensions.getUnknownXMLObjects(qname).stream().findFirst().orElse(null);
  }

  /**
   * Finds all extensions matching the supplied type.
   *
   * @param extensions the Extensions to search
   * @param clazz the extension type
   * @param <T> the type of the extension
   * @return a (possibly empty) list of extensions elements of the given type
   */
  public static <T> List<T> getMetadataExtensions(final Extensions extensions, final Class<T> clazz) {
    if (extensions == null) {
      return Collections.emptyList();
    }
    return extensions.getUnknownXMLObjects()
        .stream()
        .filter(e -> clazz.isAssignableFrom(e.getClass()))
        .map(clazz::cast)
        .collect(Collectors.toList());
  }

  /**
   * Finds all extensions matching the supplied QName.
   *
   * @param extensions the Extensions to search
   * @param qname the QName
   * @return a (possibly empty) list of extensions elements of the given type
   */
  public static List<XMLObject> getMetadataExtensions(final Extensions extensions, final QName qname) {
    if (extensions == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(extensions.getUnknownXMLObjects(qname));
  }

  /**
   * Utility that extracs certificates found under the KeyDescriptor elements of a metadata record.
   * <p>
   * If {@link UsageType#SIGNING} is supplied, the method will return all certificates with usage type signing, but also
   * those that does not have a usage. And the same goes for encryption.
   * </p>
   *
   * @param descriptor the SSO descriptor
   * @param usageType the requested usage type
   * @return a list of credentials
   */
  public static List<X509Credential> getMetadataCertificates(final SSODescriptor descriptor,
      final UsageType usageType) {
    final List<X509Credential> creds = new ArrayList<>();
    for (final KeyDescriptor kd : descriptor.getKeyDescriptors()) {
      if (usageType == kd.getUse() || kd.getUse() == null || UsageType.UNSPECIFIED == kd.getUse()) {
        if (kd.getKeyInfo() == null) {
          continue;
        }
        for (final X509Data xd : kd.getKeyInfo().getX509Datas()) {
          for (final org.opensaml.xmlsec.signature.X509Certificate cert : xd.getX509Certificates()) {
            try {
              creds.add(new BasicX509Credential((X509Certificate) certFactory.generateCertificate(
                  new ByteArrayInputStream(Base64.getDecoder().decode(cert.getValue())))));
            }
            catch (final Exception ignored) {
            }
          }
        }
      }
    }
    return creds;
  }

  /**
   * Returns a (possibly) empty list of {@code alg:DigestMethod} elements. "SAML v2.0 Metadata Profile for Algorithm
   * Support Version 1.0" states that elements found in the extension under the role descriptor has precedence over
   * those found under the entity descriptor extensions, and the sets should not be combined if both are present.
   *
   * @param ed the entity descriptor
   * @return a list of digest methods (may be empty)
   */
  public static List<DigestMethod> getDigestMethods(final EntityDescriptor ed) {
    final SSODescriptor descriptor = getSSODescriptor(ed);
    if (descriptor != null) {
      final List<DigestMethod> methods = getMetadataExtensions(descriptor.getExtensions(), DigestMethod.class);
      if (!methods.isEmpty()) {
        return methods;
      }
    }
    return getMetadataExtensions(ed.getExtensions(), DigestMethod.class);
  }

  /**
   * Returns a (possibly) empty list of {@code alg:SigningMethod} elements. "SAML v2.0 Metadata Profile for Algorithm
   * Support Version 1.0" states that elements found in the extension under the role descriptor has precedence over
   * those found under the entity descriptor extensions, and the sets should not be combined if both are present.
   *
   * @param ed the entity descriptor
   * @return a list of signing methods (may be empty)
   */
  public static List<SigningMethod> getSigningMethods(final EntityDescriptor ed) {
    final SSODescriptor descriptor = getSSODescriptor(ed);
    if (descriptor != null) {
      final List<SigningMethod> methods = getMetadataExtensions(descriptor.getExtensions(), SigningMethod.class);
      if (!methods.isEmpty()) {
        return methods;
      }
    }
    return getMetadataExtensions(ed.getExtensions(), SigningMethod.class);
  }

  /**
   * Extracts the string values found in the entity category (http://macedir.org/entity-category) attribute under a
   * EntityAttributes element found in the extensions element of the supplied entity descriptor.
   *
   * @param ed the entity descriptor
   * @return a (possible empty) list of entity category values
   */
  public static List<String> getEntityCategories(final EntityDescriptor ed) {
    final EntityAttributes attrs = getMetadataExtension(ed.getExtensions(), EntityAttributes.class);
    if (attrs == null) {
      return Collections.emptyList();
    }
    final List<String> entityCategories = new ArrayList<>();
    attrs.getAttributes().stream()
        .filter(a -> AttributeConstants.ENTITY_CATEGORY_ATTRIBUTE_NAME.equals(a.getName()))
        .forEach(a -> entityCategories.addAll(AttributeUtils.getAttributeStringValues(a)));

    return entityCategories;
  }

  /**
   * Extracts the string values found in the assurance certification
   * (urn:oasis:names:tc:SAML:attribute:assurance-certification) attribute under a EntityAttributes element found in the
   * extensions element of the supplied entity descriptor.
   *
   * @param ed the entity descriptor
   * @return a (possible empty) list of entity category values
   */
  public static List<String> getAssuranceCertificationUris(final EntityDescriptor ed) {
    final EntityAttributes attrs = getMetadataExtension(ed.getExtensions(), EntityAttributes.class);
    if (attrs == null) {
      return Collections.emptyList();
    }
    final List<String> assuranceCertificationUris = new ArrayList<>();
    attrs.getAttributes().stream()
        .filter(a -> AttributeConstants.ASSURANCE_CERTIFICATION_ATTRIBUTE_NAME.equals(a.getName()))
        .forEach(a -> assuranceCertificationUris.addAll(AttributeUtils.getAttributeStringValues(a)));

    return assuranceCertificationUris;
  }

  /**
   * Returns the SSODescriptor for the supplied SP or IdP entity descriptor.
   *
   * @param ed the entity descriptor
   * @return the SSODescriptor
   */
  public static SSODescriptor getSSODescriptor(final EntityDescriptor ed) {
    if (ed == null) {
      return null;
    }
    if (ed.getIDPSSODescriptor(SAMLConstants.SAML20P_NS) != null) {
      return ed.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
    }
    else {
      return ed.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
    }
  }

  // Hidden
  private EntityDescriptorUtils() {
  }

}
