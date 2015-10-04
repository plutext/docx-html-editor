/*
    This file is part of docx-html-editor.

	docx-html-editor is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.plutext.htmleditor;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.capaxit.imagegenerator.Margin;
import org.capaxit.imagegenerator.TextImage;
import org.capaxit.imagegenerator.imagecallbacks.BackgroundColorCallback;
import org.capaxit.imagegenerator.imageexporter.ImageType;
import org.capaxit.imagegenerator.imageexporter.ImageWriter;
import org.capaxit.imagegenerator.imageexporter.ImageWriterFactory;
import org.capaxit.imagegenerator.impl.TextImageImpl;
import org.capaxit.imagegenerator.textalign.GreedyTextWrapper;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.HTMLConversionContext;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.vml.CTImageData;
import org.docx4j.wml.R;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/** 
 * Paragraph content (eg sectPr or oMathPara) can be preserved,
 * as can run content (most stuff is preserved at this level).
 * 
 * Preserved structures are stored in a hashmap, and allocated
 * an ID.  
 * 
 * Reinstatement is then just a matter of matching the ID
 * on the non-editable content item image, with an entry in the 
 * hashmap.
 */
public class LongTailHelper {
	
	private static final Logger jul = Logger.getLogger(LongTailHelper.class.getName());

	private final static AtomicInteger counter = new AtomicInteger();
	
	private static String getId() {
		return Editor.APP_KEY+"fcb"+counter.getAndIncrement();
		// since we store this in the pkg user data, its nice to use a 'namespace'
	}
	
