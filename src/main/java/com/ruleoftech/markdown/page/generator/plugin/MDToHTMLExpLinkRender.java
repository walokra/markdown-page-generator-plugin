package com.ruleoftech.markdown.page.generator.plugin;

import static org.pegdown.FastEncoder.encode;

import org.parboiled.common.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ast.ExpLinkNode;

public class MDToHTMLExpLinkRender extends LinkRenderer {

    @Override
    public Rendering render(ExpLinkNode node, String text) {
        String url = node.url;
        if (!url.startsWith("http://") && !url.startsWith("https://") && url.endsWith(".md")) {
            url = url.substring(0, url.length() - 2) + "html";
        }
        Rendering rendering = new Rendering(url, text);
        return StringUtils.isEmpty(node.title) ? rendering : rendering.withAttribute("title", encode(node.title));
    }

}
