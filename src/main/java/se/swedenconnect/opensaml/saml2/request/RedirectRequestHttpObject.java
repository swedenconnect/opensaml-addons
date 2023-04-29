/*
 * Copyright 2016-2023 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.request;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.impl.BasicSignatureSigningParametersResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import se.swedenconnect.opensaml.xmlsec.signature.support.SAMLObjectSigner;

/**
 * A RequestHttpObject for sending using HTTP GET (redirect binding).
 * <p>
 * If signature credentials are supplied when creating the object the request will be signed.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 *
 * @param <T> the type of the request
 */
public class RedirectRequestHttpObject<T extends RequestAbstractType> extends HTTPRedirectDeflateEncoder
    implements RequestHttpObject<T> {

  /** Logging instance. */
  private static final Logger logger = LoggerFactory.getLogger(RedirectRequestHttpObject.class);

  /** The request. */
  private T request;

  /** The URL to redirect to. */
  private String sendUrl;

  /** The destination URL. */
  private String destinationUrl;

  /** HTTP headers. */
  private Map<String, String> httpHeaders = new HashMap<>();

  /** The query parameters. */
  private Map<String, String> requestParameters = null;

  /**
   * Constructor that puts together the resulting object.
   * 
   * @param request the request object
   * @param relayState the relay state
   * @param signatureCredentials optional signature credentials
   * @param endpoint the endpoint where we send this request to
   * @param recipientMetadata the recipient metadata (may be null)
   * @throws MessageEncodingException for encoding errors
   * @throws SignatureException for signature errors
   */
  public RedirectRequestHttpObject(final T request, final String relayState, final X509Credential signatureCredentials,
      final String endpoint, final EntityDescriptor recipientMetadata)
      throws MessageEncodingException, SignatureException {

    this(request, relayState, signatureCredentials, endpoint, recipientMetadata, null);
  }

  /**
   * Constructor that puts together the resulting object.
   * 
   * @param request the request object
   * @param relayState the relay state
   * @param signatureCredentials optional signature credentials
   * @param endpoint the endpoint where we send this request to
   * @param recipientMetadata the recipient metadata (may be null)
   * @param defaultSignatureSigningConfiguration the default signature configuration for the application. If null, the
   *          value returned from {@link SecurityConfigurationSupport#getGlobalSignatureSigningConfiguration()} will be
   *          used
   * @throws MessageEncodingException for encoding errors
   * @throws SignatureException for signature errors
   */
  public RedirectRequestHttpObject(final T request, final String relayState, final X509Credential signatureCredentials,
      final String endpoint, final EntityDescriptor recipientMetadata,
      final SignatureSigningConfiguration defaultSignatureSigningConfiguration)
      throws MessageEncodingException, SignatureException {

    this.request = request;

    // Set up the message context.
    //
    final MessageContext messageContext = new MessageContext();
    messageContext.setMessage(request);
    messageContext.getSubcontext(SAMLBindingContext.class, true).setRelayState(relayState);

    if (signatureCredentials != null) {

      // Check if the recipient has specified any signature preferences in its metadata.
      final SignatureSigningConfiguration peerConfig = SAMLObjectSigner.getSignaturePreferences(recipientMetadata);

      final SignatureSigningConfiguration[] configs =
          new SignatureSigningConfiguration[2 + (peerConfig != null ? 1 : 0)];
      int pos = 0;
      if (peerConfig != null) {
        configs[pos++] = peerConfig;
      }
      // The system wide configuration for signing.
      configs[pos++] = defaultSignatureSigningConfiguration != null
          ? defaultSignatureSigningConfiguration
          : SecurityConfigurationSupport.getGlobalSignatureSigningConfiguration();

      // And finally our signing credential.
      final BasicSignatureSigningConfiguration signatureCreds = new BasicSignatureSigningConfiguration();
      signatureCreds.setSigningCredentials(Collections.singletonList(signatureCredentials));
      configs[pos] = signatureCreds;

      final BasicSignatureSigningParametersResolver signatureParametersResolver =
          new BasicSignatureSigningParametersResolver();
      final CriteriaSet criteriaSet = new CriteriaSet(new SignatureSigningConfigurationCriterion(configs));

      try {
        final SignatureSigningParameters parameters = signatureParametersResolver.resolveSingle(criteriaSet);
        messageContext.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(parameters);
      }
      catch (ResolverException e) {
        throw new SignatureException(e);
      }
    }

    // Put together the message.
    //
    this.removeSignature(this.request);
    final String encodedMessage = this.deflateAndBase64Encode(this.request);
    this.sendUrl = this.buildRedirectURL(messageContext, endpoint, encodedMessage);
    this.destinationUrl = endpoint;

    logger.trace("Redirect URL is {}", this.sendUrl);

    this.httpHeaders.put("Cache-control", "no-cache, no-store");
    this.httpHeaders.put("Pragma", "no-cache");
    // TODO: UTF-8 character encoding
  }

  /** {@inheritDoc} */
  @Override
  public String getSendUrl() {
    return this.sendUrl;
  }

  /** {@inheritDoc} */
  @Override
  public String getDestinationUrl() {
    return this.destinationUrl;
  }

  /** {@inheritDoc} */
  @Override
  public String getMethod() {
    return SAMLConstants.GET_METHOD;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getRequestParameters() {
    if (this.requestParameters == null) {
      try {
        final URLBuilder urlBuilder = new URLBuilder(this.getSendUrl());
        final List<Pair<String, String>> queryParams = urlBuilder.getQueryParams();
        this.requestParameters = queryParams.stream()
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      }
      catch (final MalformedURLException e) {
        throw new RuntimeException("Invalid endpoint", e);
      }      
    }
    return this.requestParameters;
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getHttpHeaders() {
    return this.httpHeaders;
  }

  /** {@inheritDoc} */
  @Override
  public T getRequest() {
    return this.request;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("request-type='%s', sendUrl='%s', httpHeaders=%s", this.request.getClass().getSimpleName(),
        this.sendUrl,
        this.httpHeaders);
  }

}
