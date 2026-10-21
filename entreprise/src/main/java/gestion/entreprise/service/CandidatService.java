package gestion.entreprise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

@Service
public class CandidatService {

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private OffreService offreService;

    @Autowired
    private UtilisateurService utilisateurService;

    public boolean aDejaPostule(int idUtilisateur, int idDetail) {
        return candidatRepository.existsByUtilisateurIdAndDetailDemandeId(idUtilisateur, idDetail);
    }

    // Formulaire pré-rempli avec la dernière candidature de l'utilisateur (s'il y en a une)
    public Candidat preparerFormulaire(Utilisateur utilisateur, DetailDemande offre) {
        Candidat formulaire = new Candidat();
        Candidat dernier = candidatRepository.findFirstByUtilisateurIdOrderByIdDesc(utilisateur.getId());

        if (dernier != null) {
            formulaire.setPrenom(dernier.getPrenom());
            formulaire.setNom(dernier.getNom());
            formulaire.setEmail(dernier.getEmail());
            formulaire.setTelephone(dernier.getTelephone());
            formulaire.setResume(dernier.getResume());
            formulaire.setDernierPoste(dernier.getDernierPoste());
            formulaire.setEntreprise(dernier.getEntreprise());
            formulaire.setDiplome(dernier.getDiplome());
            formulaire.setEcole(dernier.getEcole());
        } else {
            formulaire.setEmail(utilisateur.getEmail());
        }

        // Le poste visé est celui de l'offre
        formulaire.setTitre(offre.getPoste().getLibelle());
        return formulaire;
    }

    public void postuler(Candidat formulaire, int idUtilisateur, int idDetail,
                         MultipartFile fichierCv, MultipartFile fichierLm) throws Exception {

        // 1. Vérifications du formulaire
        if (estVide(formulaire.getPrenom())) {
            throw new Exception("Le prénom est obligatoire");
        }
        if (estVide(formulaire.getNom())) {
            throw new Exception("Le nom est obligatoire");
        }
        if (estVide(formulaire.getEmail()) || !formulaire.getEmail().contains("@")) {
            throw new Exception("L'e-mail est invalide");
        }
        if (estVide(formulaire.getTitre())) {
            throw new Exception("Le poste visé est obligatoire");
        }
        if (fichierCv == null || fichierCv.isEmpty()) {
            throw new Exception("Le CV est obligatoire");
        }
        verifierFichier(fichierCv, "CV");

        boolean aLettre = fichierLm != null && !fichierLm.isEmpty();
        if (aLettre) {
            verifierFichier(fichierLm, "Lettre de motivation");
        }

        // 2. Vérifications de l'offre et de l'utilisateur
        DetailDemande offre = offreService.getOffreActive(idDetail);

        if (offre.isCloturee()) {
            throw new Exception("Cette offre est clôturée : tous les postes sont pourvus");
        }

        if (aDejaPostule(idUtilisateur, idDetail)) {
            throw new Exception("Vous avez déjà postulé à cette offre");
        }

        Utilisateur utilisateur = utilisateurService.getById(idUtilisateur);
        if (utilisateur == null) {
            throw new Exception("Utilisateur introuvable");
        }

        // 3. Création de la candidature (on copie seulement ce qui vient du formulaire)
        Candidat candidat = new Candidat();
        candidat.setUtilisateur(utilisateur);
        candidat.setDetailDemande(offre);
        candidat.setStatus("En attente");

        // Les fichiers sont enregistrés dans la base (octets + nom d'origine)
        candidat.setCv(fichierCv.getBytes());
        candidat.setCvNom(fichierCv.getOriginalFilename());
        if (aLettre) {
            candidat.setLm(fichierLm.getBytes());
            candidat.setLmNom(fichierLm.getOriginalFilename());
        }

        candidat.setPrenom(formulaire.getPrenom().trim());
        candidat.setNom(formulaire.getNom().trim());
        candidat.setEmail(formulaire.getEmail().trim());
        candidat.setTelephone(formulaire.getTelephone());
        candidat.setTitre(formulaire.getTitre());
        candidat.setResume(formulaire.getResume());
        candidat.setDernierPoste(formulaire.getDernierPoste());
        candidat.setEntreprise(formulaire.getEntreprise());
        candidat.setDiplome(formulaire.getDiplome());
        candidat.setEcole(formulaire.getEcole());

        candidatRepository.save(candidat);
    }

    private boolean estVide(String texte) {
        return texte == null || texte.trim().isEmpty();
    }

    private void verifierFichier(MultipartFile fichier, String nomChamp) throws Exception {
        String nom = fichier.getOriginalFilename();

        if (nom == null) {
            throw new Exception(nomChamp + " : nom de fichier invalide");
        }

        String minuscule = nom.toLowerCase();
        if (!minuscule.endsWith(".pdf") && !minuscule.endsWith(".doc") && !minuscule.endsWith(".docx")) {
            throw new Exception(nomChamp + " : formats acceptés PDF, DOC ou DOCX");
        }

        if (fichier.getSize() > 5 * 1024 * 1024) {
            throw new Exception(nomChamp + " : taille maximale 5 Mo");
        }
    }
}
