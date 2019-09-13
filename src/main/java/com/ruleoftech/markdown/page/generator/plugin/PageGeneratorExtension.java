package com.ruleoftech.markdown.page.generator.plugin;

import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class PageGeneratorExtension implements HtmlRenderer.HtmlRendererExtension {
    final static public DataKey<String> INPUT_FILE_EXTENSIONS = new DataKey<String>("INPUT_FILE_EXTENSIONS", "md");

    @Override
    public void rendererOptions(final MutableDataHolder options) {

    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
        rendererBuilder.linkResolverFactory(new FlexmarkLinkResolver.Factory());
    }

    public static Extension create() {
        return new PageGeneratorExtension();
    }
}
