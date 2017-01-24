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
import com.vladsch.flexmark.ext.wikilink.internal.WikiLinkLinkResolver;
import com.vladsch.flexmark.html.IndependentLinkResolverFactory;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.options.DataHolder;

import java.util.HashSet;
import java.util.Set;

public class FlexmarkLinkResolver implements LinkResolver {
    final String inputFileExtension;

    public FlexmarkLinkResolver(NodeRendererContext context) {
        DataHolder options = context.getOptions();
        this.inputFileExtension = options.get(PageGeneratorExtension.INPUT_FILE_EXTENSION);
    }

    @Override
    public ResolvedLink resolveLink(Node node, NodeRendererContext context, ResolvedLink link) {
        ResolvedLink result = link;

        if (link.getLinkType() == LinkType.LINK) {
            String url = link.getUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (url.endsWith("." + inputFileExtension)) {
                    url = url.substring(0, url.length() - inputFileExtension.length()) + "html";
                } else if (url.contains("." + inputFileExtension + "#")) {
                    url = url.replace("." + inputFileExtension + "#", ".html#");
                }
                result = result.withStatus(LinkStatus.VALID).withUrl(url);
            }
            return result;
        }

        return result;
    }

    public static class Factory extends IndependentLinkResolverFactory {
        @Override
        public Set<Class<? extends LinkResolverFactory>> getBeforeDependents() {
            Set<Class<? extends LinkResolverFactory>> set = new HashSet<Class<? extends LinkResolverFactory>>();
            set.add(WikiLinkLinkResolver.Factory.class);
            return set;
        }

        @Override
        public LinkResolver create(NodeRendererContext context) {
            return new FlexmarkLinkResolver(context);
        }
    }
}
