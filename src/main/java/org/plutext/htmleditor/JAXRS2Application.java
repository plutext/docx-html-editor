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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.plutext.htmleditor.toDocx.Saver;

/**
 * Portable JAX-RS application.
 *
 * @author Arul Dhesiaseelan (aruld@acm.org)
 */
public class JAXRS2Application extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register resources and features
        classes.add(MultiPartFeature.class);
        classes.add(Load.class);
        
//        classes.add(UploadICM.class);
        
        classes.add(Editor.class);
        classes.add(EditorCSS.class);
        classes.add(EditorImage.class);
        classes.add(CK3ImageBrowser.class);
        
        classes.add(Saver.class);
//        classes.add(LoggingFilter.class);
        return classes;
    }
}
