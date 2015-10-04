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

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.convert.out.html.HTMLConversionContext;
import org.docx4j.convert.out.html.XsltHTMLFunctions;
import org.docx4j.jaxb.Context;
import org.docx4j.wml.PPr;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class SessionPPrHandler {
	
	private final static AtomicInteger counter = new AtomicInteger();
	
	private static String getId() {
		return Editor.APP_KEY+"p"+counter.getAndIncrement();
		// since we store this in the pkg user data, its nice to use a 'namespace'
	}
	
	
    public static DocumentFragment createBlockForPPr( 
    		HTMLConversionContext context,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, NodeIterator childResults ) {

    	DocumentFragment df = XsltHTMLFunctions.createBlockForPPr(context, pPrNodeIt, pStyleVal, childResults);
    	
    	PPr pPr = null;
    	if (pPrNodeIt!=null) {
    		pPrNodeIt.previousNode();  // since it was read already
    		Node n = pPrNodeIt.nextNode();
    		if (n!=null) {
    			try {
        			Unmarshaller u = Context.jc.createUnmarshaller();			
        			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
        			Object jaxb = u.unmarshal(n);
    				pPr =  (PPr)jaxb;
    			} catch (ClassCastException e) {
    				context.getLog().error("Couldn't cast  to PPr!");
    			} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}        	        			
    		}
    	}
    	
    	// Store the ppr object in the pkg
    	// (this is easier than subclassing HTMLSettings, passing the storage map through the XSLT etc)
    	String id = getId();

    	if (pPr==null) {
    		context.getWmlPackage().setUserData(id, new PPrNone());
    	} else {
	    	context.getWmlPackage().setUserData(id, pPr);
    	}
    	Element e = (Element)df.getFirstChild();
    	e.setAttribute("id", id);
    	
    	
    	return df;
    	
    }

}
