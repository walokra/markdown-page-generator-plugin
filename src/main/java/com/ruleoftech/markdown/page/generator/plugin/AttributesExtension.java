package com.ruleoftech.markdown.page.generator.plugin;

import com.vladsch.flexmark.util.misc.Extension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.html.MutableAttributes;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import java.util.Map;

public class AttributesExtension implements HtmlRenderer.HtmlRendererExtension {
    final static public DataKey<Map<String, MutableAttributes>> ATTRIBUTE_MAP = new DataKey<>("ATTRIBUTE_MAP", (Map<String, MutableAttributes>) null);

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
