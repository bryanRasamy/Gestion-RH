package gestion.entreprise.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.security.EtapeValidation;

@Service
public class DemandeStatutService {
  private final DemandeRepository demandes; private final StatusRepository statuts;
  private final HistoriqueDemandeStatusRepository historiques;
  public DemandeStatutService(DemandeRepository d, StatusRepository s, HistoriqueDemandeStatusRepository h) { demandes=d; statuts=s; historiques=h; }
  public HistoriqueDemandeStatus statutCourant(int id) { return historiques.findFirstByDemandeIdOrderByIdDesc(id).orElseThrow(() -> new IllegalArgumentException("Demande sans historique")); }
  @Transactional public void initialiser(Demande demande, Employer auteur) { ecrire(demande, "CREE", EtapeValidation.CREATION, auteur, null); }
  @Transactional public void changer(int demandeId, String statutAttendu, EtapeValidation etapeAttendue, String nouveauStatut, EtapeValidation nouvelleEtape, Employer auteur, String commentaire) {
    Demande demande = demandes.findByIdForUpdate(demandeId);
    if (demande == null) throw new IllegalArgumentException("Demande introuvable");
    HistoriqueDemandeStatus courant = statutCourant(demandeId);
    if (!courant.getStatus().getSigle().equals(statutAttendu) || !courant.getEtape().equals(etapeAttendue.name())) throw new IllegalStateException("Cette demande a déjà changé d'état ou ne peut pas recevoir cette action");
    if (!estFinal(courant) && demande.getDateLimite() != null && demande.getDateLimite().isBefore(LocalDate.now())) throw new IllegalStateException("La demande est expirée");
    ecrire(demande, nouveauStatut, nouvelleEtape, auteur, commentaire);
  }
  @Transactional public int expirerDemandesDepassees() { int n=0; for (Demande d : demandes.findAll()) { HistoriqueDemandeStatus c=statutCourant(d.getId()); if (d.getDateLimite()!=null && d.getDateLimite().isBefore(LocalDate.now()) && !estFinal(c)) { ecrire(d,"EXPIRE",EtapeValidation.SYSTEME,null,"Expiration automatique"); n++; } } return n; }
  public boolean estFinal(HistoriqueDemandeStatus h) { String s=h.getStatus().getSigle(); return s.equals("REFUSE") || s.equals("ABANDONNE") || s.equals("EXPIRE") || (s.equals("VALIDE") && h.getEtape().equals("DG")); }
  private void ecrire(Demande d, String code, EtapeValidation etape, Employer auteur, String commentaire) { Status s=statuts.findBySigle(code); if(s==null) throw new IllegalStateException("Statut " + code + " introuvable"); HistoriqueDemandeStatus h=new HistoriqueDemandeStatus(); h.setDemande(d);h.setStatus(s);h.setEtape(etape.name());h.setEmployer(auteur);h.setCommentaire(commentaire);h.setDateAction(OffsetDateTime.now()); historiques.save(h); }
}
