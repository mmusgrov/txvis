package org.jboss.narayana.txvis.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.txvis.persistence.*;
import org.jboss.narayana.txvis.persistence.entities.Event;
import org.jboss.narayana.txvis.persistence.entities.Transaction;
import org.jboss.narayana.txvis.persistence.enums.EventType;
import org.jboss.narayana.txvis.persistence.enums.Status;
import org.jboss.narayana.txvis.persistence.enums.Vote;
import org.jboss.narayana.txvis.test.utils.UniqueIdGenerator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.File;
import java.sql.Timestamp;

import static junit.framework.Assert.*;

/**
 * @Author Alex Creasy &lt;a.r.creasy@newcastle.ac.uk$gt;
 * Date: 03/05/2013
 * Time: 17:31
 */
@RunWith(Arquillian.class)
public class DAOTest {

    @Deployment
    public static WebArchive createDeployment() {
        String ManifestMF = "Manifest-Version: 1.0\n"
                + "Dependencies: org.jboss.jts\n";

        File[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml").resolve("commons-io:commons-io:2.4")
                .withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "org.jboss.narayana.txvis.persistence", "org.jboss.narayana.txvis.test.utils")
                .addAsWebInfResource(new FileAsset(new File("src/test/resources/persistence.xml")), "classes/META-INF/persistence.xml")
                .addAsManifestResource(new FileAsset(new File("src/test/resources/txvis-test-ds.xml")), "txvis-test-ds.xml")
                .addAsLibraries(libs)
                .setManifest(new StringAsset(ManifestMF));

        return archive;
    }

    UniqueIdGenerator idGen;

    @EJB
    DataAccessObject dao;

    @Before
    public void setup() throws Exception {
        idGen = new UniqueIdGenerator();
    }

    @Test
    public void createRetrieveTest() throws Exception {
        final String txID = idGen.getUniqueTxId();
        dao.create(txID);
        assertNotNull(dao.retrieve(txID));
    }

    @Test
    public void enlistGetEnlistedTest() throws Exception {
        final String txID = idGen.getUniqueTxId();
        final String ptID1 = idGen.getUniqueResourceId();
        final String ptID2 = idGen.getUniqueResourceId();

        dao.create(txID);
        dao.enlistParticipant(txID, ptID1);
        dao.enlistParticipant(txID, ptID2);
        assertNotNull(dao.getEnlistedParticipant(txID, ptID1));
        assertNotNull(dao.getEnlistedParticipant(txID, ptID2));

        assertEquals(dao.getEnlistedParticipant(txID, ptID1).getResourceId(), ptID1);
        assertEquals(dao.getEnlistedParticipant(txID, ptID2).getResourceId(), ptID2);

        assertEquals(2, dao.retrieve(txID).getParticipants().size());
    }

    @Test
    public void setOutcomeTest() throws Exception {
        final String txID = idGen.getUniqueTxId();
        dao.create(txID);

        dao.setOutcome(txID, Status.COMMIT);
        assertEquals("Retrieved transaction did not report correct status", Status.COMMIT,
                dao.retrieve(txID).getStatus());

        dao.setOutcome(txID, Status.ROLLBACK_CLIENT);
        assertEquals("Retrieved transaction did not report correct status", Status.ROLLBACK_CLIENT,
                dao.retrieve(txID).getStatus());

        dao.setOutcome(txID, Status.ROLLBACK_RESOURCE);
        assertEquals("Retrieved transaction did not report correct status",Status.ROLLBACK_RESOURCE,
                dao.retrieve(txID).getStatus());
    }

    @Test
    public void setParticipantVoteTest() throws Exception {
        final String txID = idGen.getUniqueTxId();
        final String ptID1 = idGen.getUniqueResourceId();
        final String ptID2 = idGen.getUniqueResourceId();

        dao.create(txID);
        dao.enlistParticipant(txID, ptID1);
        dao.enlistParticipant(txID, ptID2);

        dao.setParticipantVote(txID, ptID1, Vote.COMMIT);
        dao.setParticipantVote(txID, ptID2, Vote.ABORT);

        assertEquals("Participant did not report correct vote", Vote.COMMIT,
                dao.getEnlistedParticipant(txID, ptID1).getVote());

        assertEquals("Participant did not report correct vote", Vote.ABORT,
                dao.getEnlistedParticipant(txID, ptID2).getVote());
    }


//    @Test
//    public void createTest() throws Exception {
//        dao.deleteAll(Transaction.class);
//
//        Transaction t = new Transaction(idGen.getUniqueTxId());
//        t.addEvent(new Event(t, new Timestamp(System.currentTimeMillis()), EventType.BEGIN));
//        dao.create(t);
//
//
//        assertEquals("Event not cascaded", 1, dao.retrieveAll(Event.class).size());
//        assertEquals("Event not bound", 1, dao.retrieveAll(Transaction.class).get(0).getEvents().size());
//    }

    @Test
    public void createAndRetrieveTest() throws Exception {
        final String txUID = idGen.getUniqueTxId();
        Transaction t = new Transaction(txUID);
        dao.create(t);

        assertNotNull("Entity did not contain an ID after attempting to persist", t.getId());

        assertNotNull("Unable to retrieve persisted Entity",
                dao.retrieve(Transaction.class, t.getId()));
    }

    @Test
    public void retrieveTransactionByTxUIDTest() throws Exception {
        final String txUID = idGen.getUniqueTxId();
        Transaction t = new Transaction(txUID);
        dao.create(t);

        assertEquals("Transaction ID did not match", txUID,
                dao.retrieveTransactionByTxUID(txUID).getTransactionId());
    }

    @Test
    public void UpdateTest() throws Exception {
        final String txUID = idGen.getUniqueTxId();

        Transaction t = new Transaction(txUID);
        dao.create(t);

        t = dao.retrieveTransactionByTxUID(txUID);
        t.setStatus(Status.COMMIT);
        dao.update(t);


        t = dao.retrieveTransactionByTxUID(txUID);
        assertEquals("Retrieved transaction entity did not report correct status",
                Status.COMMIT, t.getStatus());
    }
}
