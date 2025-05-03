-- =============================================================================
-- TP 3 - partie 1 : Reponses 

--Les vues et les sous-requetes
--Les gachettes (trigger)
--Procedure et fonction stockees en PL/SQL

-- Auteurs : -- LOUISAMA   Adlin      LOUA20309509
             -- Mebtouche  Anis       MEBA74070308
-- Date : 17 Avril 2024


--Requete 1
SELECT iv.num_vol, iv.date_vol, v.ville_depart, v.ville_arrivee, iv.heure_depart_reelle, iv.heure_arrivee_reelle
FROM Instance_Vol iv
JOIN Vol v ON iv.num_vol = v.num_vol
WHERE iv.date_vol = TO_DATE('04-03-2024', 'DD-MM-YYYY') 
AND v.ville_depart = 'Montreal'
AND iv.heure_depart_reelle = (
  SELECT MIN(iv2.heure_depart_reelle)
  FROM Instance_Vol iv2
  JOIN Vol v2 ON iv2.num_vol = v2.num_vol
  WHERE iv2.date_vol = TO_DATE('04-03-2024', 'DD-MM-YYYY') 
  AND v2.ville_depart = 'Montreal'
);

--Requete 2
SELECT DISTINCT me.matricule, me.nom
FROM Membre_equipage me
JOIN Vol_Membre_equipage vme ON me.matricule = vme.matricule
WHERE EXISTS (
    SELECT 1
    FROM Vol_Membre_equipage vme_inner
    JOIN Membre_equipage me_inner ON vme_inner.matricule = me_inner.matricule
    WHERE me_inner.nom = 'Julie Will'
    AND vme_inner.num_vol = vme.num_vol 
    AND vme_inner.date_vol = vme.date_vol
)
AND me.nom <> 'Julie Will'
ORDER BY me.matricule;

--Requete 3
--a--
CREATE VIEW V_Vol_Equipage AS
SELECT Instance_Vol.*, Vol_Membre_equipage.poste_occupe, Membre_equipage.*
FROM Instance_Vol
JOIN Vol_Membre_equipage ON Instance_Vol.num_vol = Vol_Membre_equipage.num_vol
AND Instance_Vol.date_vol = Vol_Membre_equipage.date_vol
JOIN Membre_equipage ON Vol_Membre_equipage.matricule = Membre_equipage.matricule;

--b--
SELECT num_vol, date_vol, heure_depart_reelle, matricule, nom, type_membre, poste_occupe
FROM V_Vol_Equipage
WHERE num_vol = 'AC1212'
AND date_vol = TO_DATE('04-03-2024', 'DD-MM-YYYY')
ORDER BY matricule;

--c--
SELECT V_Vol_Equipage.matricule, V_Vol_Equipage.nom, Compagnie.nom_cie, Compagnie.pays
FROM V_Vol_Equipage
JOIN Vol ON V_Vol_Equipage.num_vol = Vol.num_vol
JOIN Compagnie ON Vol.cie_code_activite = Compagnie.code_activite
WHERE V_Vol_Equipage.nom = 'Fan Yang';

--Question 1
--a Creation du Trigger pour la verification des dates dans la table Vol
CREATE OR REPLACE TRIGGER Check_Vol_Time
BEFORE INSERT OR UPDATE ON Vol
FOR EACH ROW
DECLARE
  difference_temps INTERVAL DAY TO SECOND;
BEGIN
  difference_temps := :NEW.heure_arrivee - :NEW.heure_depart;

  IF extract(HOUR FROM difference_temps) * 60 + extract(MINUTE FROM difference_temps) < 45 THEN
    RAISE_APPLICATION_ERROR(-20000, 'La différence entre l''heure de départ et l''heure d''arrivée doit être d''au moins 45 minutes.');
  END IF;

  IF :NEW.heure_depart >= :NEW.heure_arrivee THEN
    RAISE_APPLICATION_ERROR(-20001, 'L''heure d''arrivée doit être postérieure à l''heure de départ.');
  END IF;
END;

--b Requête d'insertion rejetée par le trigger
INSERT INTO Vol (num_vol, ville_depart, ville_arrivee, heure_depart, heure_arrivee, cie_code_activite) 
VALUES ('AC9999', 'Montreal', 'Toronto', TO_TIMESTAMP('10:00', 'HH24:MI'), TO_TIMESTAMP('10:30', 'HH24:MI'), 'AC3245Y');

