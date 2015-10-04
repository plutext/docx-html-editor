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

/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbj�rn Gannholm
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.plutext.htmleditor.toDocx;

import javax.servlet.http.HttpSession;

import org.docx4j.org.xhtmlrenderer.docx.Docx4JFSImage;
import org.docx4j.org.xhtmlrenderer.docx.Docx4jUserAgent;
import org.plutext.htmleditor.SessionImageHandler;

public class SessionAwareDocx4jUserAgent extends Docx4jUserAgent {
	
	HttpSession session;
	protected SessionAwareDocx4jUserAgent(HttpSession session) {
		this.session = session;
	}	


//    private byte[] readStream(InputStream is) throws IOException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());
//        byte[] buf = new byte[10240];
//        int i;
//        while ( (i = is.read(buf)) != -1) {
//            out.write(buf, 0, i);
//        }
//        out.close();
//        return out.toByteArray();
//    }

	@Override
    public Docx4JFSImage getDocx4JImageResource(String uri) {
		
//		System.out.println("Looking at " + uri);
		String name = uri.substring(uri.lastIndexOf("/")+1);
//		System.out.println("Looking for " + name);
		
		byte[] bytes = SessionImageHandler.getImageMap(session).get(name);
		if (bytes!=null) {
//			System.out.println(".. found in session ");
            return new Docx4JFSImage(bytes);			
		}
//		System.out.println(".. not found in session ");		
		return super.getDocx4JImageResource(uri);
                
    }

//    public static void main(String[] args) throws Exception {
//
//    	String uri = "http://localhost:8086/docx4j-web-editor-1.0.0-SNAPSHOT/services/image/hi.png";
//    	
//		System.out.println("Looking at " + uri);
//		String name = uri.substring(uri.lastIndexOf("/")+1);
//		System.out.println("Looking for " + name);
//    	
//    }
}
