package com.ruleoftech.markdown.page.generator.plugin;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.wikilink.internal.WikiLinkLinkResolver;
import com.vladsch.flexmark.html.IndependentLinkResolverFactory;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.options.DataHolder;

import java.util.HashSet;
import java.util.Set;

public class FlexmarkLinkResolver implements LinkResolver {
    final String inputFileExtension;

    public FlexmarkLinkResolver(LinkResolverContext context) {
        DataHolder options = context.getOptions();
        this.inputFileExtension = options.get(PageGeneratorExtension.INPUT_FILE_EXTENSION);
    }

    @Override
    public ResolvedLink resolveLink(Node node, LinkResolverContext context, ResolvedLink link) {
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
        public LinkResolver create(LinkResolverContext context) {
            return new FlexmarkLinkResolver(context);
        }
    }
}
