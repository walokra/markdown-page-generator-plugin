package com.ruleoftech.markdown.page.generator.plugin;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkType;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.RenderingPhase;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import static com.ruleoftech.markdown.page.generator.plugin.PageGeneratorExtension.INPUT_FILE_EXTENSIONS;
import static com.vladsch.flexmark.html.renderer.LinkType.LINK;

/**
 * Unit test for {@link FlexmarkLinkResolver}
 */
public class FlexmarkLinkResolverTest extends BetterAbstractMojoTestCase {

    public void testResolveLink() {
        // Given
        NodeRendererContext context = new NodeRendererContext() {
            @Override
            public Attributes extendRenderingNodeAttributes(AttributablePart part, Attributes attributes) {
                return null;
            }
            
            @Override
            public Attributes extendRenderingNodeAttributes(Node node, AttributablePart part, Attributes attributes) {
            	return null;
            }

            @Override
            public HtmlWriter getHtmlWriter() {
                return null;
            }
            
            @Override
            public NodeRendererContext getSubContext(boolean inheritIndent) {
            	return null;
            }
            
            @Override
            public NodeRendererContext getDelegatedSubContext(boolean inheritIndent) {
            	return null;
            }

            @Override
            public void delegateRender() {

            }

            @Override
            public String getNodeId(Node node) {
                return null;
            }

            @Override
            public boolean isDoNotRenderLinks() {
                return false;
            }

            @Override
            public void doNotRenderLinks(boolean doNotRenderLinks) {

            }

            @Override
            public void doNotRenderLinks() {

            }

            @Override
            public void doRenderLinks() {

            }

            @Override
            public RenderingPhase getRenderingPhase() {
                return null;
            }

            @Override
            public HtmlRendererOptions getHtmlOptions() {
                return null;
            }

            @Override
            public DataHolder getOptions() {
                MutableDataSet mutableDataSet = new MutableDataSet();
                mutableDataSet.set(INPUT_FILE_EXTENSIONS, " md, markdown ");
                return mutableDataSet;
            }

            @Override
            public Document getDocument() {
                return null;
            }

            @Override
            public String encodeUrl(CharSequence url) {
                return null;
            }

            @Override
            public void render(Node node) {

            }

            @Override
            public void renderChildren(Node parent) {

            }

            @Override
            public Node getCurrentNode() {
                return null;
            }

            @Override
            public ResolvedLink resolveLink(LinkType linkType, CharSequence url, Boolean urlEncode) {
                return null;
            }

            @Override
            public ResolvedLink resolveLink(LinkType linkType, CharSequence url, Attributes attributes, Boolean urlEncode) {
                return null;
            }
        };
        FlexmarkLinkResolver flexmarkLinkResolver = new FlexmarkLinkResolver(context);

        // When, Then
        assertEquals(flexmarkLinkResolver.resolveLink(null, context, new ResolvedLink(LINK, "test.md")).getUrl(), new ResolvedLink(LINK, "test.html").getUrl());
        assertEquals(flexmarkLinkResolver.resolveLink(null, context, new ResolvedLink(LINK, "test.markdown")).getUrl(), new ResolvedLink(LINK, "test.html").getUrl());
        assertEquals(flexmarkLinkResolver.resolveLink(null, context, new ResolvedLink(LINK, "test.md#")).getUrl(), new ResolvedLink(LINK, "test.html#").getUrl());
        assertEquals(flexmarkLinkResolver.resolveLink(null, context, new ResolvedLink(LINK, "test.markdown#")).getUrl(), new ResolvedLink(LINK, "test.html#").getUrl());
    }
}
