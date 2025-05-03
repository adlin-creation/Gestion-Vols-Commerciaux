# Gestion des Vols Commerciaux 

## Description

Ce projet consiste à concevoir une application Java connectée à une base de données Oracle afin de gérer les vols commerciaux dans un aéroport.

## Auteurs

- **Adlin Louisama**
- **Anis Mebtouche**
- Supervision : *El Hachemi Alikacem*

## Fonctionnalités

L’application permet :

- d’afficher tous les vols au départ de Montréal
- de consulter les membres d’équipage d’un vol à une date donnée
- d’ajouter une maintenance avec les techniciens et les heures d’intervention associées

## Technologies

- **Java 17+**
- **JDBC**
- **Base de données Oracle (BACLAB)**
- **Outils recommandés : IntelliJ, Eclipse, SQL Developer**

## Exécution

### 1. Compiler le projet Java :
 ```bash
 javac -d bin src/uqam/inf3080/gestionVols/*.java
```
### Lancer l’application

```bash
java -cp bin:oracle.jar uqam.inf3080.gestionVols.GestionVolsApp
```

### Choisir une option dans le menu

1. Afficher la liste des vols au départ de Montréal
2. Afficher les membres d’équipage d’un vol
3. Ajouter une maintenance


