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

import org.springframework.core.io.Resource;

/**
 * Test cases for the {@code FilesystemMetadataProvider}.
 * <p>
 * See {@link BaseMetadataProviderTest} for test cases.
 * </p>
 *
 * @author Martin Lindström (martin@idsec.se)
 */
public class FilesystemMetadataProviderTest extends BaseMetadataProviderTest {

  /** {@inheritDoc} */
  @Override
  protected AbstractMetadataProvider createMetadataProvider(final Resource resource) throws Exception {
    return new FilesystemMetadataProvider(resource.getFile());
  }

}
