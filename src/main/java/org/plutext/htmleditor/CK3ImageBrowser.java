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
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.docx4j.openpackaging.exceptions.Docx4JException;

/**
 * CKEditor user can browse the images in their
 * session (either already present in the docx),
 * or uploaded, and select one for insertion 
 * in the docx.
 * 
 * @author jharrop
 *
 */
@Path("/images/")  
public class CK3ImageBrowser {
	
	private static final Logger jul = Logger.getLogger(CK3ImageBrowser.class.getName());

	@GET
	@Path("{url : .*}")
	public Response browse(
			@Context  HttpServletRequest request,
			@Context  HttpServletResponse response,									
			@Context  ServletContext context,
			@Context UriInfo info,
			@PathParam("url") String url,
			@QueryParam("CKEditorFuncNum") String CKEditorFuncNum
			)  {
		
		try {
			
			HttpSession session = request.getSession(false);
			
//					jul.info("request.getRequestURI()"+request.getRequestURI());
//					jul.info("info.getRequestUri().toASCIIString()"+info.getRequestUri().toASCIIString());  // pretty close
//					jul.info("request.getQueryString()"+request.getQueryString());
//					jul.info("info.getPath(false)"+info.getPath(false));
//					jul.info("info.getPath(true)"+info.getPath(true));
			
//					String browseUrl = url + "?" + request.getQueryString();
//					jul.info(browseUrl);
					
			if (url.equals("")) {
				// display all the images
				
				StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				HashMap<String, byte[]> imageMap = SessionImageHandler.getImageMap(session);
				if (imageMap.size()==0) {
					sb.append("No images found. Click the 'upload' tab?");
				} else {
					for (Entry<String, byte[]> entry : imageMap.entrySet()) {
						String suffix = "?CKEditorFuncNum="+CKEditorFuncNum;
						String imgUrl = getUrl(response, entry.getKey() + suffix);	
						sb.append("<p><a href=\"" + imgUrl + "\">" + entry.getKey() + "</a></p>");
					}
				}
				sb.append("</body></html>");
				return Response.ok(sb.toString() ).build();
				
			} else {
				// its a request for a specific image
				String imageUrl = getUrl(response, url);
				
				String html = "<html><body><script type=\"text/javascript\">"
						  + "window.opener.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ", \"" + imageUrl + "\");"
								+ "</script></body></html>";
					
						
				return Response.ok(html).build();
				
			}
					
		
		} catch (Exception e) {
			e.printStackTrace();
//			System.out.println(e.getMessage());
			throw new WebApplicationException(
					new Docx4JException(e.getMessage(), e), 
					Status.INTERNAL_SERVER_ERROR);
		}

	}
	
	protected String getUrl(HttpServletResponse response, String name) {
		return response.encodeURL(Editor.getContextPath() + "/services/image/" + name);		
	}
	

}
