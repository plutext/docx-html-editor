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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractWriterRegistry;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.html.BookmarkStartWriter;
import org.docx4j.convert.out.html.BrWriter;
import org.docx4j.convert.out.html.FldSimpleWriter;
import org.docx4j.convert.out.html.HTMLExporterXslt;
import org.docx4j.convert.out.html.HyperlinkWriter;
import org.docx4j.convert.out.html.SymbolWriter;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;





@Path("/edit/")  
public class Editor {
	
	private static final Logger jul = Logger.getLogger(Editor.class
			.getName());

	protected static final String APP_KEY = "DOCX4J_EDITOR:";

	// TODO rethink this. The issue is that context is null
	// until service method starts
	// (injection happens when you enter service method)
	// so just get it locally in the service methods
	private static String APP_CONTEXT = null;
	protected static String getContextPath() {
		// eg /docx4j-web-editor-1.0.0-SNAPSHOT
		return APP_CONTEXT;
	}
	protected static void setContextPath(String contextPath) {
		APP_CONTEXT = contextPath;
	}	
	
	
	// Templates are thread safe
	private static Templates CKEditor3_XSLT;
	private static Templates BARE_XSLT;  // BARE is still intended for CKEditor3
	
	static {
		jul.info("Servlet loaded");
		
		Logger.getLogger("com.sun.jersey").setLevel(Level.WARNING);
		Logger.getLogger("org.glassfish.jersey").setLevel(Level.WARNING);
		
		try {
			CKEditor3_XSLT = XmlUtils.getTransformerTemplate(
					new StreamSource(
							org.docx4j.utils.ResourceUtils.getResource("docx2xhtml_CKEditor2013.xslt")));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		try {
			BARE_XSLT = XmlUtils.getTransformerTemplate(
					new StreamSource(
							org.docx4j.utils.ResourceUtils.getResource("docx2xhtml_Bare.xslt")));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}	
		
	

	
	@GET
	@Path("{url : .+}")
	//@Consumes(MediaType.APPLICATION_XML)
	//@Produces( {MediaType.APPLICATION_XHTML_XML})
	public Response getConfigForm(
			@Context  HttpServletRequest request,
			@Context  HttpServletResponse response,			
			@Context  ServletContext context,
			@Context UriInfo info,
			@PathParam("url") String url
			)  {
		
		try {
			
			HttpSession session = request.getSession(true);
			
			// Need repo info in a session object?
			
			// Use CMIS to get the docx from that URL
			// eg http://localhost:8080/alfresco/cmisatom/706ca2b8-d196-4834-a2bf-e99122b88994/content/cmis.docx 

					String browseUrl = url + "?" + request.getQueryString();
					jul.info(browseUrl);
					
					InputStream is = getCMISResource(request, browseUrl);
					
					WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(is);

					// Store the docx, so we can later inject XHTML into it...
					session.setAttribute("docx", wordMLPackage);
					
								
//			return Response.ok(
//					"It worked" + url,
//					MediaType.TEXT_PLAIN).build();

					ResponseBuilder builder = streamDocxAsHtml(wordMLPackage, session, EditorHTML.CKEditor3, response );
					return builder.build();
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new WebApplicationException(
					new Docx4JException(e.getMessage(), e), 
					Status.INTERNAL_SERVER_ERROR);
		}

	}
	

	protected static final AbstractWriterRegistry CUSTOM_HTML_WRITER_REGISTRY = 
			new AbstractWriterRegistry() {
				@Override
				protected void registerDefaultWriterInstances() {
					registerWriter(new EditorTableWriter());  // customised
					registerWriter(new SymbolWriter());
					registerWriter(new BrWriter());
					registerWriter(new FldSimpleWriter());
					registerWriter(new BookmarkStartWriter());
					registerWriter(new HyperlinkWriter());
				}
			};
	
	
	
	protected ResponseBuilder streamDocxAsHtml(final WordprocessingMLPackage wordMLPackage, final HttpSession session, 
			EditorHTML editorHTML, HttpServletResponse response) {
		

		// .. the HtmlSettings object
    	final HTMLSettings htmlSettings = new HTMLSettings();    	
		
    	if (editorHTML.equals(EditorHTML.BARE)) {
	    	htmlSettings.setCustomXsltTemplates(BARE_XSLT);
    	} else if  (editorHTML.equals(EditorHTML.CKEditor3)) {
	    	htmlSettings.setCustomXsltTemplates(CKEditor3_XSLT);    		
    	}

    	
    	htmlSettings.setImageHandler(new SessionImageHandler(session));

    	htmlSettings.setStyleElementHandler(new SessionStyleHandler(session));
    	
//    	htmlSettings.setUserBodyTop("<H1>TOP!</H1>");
//    	htmlSettings.setUserBodyTail("<H1>TAIL!</H1>");
		
		// Sample sdt tag handler (tag handlers insert specific
		// html depending on the contents of an sdt's tag).
		// This will only have an effect if the sdt tag contains
		// the string @class=XXX
//			SdtWriter.registerTagHandler("@class", new TagClass() );
		
//		SdtWriter.registerTagHandler(Containerization.TAG_BORDERS, new TagSingleBox() );
//		SdtWriter.registerTagHandler(Containerization.TAG_SHADING, new TagSingleBox() );

		
    	htmlSettings.setWmlPackage(wordMLPackage);
    	wordMLPackage.setUserData("HttpSession", session);
    	
    	// Since we'll store various stuff on the pkg object (eg images, long tail)
    	// avoid using a clone!
    	htmlSettings.getFeatures().remove(ConversionFeatures.PP_COMMON_DEEP_COPY);

    	// Don't convert w:r/w:br[w:@type="page"] to <w:pageBreakBefore/>
    	// since it seems that should really go on the following paragraph
    	htmlSettings.getFeatures().remove(ConversionFeatures.PP_COMMON_MOVE_PAGEBREAK);
    	
		// We'll store the images associated with this docx in a map:
		HashMap<String, byte[]> imageMap = new HashMap<String, byte[]>();  
		// and store that in the docx 
		// (so each docx has its own collection of available images; these aren't shared across
		//   the user's session, if they have multiple windows open, each with a different editor)
		wordMLPackage.setUserData(APP_KEY + "imageMap", imageMap);
		
		htmlSettings.getSettings().put("ContextPath", getContextPath());
		
		// For extension function
		htmlSettings.getSettings().put("HttpServletResponse", response);
    	
    	
		ResponseBuilder builder = Response.ok(
				
				new StreamingOutput() {				
					public void write(OutputStream output) throws IOException, WebApplicationException {					
				         try {
				     		javax.xml.transform.stream.StreamResult result 
				     			= new javax.xml.transform.stream.StreamResult(output);
				    		
				    		// Docx4J.toHTML(htmlSettings, output, Docx4J.FLAG_NONE);
				    		Exporter<HTMLSettings> exporter = new HTMLExporterXslt(CUSTOM_HTML_WRITER_REGISTRY);
				    		exporter.export(htmlSettings, output);
				    		
						} catch (Exception e) {
							throw new WebApplicationException(e);
						}							
					}
				}
			);
//						builder.header("Content-Disposition", "attachment; filename=output.pdf");
//			builder.type("application/pdf");
			
			return builder;		
	}
	
	/**
	 * XSLT extension function which encodes the specified URL by including the session ID in it, or, if encoding is not needed, returns the URL unchanged. 
	 * The implementation of this method includes the logic to determine whether the session ID needs to be encoded in the URL. 
	 * For example, if the browser supports cookies, or session tracking is turned off, URL encoding is unnecessary.
	 * 
	 * For robust session tracking, all URLs emitted by a servlet should be run through this method. 
	 * Otherwise, URL rewriting cannot be used with browsers which do not support cookies.
	 * 
	 * @param url
	 * @return
	 */
	public static String encodeURL(HttpServletResponse response, String url) {
		
		return response.encodeURL(url);
	}
	
    protected InputStream getCMISResource(HttpServletRequest req,  String browseUrl) throws ServletException, IOException {

            // get content
            URL url = new URL(browseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null) {
                conn.setRequestProperty("Authorization", authHeader);
            }
            conn.connect();

//            // ask for login
//            if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
//                resp.setHeader("WWW-Authenticate", conn.getHeaderField("WWW-Authenticate"));
//                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
//                return;
//            }
//
//            // debug messages
//            if (log.isDebugEnabled()) {
//                log.debug("'" + browseUrl + "' -> '" + conn.getContentType() + "'");
//            }

            return conn.getInputStream();
        }
	
	

	  public static String getStackTrace(Throwable aThrowable) {
		    final Writer result = new StringWriter();
		    final PrintWriter printWriter = new PrintWriter(result);
		    aThrowable.printStackTrace(printWriter);
		    return result.toString();
		  }	
	  
	  
	  
	    /**
	     * For testing purposes only
	     */
	    public static void main(String[] args) throws Exception {

			// .. the HtmlSettings object
	    	final HTMLSettings htmlSettings = new HTMLSettings();
	    	htmlSettings.getFeatures().remove(ConversionFeatures.PP_COMMON_DEEP_COPY);
	    	
			
			try {
				Source xsltSource = new StreamSource(org.docx4j.utils.ResourceUtils.getResource(
									"org/plutext/htmleditor/docx2xhtml_CKEditor2013.xslt"));
		    	htmlSettings.setCustomXsltTemplates(XmlUtils.getTransformerTemplate(xsltSource));
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			}
			

	    	
//	    	htmlSettings.setImageHandler(new SessionImageHandler(session));
//
//	    	htmlSettings.setStyleElementHandler(new SessionStyleHandler(session));
			
	    	htmlSettings.getFeatures().remove(ConversionFeatures.PP_COMMON_DEEP_COPY);
	    	htmlSettings.getFeatures().remove(ConversionFeatures.PP_COMMON_MOVE_PAGEBREAK);
	    	
			
			String inputfilepath = System.getProperty("user.dir")
//					+ "/sample-docx.docx";
//					+ "/sample-docs/word/2003/word2003xml.xml";
					+ "/docx-samples/Demo-Hayden-Management-v2.docx";
		

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
			
	    	htmlSettings.setWmlPackage(wordMLPackage);
	    	
	    	ByteArrayOutputStream output = new ByteArrayOutputStream();
	    	
    		Docx4J.toHTML(htmlSettings, output, Docx4J.FLAG_NONE);

    		System.out.println(output.toString("UTF-8"));
	    	
	    }
	
}