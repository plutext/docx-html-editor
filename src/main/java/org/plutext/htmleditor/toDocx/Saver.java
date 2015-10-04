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

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.xhtmlrenderer.docx.Docx4jUserAgent;
import org.docx4j.org.xhtmlrenderer.docx.DocxRenderer;
import org.docx4j.wml.Style;






@Path("/save")  
public class Saver {
	
	private static final Logger jul = Logger.getLogger(Saver.class
			.getName());

	
	@POST
	//@Path("{url : .+}")
	//@Consumes(MediaType.APPLICATION_XML)
	//@Produces( {MediaType.APPLICATION_XHTML_XML})
	public Response xhtmlToDocx(
			@Context  HttpServletRequest request,
//			@Context  ServletContext context,
//			@Context UriInfo info,
			@FormParam("editorOutput") String xhtml
			)  {
		
		try {
			
			HttpSession session = request.getSession(false);
			if (session==null) {
				return Response.ok(
						"Your session has expired.",
						MediaType.TEXT_PLAIN).build();				
			}			
			WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)session.getAttribute("docx");
			
			// contains &nbsp;  
			xhtml = xhtml.replace("&nbsp;", " ") ;
			
			// we need to feed our css to Flying Saucer, or it'll use inappropriate defaults
			xhtml = "<html><head><style type=\"text/css\">" + session.getAttribute("css") + "</style></head>"
					+ "<body>" + xhtml + "</body></html>";
			
			
			jul.log(Level.INFO, xhtml);
			
			XHTMLImporterImpl xHTMLImporter = new RoundtripXHTMLImporter(wordMLPackage);
			xHTMLImporter.setXHTMLImageHandler(new XHTMLtoDocxImageHandler());
			
			// SessionAwareDocx4jUserAgent knows how to find images in the session
			Docx4jUserAgent ua = new SessionAwareDocx4jUserAgent(session); 
			DocxRenderer renderer = new DocxRenderer(ua);
			xHTMLImporter.setRenderer(renderer);
			
			wordMLPackage.getMainDocumentPart().getContent().clear();
			wordMLPackage.getMainDocumentPart().getContent().addAll( 
					xHTMLImporter.convert( xhtml, null) );
			
			// Remove DocDefault virtual style, since it upsets table formatting
			// (applies space after for example, when that wouldn't otherwise be used)
			Style normal = wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle();
			if (normal!=null) {
				normal.setBasedOn(null);
			}
								
			// Now stream the docx
			final SaveToZipFile saver = new SaveToZipFile(wordMLPackage);		
			ResponseBuilder builder = Response.ok(
					
				new StreamingOutput() {				
					public void write(OutputStream output) throws IOException, WebApplicationException {					
				         try {
							saver.save(output);
						} catch (Docx4JException e) {
							throw new WebApplicationException(e);
						}							
					}
				}
			);
			builder.header("Content-Disposition", "attachment; filename=result.docx");
			builder.type("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			
			return builder.build();

		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new WebApplicationException(
					new Docx4JException(e.getMessage(), e), 
					Status.INTERNAL_SERVER_ERROR);
		}

	}
	
	
}