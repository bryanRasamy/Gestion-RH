package gestion.entreprise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gestion.entreprise.modele.Candidat;

class CandidatSchemaTest {

    @Test
    void shouldStoreDiplomeAndExperienceFields() {
        Candidat candidat = new Candidat();
        candidat.setIdDiplome(3);
        candidat.setAnneeExperienceProf(5);

        assertEquals(3, candidat.getIdDiplome());
        assertEquals(5, candidat.getAnneeExperienceProf());
    }
}
