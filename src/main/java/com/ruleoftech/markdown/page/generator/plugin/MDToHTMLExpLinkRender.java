package com.ruleoftech.markdown.page.generator.plugin;

import static org.pegdown.FastEncoder.encode;

import org.parboiled.common.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ast.ExpLinkNode;

public class MDToHTMLExpLinkRender extends LinkRenderer {

    private String inputFileExtension = "md";

    MDToHTMLExpLinkRender(){
        super();
    }
    
    MDToHTMLExpLinkRender(String inputFileExtension){
        super();
        this.inputFileExtension = inputFileExtension;
    }
    
    @Override
    public Rendering render(ExpLinkNode node, String text) {
        String url = node.url;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if(url.endsWith("."+inputFileExtension)){
                url = url.substring(0, url.length() - inputFileExtension.length()) + "html";
            }else if(url.contains("."+inputFileExtension+"#")){
                url = url.replace("."+inputFileExtension+"#", ".html#");
            }
        }
        Rendering rendering = new Rendering(url, text);
        return StringUtils.isEmpty(node.title) ? rendering : rendering.withAttribute("title", encode(node.title));
    }
}
