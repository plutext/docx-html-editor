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

//import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@Path("/css/")  
public class EditorCSS {
	
//	private static final Logger jul = Logger.getLogger(EditorCSS.class
//			.getName());
	
	@GET
	@Path("/docx.css")
	public Response getCSS(@Context  HttpServletRequest request) {
		
		// SessionStyleHandler has put the CSS into the session

		HttpSession session = request.getSession();
		
		return Response.ok(
				session.getAttribute("css"),
				"text/css").build();
		
	}
	
	// org.docx4j.convert.out.html.XsltHTMLFunctions.appendStyleElement($conversionContext)
}