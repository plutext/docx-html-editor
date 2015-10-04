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

import java.util.Map;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.org.xhtmlrenderer.newtable.TableBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableCellBox;
import org.docx4j.org.xhtmlrenderer.render.BlockBox;
import org.docx4j.wml.CTTblPrEx;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.plutext.htmleditor.PPrNone;
import org.w3c.dom.css.CSSValue;

public class RoundtripXHTMLImporter extends XHTMLImporterImpl {

	public RoundtripXHTMLImporter(WordprocessingMLPackage wordMLPackage) {
		super(wordMLPackage);
	}
	
	@Override
    protected void setupTblPr(TableBox cssTable, Tbl tbl, TableProperties tableProperties) {
		
		/*
		 * Reinstate
		 * 
			context.getWmlPackage().setUserData(id+"#Pr", table.getTblPr());
			
			as set by SessionAwareAbstractTableWriter
			
		 */
		String id = cssTable.getElement().getAttribute("id");
		
		if (id==null ) {
			log.debug("no id on table " );
			
		} else {
			log.debug("processing table with id " + id);

			Object o = wordMLPackage.getUserData(id+"#Pr");
			if (o==null) {
				
				log.debug("no #Pr UserData on table with id " + id);
			
			} else {
				tbl.setTblPr((TblPr)o);
				return;
			}
		}
		
		// we need to fall back for a newly created table
		super.setupTblPr( cssTable,  tbl,  tableProperties);
    }
	
	@Override
    protected void setupTblGrid(TableBox cssTable, Tbl tbl, TableProperties tableProperties) {

		/*
		 * Reinstate
		 * 
			context.getWmlPackage().setUserData(id+"#Grid", table.getTblGrid());
			
			as set by SessionAwareAbstractTableWriter
			
		 */
		String id = cssTable.getElement().getAttribute("id");
		
		if (id==null ) {
			log.debug("no id on table " );
			
		} else {
			log.debug("processing table with id " + id);

			Object o = wordMLPackage.getUserData(id+"#Grid");
			if (o==null) {
				
				log.debug("no #Grid UserData on table with id " + id);
			
			} else {
				tbl.setTblGrid((TblGrid)o);
				return;
			}
		}
		
		super.setupTblGrid( cssTable,  tbl,  tableProperties);
		
    }
    
	@Override
    protected void setupTrPr(org.docx4j.org.xhtmlrenderer.newtable.TableRowBox trBox, Tr tr) {
		
		/*
		 * Reinstate
		 * 
		 * 	context.getWmlPackage().setUserData(rowId+"#Pr", trPr );
			context.getWmlPackage().setUserData(rowId+"#PrEx", tblPrEx );
			
			as set by SessionAwareAbstractTableWriter
			
		 */

		String id = trBox.getElement().getAttribute("id");
		
		if (id==null ) {
			log.debug("no id on tr " );
			
		} else {
			log.debug("processing tr with id " + id);

			// TrPr
			Object o = wordMLPackage.getUserData(id+"#Pr");
			if (o==null) {
				
				log.debug("no #Pr UserData on tr with id " + id);
			
			} else {
				tr.setTrPr((TrPr)o);
			}

			Object o2 = wordMLPackage.getUserData(id+"#PrEx");
			if (o2==null) {
				
				log.debug("no #PrEx UserData on tr with id " + id);
			
			} else {
				tr.setTblPrEx((CTTblPrEx)o2);
			}
			
			if (o!=null || o2 !=null) return;
			
		}
		
		super.setupTrPr( trBox,  tr);		
    }
    
	@Override
    protected void setupTcPr(TableCellBox tcb, Tc tc, TableProperties tableProperties) {
		
		/*
		 * Reinstate
		 * 
			context.getWmlPackage().setUserData(cellId+"#Pr", cell.getTcPr() );
			
			as set by SessionAwareAbstractTableWriter
			
		 */

		String id = tcb.getElement().getAttribute("id");
		
		if (id==null ) {
			log.debug("no id on tc " );
			
		} else {
			log.debug("processing tc with id " + id);

			Object o = wordMLPackage.getUserData(id+"#Pr");
			if (o==null) {
				
				log.debug("no #Pr UserData on tc with id " + id);
			
			} else {
				tc.setTcPr((TcPr)o);
				return;
			}
		}
		
		super.setupTcPr( tcb,  tc,  tableProperties);
    }	
	
	@Override
    protected PPr getPPr(BlockBox blockBox, Map<String, CSSValue> cssMap) {

		// if the paragraph has an ID, use the preserved existing pPr
		String id = blockBox.getElement().getAttribute("id");
		
		if (id==null ) {
			log.debug("no id on p " );
			
		} else {
			log.debug("processing p with id " + id);

			Object o = wordMLPackage.getUserData(id);
			if (o==null) {
				
				log.debug("no #Pr UserData on p with id " + id);
				
			} else if (o instanceof PPrNone) {
				return null;			
			} else {
				
				return((PPr)o);
				// (TODO unless the user has changed the style)
			}
		}
		
		// A new p the user has created
        PPr pPr =  Context.getWmlObjectFactory().createPPr();
        populatePPr(pPr, blockBox, cssMap);
    	return pPr;
    }
	
}
