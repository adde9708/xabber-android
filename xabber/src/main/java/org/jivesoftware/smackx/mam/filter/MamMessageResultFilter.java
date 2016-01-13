/**
 * Copyright � 2015 Florian Schmaus
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.mam.filter;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.mam.packet.MamPacket.AbstractMamExtension;
import org.jivesoftware.smackx.mam.packet.MamPacket.MamResultExtension;
import org.jivesoftware.smackx.mam.packet.MamQueryIQ;

public class MamMessageResultFilter extends AbstractMamMessageExtensionFilter {

    public MamMessageResultFilter(MamQueryIQ mamQueryIQ) {
        super(mamQueryIQ);
    }

    @Override
    protected AbstractMamExtension getMamExtension(Message message) {
        return MamResultExtension.from(message);
    }

}
