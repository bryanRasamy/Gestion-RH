package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class CorrectionDemandeService {
	private final DemandeRepository demandeRepository;
	private final DetailDemandeRepository detailRepository;
	private final DemandeARevoirRepository revoirRepository;
	private final HistoriqueDemandeStatusRepository historiqueRepository;
	private final StatusRepository statusRepository;

	public CorrectionDemandeService(DemandeRepository demandeRepository, DetailDemandeRepository detailRepository,
									 DemandeARevoirRepository revoirRepository,
									 HistoriqueDemandeStatusRepository historiqueRepository,
									 StatusRepository statusRepository) {
		this.demandeRepository = demandeRepository;
		this.detailRepository = detailRepository;
		this.revoirRepository = revoirRepository;
		this.historiqueRepository = historiqueRepository;
		this.statusRepository = statusRepository;
	}

	public List<DemandeARevoir> lister(int idManager) {
		if (idManager > 0) return revoirRepository.findByIdDemandeurAndTraiteFalseOrderByDateAsc(idManager);
		return revoirRepository.findByTraiteFalseOrderByDateAsc();
	}

	@Transactional
	public void appliquerCorrection(int idRevoir, boolean accepter) {
		DemandeARevoir retour = revoirRepository.findOneById(idRevoir);
		if (retour == null) throw new IllegalArgumentException("Retour introuvable.");
		if (retour.isTraite()) throw new IllegalStateException("Cette correction a déjà été traitée.");
		DetailDemande detail = detailRepository.findOneById(retour.getIdDemandeDetails());
		if (detail == null) throw new IllegalArgumentException("Détail introuvable.");
		Demande demande = demandeRepository.findOneById(detail.getIdDemande());
		if (demande == null) throw new IllegalArgumentException("Demande introuvable.");
		List<DetailDemande> details = detailRepository.findByIdDemande(demande.getId());
		List<Integer> idsDetails = new java.util.ArrayList<Integer>();
		for (DetailDemande detailDemande : details) idsDetails.add(detailDemande.getId());
		List<DemandeARevoir> retours = revoirRepository.findByIdDemandeDetailsInAndTraiteFalse(idsDetails);
		List<HistoriqueDemandeStatus> historique = historiqueRepository.findByIdDemandeOrderByIdAsc(demande.getId());
		HistoriqueDemandeStatus retourStatut = historique.get(historique.size() - 1);
		Status revoir = statusRepository.findByLibelleIgnoreCase("a revoir");
		if (revoir == null || !revoir.getLibelle().equalsIgnoreCase(retourStatut.getLibelle())) {
			throw new IllegalStateException("La demande n'est plus en attente de correction.");
		}
		if (accepter) {
			for (DemandeARevoir correction : retours) {
				DetailDemande detailCorrige = detailRepository.findOneById(correction.getIdDemandeDetails());
				if (correction.getNbrPersonnelPropose() != null) {
					detailCorrige.setNbrPersonnel(correction.getNbrPersonnelPropose());
					detailRepository.save(detailCorrige);
				}
			}
			demande.setCommentaire(retour.getCommentaire());
			demande.setTraite(false);
			demandeRepository.save(demande);
			HistoriqueDemandeStatus statut = new HistoriqueDemandeStatus();
			statut.setIdDemande(demande.getId());
			statut.setLibelle(statutApresRetour(historique.size() > 1 ? historique.get(historique.size() - 2) : null));
			statut.setIdEmploye(retourStatut.getIdEmploye());
			statut.setDate(LocalDate.now());
			historiqueRepository.save(statut);
		} else {
			demande.setTraite(true);
			demandeRepository.save(demande);
			HistoriqueDemandeStatus statut = new HistoriqueDemandeStatus();
			statut.setIdDemande(demande.getId());
			Status abandonne = statusRepository.findByLibelleIgnoreCase("abandone");
			if (abandonne == null) throw new IllegalStateException("Le statut abandonne est absent de la base.");
			statut.setLibelle(abandonne.getLibelle());
			statut.setIdEmploye(demande.getIdEmployer());
			statut.setDate(LocalDate.now());
			historiqueRepository.save(statut);
		}
		for (DemandeARevoir correction : retours) {
			correction.setTraite(true);
			revoirRepository.save(correction);
		}
	}

	private String statutApresRetour(HistoriqueDemandeStatus precedent) {
		Status valider = statusRepository.findByLibelleIgnoreCase("valider");
		if (valider == null) throw new IllegalStateException("Le statut valider est absent de la base.");
		return valider.getLibelle();
	}
}
