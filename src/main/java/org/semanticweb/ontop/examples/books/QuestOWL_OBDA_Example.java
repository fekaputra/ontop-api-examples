package org.semanticweb.ontop.examples.books;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import static java.util.stream.Collectors.joining;

public class QuestOWL_OBDA_Example {

    /*
     *
     * Please use the pre-bundled H2 server from the root of this repository
     *
     */
    final String owlFile = "src/main/resources/example/books/exampleBooks.owl";
    final String obdaFile = "src/main/resources/example/books/exampleBooks.obda";
    final String sparqlFile = "src/main/resources/example/books/q1.rq";

    /**
     * Main client program
     */
    public static void main(String[] args) {
        try {
            QuestOWL_OBDA_Example example = new QuestOWL_OBDA_Example();
            example.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() throws Exception {
        OWLOntology ontology = OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(owlFile));

        OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFile);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();

        QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).build();

		/*
		 * Prepare the data connection for querying.
		 */
        String sparqlQuery = Files.lines(Paths.get(sparqlFile)).collect(joining("\n"));

        try (QuestOWL reasoner = factory.createReasoner(ontology, config);
             QuestOWLConnection conn = reasoner.getConnection();
             QuestOWLStatement st = conn.createStatement();
        ) {
            String sqlQuery = st.getUnfolding(sparqlQuery);

            System.out.println();
            System.out.println("The input SPARQL query:");
            System.out.println("=======================");
            System.out.println(sparqlQuery);
            System.out.println();

            System.out.println("The output SQL query:");
            System.out.println("=====================");
            System.out.println(sqlQuery);

            QuestOWLResultSet rs = st.executeTuple(sparqlQuery);

            int columnSize = rs.getColumnCount();
            while (rs.nextRow()) {
                for (int idx = 1; idx <= columnSize; idx++) {
                    OWLObject binding = rs.getOWLObject(idx);
                    System.out.print(ToStringRenderer.getInstance().getRendering(binding) + ", ");
                }
                System.out.print("\n");
            }
            rs.close();

        }
    }


}
