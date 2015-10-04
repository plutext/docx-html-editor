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

import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.docx4j.model.images.AbstractConversionImageHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;

public class SessionImageHandler extends AbstractConversionImageHandler {

	HttpSession session;
	public SessionImageHandler(HttpSession session) {
		
		
		super("dummyImageDirPath", false);
		this.session = session;
	}
	
	private HttpServletResponse response;
	protected void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	@Override
	protected String createStoredImage(BinaryPart binaryPart, byte[] bytes)
			throws Docx4JException {
		
		String name = this.getImageName(binaryPart);
		
		// The images associated with this docx are stored in a map in the docx 
		getImageMap(session).put(name, bytes);
		
		return getUrl(name);
		
	}
	
	protected String getUrl(String name) {
		return response.encodeURL(Editor.getContextPath() + "/services/image/" + name);		
	}

	@SuppressWarnings("unchecked")
	public
	static HashMap<String, byte[]>  getImageMap(HttpSession session) {
		
		WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)session.getAttribute("docx");	
		return (HashMap<String, byte[]>)wordMLPackage.getUserData(Editor.APP_KEY + "imageMap");
	}
}
