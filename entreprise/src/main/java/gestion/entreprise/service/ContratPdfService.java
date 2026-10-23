package gestion.entreprise.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ContratPdfService {

    @Autowired private ParametreRepository parametreRepo;
    @Autowired private PropositionEmbaucheRepository propositionRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    private static final DecimalFormat NUM = new DecimalFormat("#,##0");

    public byte[] genererPdf(int propositionId) throws Exception {
        PropositionEmbauche prop = propositionRepo.findById(propositionId)
                .orElseThrow(() -> new Exception("Proposition introuvable"));

        Candidat c = prop.getCandidat();
        String typeContrat = prop.getContratType().getLibelle();

        Document doc = new Document(PageSize.A4, 56, 56, 72, 56);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
        Font h2Font    = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
        Font bodyFont  = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font italic    = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

        Paragraph title = new Paragraph("CONTRAT DE TRAVAIL\n" + typeContrat, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(24);
        doc.add(title);

        String entreprise  = valeur("NOM_ENTREPRISE", "PIVOT SARL");
        String adresseEnt  = valeur("ADRESSE_ENTREPRISE", "Antananarivo, Madagascar");
        String numUrssaf   = valeur("NUM_COMPTE_COTISANT", "XXXXXXXXXXXX");
        String representant= valeur("REPRESENTANT_ENTREPRISE", "M. Michael Maminirina, Directeur Général");
        String devise      = valeur("DEVISE", "MGA");
        String convention  = valeur("CONVENTION_COLLECTIVE", "SYNTEC Madagascar");
        String caisseRetr  = valeur("CAISSE_RETRAITE", "CRCM");
        String prevoyance  = valeur("ORGANISME_PREVOYANCE", "Arovana Prévoyance");

        // Parties
        doc.add(paragraphe("Entre la société « " + entreprise + ", " + adresseEnt
                + " » immatriculée à l'Urssaf/MSA sous le numéro « " + numUrssaf
                + " », représentée par « " + representant
                + " », ci-après dénommée « l'entreprise »,", bodyFont));
        doc.add(paragraphe("d'une part,", italic));
        doc.add(paragraphe("et « " + c.getPrenom() + " " + c.getNom() + ", " + nvl(c.getEmail())
                + " » ci-après dénommé(e) « le salarié / la salariée » :", bodyFont));
        doc.add(paragraphe("d'autre part,", italic));
        doc.add(paragraphe("Il est convenu ce qui suit :", bodyFont));
        doc.add(Chunk.NEWLINE);

        // Article 1 — Objet
        h2(doc, "Article 1 : Objet du contrat", h2Font);
        doc.add(paragraphe("« Le salarié / la salariée » est recruté(e) par l'entreprise en qualité de « "
                + c.getTitre() + " » au coefficient hiérarchique « XXX » de la classification « "
                + convention + " ».", bodyFont));

        // Article 2 — Lieu
        h2(doc, "Article 2 : Lieu de travail", h2Font);
        doc.add(paragraphe("« Le salarié / la salariée » exercera ses fonctions dans le ou les établissements "
                + "situés au « " + prop.getLieuTravail() + " ».", bodyFont));

        // Article 3 — Date
        h2(doc, "Article 3 : Date d'embauche", h2Font);
        String duree = "CDI".equalsIgnoreCase(typeContrat)
                ? "conclu pour une durée indéterminée."
                : "conclu pour une durée déterminée dans le cadre d'un " + typeContrat + ".";
        doc.add(paragraphe("Le présent contrat prend effet le « "
                + prop.getDateDebut().format(FMT) + " » et est " + duree, bodyFont));

        // Article 4 — Période d'essai
        h2(doc, "Article 4 : Période d'essai", h2Font);
        doc.add(paragraphe("Le présent contrat prévoit une période d'essai d'une durée de « "
                + prop.getDureeEssaiMois() + " mois ». Cette période d'essai peut être reconduite, "
                + "avec l'accord du salarié, pour une durée maximum de « "
                + prop.getDureeEssaiMois() + " mois ». Il peut être mis fin à cette période d'essai "
                + "à l'initiative de l'employeur en respectant un délai de prévenance de « 15 jours » "
                + "ou du salarié en respectant un délai de prévenance de « 48 heures ».", bodyFont));

        // Article 5 — Durée du travail
        h2(doc, "Article 5 : Durée du travail", h2Font);
        doc.add(paragraphe("« Le salarié / la salariée » exercera ses fonctions dans le cadre d'un "
                + "contrat de travail à temps complet. Il/Elle est assujetti(e) à l'horaire collectif "
                + "applicable au sein de l'entreprise. Horaires : " + prop.getHoraires() + ".", bodyFont));

        // Article 6 — Rémunération
        h2(doc, "Article 6 : Rémunération", h2Font);
        doc.add(paragraphe("En contrepartie de son travail, « le salarié / la salariée » percevra une "
                + "rémunération mensuelle brute de « " + NUM.format(prop.getSalaire()) + " " + devise
                + " », correspondant au salaire de base.", bodyFont));

        // Article 7 — Congés payés
        h2(doc, "Article 7 : Congés payés", h2Font);
        doc.add(paragraphe("« Le salarié / la salariée » bénéficiera de congés payés selon les conditions "
                + "fixées par les dispositions légales et conventionnelles applicables.", bodyFont));

        // Article 8 — Sécurité sociale
        h2(doc, "Article 8 : Sécurité sociale, retraite et prévoyance", h2Font);
        doc.add(paragraphe("La caisse de retraite complémentaire à laquelle « le salarié / la salariée » "
                + "est affilié(e) est « " + caisseRetr + " ». Par ailleurs, il/elle bénéficiera du "
                + "régime de prévoyance souscrit par l'entreprise et géré par « " + prevoyance + " ».", bodyFont));

        // Article 9 — Convention collective
        h2(doc, "Article 9 : Convention collective", h2Font);
        doc.add(paragraphe("La convention collective applicable au salarié est : « " + convention + " ».", bodyFont));

        doc.add(Chunk.NEWLINE);
        doc.add(paragraphe("Fait à « Antananarivo » en deux exemplaires dont un est remis au salarié "
                + "et l'autre remis à l'entreprise.", bodyFont));
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        // Signatures
        Table sign = new Table(2);
        sign.setBorder(Rectangle.NO_BORDER);
        sign.setWidth(100);
        sign.setWidths(new float[]{50, 50});

        Cell cellRep = new Cell();
        cellRep.setBorder(Rectangle.NO_BORDER);
        cellRep.add(paragraphe("Pour l'entreprise", h2Font));
        cellRep.add(paragraphe(representant, bodyFont));
        cellRep.add(paragraphe("\n\n________________________", bodyFont));
        cellRep.add(paragraphe("Signature", italic));
        sign.addCell(cellRep);

        Cell cellSal = new Cell();
        cellSal.setBorder(Rectangle.NO_BORDER);
        cellSal.add(paragraphe("Le salarié / la salariée", h2Font));
        cellSal.add(paragraphe(c.getPrenom() + " " + c.getNom(), bodyFont));
        cellSal.add(paragraphe("\n\n________________________", bodyFont));
        cellSal.add(paragraphe("Signature", italic));
        sign.addCell(cellSal);

        doc.add(sign);

        doc.close();
        return out.toByteArray();
    }

    private String valeur(String code, String defaut) {
        Parametre p = parametreRepo.findByCode(code);
        return (p != null && p.getValeur() != null) ? p.getValeur() : defaut;
    }

    private Paragraph paragraphe(String texte, Font font) {
        Paragraph p = new Paragraph(texte, font);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setSpacingAfter(8);
        return p;
    }

    private void h2(Document doc, String texte, Font font) throws DocumentException {
        Paragraph p = new Paragraph(texte, font);
        p.setSpacingBefore(12);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private String nvl(String s) { return s == null ? "" : s; }
}