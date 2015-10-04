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

import javax.servlet.http.HttpSession;

import org.docx4j.convert.out.ConversionHTMLStyleElementHandler;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Put the CSS into the session, so it is available when the GET request comes.
 * 
 * Done this way, since EditorCSS doesn't have ready access to conversionContext, and so can't itself 
 * invoke org.docx4j.convert.out.html.XsltHTMLFunctions.appendStyleElement($conversionContext)
 *
 */
public class SessionStyleHandler implements ConversionHTMLStyleElementHandler {
	
	HttpSession session;
	public SessionStyleHandler(HttpSession session) {
		
		this.session = session;
	}
	

	@Override
	public Element createStyleElement(OpcPackage opcPackage, Document document,
			String styleDefinition) {

		
		if ((styleDefinition != null) && (styleDefinition.length() > 0)) {
    		session.setAttribute("css", styleDefinition);
//    		System.out.println(styleDefinition);
    		
    		//<link rel="stylesheet" type="text/css" href="/docx4j-web-editor-1.0.0-SNAPSHOT/services/css/docx.css" />
    		
    		
//			Element ret = document.createElement("link");
//			ret.setAttribute("rel", "stylesheet");
//			ret.setAttribute("type", "text/css");
//			ret.setAttribute("href", Editor.getContextPath() + "/services/css/docx.css");
//    		return ret;
    		
		} else {
    		System.out.println("styleDefinition was null or empty!");			
		}
		return null;
	}

}
