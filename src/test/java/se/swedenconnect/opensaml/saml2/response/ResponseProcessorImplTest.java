/*
 * Copyright 2016-2026 Sweden Connect
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
package se.swedenconnect.opensaml.saml2.response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import se.swedenconnect.opensaml.OpenSAMLTestBase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Test cases for {@link ResponseProcessorImpl}.
 *
 * @author Martin Lindström
 */
class ResponseProcessorImplTest extends OpenSAMLTestBase {

  @Test
  void testDecodeNotResponse() {
    final String xml = "<samlp:LogoutResponse xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
        + "ID=\"_abc\" Version=\"2.0\" IssueInstant=\"2026-01-01T00:00:00Z\"/>";
    final String encoded = Base64.getEncoder().encodeToString(xml.getBytes(StandardCharsets.UTF_8));

    final ResponseProcessorImpl processor = new ResponseProcessorImpl();
    Assertions.assertThrows(ResponseProcessingException.class, () -> processor.decodeResponse(encoded));
  }

}
