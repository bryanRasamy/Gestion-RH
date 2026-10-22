package gestion.entreprise.service;

import java.util.List;
import org.springframework.stereotype.Service;
import gestion.entreprise.modele.*;
import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.security.EtapeValidation;

@Service
public class WorkflowDemandeService {
  public record Ligne(Demande demande, HistoriqueDemandeStatus statut) {}
  private final DemandeRepository demandes; private final DemandeStatutService statuts;
  public WorkflowDemandeService(DemandeRepository d, DemandeStatutService s) { demandes=d;statuts=s; }
  public List<Ligne> aTraiter(EtapeValidation etape) { return demandes.findAll().stream().map((d)->new Ligne(d,statuts.statutCourant(d.getId()))).filter(l->l.statut().getEtape().equals(etape==EtapeValidation.RH?"CREATION":etape==EtapeValidation.FINANCE?"RH":"FINANCE") && l.statut().getStatus().getSigle().equals(etape==EtapeValidation.RH?"CREE":"VALIDE")).toList(); }
  public void decider(int id, EtapeValidation etape, boolean accepter, Employer auteur, String commentaire) {
    String attendu=etape==EtapeValidation.RH?"CREE":"VALIDE"; String etapeAttendue=etape==EtapeValidation.RH?"CREATION":etape==EtapeValidation.FINANCE?"RH":"FINANCE";
    if (!accepter && (commentaire==null || commentaire.isBlank())) throw new IllegalArgumentException("Un motif est obligatoire pour un refus");
    statuts.changer(id,attendu,EtapeValidation.valueOf(etapeAttendue),accepter?"VALIDE":"REFUSE",etape,auteur,commentaire);
  }
}