    /**
     * Takes a paragraph, or some specific object (eg a footnote reference),
     * and returns an image which will represent it for round trip purposes. 
     * 
     * @param context
     * @param nodeIt
     * @param objectCount
     * @param frozenContentTypeHint
     * @return
     */
    public static DocumentFragment createFrozenContentBlock( 
    		HTMLConversionContext context,
    		NodeIterator nodeIt,
    		int objectCount,
    		String frozenContentTypeHint, HttpServletResponse response) {	
    	
    	/* 
					self::v:textbox
										or self::v:imagedata
										or self::w:control
										or self::a:blip
										or self::dgm:relIds
										or self::w:pict
										or self::w:object
										or self::c:chart
										or self::m:oMathPara

										or self::w:sectPr
										or self::o:signatureline
										
										    	 */
    	
        try {
        	
//        	context.getLog().info("objectCount " + objectCount);
//        	jul.info("objectCount " + objectCount);
        	
        	Object jaxbEl = null;
        	if (nodeIt!=null) { //It is never null
        		Node n = nodeIt.nextNode();
        		if (n!=null) {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			jaxbEl = u.unmarshal(n);
        		}
        	}
        	
        	// Store the frozen object in the pkg
        	// (this is easier than subclassing HTMLSettings, passing the storage map through the XSLT etc)
        	String id = getId();
        	context.getWmlPackage().setUserData(id, jaxbEl);
        	
//        	System.out.println(id + " --> \n" + XmlUtils.marshaltoString(jaxbEl, true, true));	
        	
        	        	
        	// Get the text content of wmlP
        	StringWriter sw = new StringWriter();
        	TextUtils.extractText(jaxbEl, sw);
        	String textContent = sw.toString().trim();
        	
        	if (objectCount==1) {
        		
        		if (frozenContentTypeHint.equals("sectPr")) {
        			return staticPlaceholder("sectPr.png", id, true);
        		} 

        		if (frozenContentTypeHint.equals("br")) {
        			// TODO - handle other break types
        			return staticPlaceholder("page_break.png", id, true);
        		} 

        		if (frozenContentTypeHint.equals("tab")) {
        			return staticPlaceholder("tab.png", id, false);
        		} 
        		
        		if (frozenContentTypeHint.equals("signatureline")) {
        			return staticPlaceholder("signature.png", id, true);
        		} 

        		if (frozenContentTypeHint.equals("chart")) {
        			return staticPlaceholder("chart.png", id, true);
        		} 

        		if (frozenContentTypeHint.equals("oMathPara")) {
        			return staticPlaceholder("equation.png", id, true);
        		} 

        		if (frozenContentTypeHint.equals("bookmarkStart")) {
        			return staticPlaceholder("bookmark_start.png", id, false);
        		} 

        		if (frozenContentTypeHint.equals("bookmarkEnd")) {
        			return staticPlaceholder("bookmark_end.png", id, false);
        		} 

        		if (frozenContentTypeHint.equals("commentRangeStart")) {
        			return staticPlaceholder("comment_range_start.png", id, false);
        		} 

        		if (frozenContentTypeHint.equals("commentRangeEnd")) {
        			return staticPlaceholder("comment_range_end.png", id, false);
        		} 

        		if (frozenContentTypeHint.equals("commentReference")) {
        			return staticPlaceholder("comment_ref.png", id, false);
        		} 

        		if (frozenContentTypeHint.equals("footnoteReference")) {
        			return staticPlaceholder("footnote.png", id, false);
        		} 

        		if (frozenContentTypeHint.equals("endnoteReference")) {
        			return staticPlaceholder("endnote.png", id, false);
        		} 
        		
        		if (jaxbEl instanceof R) {
        			
        			R p = (R)jaxbEl;
        		
	        		// Handle w:drawing image
	        		BlipFinder bf = new BlipFinder();
	        		new TraversalUtil(p.getContent(), bf);

        			SessionImageHandler sih = (SessionImageHandler)context.getImageHandler();
	        		sih.setResponse(response);
	        		
	        		if (bf.blips.size()==1) {
	        			// handle an ordinary image - WordXmlPictureE20
	        			DocumentFragment createHtmlImgE20 = WordXmlPictureE20.createHtmlImgE20(context, bf.anchorOrInline, id);
	        			return createHtmlImgE20;
	        			
	        		} else {
	        			
	        			// Handle w:pict//v:imagedata
	        			ShapeFinder sf = new ShapeFinder();
	            		new TraversalUtil(p.getContent(), sf);
	        			
	            		if (sf.shapeRefs.size()==1) {
	            			// handle VML image
	            			DocumentFragment createHtmlImgE10 = WordXmlPictureE10.createHtmlImgE10(context, sf.pict, id);
	            			return createHtmlImgE10;
	            		}
	        		}
        		}
        	}

        	// Otherwise, its not just a simple image
			if (context.getLog().isDebugEnabled() && jaxbEl!=null) {					
				context.getLog().debug(XmlUtils.marshaltoString(jaxbEl, true, true));					
			}		
			
			
        	// this is where we generate a placeholder image
        	HttpSession session = (HttpSession)context.getWmlPackage().getUserData("HttpSession");
        	String url = createPlaceholder(session, id, frozenContentTypeHint, textContent);
        	
            // Create a DOM document to take the results			
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
			Document document = factory.newDocumentBuilder().newDocument();			
//			Element xhtmlBlock = document.createElement("p");			
//			document.appendChild(xhtmlBlock);

			Element xhtmlBlock = document.createElement("img");			
			document.appendChild(xhtmlBlock);
			
			xhtmlBlock.setAttribute("src", response.encodeURL(url));
			xhtmlBlock.setAttribute("id", id);
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			context.getLog().error(e.getMessage(), e);
		} 
    	
    	return null;
    }
    
    private static DocumentFragment staticPlaceholder(String imageName, String id, boolean block) {
    	
    	String url = Editor.getContextPath()+"/placeholders/" + imageName;

        // Create a DOM document to take the results			
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
		Document document=null;
		try {
			document = factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
//		Element xhtmlBlock = document.createElement("p");			
//		document.appendChild(xhtmlBlock);

		Element xhtmlBlock = document.createElement("img");			
		document.appendChild(xhtmlBlock);
		
		xhtmlBlock.setAttribute("src", url);
		xhtmlBlock.setAttribute("id", id);
		if(block) {
			xhtmlBlock.setAttribute("style", "display:block");			
		}
		
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());

		return docfrag;
    	
    }
    