--Question 2
--a Création du Trigger pour vérifier le nombre de passagers dans Instance_Vol
CREATE OR REPLACE TRIGGER Check_Passenger_Count
BEFORE INSERT OR UPDATE OF nbre_passager ON Instance_Vol
FOR EACH ROW
DECLARE
--place_max
  max_seats NUMBER;
BEGIN
--SELECT nbre_places INTO place_max
  SELECT nbre_places INTO max_seats
  FROM Aeronef
  WHERE immatriculation = :NEW.immatriculation;

  -- Vérifier si le nombre de passagers n'est pas supérieur au nombre de places de l'aéronef
  IF :NEW.nbre_passager > max_seats THEN
    RAISE_APPLICATION_ERROR(-20002, 'Le nombre de passagers ne peut pas dépasser le nombre de places de l''aéronef.');
  END IF;
END;

--b Requete de mise a jour rejetee par le trigger
UPDATE Instance_Vol
SET nbre_passager = 300 
WHERE num_vol = 'AC1212' AND date_vol = TO_DATE('04-03-2024', 'DD-MM-YYYY');


--Question 3
--Creation de la procedure
CREATE OR REPLACE PROCEDURE Lister_Vols_Par_Compagnie (p_nom_compagnie IN VARCHAR2) AS
  v_total_passagers NUMBER := 0;
  v_compte NUMBER := 0;
BEGIN
  -- Vérifier si le nom de la compagnie existe
  SELECT COUNT(*)
  INTO v_compte
  FROM Compagnie
  WHERE nom_cie = p_nom_compagnie;

  IF v_compte = 0 THEN
    DBMS_OUTPUT.PUT_LINE('Aucune compagnie trouvée avec le nom ' || p_nom_compagnie);
    RETURN;
  END IF;

  FOR un_vol IN (
    SELECT v.num_vol, iv.date_vol, v.ville_depart, v.ville_arrivee, iv.heure_depart_reelle, iv.heure_arrivee_reelle, iv.nbre_passager
    FROM Vol v
    JOIN Compagnie c ON c.code_activite = v.cie_code_activite
    JOIN Instance_Vol iv ON iv.num_vol = v.num_vol
    WHERE c.nom_cie = p_nom_compagnie
    ORDER BY iv.date_vol, iv.heure_depart_reelle
  ) LOOP
    DBMS_OUTPUT.PUT_LINE(
      RPAD(un_vol.num_vol, 10) || ' ' ||
      TO_CHAR(un_vol.date_vol, 'DD-MM-YYYY') || ' ' ||
      RPAD(un_vol.ville_depart, 15) || ' ' ||
      RPAD(un_vol.ville_arrivee, 15) || ' ' ||
      TO_CHAR(un_vol.heure_depart_reelle, 'HH24:MI') || ' ' ||
      TO_CHAR(un_vol.heure_arrivee_reelle, 'HH24:MI') || ' ' ||
      RPAD(un_vol.nbre_passager, 10)
    );
    v_total_passagers := v_total_passagers + un_vol.nbre_passager;
  END LOOP;

  DBMS_OUTPUT.PUT_LINE('Nombre total de passagers : ' || v_total_passagers);
END;
/

BEGIN
  DBMS_OUTPUT.ENABLE;
  Lister_Vols_Par_Compagnie('AIRCANADA');
END;
/

--Question 4 
--a Création de la séquence et du trigger pour la génération automatique du numéro de facture
CREATE SEQUENCE Facture_Seq
START WITH 1000
INCREMENT BY 1
NOCACHE;

--créez un trigger sur la table Facture qui utilise cette séquence pour attribuer un numero de facture automatiquement a chaque nouvelle insertion.
CREATE OR REPLACE TRIGGER Facture_Before_Insert
BEFORE INSERT ON Facture
FOR EACH ROW
BEGIN
  IF :NEW.num_facture IS NULL THEN
    SELECT Facture_Seq.NEXTVAL
    INTO :NEW.num_facture
    FROM DUAL;
  END IF;
END;
/

--b Requête d'insertion pour tester le fonctionnement du trigger
INSERT INTO Facture (date_facturation, total_maindoeuvre, total_piece, taxes, id_maintenance)
VALUES (TO_DATE('2024-03-15', 'YYYY-MM-DD'), 1500, 300, 200, 1);

--ROLLBACK;

--  Question 5
-- a Creation de la fonction stockee pour calculer le cout des pieces d'une maintenance
CREATE OR REPLACE FUNCTION Calculer_Cout_Pieces(p_id_maintenance IN NUMBER) RETURN NUMBER AS
  v_cout_total NUMBER := 0;
  v_compte NUMBER := 0;
