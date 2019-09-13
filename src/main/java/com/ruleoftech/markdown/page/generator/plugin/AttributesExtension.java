package com.ruleoftech.markdown.page.generator.plugin;

import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import java.util.Map;

public class AttributesExtension implements HtmlRenderer.HtmlRendererExtension {
    final static public DataKey<Map<String, Attributes>> ATTRIBUTE_MAP = new DataKey<>("ATTRIBUTE_MAP", (Map<String, Attributes>) null);

    @Override
    public void rendererOptions(final MutableDataHolder options) {

    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder, String rendererType) {
        rendererBuilder.attributeProviderFactory(new FlexmarkAttributeProvider.Factory());
    }

    public static Extension create() {
        return new AttributesExtension();
    }
}
