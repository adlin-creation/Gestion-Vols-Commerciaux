package uqam.inf3080.gestionVols;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;



/**
 * Classe principale - Suite du TP-3   
 * INF3080 - Hiver 2024
 * 
 * Auteurs : LOUISAMA   Adlin      LOUA20309509
             Mebtouche  Anis       MEBA74070308
 *  
 * @author el hachemi Alikacem 
 * 
 */

public class GestionVolsApp {
	public static String uri = "jdbc:oracle:thin:@zeta2.labunix.uqam.ca:1521:BACLAB";
	public static Connection connexion = null;

	/**
	 * Bloc statique pour le chargement du pilote Oracle 
	 * Ce bloc est éxécuté une seul fois, au chargement de 
	 * la classe 
	 * 
	 */
	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");

		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------------------   
	/**
	 * Question connexion : Ouverture de la connexion
	 * 
	 * @param login 
	 * @param password 
	 * @param uri
	 * @return	
	 */
	public static Connection connexionBDD(String login, String password, String uri) {

		 try {
		        Class.forName("oracle.jdbc.driver.OracleDriver");
		        Connection une_connexion = DriverManager.getConnection(uri, login, password);
		        return une_connexion;
		    } catch (ClassNotFoundException e) {
		        System.err.println("Pilote non trouvé : " + e.getMessage());
		        return null;
		    } catch (SQLException e) {
		        System.err.println("Erreur de connexion : " + e.getMessage());
		        return null;
		    } 
	}

	// ------------------------------------------------------------------------------   
	/**
	 * Question déconnexion - fermeture de la connexion
	 * 
	 * @return
	 */
	public static boolean fermetureConnexion() {
		  try {
		        if (connexion != null && !connexion.isClosed()) {
		            connexion.close();
		            System.out.println("Connexion fermée.");
		            return true;
		        } else {
		            System.out.println("La connexion était déjà fermée ou n'a jamais été ouverte.");
		            return false;
		        }
		    } catch (SQLException e) {
		        System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
		        return false;
		    } 
	}

	// ------------------------------------------------------------------------------   
	/**
	 * Question 1 - liste les vols au départ de Montréal 
	 * 
	 * @param codeSession
	 */
	public static void listeVolsDepMontreal() {
		
		 String query = "SELECT Vol.num_vol, Vol.ville_depart, Vol.ville_arrivee, Instance_Vol.date_vol, Instance_Vol.heure_depart_reelle " +
                 "FROM Instance_Vol JOIN Vol ON Instance_Vol.num_vol = Vol.num_vol " +
                 "WHERE ville_depart = 'Montreal' " +
                 "ORDER BY date_vol, heure_depart_reelle";

  try (Statement statement = connexion.createStatement();
       ResultSet resultSet = statement.executeQuery(query)) {
      
	  System.out.println("------------------------------------------------------------------");
	  System.out.println("| Num.   | Depart       | Arrivée      | Date       | Heure      |");
	  System.out.println("------------------------------------------------------------------");

	  while (resultSet.next()) {
	      String num = resultSet.getString("num_vol");
	      String depart = resultSet.getString("ville_depart");
	      String arrivee = resultSet.getString("ville_arrivee");
	      Date date = resultSet.getDate("date_vol");
	      Time heure = resultSet.getTime("heure_depart_reelle");

	      System.out.format("| %-6s | %-12s | %-12s | %-10s | %-10s |\n", num, depart, arrivee, date.toString(), heure.toString());
	      System.out.println("------------------------------------------------------------------");
	  }
  } catch (SQLException e) {
      System.err.println("Erreur lors de la récupération des données : " + e.getMessage());
  }
		
	} 


