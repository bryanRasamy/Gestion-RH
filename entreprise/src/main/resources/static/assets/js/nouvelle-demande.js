(function () {
    'use strict';

    // ---------- État global ----------
    var DEPARTEMENTS = [];
    var DIPLOMES = [];
    var nextId = 1;

    // ---------- DOM ----------
    var form      = document.getElementById('requestForm');
    var rowsEl    = document.getElementById('rows');
    var addBtn    = document.getElementById('addRow');
    var countEl   = document.getElementById('count');
    var submitBtn = document.getElementById('submitBtn');
    var success   = document.getElementById('formSuccess');
    var successTx = document.getElementById('formSuccessText') || success;
    var deadline  = document.getElementById('deadline');

    var now = new Date();
    var todayISO = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString().split('T')[0];
    deadline.min = todayISO;

    // ---------- Chargement initial des données métier ----------
    async function chargerJSON(url) {
        try {
            var r = await fetch(url);
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return await r.json();
        } catch (e) {
            console.error('Impossible de charger ' + url, e);
            return [];
        }
    }

    async function chargerDepartements() {
        DEPARTEMENTS = await chargerJSON('/api/departements');
    }

    async function chargerDiplomes() {
        DIPLOMES = await chargerJSON('/api/diplomes');
    }

    async function chargerPostes(idDepartement) {
        return await chargerJSON('/api/postes?idDepartement=' + encodeURIComponent(idDepartement));
    }

    // ---------- Rendu d'une ligne ----------
    function construireOptionsDepartements() {
        if (DEPARTEMENTS.length === 0) {
            return '<option value="">Chargement…</option>';
        }
        var html = '';
        for (var i = 0; i < DEPARTEMENTS.length; i++) {
            var d = DEPARTEMENTS[i];
            html += '<option value="' + d.id + '">' + escapeHtml(d.libelle) + '</option>';
        }
        return html;
    }

    function construireOptionsPostes(postes) {
        if (postes.length === 0) {
            return '<option value="">Aucun poste dans ce département</option>';
        }
        var html = '<option value="">Sélectionner un poste…</option>';
        for (var i = 0; i < postes.length; i++) {
            var p = postes[i];
            html += '<option value="' + p.id + '">' + escapeHtml(p.libelle) + '</option>';
        }
        return html;
    }

    function construireOptionsDiplomes() {
        if (DIPLOMES.length === 0) {
            return '<option value="">Chargement…</option>';
        }
        var html = '';
        for (var i = 0; i < DIPLOMES.length; i++) {
            var d = DIPLOMES[i];
            html += '<option value="' + d.id + '">'
                + escapeHtml(d.nomDiplome) + ' — ' + d.anneeEtude + ' ans</option>';
        }
        return html;
    }

    function creerLigne() {
        var n = nextId++;
        var row = document.createElement('div');
        row.className = 'req-row';
        row.innerHTML = ''
            + '<span class="req-row__index">' + String(n).padStart(2, '0') + '</span>'
            + '<div class="req-row__field req-row__field--dept">'
            +   '<label class="req-row__label-mobile">Département</label>'
            +   '<select class="departement" required>'
            +     construireOptionsDepartements()
            +   '</select>'
            + '</div>'
            + '<div class="req-row__field req-row__field--poste">'
            +   '<label class="req-row__label-mobile">Poste</label>'
            +   '<select class="poste" required>'
            +     '<option value="">Sélectionner un département d\'abord…</option>'
            +   '</select>'
            + '</div>'
            + '<div class="req-row__field req-row__field--num">'
            +   '<label class="req-row__label-mobile">Nombre</label>'
            +   '<input type="number" class="nombre" min="1" value="1" required>'
            + '</div>'
            + '<div class="req-row__field req-row__field--diplome">'
            +   '<label class="req-row__label-mobile">Diplôme</label>'
            +   '<select class="diplome" required>'
            +     construireOptionsDiplomes()
            +   '</select>'
            + '</div>'
            + '<div class="req-row__field req-row__field--experience">'
            +   '<label class="req-row__label-mobile">Expérience (ans)</label>'
            +   '<input type="number" class="experience" min="0" value="0" required>'
            + '</div>'
            + '<div class="req-row__field req-row__field--motif">'
            +   '<label class="req-row__label-mobile">Description</label>'
            +   '<input type="text" class="description" placeholder="Ex : renfort sur le projet Atlas" required>'
            + '</div>'
            + '<button type="button" class="req-row__remove" aria-label="Supprimer">'
            +   '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" '
            +       'stroke="currentColor" stroke-width="2.2" stroke-linecap="round">'
            +     '<path d="M5 12h14"/>'
            +   '</svg>'
            + '</button>';

        var deptSelect  = row.querySelector('.departement');
        var posteSelect = row.querySelector('.poste');

        async function rechargerPostes() {
            var idDept = parseInt(deptSelect.value, 10);
            posteSelect.innerHTML = '<option value="">Chargement…</option>';
            if (!idDept) {
                posteSelect.innerHTML = '<option value="">Sélectionner un département d\'abord…</option>';
                return;
            }
            var postes = await chargerPostes(idDept);
            posteSelect.innerHTML = construireOptionsPostes(postes);
        }

        deptSelect.addEventListener('change', rechargerPostes);
        if (deptSelect.value) rechargerPostes();

        return row;
    }

    // ---------- Ajout / suppression de lignes ----------
    function ajouterLigne() {
        rowsEl.appendChild(creerLigne());
        mettreAJourCompteur();
    }

    function supprimerLigne(row) {
        if (rowsEl.querySelectorAll('.req-row').length <= 1) return;
        row.classList.add('is-removing');
        setTimeout(function () {
            row.remove();
            mettreAJourCompteur();
        }, 280);
    }

    function mettreAJourCompteur() {
        var rows = rowsEl.querySelectorAll('.req-row');
        var total = 0;
        for (var i = 0; i < rows.length; i++) {
            var v = parseInt(rows[i].querySelector('.nombre').value, 10);
            if (v > 0) total += v;
        }
        countEl.textContent = String(rows.length).padStart(2, '0')
            + (rows.length > 1 ? ' postes' : ' poste')
            + ' · '
            + String(total).padStart(2, '0')
            + (total > 1 ? ' personnes' : ' personne');
    }

    // ---------- Feedback utilisateur ----------
    function afficherSucces(message) {
        success.classList.remove('is-error');
        success.classList.add('is-success');
        successTx.textContent = message;
        success.classList.add('is-visible');
        success.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function afficherErreur(message) {
        success.classList.remove('is-success');
        success.classList.add('is-error');
        successTx.textContent = message;
        success.classList.add('is-visible');
        success.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function cacherFeedback() {
        success.classList.remove('is-visible', 'is-success', 'is-error');
        successTx.textContent = '';
    }

    // ---------- Soumission ----------
    async function soumettre(e) {
        e.preventDefault();
        cacherFeedback();

        deadline.classList.remove('is-invalid');
        if (!deadline.value) { deadline.classList.add('is-invalid'); deadline.focus(); return; }
        if (deadline.value < todayISO) { deadline.classList.add('is-invalid'); deadline.focus(); return; }

        var lignes = [];
        var ok = true;
        var rows = rowsEl.querySelectorAll('.req-row');

        for (var i = 0; i < rows.length; i++) {
            var row = rows[i];
            var deptEl      = row.querySelector('.departement');
            var posteEl     = row.querySelector('.poste');
            var numEl       = row.querySelector('.nombre');
            var diplomeEl   = row.querySelector('.diplome');
            var experienceEl= row.querySelector('.experience');
            var descEl      = row.querySelector('.description');

            [deptEl, posteEl, numEl, diplomeEl, experienceEl, descEl].forEach(function (el) {
                el.classList.remove('is-invalid');
            });

            var idDepartement   = parseInt(deptEl.value, 10);
            var idPoste         = parseInt(posteEl.value, 10);
            var nbrPersonnel    = parseInt(numEl.value, 10);
            var idDiplome       = parseInt(diplomeEl.value, 10);
            var anneeExperience = parseInt(experienceEl.value, 10);
            var description     = descEl.value.trim();

            if (!idDepartement)   { deptEl.classList.add('is-invalid'); ok = false; }
            if (!idPoste)         { posteEl.classList.add('is-invalid'); ok = false; }
            if (!nbrPersonnel || nbrPersonnel < 1) { numEl.classList.add('is-invalid'); ok = false; }
            if (!idDiplome)       { diplomeEl.classList.add('is-invalid'); ok = false; }
            if (isNaN(anneeExperience) || anneeExperience < 0) { experienceEl.classList.add('is-invalid'); ok = false; }
            if (!description)     { descEl.classList.add('is-invalid'); ok = false; }

            if (ok) {
                lignes.push({
                    idDepartement: idDepartement,
                    idPoste: idPoste,
                    nbrPersonnel: nbrPersonnel,
                    idDiplome: idDiplome,
                    anneeExperience: anneeExperience,
                    description: description
                });
            }
        }

        if (!ok) {
            var firstInvalid = rowsEl.querySelector('.is-invalid');
            if (firstInvalid) firstInvalid.focus();
            return;
        }

        // Le manager connecte est determine par la session : il n'est pas envoye.
        var payload = {
            dateLimite: deadline.value,
            lignes: lignes
        };

        submitBtn.classList.add('is-loading');
        submitBtn.disabled = true;

        try {
            var r = await fetch('/api/demandes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            var data = await r.json();
            if (data.success) {
                afficherSucces('Demande ' + data.matricule
                    + ' créée · échéance ' + formatDateFR(deadline.value) + '.');
            } else {
                afficherErreur(data.message || 'Une erreur est survenue.');
            }
        } catch (err) {
            afficherErreur('Erreur réseau : ' + err.message);
        } finally {
            submitBtn.classList.remove('is-loading');
            submitBtn.disabled = false;
        }
    }

    // ---------- Helpers ----------
    function formatDateFR(iso) {
        if (!iso) return '';
        var parts = iso.split('-');
        return parts[2] + '/' + parts[1] + '/' + parts[0];
    }

    function escapeHtml(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c];
        });
    }

    // ---------- Événements ----------
    addBtn.addEventListener('click', ajouterLigne);

    rowsEl.addEventListener('click', function (e) {
        var btn = e.target.closest('.req-row__remove');
        if (btn && !btn.disabled) supprimerLigne(btn.closest('.req-row'));
    });

    rowsEl.addEventListener('input', function (e) {
        if (e.target.matches('.nombre')) mettreAJourCompteur();
        cacherFeedback();
    });

    deadline.addEventListener('input', cacherFeedback);

    form.addEventListener('submit', soumettre);

    // ---------- Démarrage ----------
    (async function init() {
        await Promise.all([chargerDepartements(), chargerDiplomes()]);
        ajouterLigne();
    })();

})();
