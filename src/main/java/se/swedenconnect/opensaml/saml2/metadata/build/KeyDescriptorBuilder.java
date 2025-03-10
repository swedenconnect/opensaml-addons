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

import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.saml2.metadata.EncryptionMethod;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.KeyName;
import org.opensaml.xmlsec.signature.X509Data;
import se.swedenconnect.opensaml.common.builder.AbstractSAMLObjectBuilder;

import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * A builder for {@code KeyDescriptor} elements.
 * <p>
 * This builder only supports a subset of the possible elements of a key descriptor, but should be sufficient for most
 * cases.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class KeyDescriptorBuilder extends AbstractSAMLObjectBuilder<KeyDescriptor> {

  /**
   * Utility method that creates a builder.
   *
   * @return a builder
   */
  public static KeyDescriptorBuilder builder() {
    return new KeyDescriptorBuilder();
  }

  /** {@inheritDoc} */
  @Override
  protected Class<KeyDescriptor> getObjectType() {
    return KeyDescriptor.class;
  }

  /**
   * Assigns the usage type for the key descriptor.
   *
   * @param usageType the usage type
   * @return the builder
   */
  public KeyDescriptorBuilder use(final UsageType usageType) {
    if (UsageType.UNSPECIFIED == usageType) {
      this.object().setUse(null);
    }
    else {
      this.object().setUse(usageType);
    }
    return this;
  }

  /**
   * Assigns the key name of the {@code KeyInfo} element within the key descriptor.
   *
   * @param name the key name
   * @return the builder
   */
  public KeyDescriptorBuilder keyName(final String name) {
    if (name == null) {
      if (this.object().getKeyInfo() != null && !this.object().getKeyInfo().getKeyNames().isEmpty()) {
        this.object().getKeyInfo().getKeyNames().clear();
      }
    }
    if (this.object().getKeyInfo() == null) {
      this.object().setKeyInfo((KeyInfo) XMLObjectSupport.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
    }
    this.object().getKeyInfo().getKeyNames().clear();
    final KeyName keyName = (KeyName) XMLObjectSupport.buildXMLObject(KeyName.DEFAULT_ELEMENT_NAME);
    keyName.setValue(name);
    this.object().getKeyInfo().getKeyNames().add(keyName);
    return this;
  }

  /**
   * Assigns a certificate to be used as an X.509 data element of the {@code KeyInfo} element within the key
   * descriptor.
   *
   * @param certificate the certificate
   * @return the builder
   */
  public KeyDescriptorBuilder certificate(final X509Certificate certificate) {
    try {
      return this.certificate(
          certificate != null ? Base64.getEncoder().encodeToString(certificate.getEncoded()) : null);
    }
    catch (final CertificateEncodingException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Assigns an input stream to a certificate resource that is to be used as an X.509 data element of the
   * {@code KeyInfo} element within the key descriptor.
   *
   * @param certificate the certificate resource
   * @return the builder
   */
  public KeyDescriptorBuilder certificate(final InputStream certificate) {
    try {
      return this.certificate(
          certificate != null ? Base64.getEncoder().encodeToString(
              CertificateFactory.getInstance("X.509").generateCertificate(certificate).getEncoded()) : null);
    }
    catch (final CertificateException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Assigns a certificate (in Base64-encoded format) to be used as an X.509 data element of the {@code KeyInfo} element
   * within the key descriptor.
   *
   * @param base64Encoding the base64 encoding (note: not PEM-format)
   * @return the builder
   */
  public KeyDescriptorBuilder certificate(final String base64Encoding) {
    if (base64Encoding == null) {
      if (this.object().getKeyInfo() != null && !this.object().getKeyInfo().getX509Datas().isEmpty()) {
        this.object().getKeyInfo().getX509Datas().clear();
      }
    }
    if (this.object().getKeyInfo() == null) {
      this.object().setKeyInfo((KeyInfo) XMLObjectSupport.buildXMLObject(KeyInfo.DEFAULT_ELEMENT_NAME));
    }
    this.object().getKeyInfo().getX509Datas().clear();
    final X509Data x509Data = (X509Data) XMLObjectSupport.buildXMLObject(X509Data.DEFAULT_ELEMENT_NAME);
    final org.opensaml.xmlsec.signature.X509Certificate cert =
        (org.opensaml.xmlsec.signature.X509Certificate) XMLObjectSupport
            .buildXMLObject(org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
    cert.setValue(base64Encoding);
    x509Data.getX509Certificates().add(cert);
    this.object().getKeyInfo().getX509Datas().add(x509Data);
    return this;
  }

  /**
   * Assigns a certificate in OpenSAML credential format to be used as an X.509 data element of the {@code KeyInfo}
   * element within the key descriptor.
   *
   * @param credential the credential
   * @return the builder
   */
  public KeyDescriptorBuilder certificate(final X509Credential credential) {
    return this.certificate(credential != null ? credential.getEntityCertificate() : null);
  }

  /**
   * Assigns a list of encryption methods.
   * <p>
   * Note: the method only accepts algorithm URI:s. If you need to assign other parts of an {@code EncryptionMethod}
   * object you must use {@link #encryptionMethodsExt(List)}.
   * </p>
   *
   * @param algorithms list of algorithms
   * @return the builder
   */
  public KeyDescriptorBuilder encryptionMethods(final List<String> algorithms) {
    if (algorithms != null && !algorithms.isEmpty()) {
      for (final String algo : algorithms) {
        final EncryptionMethod method =
            (EncryptionMethod) XMLObjectSupport.buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
        method.setAlgorithm(algo);
        this.object().getEncryptionMethods().add(method);
      }
    }
    else {
      this.object().getEncryptionMethods().clear();
    }
    return this;
  }

  /**
   * See {@link #encryptionMethods(List)}.
   *
   * @param algorithms list of algorithms
   * @return the builder
   */
  public KeyDescriptorBuilder encryptionMethods(final String... algorithms) {
    return this.encryptionMethods(algorithms != null ? Arrays.asList(algorithms) : null);
  }

  /**
   * Assigns a list of encryption methods.
   *
   * @param algorithms ordered list of encryption methods
   * @return the builder
   */
  public KeyDescriptorBuilder encryptionMethodsExt(final List<EncryptionMethod> algorithms) {
    if (algorithms != null && !algorithms.isEmpty()) {
      for (final EncryptionMethod em : algorithms) {
        try {
          if (em != null) {
            this.object().getEncryptionMethods().add(XMLObjectSupport.cloneXMLObject(em));
          }
        }
        catch (final MarshallingException | UnmarshallingException e) {
          throw new RuntimeException(e);
        }
      }
    }
    else {
      this.object().getEncryptionMethods().clear();
    }
    return this;
  }

  /**
   * See {@link #encryptionMethodsExt(List)}.
   *
   * @param algorithms ordered list of encryption methods
   * @return the builder
   */
  public KeyDescriptorBuilder encryptionMethods(final EncryptionMethod... algorithms) {
    return this.encryptionMethodsExt(algorithms != null ? Arrays.asList(algorithms) : null);
  }

}
