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
package se.swedenconnect.opensaml.saml2.metadata.provider;

import net.shibboleth.shared.logic.Constraint;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;

/**
 * Function which examines an entity ID from supplied criteria and returns a metadata request URL for MDQ.
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class MDQRequestURLBuilder implements Function<CriteriaSet, String> {

  /** Logger. */
  private static final Logger log = LoggerFactory.getLogger(MDQRequestURLBuilder.class);

  /** The metadata base URL. */
  private String baseUrl;

  /**
   * Constructor.
   *
   * @param baseUrl the metadata base URL
   */
  public MDQRequestURLBuilder(final String baseUrl) {
    this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
    if (this.baseUrl.endsWith("/")) {
      this.baseUrl = this.baseUrl.substring(0, this.baseUrl.length() - 1);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String apply(@Nullable final CriteriaSet criteria) {
    Constraint.isNotNull(criteria, "Criteria was null");
    if (!criteria.contains(EntityIdCriterion.class)) {
      log.trace("Criteria did not contain entity ID, unable to build request URL");
      return null;
    }
    final String entityID = criteria.get(EntityIdCriterion.class).getEntityId();
    Constraint.isNotNull(entityID, "Entity ID was null");

    final String url =
        String.format("%s/entities/%s", this.baseUrl, URLEncoder.encode(entityID, StandardCharsets.UTF_8));
    log.debug("Returning request URL: {}", url);
    return url;
  }

}
