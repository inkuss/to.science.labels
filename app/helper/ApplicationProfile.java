/*Copyright (c) 2015 "hbz"

This file is part of etikett.

etikett is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package helper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import models.Etikett;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

import com.avaje.ebean.Ebean;

import play.Play;

/**
 * @author Jan Schnasse
 *
 */
public class ApplicationProfile {

    /**
     * prefLabel predicate will be analysed
     */
    public final static String prefLabel = "http://www.w3.org/2004/02/skos/core#prefLabel";

    /**
     * icon predicate will be analyzed
     */
    public final static String icon = "http://www.w3.org/1999/xhtml/vocab#icon";

    /**
     * name predicate will be analyzed
     */
    public final static String name = "http://hbz-nrw.de/regal#jsonName";

    /**
     * type predicate will be analyzed
     */
    public final static String referenceType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    /**
     * Associates labels to rdf predicates or known objects
     */
    public ApplicationProfile() {
	String[] configs = Play.application().configuration()
		.getString("etikett.configs").split("\\s*,[,\\s]*");
	for (String s : configs) {
	    addRdfData(s);
	}
    }

    private void addRdfData(String fileName) {
	try (InputStream in = Play.application().resourceAsStream(fileName)) {
	    addRdfData(in);
	} catch (Exception e) {
	    e.printStackTrace();
	    play.Logger.info("config file " + fileName + " not found.");
	}
    }

    /**
     * @param in
     *            an input stream with rdf in turtle format
     */
    public void addRdfData(InputStream in) {
	Graph g = RdfUtils.readRdfToGraph(in, RDFFormat.TURTLE, "");
	Iterator<Statement> statements = g.iterator();
	while (statements.hasNext()) {
	    Statement st = statements.next();
	    String subj = st.getSubject().stringValue();
	    String obj = st.getObject().stringValue();
	    if (prefLabel.equals(st.getPredicate().stringValue())) {
		addLabel(subj, obj);
	    }
	    if (icon.equals(st.getPredicate().stringValue())) {
		addIcon(subj, obj);
	    }
	    if (name.equals(st.getPredicate().stringValue())) {
		addName(subj, obj);
	    }
	    if (referenceType.equals(st.getPredicate().stringValue())) {
		addReferenceType(subj, obj);
	    }
	}
    }

    private void addReferenceType(String key, String obj) {
	Etikett e = getValue(key);
	e.uri = key;
	e.referenceType = obj;
	Ebean.update(e);
    }

    void addLabel(String key, String obj) {
	Etikett e = getValue(key);
	e.uri = key;
	e.label = obj;
	Ebean.update(e);
    }

    void addIcon(String key, String obj) {
	Etikett e = getValue(key);
	e.uri = key;
	e.icon = obj;
	Ebean.update(e);
    }

    void addName(String key, String obj) {
	Etikett e = getValue(key);
	e.uri = key;
	e.name = obj;
	Ebean.update(e);
    }

    /**
     * @return all Values from etikett store
     */
    public Collection<? extends Etikett> getValues() {
	return Ebean.find(Etikett.class).findList();
    }

    /**
     * @param urlAddress
     * @return data associated with the url
     */
    public Etikett getValue(String urlAddress) {
	return Ebean.find(Etikett.class).where().eq("uri", urlAddress)
		.findUnique();
    }

}
