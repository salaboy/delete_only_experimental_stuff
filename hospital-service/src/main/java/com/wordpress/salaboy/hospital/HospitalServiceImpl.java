/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.wordpress.salaboy.hospital;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.*;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.*;

/**
 *
 * @author salaboy
 */
public class HospitalServiceImpl implements HospitalService { 

    private StatefulKnowledgeSession ksession;
    public static final List updated = new ArrayList();
    public static final List removed = new ArrayList();
    public static final List added = new ArrayList();

    public HospitalServiceImpl() {
        System.out.println("Creating new Hospital Service! "+System.currentTimeMillis());
        createKnowledgeBasedHospital();
    }

    public List<String> getSpecialities() {
        return null;
    }

    public String requestBed(String id) {
        System.out.println("Requesting a Bed "+System.currentTimeMillis());
        ksession.insert(new RequestBed(id));
        return UUID.randomUUID().toString();
    }

    
    private void createKnowledgeBasedHospital() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add(new ClassPathResource("hospital.drl"), ResourceType.DRL);

        if (kbuilder.hasErrors()) {
            KnowledgeBuilderErrors errors = kbuilder.getErrors();

            for (KnowledgeBuilderError error : errors) {
                System.out.println(">>> Error:" + error.getMessage());

            }
            throw new IllegalStateException(">>> Knowledge couldn't be parsed! ");
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        ksession = kbase.newStatefulKnowledgeSession();
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

        new Thread() {

            @Override
            public void run() {
                ksession.fireUntilHalt();
            }
        }.start();


        Hospital hospital = new Hospital("Hospital 1", 1, 1);
        hospital.setAvailableBeds(30);
        ksession.insert(hospital);


        ViewChangedEventListener listener = new ViewChangedEventListener() {

            public void rowUpdated(Row row) {

                updated.add(row.get("$availableBeds"));
                System.out.println("Updating the Available Beds = " + row.get("$availableBeds"));
            }

            public void rowRemoved(Row row) {
                removed.add(row.get("$availableBeds"));
            }

            public void rowAdded(Row row) {
                added.add(row.get("$availableBeds"));
                System.out.println("Adding the Available Beds = " + row.get("$availableBeds"));
            }
        };

        // Open the LiveQuery
        LiveQuery query = ksession.openLiveQuery("getAvailableBeds", new Object[]{}, listener);




    }

    public int getAvailableBeds() {
        System.out.println("Get Available Beds! "+System.currentTimeMillis());
        QueryResults queryResults = ksession.getQueryResults("getAvailableBeds");
        for (QueryResultsRow row : queryResults) {
            return (Integer) row.get("$availableBeds");

        }

        return 0;

    }
}
