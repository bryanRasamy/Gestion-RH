(function () {
    'use strict';

    /**
     * Modales du circuit de validation.
     * Les boutons portent l'attribut data-dialog="<id>" ; les fenetres <dialog>
     * portent l'id correspondant. Le bouton [data-close-dialog] ferme la fenetre
     * qui le contient.
     */
    function ouvrirDialogue() {
        var declencheurs = document.querySelectorAll('[data-dialog]');
        declencheurs.forEach(function (bouton) {
            bouton.addEventListener('click', function () {
                var cible = document.getElementById(bouton.getAttribute('data-dialog'));
                if (!cible) return;
                if (typeof cible.showModal === 'function') {
                    cible.showModal();
                } else {
                    cible.setAttribute('open', 'open');
                }
            });
        });
    }

    function fermerDialogues() {
        document.querySelectorAll('[data-close-dialog]').forEach(function (bouton) {
            bouton.addEventListener('click', function () {
                var dialogue = bouton.closest('dialog');
                if (!dialogue) return;
                if (typeof dialogue.close === 'function') {
                    dialogue.close();
                } else {
                    dialogue.removeAttribute('open');
                }
            });
        });
    }

    /** Confirme la suppression d'une ligne de demande avant envoi. */
    function confirmerEnvoi() {
        var form = document.getElementById('requestForm');
        if (!form) return;
        form.addEventListener('submit', function (event) {
            var motif = event.submitter && event.submitter.dataset
                ? event.submitter.dataset.confirm : null;
            if (motif && !window.confirm(motif)) {
                event.preventDefault();
            }
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        ouvrirDialogue();
        fermerDialogues();
        confirmerEnvoi();
    });
})();
