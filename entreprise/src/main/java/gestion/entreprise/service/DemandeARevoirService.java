package gestion.entreprise.service;

import gestion.entreprise.modele.DemandeARevoir;
import gestion.entreprise.repository.DemandeARevoirRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DemandeARevoirService {
	private final DemandeARevoirRepository repository;

	public DemandeARevoirService(DemandeARevoirRepository repository) {
		this.repository = repository;
	}

	public List<DemandeARevoir> listerNonTraitees() {
		return repository.findByTraiteFalseOrderByDateAsc();
	}

	public List<DemandeARevoir> listerPourManager(int idManager) {
		return repository.findByIdDemandeurAndTraiteFalseOrderByDateAsc(idManager);
	}
}
