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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.LoadFromZipNG;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;



@Path("/load")  
public class Load {
	
	private static final Logger jul = Logger.getLogger(Load.class
			.getName());

	
//	static {
//		jul.info("Servlet loaded");
//		
//		Logger.getLogger("com.sun.jersey").setLevel(Level.FINEST);
//		Logger.getLogger("org.glassfish.jersey").setLevel(Level.FINEST);
//	}	
		
	
	@POST
	@Consumes("multipart/form-data")
	@Produces( {"application/vnd.openxmlformats-officedocument.wordprocessingml.document" , 
				"text/html"})
	public Response processForm(
			@Context  HttpServletRequest request,
			@Context  HttpServletResponse response,
			@Context ServletContext servletContext, 
			@FormDataParam("docxfile") InputStream docxInputStream,
			@FormDataParam("docxfile") FormDataContentDisposition docxFileDetail,
			@FormDataParam("adv") String editorHtml
			) throws Docx4JException, IOException {
		
		try {
			
			HttpSession session = request.getSession(true);
		
		
			final WordprocessingMLPackage wordMLPackage;
			WordprocessingMLPackage tmpPkg=null;
			String docxname = getFileName(docxFileDetail.getFileName());
			
			LoadFromZipNG loader = new LoadFromZipNG();
			try {
				tmpPkg = (WordprocessingMLPackage)loader.get(docxInputStream );
			} catch (Exception e) {
				throw new WebApplicationException(
						new Docx4JException("Error reading docx file (is this a valid docx?)"), 
						Status.BAD_REQUEST);
			}
			
			if (tmpPkg==null) {
				throw new WebApplicationException(
						new Docx4JException("No docx file provided"), 
						Status.BAD_REQUEST);
			}
			wordMLPackage = tmpPkg;
			// Store the docx, so we can later inject XHTML into it...
			session.setAttribute("docx", wordMLPackage);

			Editor editor = new Editor();
			Editor.setContextPath(servletContext.getContextPath());
			
			ResponseBuilder builder;
			if (editorHtml!=null && editorHtml.equals("bare")) {
				builder = editor.streamDocxAsHtml(wordMLPackage, session, EditorHTML.BARE, response);
			} else {
				builder = editor.streamDocxAsHtml(wordMLPackage, session, EditorHTML.CKEditor3, response);
			}
			return builder.build();
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new WebApplicationException(
					new Docx4JException(e.getMessage(), e), 
					Status.INTERNAL_SERVER_ERROR);
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