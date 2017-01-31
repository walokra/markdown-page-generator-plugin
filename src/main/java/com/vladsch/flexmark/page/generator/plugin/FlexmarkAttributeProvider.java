/*
 * Copyright (c) 2015-2016 Vladimir Schneider <vladimir.schneider@gmail.com>, all rights reserved.
 *
 * This code is private property of the copyright holder and cannot be used without
 * having obtained a license or prior written permission of the of the copyright holder.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package com.vladsch.flexmark.page.generator.plugin;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.options.DataHolder;

import java.util.Map;

public class FlexmarkAttributeProvider implements AttributeProvider {

    final protected Map<String, Attributes> attributeMap;

    public FlexmarkAttributeProvider(NodeRendererContext context) {
        DataHolder options = context.getOptions();
        attributeMap = options.get(AttributesExtension.ATTRIBUTE_MAP);
    }

    @Override
    public void setAttributes(Node node, AttributablePart part, Attributes attributes) {
        if (attributeMap != null) {
            Attributes attributes1 = attributeMap.get(node.getClass().getSimpleName());
            if (attributes1 != null) {
                attributes.replaceValues(attributes1);
            }
        }
    }

    public static class Factory extends IndependentAttributeProviderFactory {
        @Override
        public AttributeProvider create(NodeRendererContext context) {
            return new FlexmarkAttributeProvider(context);
        }
    }
}
