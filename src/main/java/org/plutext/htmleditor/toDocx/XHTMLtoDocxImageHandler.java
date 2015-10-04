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

package org.plutext.htmleditor.toDocx;

import org.docx4j.convert.in.xhtml.XHTMLImageHandlerDefault;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.xhtmlrenderer.docx.Docx4jUserAgent;
import org.docx4j.wml.P;
import org.w3c.dom.Element;

public class XHTMLtoDocxImageHandler extends XHTMLImageHandlerDefault {

	@Override
	public void addImage(Docx4jUserAgent docx4jUserAgent, WordprocessingMLPackage wordMLPackage, P targetP, Element e, Long cx, Long cy) {
		
		// This design doesn't allow the user to resize the image in the editor,
		// since we don't trust it to allow that to be done accurately
		
		
		String id = e.getAttribute("id");

		
		if (id==null ) {
			log.debug("no id on image with src " + e.getAttribute("src") );
//			System.out.println("no id on image with src " + e.getAttribute("src") );
			
		} else {
			log.debug("processing image with id " + id);
//			System.out.println("processing image with id " + id);

			Object o = wordMLPackage.getUserData(id);
			if (o==null) {
				
				log.debug("no UserData on image with id " + id);
//				System.out.println("no UserData on image with id " + id);
			
			} else {
				if (o instanceof P) {
					P pStored = (P)o;
					// replace p contents
					targetP.setPPr(pStored.getPPr());
					targetP.getContent().clear();
					targetP.getContent().addAll(pStored.getContent());
					return;
				} else {
					log.debug("adding " + o.getClass().getName() );
					
					// Just add the contents
					targetP.getContent().add(o);
					return;
				}
				
			}
		}
		
		super.addImage(docx4jUserAgent, wordMLPackage, targetP, e, cx, cy);

	}
	
}
