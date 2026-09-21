package com.thorben.janssen;

import com.thorben.janssen.data.ChessPlayerStatefulRepository;
import com.thorben.janssen.data._ChessPlayerRepository;
import com.thorben.janssen.model.ChessClub;
import com.thorben.janssen.model.ChessPlayer;
import com.thorben.janssen.model.ChessPlayer_;
import com.thorben.janssen.model._ChessPlayer;
import com.thorben.janssen.persistence.ChessClubQueries_;
import com.thorben.janssen.data.ChessPlayerRepository;
import com.thorben.janssen.persistence.ClubName;
import jakarta.data.Order;
import jakarta.data.restrict.Restrict;
import jakarta.data.restrict.Restriction;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.sql.ResultSetMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FetchMethod;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestPersistence {

	Logger log = LogManager.getLogger(this.getClass().getName());

	private static EntityManagerFactory emf;

	private ChessPlayerRepository playerRepo;
	private ChessPlayerStatefulRepository playerStatefulRepo;

	/**
	 * Jakarta Persistence 4.0
	 */

	@Test
	public void testEntityAgent() {
		log.info("... testEntityAgent ...");

		var entityAgent = emf.createEntityAgent();
		entityAgent.getTransaction().begin();

		ChessPlayer player = new ChessPlayer();
		player.setFirstName("Thorben");
		player.setLastName("Jansen");
		entityAgent.insert(player);

		player = entityAgent.createQuery("SELECT p FROM ChessPlayer p WHERE p.id =:id", ChessPlayer.class).setParameter("id", player.getId()).getSingleResult();

		player.setLastName("Janssen");
		log.info("#################");
		entityAgent.update(player);
		log.info("#################");

		entityAgent.getTransaction().commit();
		entityAgent.close();
	}

	@Test
	public void testEntityAgentLazyFetching() {
		log.info("... testEntityAgentLazyFetching ...");

		var entityAgent = emf.createEntityAgent();
		entityAgent.getTransaction().begin();

		var player = entityAgent.createQuery("SELECT p FROM ChessPlayer p WHERE p.id =:id", ChessPlayer.class)
				.setParameter("id", 1L)
				.getSingleResult();
		log.info(player);
		entityAgent.fetch(player.getClub());
		log.info(player.getClub().getName());

		entityAgent.getTransaction().commit();
		entityAgent.close();
	}

	@Test
	public void testJakartaQuery() {
		log.info("... testJakartaQuery ...");

		var entityManager = emf.createEntityManager();
		entityManager.getTransaction().begin();

		var clubs = entityManager.createQuery(ChessClubQueries_.nativeFindClubByName("%Chess%"))
				.getResultList();

		clubs.forEach(club -> log.info(club));

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Test
	public void testProgrammaticResultMapping() {
		log.info("... testProgrammaticResultMapping ...");

		var entityManager = emf.createEntityManager();
		entityManager.getTransaction().begin();

		var resultMapping = ResultSetMapping.constructor(
				ClubName.class,
				ResultSetMapping.column("clubId", Long.class),
				ResultSetMapping.column("clubName", String.class));
		var clubs = entityManager.createNativeQuery("SELECT c.name as clubName, c.id as clubId FROM ChessClub c", resultMapping)
				.getResultList();

		clubs.forEach(club -> log.info(club));

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	// getResultCount & default lazy fetching
	@Test
	public void testQuery() {
		log.info("... testQuery ...");

		var entityManager = emf.createEntityManager();
		entityManager.getTransaction().begin();

		var query = entityManager.createQuery("SELECT p FROM ChessPlayer p WHERE p.firstName = :firstName", ChessPlayer.class)
						.setParameter("firstName", "Magnus");
		log.info("Found " + query.getResultCount() + " players.");

		var players = query.getResultList();
		players.forEach(player -> log.info(player));

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	/**
	 * Jakarta Data 1.1
	 */

	// Stateful Repository
	// Not supported in Hibernate 8.0.0.Beta1

	@Test
	public void testRestrict() {
		log.info("... testRestrict ...");

		var players = playerRepo.findPlayers(
				_ChessPlayer.firstName.like("%n%"),
				Order.by(_ChessPlayer.lastName.asc()));
		players.forEach(player -> log.info(player));
	}

	@Test
	public void testComplexRestrict() {
		log.info("... testComplexRestrict ...");

		var players = playerRepo.findPlayers(
				Restrict.any(
						Restrict.all(
								_ChessPlayer.firstName.equalTo("Magnus"),
								_ChessPlayer.lastName.equalTo("Carlsen")
						),
						Restrict.all(
								_ChessPlayer.firstName.equalTo("Vincent"),
								_ChessPlayer.lastName.equalTo("Keymer")
						)),
				Order.by(_ChessPlayer.lastName.asc()));
		players.forEach(player -> log.info(player));
	}

	@Test
	public void testProjection() {
		log.info("... testProjection ...");

		var players = playerRepo.getPlayerNames();
		players.forEach(player -> log.info(player));
	}

	/**
	 * Hibernate 8
	 */

	@Test
	public void testSubselectFetching() {
		log.info("... testSubselectFetching ...");

		var entityManager = emf.createEntityManager();
		entityManager.getTransaction().begin();

		EntityGraph<ChessPlayer> graph =
				entityManager.createEntityGraph(ChessPlayer.class);
		graph.addAttributeNode(ChessPlayer_.club)
				.addOption(FetchMethod.BY_SUBQUERY)
		;

		var players = entityManager
				.createQuery("""
                    SELECT p
                    FROM ChessPlayer p
                    ORDER BY p.id
                    """, ChessPlayer.class)
				.setHint("jakarta.persistence.fetchgraph", graph)
				.getResultList();

		players.forEach(player -> log.info("{} {} plays for {}", player.getFirstName(), player.getLastName(), player.getClub() != null ? player.getClub().getName() : ""));

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Test
	public void testBidirectionalManagement() {
		log.info("... testBidirectionalManagement ...");

		var entityManager = emf.createEntityManager();
		entityManager.getTransaction().begin();

		var club = entityManager.find(ChessClub.class, 1L);
		log.info("{} has {} players.", club.getName(), club.getPlayers().size());

		var newPlayer = new ChessPlayer();
		newPlayer.setFirstName("Thorben");
		newPlayer.setLastName("Janssen");
		newPlayer.setClub(club);
		entityManager.persist(newPlayer);

//		club.getPlayers().add(newPlayer);
//		entityManager.flush();
		log.info("Before commit");
		log.info("{} has {} players.", club.getName(), club.getPlayers().size());

		entityManager.getTransaction().commit();
		entityManager.close();

		log.info("After commit");
		log.info("{} has {} players.", club.getName(), club.getPlayers().size());
	}

//	/**
//	 * Hibernate 7.4
//	 */
//
//	@Test
//	public void testJoinFetchLimit() {
//		log.info("... testJoinFetchLimit ...");
//
//		EntityManager em = emf.createEntityManager();
//		em.getTransaction().begin();
//
//		var clubs = em.createQuery("SELECT c FROM ChessClub c JOIN FETCH c.players", ChessClub.class).setMaxResults(2).getResultList();
//
//		em.getTransaction().commit();
//		em.close();
//	}
//
//	/**
//	 * Jakarta Persistence 3.2
//	 */
//
//	@Test
//	public void testEnumeratedValue() {
//		log.info("... testEnumeratedValue ...");
//
//		EntityManager em = emf.createEntityManager();
//		em.getTransaction().begin();
//
//		ChessPlayer player = new ChessPlayer();
//		player.setFirstName("Thorben");
//		player.setLastName("Janssen");
//		player.setPlayerType(PlayerType.Hobby);
//		em.persist(player);
//
//		em.getTransaction().commit();
//		em.close();
//	}




	@BeforeAll
	public static void init() {
		emf = Persistence.createEntityManagerFactory("my-persistence-unit");
	}

	@BeforeEach
	public void before() {
		this.playerRepo = new _ChessPlayerRepository(emf.createEntityAgent());
	}

	@AfterAll
	public static void close() {
		emf.close();
	}
}