	// ------------------------------------------------------------------------------   
	/**
	 * Question 2 - Liste l'équipage d'un vol 
	 * 
	 * @param codeSession
	 * @param numVol  Le numéro du vol pour lequel on souhaite lister l'équipage.
	 * @param dateVol La date du vol pour lequel on souhaite lister l'équipage.
	 * @throws SQLException Si une erreur se produit.
	 */
	public static void listeEquipage(String numVol , String dateVol ) {
		
		 String query = "SELECT m.matricule, m.nom, m.type_membre, vme.poste_occupe " +
                 "FROM Membre_equipage m " +
                 "JOIN Vol_Membre_equipage vme ON m.matricule = vme.matricule " +
                 "WHERE vme.num_vol = ? AND vme.date_vol = TO_DATE(?, 'DD-MM-YYYY') " +
                 "ORDER BY m.matricule";

  try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
      pstmt.setString(1, numVol);
      pstmt.setString(2, dateVol);

	  System.out.println("---------------------------------------------------------------");
	  System.out.println("| Matricule    | Nom          | Type_menbre  | Poste_occupé   |");
	  System.out.println("---------------------------------------------------------------");
      try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
              int matricule = rs.getInt("matricule");
              String nom = rs.getString("nom");
              String typeMembre = rs.getString("type_membre");
              String posteOccupe = rs.getString("poste_occupe");

              System.out.format("| %-12s | %-12s | %-12s | %-14s |\n", matricule, nom, typeMembre, posteOccupe);
    	      System.out.println("---------------------------------------------------------------");
          }
      }
  } catch (SQLException e) {
      e.printStackTrace();
  }
	}

	// ------------------------------------------------------------------------------   
	/**
	 * Ajoute une nouvelle maintenance dans la base de donnees avec les interventions specifiees.

	 * @param idMaintenance      L'identifiant de la maintenance à ajouter.
	 * @param dateMaintenance    La date de la maintenance au format "DD-MM-YYYY".
	 * @param immatriculation    L'immatriculation de l'aéronef concerne par la maintenance.
	 * @param listeInterv        La liste des interventions réalisées lors de la maintenance.
	 * @throws SQLException Si une erreur de base de données se produit pendant l'opération.
	 */
	public static void ajoutMaintenance(int idMaintenance, String dateMaintenance, String immatriculation, ArrayList<Intervention> listeInterv) {
	    if (connexion == null) {
	        System.out.println("Connexion non établie.");
	        return;
	    }

	    String checkImmatriculationQuery = "SELECT COUNT(*) FROM Aeronef WHERE immatriculation = ?";
	    try (PreparedStatement checkStmt = connexion.prepareStatement(checkImmatriculationQuery)) {
	        checkStmt.setString(1, immatriculation);
	        ResultSet rs = checkStmt.executeQuery();
	        if (!rs.next() || rs.getInt(1) == 0) {
	            System.out.println("Immatriculation invalide.");
	            return;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return;
	    }

	    String insertMaintenanceQuery = "INSERT INTO Maintenance (id_maintenance, date_maintenance, immatriculation) VALUES (?, TO_DATE(?, 'DD-MM-YYYY'), ?)";
	    String insertInterventionQuery = "INSERT INTO Intervention (id_maintenance, matricule, nbre_heures) VALUES (?, ?, ?)";
	    
	    try (PreparedStatement insertStmt = connexion.prepareStatement(insertMaintenanceQuery);
	         PreparedStatement insertIntervStmt = connexion.prepareStatement(insertInterventionQuery)) {
	        
	        connexion.setAutoCommit(false);

	        insertStmt.setInt(1, idMaintenance);
	        insertStmt.setString(2, dateMaintenance);
	        insertStmt.setString(3, immatriculation);
	        insertStmt.executeUpdate();

	        for (Intervention interv : listeInterv) {
	            insertIntervStmt.setInt(1, idMaintenance);
	            insertIntervStmt.setInt(2, interv.maticule);
	            insertIntervStmt.setInt(3, interv.nbreHeures);
	            insertIntervStmt.executeUpdate();
	        }

	        connexion.commit();
	        System.out.println("Maintenance ajoutée avec succès.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	        try {
	            connexion.rollback();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    } finally {
	        try {
	            connexion.setAutoCommit(true);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// 
	// Vous n'avez pas besoin de modifier le reste du code 
	// sauf pour mettre le username et mot de passe
	// ---------------------------------------------------
	
	// ------------------------------------------------------------------------------   
	/**
	 * Méthode réalise la lecture des matricules de techniciens et les heures 
	 * d'intervention dans une maintenance. 
	 * 
	 * Pour chaque paire (matricule, heures) est stockée dans un objet de la classe 
	 * Intervention. 
	 * 
	 * À la fin, la méthode retourne une ArrayList<Intervention> qui contient les objets 
	 * Intervention Créés 
	 * 
	 * Note : pour terminer la saisie des matricules et heures, on introduit la valeur -1
	 *  
	 * @return ArrayList des données des l'intervention
	 */
	private static ArrayList<Intervention> lireHeuresTechnicien() {
		ArrayList<Intervention> listeInterv = new ArrayList<Intervention>() ; 
		
		boolean stop = false ; 
		Scanner sc = new Scanner(System.in); ; 
		while (!stop) {
			System.out.print("Veuillez introduire le matricule du technicien (faire -1 pour : terminer) : ");
			int matricule = sc.nextInt();
			if (matricule != -1 ) {
				System.out.print("Veuillez introduire le nombre d'heures : ");                   
				int nbHeures = sc.nextInt() ;				
					Intervention uneIntervention = new Intervention(matricule , nbHeures) ;
					listeInterv.add(uneIntervention); 
			} else {
				stop = true ; 
			}		

		}
		return listeInterv ; 
	}


	// ------------------------------------------------------------------------------   
	/**
	 * Affiche un menu pour le choix des opérations 
	 * 
	 */
	public static void afficheMenu(){
		System.out.println(); 
		System.out.println("\t0. Quitter le programme");
		System.out.println("\t1. Afficher la liste des vols au départ de Montréal");
		System.out.println("\t2. Afficher les membres d'équipage d'un vol");
		System.out.println("\t3. Ajouter une maintenance");
		System.out.print("\n\t\tVotre choix...");
	}

	// ------------------------------------------------------------------------------   
	/**
	 * Méthode principale du TP 
	 * 
	 * @param args
	 */
	public static void main(String args[]){

		// Important : Mettre les informations de votre compte Oracle 
		String username   = "XXXXXXXX" ; 
		String motDePasse = "YYYYYYYY" ; 

		// Appel de le méthode pour établir la connexion avec le SGBD 
		connexion = connexionBDD(username , motDePasse , uri ) ;

		if (connexion != null) {

			System.out.println("Connection reussie...\n");

			// Affichage du menu pour le choix des opérations 
			afficheMenu(); 

			// Lecture du choix de l'utilisateur 
			Scanner scChoix = new Scanner(System.in);
			String choix = scChoix.nextLine().trim();

			while(!choix.equals("0")){

				switch (choix) {
				case "1" : 
					listeVolsDepMontreal(); 
					break ; 

				case "2" :
					System.out.print("Veuillez saisir le numéro de vol : ");
					Scanner scQ2 = new Scanner(System.in);
					String numVol = scQ2.nextLine().trim() ;    

					System.out.print("Veuillez saisir la date du vol (DD-MM-YYYY) : ");
					scQ2 = new Scanner(System.in);
					String dateVol = scQ2.nextLine().trim() ;        

					listeEquipage(numVol , dateVol ) ; 

					break ; 

				case "3" :
					Scanner scQ3 = new Scanner(System.in);
					System.out.print("Veuillez introduire l'identifiant de la maintenance : ");                   
					int id_maintenance = scQ3.nextInt() ;
					
					scQ3 = new Scanner(System.in);
					System.out.print("Veuillez introduire la date de la maintenance (DD-MM-YYYY) : ");
					String dateMaint = scQ3.nextLine().trim();
					
					scQ3 = new Scanner(System.in);
					System.out.print("Veuillez introduire l'immaticulation de l'aéronef : ");
					String immatAeronef=  scQ3.nextLine().trim();
					ArrayList<Intervention> listIntervention= lireHeuresTechnicien() ; 
					
					// Appel de la méthode d'ajout de la maintenance  
					ajoutMaintenance(id_maintenance ,dateMaint , immatAeronef , listIntervention) ; 

					break ; 

				default : 
					System.out.print("Choix invalide. Veuillez recommencer !") ; 
				}

				afficheMenu();
				scChoix = new Scanner(System.in);
				choix = scChoix.nextLine();

			} // while 

			// FIn de la boucle While - Fermeture de la connexion 
			if(fermetureConnexion()){
				System.out.println("Deconnection reussie...");
			}else{
				System.out.println("Échec ou Erreur lors de le déconnexion...");
			}

		} else {  
			System.out.println("Echec de la Connection. Au revoir ! ");

		} // if (connexion != null) {

	} // main()

}

// ============================================================================
/**
 * Data Classe pour stocker les données d'une intervention : le matricule du technicien 
 * et le nombre d'heures. 
 * 
 */
class Intervention {
	int maticule ; 
	int nbreHeures ;
	
	/**
	 * 
	 * @param maticule
	 * @param nbreHeures
	 */
	public Intervention(int maticule, int nbreHeures) {
		this.maticule = maticule;
		this.nbreHeures = nbreHeures;
	}	 
}