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
package se.swedenconnect.opensaml.saml2.response.replay;

import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.storage.ReplayCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

/**
 * Message replay checker implementation using OpenSAML's {@link ReplayCache} as an underlying cache.
 *
 * @author Martin Lindström (martin.lindstrom@litsec.se)
 */
public class MessageReplayCheckerImpl implements MessageReplayChecker {

  /** Logging instance. */
  private static final Logger log = LoggerFactory.getLogger(MessageReplayCheckerImpl.class);

  /** The replay cache. */
  private ReplayCache replayCache;

  /** Number of milliseconds to keep elements in the replay cache - default is 5 minutes. */
  private long replayCacheExpiration = 300 * 1000L;

  /** The name of the replay cache. */
  private String replayCacheName;

  /**
   * Constructor.
   *
   * @param replayCache the OpenSAML {@link ReplayCache} object to use
   * @param replayCacheName the name of the replay cache
   */
  public MessageReplayCheckerImpl(final ReplayCache replayCache, final String replayCacheName) {
    this.replayCache = Optional.ofNullable(replayCache)
        .orElseThrow(() -> new IllegalArgumentException("replayCache must not be null"));
    this.replayCacheName = Optional.ofNullable(replayCacheName)
        .orElseThrow(() -> new IllegalArgumentException("replayCacheName must not be null"));
  }

  /** {@inheritDoc} */
  @Override
  public void checkReplay(final String id) throws MessageReplayException {
    if (!this.replayCache.check(this.replayCacheName, id,
        Instant.ofEpochMilli(this.replayCacheExpiration + System.currentTimeMillis()))) {
      final String msg = String.format("Replay check of ID '%s' failed", id);
      log.warn(msg);
      throw new MessageReplayException(msg);
    }
    log.debug("Message replay check of ID '{}' succeeded", id);
  }

  /** {@inheritDoc} */
  @Override
  public void checkReplay(final SAMLObject object) throws MessageReplayException, IllegalArgumentException {
    String id = null;
    if (object instanceof Response) {
      id = ((Response) object).getID();
    }
    else if (object instanceof Assertion) {
      id = ((Assertion) object).getID();
    }
    else if (object instanceof RequestAbstractType) {
      id = ((RequestAbstractType) object).getID();
    }
    if (id == null) {
      throw new IllegalArgumentException("Unsupported object type");
    }
    this.checkReplay(id);
  }

  /**
   * Assigns the replay cache to use when checking against replay attacks.
   *
   * @param replayCache the cache
   */
  public void setReplayCache(final ReplayCache replayCache) {
    this.replayCache = replayCache;
  }

  /**
   * Assigns the name of the replay cache.
   *
   * @param replayCacheName the name
   */
  public void setReplayCacheName(final String replayCacheName) {
    this.replayCacheName = replayCacheName;
  }

  /**
   * Assigns the number of milliseconds each stored ID should be kept in the cache. The default is 5 minutes.
   *
   * @param replayCacheExpiration number of millis
   */
  public void setReplayCacheExpiration(final long replayCacheExpiration) {
    if (replayCacheExpiration < 0) {
      throw new IllegalArgumentException("replayCacheExpiration must be greater than 0");
    }
    this.replayCacheExpiration = replayCacheExpiration;
  }

}