	private static String createPlaceholder(HttpSession session, String id, String frozenContentTypeHint, String textContent) throws Exception {
		
		// There's another library https://code.google.com/p/litetext/
		// which I haven't tried, developed for Google App Engine use where AWT and BufferedImage et al do not exist.
		
		TextImage textImage;
		if (frozenContentTypeHint.equals("textbox") ) {
			
			if (textContent.length()==0) {
				textContent="[empty text box]";
			}
			
			int rows = (int) Math.ceil((double)textContent.length()/40); // 40 chars per row
			int height = rows * 16;
			
			textImage = new TextImageImpl(300, height, new Margin(0, 0));
			textImage.useTextWrapper(new GreedyTextWrapper() );
			textImage.wrap(true);
			
	        textImage.performAction(new BackgroundColorCallback(Color.LIGHT_GRAY, Color.BLACK, textImage));
			
			textImage.write(textContent);
			
		} else {
		
			// 1. create a new TextImageImpl with a size of 300x300 pixels
			// and a left and top margin of 5 pixels. The default font is SansSerif,
			// PLAIN,12
			textImage = new TextImageImpl(100, 20, new Margin(0, 0));
			
	//		String tag = id.substring(id.indexOf(":")+1);
	
			// 2. These methods add text and a newline
	//		textImage.writeLine("[" + tag + "]");
			textImage.writeLine("[" + frozenContentTypeHint + "]");
			
		}
		
		// 3. Add explicit newlines. All methods can be chained for convenience.
//		textImage.newLine().newLine();
//		textImage.withFontStyle(Style.UNDERLINED).write("Hello world!");

        ImageWriter imageWriter = ImageWriterFactory.getImageWriter(ImageType.PNG);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
//        imageWriter.writeImageToFile(textImage, new File("simple.png"));
        imageWriter.writeImageToOutputStream(textImage, baos);
        
//		session.setAttribute(id, baos.toByteArray());
		SessionImageHandler.getImageMap(session).put(id, baos.toByteArray());
		
		
		return Editor.getContextPath() + "/services/image/" + id;
        
        
	}    
    
    static class BlipFinder extends CallbackImpl {
    	
    	Object anchorOrInline; // BlipFinder is only useful to us when there is a single element in the List, so this is OK 
		
    	List<CTBlip> blips = new ArrayList<CTBlip>();  
    	
    	@Override
		public List<Object> apply(Object o) {

			if (o instanceof Inline) {
				anchorOrInline = o;				
			} else if (o instanceof Anchor) {
				anchorOrInline = o;				
			} else if (o instanceof CTBlip) {
				blips.add((CTBlip)o);
			}
			
			return null;
		}
	}
    
    static class ShapeFinder extends CallbackImpl {
    	
    	org.docx4j.wml.Pict pict;		// ShapeFinder is only useful to us when there is a single element in the List, so this is OK
    	List<CTImageData> shapeRefs = new ArrayList<CTImageData>();  
    	    	
    	@Override
		public List<Object> apply(Object o) {
			
    		if (o instanceof org.docx4j.wml.Pict) {
    			this.pict = (org.docx4j.wml.Pict)o;
    		} else if (o instanceof org.docx4j.vml.CTImageData) {
				CTImageData imageData = (org.docx4j.vml.CTImageData)o; 
				shapeRefs.add(imageData);
			}			
			return null;
		}
	}
    
//    public static void main(String[] args) throws Exception {
//    	 String textContent = "Product Launch Revenue Plan";
//    	 System.out.println(Math.ceil((double)textContent.length()/40));
//    }
}
