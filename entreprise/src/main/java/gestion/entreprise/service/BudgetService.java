package gestion.entreprise.service;

import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {
  public record Evaluation(BigDecimal minimum, BigDecimal maximum, BigDecimal budget, String indication) {}
  private final JdbcTemplate jdbc;
  public BudgetService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
  public Evaluation evaluer(int demandeId) {
    Object[] totaux=jdbc.queryForObject("SELECT COUNT(*), COUNT(g.budget_min), COALESCE(SUM(dd.nbr_personnel*g.budget_min),0), COALESCE(SUM(dd.nbr_personnel*g.budget_max),0) FROM detail_demande dd LEFT JOIN LATERAL (SELECT budget_min,budget_max FROM grille_budget WHERE id_poste=dd.id_poste AND date_effective<=CURRENT_DATE ORDER BY date_effective DESC,id DESC LIMIT 1) g ON true WHERE dd.id_demande=?", (rs,n)->new Object[]{rs.getLong(1),rs.getLong(2),rs.getBigDecimal(3),rs.getBigDecimal(4)}, demandeId);
    if (!totaux[0].equals(totaux[1])) throw new IllegalStateException("La grille budgétaire est incomplète pour cette demande");
    BigDecimal budget=jdbc.queryForObject("SELECT valeur FROM parametre WHERE code='BUDGET'", BigDecimal.class);
    BigDecimal minimum=(BigDecimal)totaux[2], maximum=(BigDecimal)totaux[3];
    String indication=maximum.compareTo(budget)<=0?"OK":minimum.compareTo(budget)<=0?"À revoir":"NON";
    return new Evaluation(minimum,maximum,budget,indication);
  }
}
