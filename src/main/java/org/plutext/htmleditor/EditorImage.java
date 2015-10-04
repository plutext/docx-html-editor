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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


/**
 * @author jharrop
 *
 */
@Path("/image/")  
public class EditorImage {
	
	private static final Logger jul = Logger.getLogger(EditorImage.class
			.getName());
	
	/**
	 * The editor (eg CKEditor) invokes this to display an image.
	 * 
	 * @param request
	 * @param context
	 * @param info
	 * @param name
	 * @return
	 */
	@GET
	@Path("{name}")  
	public Response getImage(
			@Context  HttpServletRequest request,
			@Context  ServletContext context,
			@Context UriInfo info,
			@PathParam("name") String name
			) {

		HttpSession session = request.getSession();
		
//		jul.info("request.getRequestURI()"+request.getRequestURI());
//		jul.info("info.getRequestUri().toASCIIString()"+info.getRequestUri().toASCIIString());  // pretty close
//		jul.info("request.getQueryString()"+request.getQueryString());
//		jul.info("info.getPath(false)"+info.getPath(false));
//		jul.info("info.getPath(true)"+info.getPath(true));
		
		
		return Response.ok(
				SessionImageHandler.getImageMap(session).get(name),
				MediaType.APPLICATION_OCTET_STREAM).build();
		
	}
	
	/**
	 * CK3 specific image upload
	 * 
	 * @param request
	 * @param CKEditorFuncNum
	 * @param imageInputStream
	 * @param imageFileDetail
	 * @return
	 * @throws Docx4JException
	 * @throws IOException
	 */
	@POST
	@Consumes("multipart/form-data")
	@Produces("text/html")
	public Response ck3ImageUpload(
//			@Context ServletContext servletContext, 
			@Context  HttpServletRequest request,
			@Context  HttpServletResponse response,						
			@QueryParam("CKEditorFuncNum") String CKEditorFuncNum,
			@FormDataParam("upload") InputStream imageInputStream,
			@FormDataParam("upload") FormDataContentDisposition imageFileDetail
			) throws Docx4JException, IOException {
		
		// <input id="cke_131_fileInput_input" aria-labelledby="cke_130_label" type="file" name="upload" size="38">
		
		try {
			
			HttpSession session = request.getSession(false);
			
			String name = "uploaded_" + this.getFileName(imageFileDetail.getFileName());
			
			SessionImageHandler.getImageMap(session).put(name, IOUtils.toByteArray(imageInputStream) );
			
			jul.info("stored " + name);
			
			String url = response.encodeURL(Editor.getContextPath() + "/services/image/" + name);
						
			String html = "<html><body><script type=\"text/javascript\">"
			  + "window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ", \"" 
					    + url + "\", \"\" );window.close();"
					+ "</script></body></html>";
		
			
			return Response.ok(html).build();
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			
			String html = "<html><body><script type=\"text/javascript\">"
					  + "window.close();window.opener.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + "," 
					  			+" \"\", \"An error occured\" );"
							+ "</script></body></html>";
				
					
					return Response.ok(html).build(); // TODO not ok
			
//			throw new WebApplicationException(
//					new Docx4JException(e.getMessage(), e), 
//					Status.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	/**
	 * header sample
	 * {
	 * 	Content-Type=[image/png], 
	 * 	Content-Disposition=[form-data; name="file"; filename="filename.extension"]
	 * }
	 **/
	private String getFileName(String name) {
 
		String finalFileName = name.trim().replaceAll("\"", "");
		
		if (finalFileName.lastIndexOf(".")==-1) {
			return finalFileName;
		} else {
			return finalFileName.substring(0, finalFileName.lastIndexOf(".") ); 
		}
     }	
	
	
	
}