BEGIN
  -- Verifier si l'identifiant de maintenance existe
  SELECT COUNT(*)
  INTO v_compte
  FROM Maintenance
  WHERE id_maintenance = p_id_maintenance;

  IF v_compte = 0 THEN
    RETURN -1; -- Retourne -1 si l'identifiant ne correspond a aucune maintenance
  END IF;

  -- Calculer le cout total des pièces pour la maintenance specifiee
  SELECT SUM(p.prix_unitaire * mp.quantite)
  INTO v_cout_total
  FROM Maintenance_Piece mp
  JOIN Piece p ON mp.code_piece = p.code_piece
  WHERE mp.id_maintenance = p_id_maintenance;

  RETURN NVL(v_cout_total, 0); -- Retourne 0 si aucune pièce n'est associée à la maintenance
END;
/

--b Requete d'appel a la fonction pour les maintenances 900 et 999
SELECT Calculer_Cout_Pieces(900) AS Cout_Pieces_Maintenance_900 FROM DUAL;

SELECT Calculer_Cout_Pieces(999) AS Cout_Pieces_Maintenance_999 FROM DUAL;


--Question 6
--a) Creation de la fonction stockee pour calculer le cout de la main d'oeuvre d'une maintenance
CREATE OR REPLACE FUNCTION Calculer_Cout_MaindOeuvre(p_id_maintenance IN NUMBER) RETURN NUMBER AS
  v_cout_total NUMBER := 0;
  v_compte NUMBER := 0;
  v_taux_horaire CONSTANT NUMBER := 89.50;
BEGIN
  -- Verifier si l'identifiant de maintenance existe
  SELECT COUNT(*)
  INTO v_compte
  FROM Maintenance
  WHERE id_maintenance = p_id_maintenance;

  IF v_compte = 0 THEN
    RETURN -1; -- Retourne -1 si l'identifiant ne correspond a aucune maintenance
  END IF;

  -- Calculer le cout total de la main d'oeuvre pour la maintenance specifiee
  SELECT SUM(nbre_heures * v_taux_horaire)
  INTO v_cout_total
  FROM Intervention
  WHERE id_maintenance = p_id_maintenance;

  RETURN NVL(v_cout_total, 0); -- Retourne 0 si aucune intervention n'est associee a la maintenance
END;
/

--b) Requetes d'appel a la fonction pour les maintenances 900 et 999
SELECT Calculer_Cout_MaindOeuvre(900) AS Cout_MO_900 FROM DUAL;
SELECT Calculer_Cout_MaindOeuvre(999) AS Cout_MO_900 FROM DUAL;

-- Question 7
--a) Creation de la procedure stockee pour generer la facture d'une maintenance
CREATE OR REPLACE PROCEDURE Generer_Facture_Maintenance(p_id_maintenance IN NUMBER) AS
  v_cout_pieces NUMBER;
  v_cout_maindoeuvre NUMBER;
  v_total_taxe NUMBER;
  v_total_sans_taxe NUMBER;
  v_date_facturation DATE := SYSDATE;
  v_compte NUMBER;
BEGIN
  -- Verifier l'existence de la maintenance
  SELECT COUNT(*)
  INTO v_compte
  FROM Maintenance
  WHERE id_maintenance = p_id_maintenance;

  IF v_compte = 0 THEN
    DBMS_OUTPUT.PUT_LINE('L''identifiant de maintenance ' || p_id_maintenance || ' n''existe pas.');
    RETURN;
  END IF;

  -- Calculer les couts des pieces et de la main d’oeuvre
  v_cout_pieces := Calculer_Cout_Pieces(p_id_maintenance);
  v_cout_maindoeuvre := Calculer_Cout_MaindOeuvre(p_id_maintenance);

  -- Calculer le total sans taxe
  v_total_sans_taxe := v_cout_pieces + v_cout_maindoeuvre;

  -- Calculer les taxes (15% du total sans taxe)
  v_total_taxe := v_total_sans_taxe * 0.15;

  -- Inserer la nouvelle ligne dans Facture
  INSERT INTO Facture (date_facturation, total_maindoeuvre, total_piece, taxes, id_maintenance)
  VALUES (v_date_facturation, v_cout_maindoeuvre, v_cout_pieces, v_total_taxe, p_id_maintenance);

  COMMIT;

  DBMS_OUTPUT.PUT_LINE('Facture générée pour la maintenance ' || p_id_maintenance || '.');
END;
/

--b Création de la facture pour la maintenance 900
BEGIN
  Generer_Facture_Maintenance(900);
END;
/
